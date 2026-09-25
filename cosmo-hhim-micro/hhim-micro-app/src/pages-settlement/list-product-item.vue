<template>
  <view v-for="(item, index) in dataList" :key="index" class="flex align-center py-32 px-32 box bg-fff mb-8">
    <h-text-display class="bold color-333 font-28" :text="item.productName" :width="200" />
    <view class="font-28 color-999 bold">({{ item.productCode }})</view>
    <view class="flex-1" />
    <view class="mark-ebf0f5">完工</view>
    <view class="font-28 color-333 bold ml-12">{{ item.completeTotalNum }}</view>
    <view class="font-28 color-333 bold ml-8">{{ item.productUnit || '' }}</view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { $store } from '@/utils/common'
import HTextDisplay from '@/components/h-text-display.vue'

const dataList = computed(() => $store.state.settlement.dataList)
</script>
