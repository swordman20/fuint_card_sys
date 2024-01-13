<template>
	<!-- <view class="modal" v-if="isShow">
		<view v-if="isShow" @click="Call"><text>联系电话：</text>18742034648<text>(可点击拨打)</text></view>
	</view> -->
	
	<!--弹出层 -->
	<view class="modal">
		<u-popup v-model="show" mode="center" class="popup" width="60%" height="50%">
		  <view>
			这是一个弹出层，可以放个图片
		  </view>
		  <view class="circle">
			<view class="child">
			  <u-icon name="close" class="close" @click="show = false"></u-icon>
			</view>
		  </view>
		</u-popup>      
	</view>
	
</template>

<script>
	export default {
		props: {
			isShow: {
				type: Boolean,
				default: false
			},
			// 子组件接收一个布尔类型的bottom，如果为true则弹窗则在页面的底部，false为默认居中显示
			bottom: {
				type: Boolean,
				default: false
			}
		},
		data() {
			return {
				visible: false
			}
		},
		methods: {
			close(){
				this.$emit('close')
			},
			cancel(){
				this.$emit('cancel')
			},
			confirm(){
				this.$emit('confirm')
			},
			showModel() {
				this.visible = true;
			},
			closeModel() {
				this.visible = false;
			},
			Call() {
				wx.makePhoneCall({
				  phoneNumber: "18742034648"
				})
			},
		}
	}
</script>

<style lang="scss">
	.circle{
	      position:fixed;
	      margin-left:27%;
	      margin-top:86%;
	      width: 37rpx;
	      height: 37rpx;
	      //background-image: linear-gradient(to bottom, #0c0c0c, #0c0c0c);
	      border-radius: 50%;
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
	
	.modal {
		position: fixed;
		background-color: gold;
		// margin-top: 200x;
		// margin-bottom: 150rpx;
		// margin-right: 50rpx;
		// margin-left: 50rpx;
		width: 300rpx;
		height: 300rpx;
		z-index: 99999999;
	}
	.content {
		
		
	}
	
</style>
