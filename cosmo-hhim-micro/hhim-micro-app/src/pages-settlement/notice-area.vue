<template>
  <view class="box flex align-center bg-f7f8e7 px-16 h-64" v-if="show">
    <image src="/static/images/icon_tips.svg" class="icon-32" />
    <view class="flex-1 ml-16">
      <text class="font-24 color-5a6f82">统计数据源自完工报告，如有问题请先调整完工报告</text>
    </view>
    <image @click="handleCloseNotice" src="/static/images/icon_close_666.svg" class="icon-48" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const show = ref(false)
if (!uni.getStorageSync('micro_settlement_notice')) {
  show.value = true
}
function handleCloseNotice() {
  uni.setStorageSync('micro_settlement_notice', 1)
  show.value = false
}
</script>
