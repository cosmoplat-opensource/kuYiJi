<template>
  <view class="h-full flex align-center justify-center">
    <image :src="formatImage('img_loading', 'jpg')" mode="widthFix" style="width: 100%" />
    <view
      v-if="uuc"
      class="uuc-back h-64 w-80 pr-32 justify-center px-24 bg-fff border-2 border-cad7eb rounded-32"
      @tap="handleQuit"
    >
      <image src="/static/images/icon_2circle.svg" class="icon-40" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { formatImage } from '@/utils/common'
import { onLaunch, onLoad, onShow } from '@dcloudio/uni-app'
import { uuc } from '@/utils/common'
onLoad((options) => {
  if (!uni.getStorageSync('micro_token')) {
    setTimeout(() => {
      uni.reLaunch({ url: `/pages-login/index?scene=${options.scene}` })
    }, 2000)
  }
})
function handleQuit() {
  plus.runtime.quit()
}
</script>

<style lang="scss" scoped>
.uuc-back {
  position: absolute;
  display: flex;
  align-items: center;
  top: px2vw(80);
  right: px2vw(32);
}
</style>
