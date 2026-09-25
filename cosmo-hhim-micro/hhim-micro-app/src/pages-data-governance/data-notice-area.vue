<template>
  <view class="flex align-center bg-f7f8e7 px-32 py-16">
    <image src="/static/images/icon_tips.svg" class="icon-32" />
    <view class="flex-1 ml-16">
      <text class="font-24 color-5a6f82">{{ text }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
const props = defineProps({
  text: {
    type: String,
    default: ''
  }
})
</script>

<style lang="scss" scoped></style>
