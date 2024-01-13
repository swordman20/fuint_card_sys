<template>
  <view v-show="!isLoading" class="container">
    <!-- 商品图片轮播 -->
    <!-- <SlideImage :images="storeDto.logo" /> -->
    <image :src="storeDto.logo" class="logo-class"></image>
    <!-- <view bindtap="navigation">微信小程序调起地图</view> -->
    <!-- <button @click="navRoad">{{storeDto.address}}</button> -->
    <!-- <view @click="navToEndPoint">
      <text>{{storeDto.address}}</text>
    </view> -->

    <!-- <map style="width:100%; height:300rpx;" :longitude="storeDto.longitude" :latitude="storeDto.latitude" scale="17"
			:markers="markers" @markertap="navRoad" show-location :data-marker="markers[0]"/> -->
	<!-- <view class="phone" @bindtap="Call">
		{{storeDto.address}}
	</view>
	<view class="phone" @bindtap="Call">
		{{storeDto.phone}}(可点击拨打)
	</view> -->

    <!-- 商品信息 -->
    <view v-if="!isLoading" class="goods-info">
      <!-- 标题、分享 -->
      <view class="info-item info-item__name dis-flex flex-y-center">
        <view class="goods-name flex-box">
          <text class="twolist-hidden" style="font-size:38rpx;">{{ storeDto.name }}</text>
		  <view class="address" @click="navRoad" style="padding-top: 25rpx;"><text class="location-icon iconfont icon-dingwei"></text>{{storeDto.address}}
				<!-- <image src="https://www.gtsmhlife.com/static/location.jpg" style=""></image> -->
		  </view>
		  <view class="phone" @click="Call" style="padding-top: 25rpx;"><text>联系电话：</text>{{storeDto.phone}}
				<!-- <text>(可点击拨打)</text> -->
				
		  </view>
        </view>
        <!-- #ifdef MP-WEIXIN -->
        <view class="goods-share__line"></view>
        <view class="goods-share">
          <button class="share-btn dis-flex flex-dir-column" >
			  <image @click="navRoad" src="https://www.gtsmhlife.com/static/location.jpg" style="width: 50rpx; height: 70rpx; padding-top: 81rpx; padding-bottom: 10rpx;"></image>
			  <image @click="Call" src="https://www.gtsmhlife.com/static/phone.jpg" style="width: 50rpx; height: 70rpx; "></image>
            <!-- <text class="share__icon iconfont icon-fenxiang"></text>
            <text class="f-24">分享</text> -->
          </button>
        </view>
        <!-- #endif -->
      </view>
      <!-- 商品卖点 -->
      <!-- <view v-if="storeDto.description" class="info-item info-item_selling-point" style="color: #ff5500;">
        <text>{{ storeDto.description }}</text>
      </view> -->
    </view>

    <!-- 选择商品规格 -->
    <!-- <view v-if="goods.isSingleSpec == 'N'" class="goods-choice m-top20 b-f" @click="onShowSkuPopup(3)">
      <view class="spec-list">
        <view class="flex-box">
          <text class="col-8">选择：</text>
          <text class="spec-name" v-for="(item, index) in goods.specList" :key="index">{{ item.name }}</text>
        </view>
        <view class="f-26 col-9 t-r">
          <text class="iconfont icon-xiangyoujiantou"></text>
        </view>
      </view>
    </view> -->

    <!-- 商品SKU弹窗 -->
    <!-- <SkuPopup v-if="!isLoading" v-model="showSkuPopup" :skuMode="skuMode" :goods="goods" @addCart="onAddCart"/> -->

    
    <!-- 卡券列表 -->
    <view v-if="!isLoading" class="goods-content m-top20">
      <view class="item-title b-f">
        <text>优惠详情</text>
      </view>
      <view v-for="(couponItem, couponIndex) in storeDto.coupons" :key="couponItem.id" @click="onTargetCouponDetail(couponItem.id, couponItem.type, couponItem.userCouponId)">
        <view class="bottom" v-if="couponItem.status === 'A' || couponItem.status === 'B' || couponItem.status === 'E' ">
          <!-- 卡券卖点 -->
          <!-- <view class="desc-selling_point dis-flex">
            <text class="onelist-hidden">{{ dataItem.sellingPoint }}</text>
          </view> -->
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

    <!-- 商品描述 -->
    <view v-if="!isLoading" class="goods-content m-top20">
      <view class="item-title b-f store-detail">
        <text>店铺详情</text>
      </view>
      <block v-if="storeDto.detailDescription != ''">
        <view class="goods-content-detail b-f">
          <jyf-parser :html="storeDto.detailDescription"></jyf-parser>
        </view>
      </block>
      <empty v-else tips="亲，暂无商品描述" />
    </view>

    <!-- 底部选项卡 -->
    <!-- <view class="footer-fixed">
      <view class="footer-container">
        <view class="foo-item-fast">
          <view class="fast-item fast-item--home" @click="onTargetHome">
            <view class="fast-icon">
              <text class="iconfont icon-shouye"></text>
            </view>
            <view class="fast-text">
              <text>首页</text>
            </view>
          </view>

          <view class="fast-item fast-item--cart" @click="onTargetCart">
            <view v-if="cartTotal > 0" class="fast-badge fast-badge--fixed">{{ cartTotal > 99 ? '99+' : cartTotal }}</view>
            <view class="fast-icon">
              <text class="iconfont icon-gouwuche"></text>
            </view>
            <view class="fast-text">
              <text>购物车</text>
            </view>
          </view>
        </view>
        <view class="foo-item-btn">
          <view class="btn-wrapper">
            <view class="btn-item btn-item-deputy" @click="onShowSkuPopup(2)">
              <text>加入购物车</text>
            </view>
            <view class="btn-item btn-item-main" @click="onShowSkuPopup(3)">
              <text>立即购买</text>
            </view>
          </view>
        </view>
      </view>
    </view> -->

    <!-- 快捷导航 -->
    <shortcut bottom="200rpx" />

  </view>
