<template>
  <view v-if="isShareShow" class="shareTimeLine">
    <image :src="formatImage('0825ba69-14cd-4a36-aff6-3656a41be853')" class="share-image" />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { $state } from '@/utils/common'
import { formatImage } from '@/utils/common'

// H5 无“分享到朋友圈”能力，引导图在 H5 下恒不显示（避免整屏遮挡页面）
// 条件编译放在函数体内：原生 vite 下不会产生重复的 const 声明
const isShareShow = computed(() => {
  // #ifdef H5
  return false
  // #endif
  // #ifndef H5
  return $state.guide.shareTimeLine
  // #endif
})
</script>

<style lang="scss" scoped>
.shareTimeLine {
  position: absolute;
  left: 0;
  top: 0;
  z-index: 99999;
  width: 100%;
  height: 100%;
  .share-image {
    width: 100%;
    height: 100%;
  }
}
</style>
