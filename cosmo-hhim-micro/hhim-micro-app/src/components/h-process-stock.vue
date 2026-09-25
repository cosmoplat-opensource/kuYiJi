<template>
  <view class="flex align-center color-5a6f82 font-24">
    <text>未审核</text>
    <text class="color-333 mx-8 bold">{{ dataItem.unapprovedNum }}</text>
    <text class="mx-16">|</text>
    <text>库存</text>
    <text class="color-333 mx-8 bold">{{ dataItem.totalStockNum }}</text>
  </view>
</template>

<script setup lang="ts">
const props = defineProps({
  dataItem: {
    type: Object,
    default: () => {}
  }
})
</script>

<style lang="scss" scoped></style>
