<!--页面顶部安全区域-->
<template>
  <view class="h-status-header" :style="{ backgroundColor }">
    <view class="p-16">
      <image :src="formatImage('icon_return', 'svg')" class="img-back" @tap="handleBack" v-show="showBack" />
    </view>
    <text class="title">{{ props.title }}</text>
  </view>
</template>
<script setup lang="ts">
import { formatImage } from '@/utils/common'

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  showBack: {
    type: Boolean,
    default: true
  },
  backgroundColor: {
    type: String,
    default: '#f3f3f5'
  }
})

function handleBack() {
  uni.navigateBack()
}
</script>
<style lang="scss">
.h-status-header {
  height: px2vw(88);
  display: flex;
  align-items: center;
  padding-left: px2vw(8);
  padding-right: px2vw(24);
  position: relative;

  .img-back {
    width: px2vw(48);
    height: px2vw(48);
  }

  .title {
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
    font-size: px2vw(32);
    color: #333;
    font-weight: bold;
  }
}
</style>
