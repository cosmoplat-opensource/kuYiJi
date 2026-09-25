<template>
  <view class="flex justify-center py-16">
    <view class="px-24 py-12 bg-fff rounded-24 font-24 color-5a6f82">{{ dateTime }}</view>
  </view>
</template>

<script setup lang="ts">
const props = defineProps({
  dateTime: {
    type: String,
    default: ''
  }
})
</script>

<style lang="scss" scoped></style>
