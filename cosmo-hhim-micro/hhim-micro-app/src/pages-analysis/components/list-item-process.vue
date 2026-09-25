<template>
  <view
    class="bg-fff pt-24 pb-24 ml-16 pr-32 box mb-8"
    :class="[isLast ? '' : 'border-bottom-f5f5f5']"
    @tap="toHistory(dataItem)"
  >
    <view class="flex align-center justify-between mid">
      <view class="flex align-center">
        <view class="color-333 font-28 bold pl-32">
          <h-text-display :text="dataItem.processName" :width="512" />
        </view>
        <word-icon v-if="dataItem.isLastProcess === '0'" class="ml-16" text="尾" />
        <word-icon v-if="dataItem.isFirstProcess === '0'" class="ml-16" text="首" />
      </view>
      <image
        v-if="new BigNumber(dataItem.passNum).plus(dataItem.ngNum).toNumber() < 0 && showExplainIcon"
        @tap.stop="toHistory(dataItem, 'error')"
        :src="formatImage('icon_tips_tanhao_red', 'svg')"
        class="icon-32"
      />
    </view>
    <view class="flex align-center mt-24 pl-32">
      <area-date-info :dataItem="{ pass: dataItem.passNum, ng: dataItem.ngNum, productUnit: dataItem.productUnit }" />
      <view class="flex-1" />
      <view class="p-4 rounded-4 color-5a6f82 font-28 line-height-24 flex align-center">
        <word-icon class="mr-8" text="共" />
        <text class="color-333 bold">{{ new BigNumber(dataItem.passNum).plus(dataItem.ngNum).toNumber() }}</text>
        <text class="color-333 bold ml-8">{{ formatStr(dataItem.productUnit, 5) }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import AreaDateInfo from '@/pages/report-production/components/area-date-info.vue'
import { BigNumber } from 'bignumber.js'
import { formatImage, formatStr } from '@/utils/common'

const props = defineProps({
  dataItem: {
    type: Object,
    default: () => {}
  },
  idx: {
    type: Number,
    default: 1
  },
  isLast: {
    type: Boolean,
    default: false
  },
  showExplainIcon: {
    type: Boolean,
    default: false
  }
})
const emit = defineEmits(['editNum', 'toHistory'])
function editNum(dataItem) {
  emit('editNum', dataItem)
}
function toHistory(dataItem, type) {
  emit('toHistory', dataItem, type)
}
</script>

<style lang="scss" scoped></style>
