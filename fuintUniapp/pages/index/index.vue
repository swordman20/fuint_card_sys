<template>
  <view class="container">
	  
	
	<!-- @click="open" -->
	<!-- <modal :visible.sync="visible"></modal> -->
	<!-- <modal :show=true @close="closeMadle" @cancel="cancel" @confirm="confirm" >
		
	</modal> -->
	<!-- <modal :isShow='visible' :bottom='true'  @close="closeMadle" @cancel="cancel" @confirm="confirm">
		<template v-slot:title>
			<view class="title">
				父组件自定义标题
			</view>
		</template>
		<template v-slot:body>
			<view class="body" >
				这里是父组件引用子组件，使用具名插槽编写的自定义内容和样式。
			</view>
		</template>
	</modal> -->
    <!-- <block v-if="storeInfo">
        <Location :storeInfo="storeInfo"/>
    </block> -->
    <block>
        <Search tips="请输入搜索关键字..." @event="$navTo('pages/search/index')"/>
    </block>
    <block>
        <Banner :itemStyle="options.bannerStyle" :params="options.bannerParam" :dataList="banner"/>
    </block>
    <block>
        <Blank :itemStyle="options.blankStyle"/>
    </block>
    <block>
        <NavBar :itemStyle="options.navStyle" :params="{}" :dataList="options.navBar"/>
    </block>
    <block>
        <Blank :itemStyle="options.blankStyle"/>
    </block>
    <!-- <block>
        <Goods :itemStyle="options.goodsStyle" :isReflash="isReflash" ref="mescrollItem" :params="options.goodsParams"/>
    </block> -->
    <!-- @click="onTargetDetail(item.id, item.type, item.userCouponId)" -->
    <block>
      <view class="goods-list" :class="['column-1']">
        <view class="goods-item" v-for="(storeItem, storeIndex) in storeList" :key="storeIndex" @click="onTargetGoods(storeItem.id)">
          <view class="dis-flex" >
            <!-- 店铺图片 -->
            <view class="goods-item_left" >
              <image class="image" :src="storeItem.logo"></image>
            </view>
            <view class="goods-item_right">
              <!-- 店铺名称 -->
              <view class="goods-name twolist-hidden">
                <text>{{ storeItem.name }}</text>
              </view>
              <view class="goods-item_desc">
				<text class="desc-selling_point dis-flex onelist-hidden">{{ storeItem.description }}</text>
                <!-- 店铺联系电话 --> 
                <view class="">
                  <text class="onelist-hidden">联系电话:  {{ storeItem.phone }}</text>
                </view>
				
              </view>
            </view>
          </view>
          <view>
            <view v-for="(couponItem, couponIndex) in storeList[storeIndex].coupons" :key="couponItem.id" @click="onTargetDetail(couponItem.id, couponItem.type, couponItem.userCouponId)">
              <!-- 卡券简介 -->
              <!-- <text>{{ couponItem.name }}</text>
              <button >使用</button> -->
              <view class="bottom" v-if="couponItem.status === 'A' || couponItem.status === 'B' || couponItem.status === 'E' ">
                <view class="receive left-box">
                  <text v-if="couponItem.type === 'C'">立即领取</text>
                  <text v-if="couponItem.type === 'P'">立即预存</text>
                  <text v-if="couponItem.type === 'T'">限使{{ couponItem.outRule }}次</text>
                </view>
                <view class="txt">
                  <text>{{ couponItem.name }}</text>
                </view>
                <view class="receive right-box" v-if="couponItem.type === 'C' && couponItem.isReceive === false">
                    <text v-if="!couponItem.point || couponItem.point < 1">立即领取</text>
                  <text v-if="couponItem.point && couponItem.point > 0">立即兑换</text>
                </view>
                <view class="receive right-box state" v-if="couponItem.type === 'C' && couponItem.isReceive === true">
                    <text>已领取</text>
                </view>
                <view class="receive right-box" v-if="couponItem.type === 'P' && couponItem.isReceive === false">
                  <text>立即预存</text>
                </view>
                <view v-if="couponItem.type === 'T' && couponItem.isReceive === false" class="receive right-box">
                    <text>领取次卡</text>
                </view>
                <view v-if="couponItem.type === 'T' && couponItem.isReceive === true" class="receive right-box state">
                    <text>已领取</text>
                </view>
                <!-- <view class="receive right-box">
                  <text>领取</text>
                </view> -->
              </view>
            </view>
          </view>
        </view>
      </view>
	  
	  <!--弹出层 -->
		<view>
		  <u-popup v-model="show" mode="center" class="popup" width="60%" >
			  <view style="padding-left: 89%;">
				  <image style="width: 50rpx; height: 50rpx;" src="https://www.gtsmhlife.com/static/exit1.jpg" @click="show = false" ></image>
			  </view>
			
			<!-- <view>
				
			  <view><text>联系电话(微信同号)：</text></view>
			  </text>18742034648<text>
			</view> -->
			<image src="https://www.gtsmhlife.com/static/weixin.png" style="width: 90rpx; height: 83rpx; padding-top: 75rpx; padding-left: 179rpx;"></image>
			<view style="padding-top: 45rpx; padding-left: 133rpx; padding-bottom: 75rpx;" @click="ctrlC">18742034648</view>
			<button  @click="Call" style="font-weight: bold;">拨打电话</button>
			<!-- <view class="circle">
			  <view class="child">
				<u-icon name="close" class="close" @click="show = false"></u-icon>
			  </view>
			</view> -->
		  </u-popup>      
		</view>
		
		<view class="storeIn" mode="widthFix" @click="show = true">
			<image src="https://www.gtsmhlife.com/static/storeIn.png" style="width: 150rpx;height: 110rpx;"></image>
		</view>

    </block>
  </view>
