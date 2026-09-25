<template>
  <view class="flex align-center bg-f7f8e7 px-32 py-16">
    <image src="/static/images/icon_tips.svg" class="icon-32" />
    <view class="flex-1 ml-16 font-24">
      <text class="color-5a6f82">该员工已 </text>
      <text class="color-ff0000">{{ days }}</text>
      <text class="color-5a6f82"> 天未记工，是否关停账号？</text>
    </view>
    <slot />
  </view>
</template>

<script setup lang="ts">
const props = defineProps({
  days: {
    type: String,
    default: ''
  }
})
</script>

<style lang="scss" scoped></style>
