<template>
  <view class="w-100 bg-fff p-32 box mb-8">
    <view class="flex align-center justify-between">
      <view class="font-28 relative bold flex align-center color-333">
        <h-text-display :text="dataItem.productName" :width="220" class="overflow-hidden" />
        <text class="color-999">({{ dataItem.productSeq }})</text>
      </view>
      <image :src="formatImage('icon_detail')" class="icon-32" />
    </view>
    <view class="mt-24 color-5a6f82 font-24"
      >完工数量 <text class="color-333">{{ dataItem.completeTotalNum }}</text></view
    >
  </view>
</template>
<script setup lang="ts">
import { formatImage } from '@/utils/common'
const props = defineProps({
  dataItem: {
    type: Object,
    default: () => {}
  }
})
</script>
<style lang="scss"></style>
