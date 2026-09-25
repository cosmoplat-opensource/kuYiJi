<template>
  <view class="flex align-center bg-f7f8e7 px-16" v-if="show">
    <image src="/static/images/icon_tips.svg" class="icon-32" />
    <view class="flex-1 ml-16">
      <text class="font-24 color-5a6f82">为保障工序库存的准确性，请准确选择前工序!</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const show = ref(false)
if (!uni.getStorageSync('micro_process_notices')) {
  show.value = true
}
function handleCloseNotice() {
  uni.setStorageSync('micro_process_notices', 1)
  show.value = false
}
</script>
