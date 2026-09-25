<template>
  <view class="mx-16 box bg-fff p-32 b-b-1 border-f5f5f5" @tap="handleClick">
    <view class="flex align-center relative">
      <view class="flex-1">
        <h-text-display
          class="color-333 font-28 bold"
          :text="product ? dataItem.productName : dataItem.processName"
          :width="212"
        />
      </view>
      <h-checkbox :checked="dataItem.checked" @checkedChange="handleClick" />
    </view>
    <view class="color-999 font-24 mt-16">{{ product ? dataItem.productCode : dataItem.processCode }}</view>
    <view class="mt-24 flex align-center">
      <view class="mark-ebf0f5">添加人</view>
      <text class="ml-16 color-5a6f82 font-28">{{ dataItem.createdBy }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import HTextDisplay from '@/components/h-text-display.vue'

const props = defineProps({
  dataItem: {
    type: Object,
    default: () => {}
  },
  index: {
    type: Number,
    default: 1
  },
  product: {
    type: Boolean,
    default: false
  }
})

const emits = defineEmits(['handleItemCheck'])
function handleClick() {
  emits('handleItemCheck', props.dataItem)
}
</script>
