<template>
  <view class="bg-fff border-bottom-f5f5f5 px-32 py-24 box">
    <view class="flex align-center mid">
      <view class="color-333 font-28 bold">
        <h-text-display :text="dataItem.productName" :width="200" />
      </view>
      <view class="color-999 font-28 flex-1 flex bold">
        ( <h-text-display :text="dataItem.productCode" :width="200" />)
      </view>
      <view class="flex align-center">
        <view class="color-5a6f82 font-24 flex align-center">
          <word-icon class="mr-8" text="共" />
          <text class="color-ff0000">{{ new BigNumber(dataItem.ngNum).plus(dataItem.passNum).toNumber() }}</text>
          <text>{{ formatStr(dataItem.productUnit, 5) }}</text>
        </view>
      </view>
    </view>
    <view class="flex align-center mt-24">
      <h-text-display :width="212" :text="dataItem.processName" class="color-5a6f82 font-28" />
      <view class="flex-1" />
      <area-date-info :dataItem="{ pass: dataItem.passNum, ng: dataItem.ngNum, productUnit: dataItem.productUnit }" />
    </view>
  </view>
</template>

<script setup lang="ts">
import AreaDateInfo from '@/pages/report-production/components/area-date-info.vue'
import { BigNumber } from 'bignumber.js'
import { formatStr } from '@/utils/common'

const props = defineProps({
  dataItem: {
    type: Object,
    default: () => {}
  },
  idx: {
    type: Number,
    default: 1
  }
})
const emit = defineEmits(['editNum', 'toHistory'])
function editNum(dataItem) {
  emit('editNum', dataItem)
}
function toHistory(dataItem) {
  emit('toHistory', dataItem)
}
</script>

<style lang="scss" scoped></style>
