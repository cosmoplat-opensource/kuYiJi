<template>
  <view v-for="(dataItem, index) in dataList" :key="`p-${index}`" class="mb-8 bg-fff pb-24">
    <view class="box pt-32 px-32 flex align-center" :class="{ 'border-bottom-dashed': dataItem.listShow }">
      <view class="mark-ebf0f5">记工</view>
      <view class="font-28 color-5a6f82 ml-16">{{ dataItem.submitNickName }}</view>
      <view class="font-28 color-5a6f82 ml-16">{{ dataItem.submitDate }}</view>
    </view>
    <view class="flex align-center mt-24 px-32 box">
      <view class="color-333 font-28 bold">{{ dataItem.productName }}</view>
      <view class="color-999 font-28 bold">({{ dataItem.productCode }})</view>
    </view>
    <!--工序-->
    <view class="mt-24 px-32">
      <h-text-display :text="dataItem.processName" :width="512" class="color-5a6f82 font-28" />
    </view>
    <view class="flex align-center mt-24 px-32 box">
      <view class="mark-ebf0f5">质检</view>
      <view class="font-28 color-5a6f82 ml-16">{{ dataItem.qcNickName }}</view>
      <view class="font-28 color-5a6f82 ml-16">{{ dataItem.qcDate }}</view>
    </view>
    <view class="px-32 box mt-24">
      <area-date-info :dataItem="{ pass: dataItem.passNum, ng: dataItem.ngNum }" :unit="dataItem.productUnit" />
    </view>
    <rejects-type-displpy
      :dataList="dataItem.ngProductDetailInfoList"
      :unit="dataItem.productUnit || '件'"
      className="b-t-1 border-f5f5f5 mt-24"
    />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { $store } from '@/utils/common'
import HTextDisplay from '@/components/h-text-display.vue'
import AreaDateInfo from '@/pages/report-production/components/area-date-info.vue'
import RejectsTypeDisplpy from '@/components/rejects-type-displpy.vue'

const dataList = computed(() => {
  return $store.state.settlement.dataList
})
</script>

<style lang="scss" scoped></style>