</template>

<script>
  import { setCartTabBadge, setCartTotalNum } from '@/utils/app'
  import * as GoodsApi from '@/api/goods'
  import * as CartApi from '@/api/cart'
  import jyfParser from '@/components/jyf-parser/jyf-parser'
  import Shortcut from '@/components/shortcut'
  import SlideImage from './components/SlideImage'
  import SkuPopup from './components/SkuPopup'
  import * as settingApi from '@/api/setting'

  // var amapFile = require('../../libs/qqmap-wx-jssdk.min');

  export default {
    components: {
      jyfParser,
      Shortcut,
      SlideImage,
      SkuPopup,
    },
    data() {
      return {
        // 正在加载
        isLoading: true,
        // 当前商品ID
        goodsId: null,
        // 商品详情
        goods: {},
        // 购物车总数量
        cartTotal: 0,
        // 显示/隐藏SKU弹窗
        showSkuPopup: false,
        // 模式 1:都显示 2:只显示购物车 3:只显示立即购买
        skuMode: 1,
        storeDto: {},
        storeId: 0,
        markers: [{
					id: 0,
					name: "",
					address: "",
					latitude: "",
					longitude: "",
					width: 25,
					height: 25
				}]
      }
    },

    // 导航
  navigation(){
    var that = this;
    var qqmapsdk = new amapFile({
      key: 'TEKBZ-7WW34-BRBUY-KIOQ3-77AOE-B6B66'
    });
    let name=this.storeDto.name
    let address=this.storeDto.address
    qqmapsdk.geocoder({
      address: address,
      success: function(res) {
        // 经度lng  	纬度lat
        let latitude=res.result.location.lat
        let longitude=res.result.location.lng
        console.log(res,'res')
        wx.getLocation({//获取当前经纬度
          type: 'wgs84', //返回可以用于wx.openLocation的经纬度，
          success: function (res) {
            wx.openLocation({ //使用微信内置地图查看位置。
              latitude: this.storeDto.latitude, //要去的纬度-地址
              longitude: this.storeDto.longitude, //要去的经度-地址
              name: this.storeDto.name,
              address: this.storeDto.address
            })
          }
        })
      },
      complete: res => {
        console.log(res.result.location)
      }
    })
  },

    onShow() {
      this.onRefreshPage()
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      // 商品ID
      console.log("options == ", options);
      this.storeId = options.storeId;
      uni.setStorageSync("storeId", this.storeId);
      // 加载页面数据
      this.isLoading = true
      this.onRefreshPage()
    },

    methods: {
		navRoad(event) {
			console.log("this.storeDto == ", this.storeDto)
			console.log(event)
			var latitude = Number(this.storeDto.latitude) //要去的纬度-地址
			var longitude = Number(this.storeDto.longitude) //要去的经度-地址
			var name = this.storeDto.name
			var address = this.storeDto.address
			wx.getLocation({ //获取当前经纬度
				type: 'wgs84', //返回可以用于wx.openLocation的经纬度，
				success: function (res) {
				console.log("wx.getLocation == ", res);
				wx.openLocation({ //使用微信内置地图查看位置。
					latitude: latitude, //要去的纬度-地址
					longitude: longitude, //要去的经度-地址
					name: name,
					address: address,
					fail: function () {
					  console.log("111")
					}
				})
				},
				fail: function (res) {
				  console.log("222")
				}
			})
		},

		navToEndPoint() {
			let plugin = requirePlugin('routePlan');
			let key = '';  //使用在腾讯位置服务申请的key
			let referer = '';   //调用插件的app的名称
			let endPoint = JSON.stringify({  //终点
				'name': '吉野家(北京西站北口店)',
				'latitude': 39.89631551,
				'longitude': 116.323459711
			});
			wx.navigateTo({
				url: 'plugin://routePlan/index?key=' + key + '&referer=' + referer + '&endPoint=' + endPoint
			});
	    },
		openMap() {
			wx.openLocation({
			  latitude: this.storeDto.latitude,//纬度
			  longitude: this.storeDto.longitude,//经度
			  name: this.storeDto.name,
			  address: this.storeDto.address,
			  scale: 18,
			  success: (res) => {
				console.log('打开成功')
			  },
			  fail: (err) => {
				console.log('打开失败')
			  }
			})
        },
		  //拨打电话
	    Call() {
			wx.makePhoneCall({
			  phoneNumber: this.storeDto.phone
			})
		},

       // 跳转详情页
       onTargetCouponDetail(couponId, type, userCouponId) {
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

      // 刷新页面数据
      onRefreshPage() {
        const app = this
        app.isLoading = true
        // Promise.all([app.getGoodsDetail(), app.getCartTotal()])
        //   .finally(() => app.isLoading = false)
        // Promise.all([settingApi.storeDetail()])
        //   .finally(() => app.isLoading = false)
        settingApi.storeDetail()
        .then(result => {
            app.storeDto = result.data.storeInfo;
            console.log("detail storeDto == ", app.storeDto);
            app.isLoading = false
            app.markers[0].name = app.storeDto.name
            app.markers[0].address = app.storeDto.address
            app.markers[0].latitude = Number(app.storeDto.latitude)
            app.markers[0].longitude = Number(app.storeDto.longitude)
        })
      },

      // 获取商品信息
      getGoodsDetail() {
        const app = this
        return new Promise((resolve, reject) => {
          GoodsApi.detail(app.goodsId)
            .then(result => {
                const goodsData = result.data;
                if (goodsData.skuList) {
                    goodsData.skuList.forEach(function(sku, index) {
                       goodsData.skuList[index].specIds = sku.specIds.split('-');
                       goodsData.skuList[index].skuId = sku.id;
                    })
                }
                app.goods = goodsData;
                app.skuMode = 3;
                resolve(result);
            })
            .catch(err => reject(err))
        })
      },

      // 获取购物车总数量
      getCartTotal() {
        const app = this
        return new Promise((resolve, reject) => {
          CartApi.list()
            .then(result => {
              app.cartTotal = result.data.totalNum;
              setCartTotalNum(app.cartTotal);
              setCartTabBadge();
              resolve(result);
            })
            .catch(err => reject(err));
        })
      },

      // 更新购物车数量
      onAddCart() {
         this.$toast("添加购物车成功");
         this.getCartTotal();
      },

      /**
       * 显示/隐藏SKU弹窗
       * @param {skuMode} 模式 1:都显示 2:只显示购物车 3:只显示立即购买
       */
      onShowSkuPopup(skuMode = 1) {
        this.skuMode = skuMode;
        this.showSkuPopup = !this.showSkuPopup;
      },

      // 跳转到首页
      onTargetHome(e) {
        this.$navTo('pages/index/index');
      },

      // 跳转到购物车页
      onTargetCart() {
        this.$navTo('pages/cart/index')
      }
    },

    /**
     * 分享当前页面
     */
    onShareAppMessage() {
      const app = this
      // 构建页面参数
      const params = app.$getShareUrlParams({
          goodsId: app.goodsId,
      })
      return {
        title: app.goods.name,
        imageUrl: app.goods.images[0],
        path: `/pages/goods/detail?${params}`
      }
    },

    /**
     * 分享到朋友圈
     * 本接口为 Beta 版本，暂只在 Android 平台支持，详见分享到朋友圈 (Beta)
     * https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/share-timeline.html
     */
    onShareTimeline() {
      const app = this
      // 构建页面参数
      const params = app.$getShareUrlParams({
        goodsId: app.goodsId,
      })
      return {
        title: app.goods.name,
        imageUrl: app.goods.images[0],
        path: `/pages/goods/detail?${params}`
      }
    }
  }
</script>

<style>
  page {
    background: #fafafa;
  }
</style>
<style lang="scss" scoped>
  @import "./detail.scss";
  .store-detail{
    padding-top:10rpx;
  }
  .logo-class{
    width: 100%;
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
	.store-info {
	  padding: 10px 0;
	  overflow: hidden;
	  border: 2rpx solid #cccccc;
	  min-height: 240rpx;
	  border-radius: 5rpx;
	  margin-top: 10rpx;
	  margin-bottom: 10rpx;
	  padding: 30rpx;
	  background: #FFFFFF;
	  .base-info {
	      float: left;
	      width: 70%;
	      .name {
	          font-size: 34rpx;
	          font-weight: bold;
	          margin-top: 15rpx;
	          margin-bottom: 12rpx;
	          color: #666;
	      }
	      .location-icon {
	          color: #f03c3c;
	          font-weight: bold;
	      }
	  }
	  }

</style>
