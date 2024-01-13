package com.fuint.module.clientApi.controller;

import com.fuint.common.dto.AccountInfo;
import com.fuint.common.dto.CouponDto;
import com.fuint.common.dto.StoreDto;
import com.fuint.common.dto.UserInfo;
import com.fuint.common.service.CouponService;
import com.fuint.common.service.StoreService;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.MtStore;
import com.fuint.utils.StringUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 店铺接口controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="会员端-店铺相关接口")
@RestController
@RequestMapping(value = "/clientApi/store")
public class ClientStoreController extends BaseController {

    /**
     * 店铺服务接口
     * */
    @Autowired
    private StoreService storeService;

    @Resource
    private CouponService couponService;

    /**
     * 获取店铺列表（根据距离排序）
     *
     * @param request Request对象
     */
    @ApiOperation(value = "获取店铺列表（根据距离排序）")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject list(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        UserInfo mtUser = TokenUtil.getUserInfoByToken(token);

        String keyword = "";
        String latitude = "";
        String longitude = "";
        Integer merchantNo = 0;

        List<StoreDto> storeList = storeService.queryByDistance(merchantNo, keyword, latitude, longitude, mtUser);

        Map<String, Object> outParams = new HashMap<>();
        outParams.put("data", storeList);

        return getSuccessResult(outParams);
    }

    /**
     * 获取店铺详情
     *
     * @param request Request对象
     */
    @ApiOperation(value = "获取店铺详情")
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject detail(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        UserInfo mtUser = TokenUtil.getUserInfoByToken(token);
        Integer userId = 0;
        if (null != mtUser) {
            userId = mtUser.getId();
        }

        Integer storeId = request.getHeader("storeId") == null ? 0 : Integer.parseInt(request.getHeader("storeId"));

        MtStore storeInfo = storeService.queryStoreById(storeId);

        StoreDto storeDto = new StoreDto();
        BeanUtils.copyProperties(storeInfo, storeDto);
        List<CouponDto> couponList = couponService.queryCouponListByStoreId(storeDto, userId);
        storeDto.setCoupons(couponList);

        Map<String, Object> outParams = new HashMap<>();
        outParams.put("storeInfo", storeDto);

        return getSuccessResult(outParams);
    }
}
