package com.fuint.common.service.impl;

import com.fuint.common.dto.*;
import com.fuint.common.enums.*;
import com.fuint.common.service.*;
import com.fuint.common.util.CommonUtil;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.mapper.MtOrderMapper;
import com.fuint.repository.mapper.MtUserGradeMapper;
import com.fuint.repository.model.*;
import com.fuint.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付相关接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Resource
    private MtOrderMapper mtOrderMapper;

    @Resource
    private MtUserGradeMapper mtUserGradeMapper;

    /**
     * 微信服务接口
     * */
    @Autowired
    private WeixinService weixinService;

    /**
     * 支付宝服务接口
     * */
    @Autowired
    private AlipayService alipayService;

    /**
     * 会员服务接口
     * */
    @Autowired
    private MemberService memberService;

    /**
     * 订单服务接口
     * */
    @Autowired
    private OrderService orderService;

    /**
     * 余额服务接口
     * */
    @Autowired
    private BalanceService balanceService;

    @Autowired
    private PointService pointService;

    @Autowired
    private UserCouponService userCouponService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private UserGradeService userGradeService;

    @Autowired
    private OpenGiftService openGiftService;

    /**
     * 创建支付订单
     * @return
     * */
    @Override
    public ResponseObject createPrepayOrder(MtUser userInfo, MtOrder orderInfo, Integer payAmount, String authCode, Integer giveAmount, String ip, String platform) throws BusinessCheckException {
        logger.info("PaymentService createPrepayOrder inParams userInfo={} payAmount={} giveAmount={} goodsInfo={}", userInfo, payAmount, giveAmount, orderInfo);

        ResponseObject responseObject;
        if (orderInfo.getPayType().equals(PayTypeEnum.ALISCAN.getKey())) {
            // 支付宝支付
            responseObject = alipayService.createPrepayOrder(userInfo, orderInfo, payAmount, authCode, giveAmount, ip, platform);
        } else {
            // 微信支付
            responseObject = weixinService.createPrepayOrder(userInfo, orderInfo, payAmount, authCode, giveAmount, ip, platform);
        }

        logger.info("PaymentService createPrepayOrder outParams {}", responseObject.toString());
        return responseObject;
    }

    /**
     * 支付成功回调
     * @return
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean paymentCallback(UserOrderDto orderInfo) throws BusinessCheckException {
        logger.info("paymentCallback outParams {}", orderInfo.toString());
        MtOrder mtOrder = mtOrderMapper.selectById(orderInfo.getId());

        // 更新订单状态为已支付
        Boolean isPay = orderService.setOrderPayed(orderInfo.getId(), null);
        if (mtOrder == null || !isPay) {
            return false;
        }

        // 会员升级订单
        if (mtOrder.getType().equals(OrderTypeEnum.MEMBER.getKey())) {
            openGiftService.openGift(mtOrder.getUserId(), Integer.parseInt(mtOrder.getParam()), false);
        }

        // 储值卡订单
        if (orderInfo.getType().equals(OrderTypeEnum.PRESTORE.getKey())) {
            Map<String, Object> param = new HashMap<>();
            param.put("couponId", orderInfo.getCouponId());
            param.put("userId", orderInfo.getUserId());
            param.put("param", orderInfo.getParam());
            param.put("orderId", orderInfo.getId());
            userCouponService.preStore(param);
        }

        // 充值订单
        if (orderInfo.getType().equals(OrderTypeEnum.RECHARGE.getKey())) {
            // 余额支付
            MtBalance mtBalance = new MtBalance();
            OrderUserDto userDto = orderInfo.getUserInfo();
            if (userDto.getMobile() != null && StringUtil.isNotEmpty(userDto.getMobile())) {
                mtBalance.setMobile(userDto.getMobile());
            }
            mtBalance.setOrderSn(orderInfo.getOrderSn());
            mtBalance.setUserId(orderInfo.getUserId());
            String param = orderInfo.getParam();
            if (StringUtil.isNotEmpty(param)) {
                String params[] = param.split("_");
                if (params.length == 2) {
                    BigDecimal amount = new BigDecimal(params[0]).add(new BigDecimal(params[1]));
                    mtBalance.setAmount(amount);
                    balanceService.addBalance(mtBalance);
                }
            }
        }

        // 处理消费返积分，查询返1积分所需消费金额
        MtSetting setting = settingService.querySettingByName("pointNeedConsume");
        if (setting != null) {
            String needPayAmount = setting.getValue();
            Integer needPayAmountInt = Math.round(Integer.parseInt(needPayAmount));
            Double pointNum = 0d;
            if (needPayAmountInt > 0 && orderInfo.getPayAmount().compareTo(new BigDecimal(needPayAmountInt)) > 0) {
                BigDecimal point = orderInfo.getPayAmount().divide(new BigDecimal(needPayAmountInt), BigDecimal.ROUND_CEILING, 3);
                pointNum = Math.ceil(point.doubleValue());
            }
            logger.info("PaymentService paymentCallback Point orderSn = {} , pointNum ={}", orderInfo.getOrderSn(), pointNum);
            if (pointNum > 0) {
                MtUser userInfo = memberService.queryMemberById(orderInfo.getUserId());
                MtUserGrade userGrade = userGradeService.queryUserGradeById(Integer.parseInt(userInfo.getGradeId()), orderInfo.getUserId());
                // 是否会员积分加倍
                if (userGrade.getSpeedPoint() > 1) {
                    pointNum = pointNum * userGrade.getSpeedPoint();
                }
                MtPoint reqPointDto = new MtPoint();
                reqPointDto.setAmount(pointNum.intValue());
                reqPointDto.setUserId(orderInfo.getUserId());
                reqPointDto.setOrderSn(orderInfo.getOrderSn());
                reqPointDto.setDescription("支付￥"+orderInfo.getPayAmount()+"返"+pointNum+"积分");
                reqPointDto.setOperator("系统");
                pointService.addPoint(reqPointDto);
            }
        }

        // 计算是否要升级（购物订单、付款订单、充值订单）
        if (orderInfo.getType().equals(OrderTypeEnum.GOOGS.getKey()) || orderInfo.getType().equals(OrderTypeEnum.PAYMENT.getKey()) || orderInfo.getType().equals(OrderTypeEnum.RECHARGE.getKey())) {
            try {
                if (orderInfo.getIsVisitor().equals(YesOrNoEnum.NO.getKey())) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("status", StatusEnum.ENABLED.getKey());
                    MtUser mtUser = memberService.queryMemberById(orderInfo.getUserId());
                    MtUserGrade mtUserGrade = mtUserGradeMapper.selectById(mtUser.getGradeId());
                    if (mtUserGrade == null) {
                        mtUserGrade = userGradeService.getInitUserGrade();
                    }
                    List<MtUserGrade> userGradeList = mtUserGradeMapper.selectByMap(param);
                    if (mtUserGrade != null && userGradeList != null && userGradeList.size() > 0) {
                        // 会员已支付金额
                        BigDecimal payMoney = orderService.getUserPayMoney(orderInfo.getUserId());
                        // 会员支付订单笔数
                        Integer payOrderCount = orderService.getUserPayOrderCount(orderInfo.getUserId());
                        BigDecimal payOrderCountValue = new BigDecimal(payOrderCount);
                        for (MtUserGrade grade : userGradeList) {
                            if (grade.getCatchValue() != null && grade.getCatchType() != null) {
                                // 累计消费金额已达到
                                if (grade.getCatchType().equals(UserGradeCatchTypeEnum.AMOUNT.getKey())) {
                                    if (grade.getGrade().compareTo(mtUserGrade.getGrade()) > 0 && payMoney.compareTo(grade.getCatchValue()) >= 0) {
                                        openGiftService.openGift(mtOrder.getUserId(), grade.getId(), false);
                                    }
                                }
                                // 累计消费次数已达到
                                if (grade.getCatchType().equals(UserGradeCatchTypeEnum.FREQUENCY.getKey()) && payOrderCountValue.compareTo(grade.getCatchValue()) >= 0) {
                                    if (grade.getGrade().compareTo(mtUserGrade.getGrade()) > 0) {
                                        openGiftService.openGift(mtOrder.getUserId(), grade.getId(), false);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("会员升级出错啦，userId = {}，message = {}", orderInfo.getUserId(), ex.getMessage());
            }
        }
        logger.info("PaymentService paymentCallback Success orderSn {}", orderInfo.getOrderSn());
        return true;
    }

    /**
     * 订单支付
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> doPay(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        String platform = request.getHeader("platform") == null ? "" : request.getHeader("platform");
        String payType = request.getParameter("payType") == null ? PayTypeEnum.JSAPI.getKey() : request.getParameter("payType");
        String cashierPayAmount = request.getParameter("cashierPayAmount") == null ? "" : request.getParameter("cashierPayAmount"); // 收银台实付金额
        String cashierDiscountAmount = request.getParameter("cashierDiscountAmount") == null ? "" : request.getParameter("cashierDiscountAmount"); // 收银台优惠金额
        UserInfo loginInfo = TokenUtil.getUserInfoByToken(token);
        String orderId = request.getParameter("orderId");
        String userId = request.getParameter("userId");
        String authCode = request.getParameter("authCode");
        if (StringUtil.isEmpty(orderId)) {
            throw new BusinessCheckException("订单不能为空");
        }

        MtOrder orderInfo = mtOrderMapper.selectById(Integer.parseInt(orderId));
        if (orderInfo == null) {
            throw new BusinessCheckException("该订单不存在");
        }
        MtUser mtUser = null;
        if (loginInfo != null) {
            mtUser = memberService.queryMemberById(loginInfo.getId());
        }

        // 收银员操作
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (loginInfo == null && accountInfo != null) {
            // 游客订单绑定到会员
            if (orderInfo.getIsVisitor().equals(YesOrNoEnum.YES.getKey()) && StringUtil.isNotEmpty(userId)) {
                mtUser = memberService.queryMemberById(Integer.parseInt(userId));
                orderInfo.setUserId(Integer.parseInt(userId));
                orderInfo.setIsVisitor(YesOrNoEnum.NO.getKey());
                orderService.updateOrder(orderInfo);
            } else {
                mtUser = memberService.queryMemberById(orderInfo.getUserId());
            }
        }

        if (mtUser == null) {
            throw new BusinessCheckException("登录信息失效");
        }

        if (accountInfo != null && StringUtil.isNotEmpty(cashierPayAmount) && StringUtil.isNotEmpty(cashierDiscountAmount)) {
            orderInfo.setDiscount(new BigDecimal(cashierDiscountAmount));
            if (loginInfo == null) {
                MtUser user = memberService.queryMemberById(orderInfo.getUserId());
                if (user != null) {
                    loginInfo = new UserInfo();
                    loginInfo.setId(user.getId());
                }
            }
        }

        // 实付金额 = 总金额 - 优惠金额 - 积分金额
        BigDecimal realPayAmount = orderInfo.getAmount().subtract(new BigDecimal(orderInfo.getDiscount().toString())).subtract(new BigDecimal(orderInfo.getPointAmount().toString())).add(orderInfo.getDeliveryFee());
        Object payment = null;
        if (payType.equals(PayTypeEnum.BALANCE.getKey())) {
            // 余额支付
            MtBalance balance = new MtBalance();
            balance.setMobile(mtUser.getMobile());
            balance.setOrderSn(orderInfo.getOrderSn());
            balance.setUserId(mtUser.getId());
            BigDecimal balanceAmount = realPayAmount.subtract(realPayAmount).subtract(realPayAmount);
            balance.setAmount(balanceAmount);
            boolean isPay = balanceService.addBalance(balance);
            if (isPay) {
                orderService.setOrderPayed(orderInfo.getId(), balanceAmount);
                OrderDto reqOrder = new OrderDto();
                reqOrder.setId(orderInfo.getId());
                reqOrder.setPayAmount(realPayAmount);
                reqOrder.setDiscount(orderInfo.getDiscount());
                reqOrder.setPayType(PayTypeEnum.BALANCE.getKey());
                if (accountInfo != null) {
                    reqOrder.setOperator(accountInfo.getAccountName());
                }
                orderService.updateOrder(reqOrder);
                orderInfo = orderService.getOrderInfo(orderInfo.getId());
            } else {
                throw new BusinessCheckException("会员余额不足");
            }
        } else if (payType.equals(PayTypeEnum.CASH.getKey()) && accountInfo != null) {
            // 现金支付
            OrderDto reqOrder = new OrderDto();
            reqOrder.setId(orderInfo.getId());
            reqOrder.setAmount(new BigDecimal(cashierPayAmount).add(new BigDecimal(cashierDiscountAmount)));
            reqOrder.setDiscount(new BigDecimal(cashierDiscountAmount));
            reqOrder.setPayAmount(new BigDecimal(cashierPayAmount));
            reqOrder.setPayTime(new Date());
            reqOrder.setPayType(PayTypeEnum.CASH.getKey());
            reqOrder.setOperator(accountInfo.getAccountName());
            orderService.updateOrder(reqOrder);
            orderService.setOrderPayed(orderInfo.getId(), null);
            orderInfo = orderService.getOrderInfo(orderInfo.getId());
        } else {
            String ip = CommonUtil.getIPFromHttpRequest(request);
            BigDecimal pay = realPayAmount.multiply(new BigDecimal("100"));
            orderInfo.setPayType(payType);
            ResponseObject paymentInfo = createPrepayOrder(mtUser, orderInfo, (pay.intValue()), authCode, 0, ip, platform);
            if (paymentInfo.getData() == null) {
                throw new BusinessCheckException("抱歉，支付失败");
            }
            payment = paymentInfo.getData();
        }

        Map<String, Object> result = new HashMap();
        result.put("isCreated", true);
        result.put("payType", payType);
        result.put("orderInfo", orderInfo);
        result.put("payment", payment);

        return result;
    }
}