</template>

<script>
  import { setCartTabBadge, showMessage } from '@/utils/app'
  import Location from '@/components/page/location'
  import Search from '@/components/search'
  import Banner from '@/components/page/banner'
  import NavBar from '@/components/page/navBar'
  import Blank from '@/components/page/blank'
  import Goods from '@/components/page/goods'
  import * as settingApi from '@/api/setting'
  import * as Api from '@/api/page'
  import MescrollCompMixin from "@/components/mescroll-uni/mixins/mescroll-comp.js";
  import modal from "@/pages/index/storeIn"

  const App = getApp()
  
  export default {
    mixins: [MescrollCompMixin],
    components: {
       Location,
       Search,
       Banner,
       NavBar,
       Blank,
       Goods,
	   // modal
    },
    data() {
      return {
        options: {
            "blankStyle": {
                "height": "5",
                "background": "#ffffff",
            },
            "navBar": [{
                        "imgUrl": "/static/nav/3.png",
                        "imgName": "icon-1.png",
                        "linkUrl": "pages\/coupon\/list?type=C",
                        "text": "领券中心",
                        "tip": "积分换好礼",
                        "color": "#666666"
                      },{
                        "imgUrl": "/static/nav/4.png",
                        "imgName": "icon-1.png",
                        "linkUrl": "pages\/coupon\/list?type=T",
                        "text": "热门活动",
                        "tip": "抽奖、次卡等",
                        "color": "#666666"
                      }],
                      // {
                      //   "imgUrl": "/static/nav/1.png",
                      //   "imgName": "icon-1.png",
                      //   "linkUrl": "pages\/pay\/index",
                      //   "text": "买单支付",
                      //   "tip": "支付攒积分",
                      //   "color": "#666666"
                      // }, {
                      //   "imgUrl": "/static/nav/2.png",
                      //   "imgName": "icon-1.png",
                      //   "linkUrl": "pages\/coupon\/list?type=P",
                      //   "text": "预存充值",
                      //   "tip": "充值有优惠",
                      //   "color": "#666666",
                      // }
                      
            "goodsStyle": {
                "background": "#F6F6F6",
                "display": "list",
                "column": 1,
                "show": ["goodsName", "goodsPrice", "linePrice", "sellingPoint", "goodsSales", "goodsLocator"]
            },
            "goodsParams": {
                "source": "auto",
                "auto": {
                    "category": 0,
                    "goodsSort": "all",
                    "showNum": 40
                }
            },
            "bannerStyle": {
                "btnColor": "#ffffff",
                "btnShape": "round",
                "interval": 2.5,
                
            },
            "bannerParam": {
                "interval": 2000
            },
            "navStyle": {
                "background": "#ffffff",
                "rowsNum": "2",
            },
            "storesStyle": {
                "background": "#F6F6F6",
                "display": "list",
                "column": 1,
                "show": ["goodsName", "goodsPrice", "linePrice", "sellingPoint", "goodsSales", "goodsLocator"]
            },
        },
        banner: [],
        goods: [],
        storeInfo: null,
        isReflash: false,
        storeList: [],
		visible: false,
		show:false,//弹出层
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad() {
      this.getPageData();
    },

    /**
     * 生命周期函数--监听页面显示
     */
    onShow() {
      const app = this
      showMessage();
      app.onGetStoreInfo();
      let isReflash = uni.getStorageSync("reflashHomeData");
      app.isReflash = isReflash;
      // if (isReflash === true) {
      //     app.getPageData();
      // }
      app.getPageData();
      uni.getLocation({
          type: 'gcj02',
          success(res){
              uni.setStorageSync('latitude', res.latitude);
              uni.setStorageSync('longitude', res.longitude);
            app.onGetStoreInfo();
          },
          fail(e) {
             // empty
          }
      })
      
    },

    methods: {
		ctrlC() {
			uni.setClipboardData({
				data: '18742034648',
				success: function () {
					console.log('success');
				}
			});
		},
		Call() {
			wx.makePhoneCall({
			  phoneNumber: "18742034648"
			})
		},
		open(){
			this.visible = true
			uni.hideTabBar()
		},
		closeMadle(){
			this.visible = false
			uni.showTabBar()
		},
		confirm(){
			// 这里调用接口执行点击确定后的操作并关闭弹窗
			console.log('点击了确认按钮')
			this.visible = false
		},
		cancel(){
			// 点击了取消按钮直接关闭弹窗
			console.log('点击了取消按钮')
			this.visible = false
		},
		showModal() {
			this.visible = true
			console.log("showModal")
		},
		  onTargetGoods(storeId) {
			this.$navTo(`pages/goods/detail`, { storeId })
		  },
        
        /**
         * 加载页面数据
         * @param {Object} callback
         */
        getPageData(callback) {
          const app = this
          Api.home()
            .then(result => {
                 app.banner = result.data.banner;
                 app.goods = result.data.goods;
                 uni.removeStorageSync("reflashHomeData");
                 app.isReflash = false;
            })
            .finally(() => callback && callback())
          settingApi.storeList()
            .then(result => {
              app.storeList = result.data.data
              console.log("index——getStoreList == ", result);
          })
        },
        
        /**
         * 下拉刷新
         */
        onPullDownRefresh() {
          // 获取数据
          this.getPageData(() => {
             uni.stopPullDownRefresh()
          })
        },
        
        /**
         * 获取默认店铺
         * */
         onGetStoreInfo() {
            const app = this
            settingApi.systemConfig()
             .then(result => {
               app.storeInfo = result.data.storeInfo
               if (app.storeInfo) {
                   uni.setStorageSync("storeId", app.storeInfo.id)
               }
             })
         },

         // 跳转详情页
        onTargetDetail(couponId, type, userCouponId) {
          if (type === 'P') {
              this.$navTo(`pages/prestore/buy`, { couponId })
          } else {
              if (type === 'C') {
                  this.$navTo(`pages/coupon/detail`, { couponId: couponId, userCouponId: userCouponId })
              } else if(type === 'T'){
                  this.$navTo(`pages/timer/detail`, { couponId: couponId, userCouponId: userCouponId })
              }
          }
        },
    },

    /**
     * 分享当前页面
     */
    onShareAppMessage() {
      const app = this
      return {
         title: "fuint会员系统",
         path: "/pages/index/index?" + app.$getShareUrlParams()
      }
    },

    /**
     * 分享到朋友圈
     * 本接口为 Beta 版本，暂只在 Android 平台支持，详见分享到朋友圈 (Beta)
     * https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/share-timeline.html
     */
    onShareTimeline() {
      const app = this
      const { page } = app
      return {
        title: page.params.share_title,
        path: "/pages/index/index?" + app.$getShareUrlParams()
      }
    }

  }
</script>

<style lang="scss" scoped>
	
	.circle{
	      position:fixed;
	      margin-left:27%;
	      margin-top:86%;
	      width: 37rpx;
	      height: 37rpx;
	      //background-image: linear-gradient(to bottom, #0c0c0c, #0c0c0c);
	      border-radius: 50%;
		  background-color: #00acac;
	  }
	  .child{
	      width: 46rpx;
	      height: 46rpx;
	      border-radius: 50%;
	      background-color: #fff;
	      position: relative;
	      top:2rpx;
	      left: 2rpx;
	      padding-left:8rpx;
	      padding-top:6rpx;
	  }
	.title{
		text-align: center;
		font-weight: 600;
		height: 50rpx;
		line-height: 50rpx;
		margin-bottom: 20rpx;
	}
	.body{
		font-size: 14px;
		font-weight: 600;
		color: darkorchid;
	}

.bottom{
 
 height: 30px;
 border-radius: 10rpx;
 padding-top:10rpx;
 padding-bottom:10rpx;
 padding-left:20rpx;
 padding-right:20rpx;
 margin-top: 8rpx;
 margin-left:0rpx;
 background: #fcecd0;
 width: 100%;
 display: flex;
 justify-content: flex-start;
 color: #c93131;
 font-size: 22rpx;
 line-height: 46rpx;
 text-align: center;
}
.txt{
 display: block;
 margin-left:15rpx;
}
.receive {
     height: 46rpx;
     width: 128rpx;
     line-height: 46rpx;
     text-align: center;
     border: 1px solid #fea68d;
     border-radius: 5rpx;
     color: #FFF;
     background: #efa591;
     font-size: 22rpx;
     &.left-box {
       border: 1px solid #c93131;
       color: #c93131;
       background: #F5F5F5;
     }
     &.right-box{
       margin-left:auto;
     }
   }


  .container {
    background: #fff;
  }

// 卡券列表
.goods-list {
    padding: 4rpx;
    box-sizing: border-box;
  }
  
  // 空数据按钮
  .empty-ipt {
    width: 220rpx;
    margin: 10rpx auto;
    font-size: 28rpx;
    height: 64rpx;
    line-height: 64rpx;
    text-align: center;
    color: #fff;
    border-radius: 5rpx;
    background: linear-gradient(to right, #00acac, #00acac);
  }

  // 单列显示
  .goods-list.column-1 {
    .goods-item {
      width: 100%;
      // height: 260rpx;
      margin-bottom: 12rpx;
      padding: 20rpx;
      box-sizing: border-box;
      background: #fff;
      line-height: 1.6;

      &:last-child {
        margin-bottom: 0;
      }
    }

    .goods-item_left {
      display: flex;
      width: 35%;
      background: #fff;
      align-items: center;

      .image {
        display: block;
        width: 200rpx;
        height: 157rpx;
        margin-top: 20rpx;
        border-radius: 6rpx;
      }
    }

    .goods-item_right {
      position: relative;
      flex: 1;

      .goods-name {
        margin-top: 30rpx;
        height: 44rpx;
        line-height: 1.3;
        white-space: normal;
        color: #484848;
        font-size: 30rpx;
      }
    }

    .goods-item_desc {
      margin-top: 0rpx;
      .coupon-attr {
         .attr-l {
             float: left;
             width: 70%;
         }
         .attr-r {
             margin-top: 0rpx;
             float: left;
         }
      }
    }
    .desc-selling_point {
      width: 400rpx;
      font-size: 24rpx;
      color: #e49a3d;
    }
    .receive {
      height: 46rpx;
      width: 128rpx;
      line-height: 46rpx;
      text-align: center;
      border: 1px solid #f8df00;
      border-radius: 5rpx;
      color: #f86d48;
      background: #f8df98;
      font-size: 22rpx;
      &.state {
        border: none;
        color: #cccccc;
        background: #F5F5F5;
      }
    }

    .desc-goods_sales {
      color: #999;
      font-size: 24rpx;
    }

    .desc_footer {
      font-size: 24rpx;

      .price_x {
        margin-right: 16rpx;
        color: #f03c3c;
        font-size: 30rpx;
      }

      .price_y {
        text-decoration: line-through;
      }
    }
  }

  .goods-item {
    float: left;
    box-sizing: border-box;
    padding: 6rpx;

    .goods-image {
      position: relative;
      width: 100%;
      height: 0;
      padding-bottom: 100%;
      overflow: hidden;
      background: #fff;

      &:after {
        content: '';
        display: block;
        margin-top: 100%;
      }

      .image {
        position: absolute;
        width: 100%;
        height: 100%;
        top: 0;
        left: 0;
        -o-object-fit: cover;
        object-fit: cover;
      }
    }

    .detail {
      padding: 8rpx;
      background: #fff;

      .goods-name {
        height: 64rpx;
        line-height: 32rpx;
        white-space: normal;
        color: #484848;
        font-size: 26rpx;
      }

      .detail-price {
        .goods-price {
          margin-right: 8rpx;
        }

        .line-price {
          text-decoration: line-through;
        }
      }
    }
  }
  .storeIn {
	  // position: fixed;
	  // bottom: 180rpx;
	  // right: 10rpx;
	  // width: 20%;
	  position: fixed;
	  margin-top: 40%;
	  margin-left: 75%;
	  
  }
</style>
