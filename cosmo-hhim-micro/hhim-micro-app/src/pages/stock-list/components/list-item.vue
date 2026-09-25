<template>
  <view class="report-production__list-item relative" @tap="toHistory(dataItem)">
    <view class="flex align-center mid">
      <view class="color-333 font-28 bold">
        <h-text-display :text="dataItem.productName" :width="300" />
      </view>
      <view class="color-999 font-28 flex-1 overflow-hidden bold flex"> ({{ dataItem.productCode }}) </view>
      <view class="flex align-center">
        <word-icon text="共" />
        <text class="color-5a6f82 font-28 ml-8">
          {{ new BigNumber(dataItem.ngNum).plus(dataItem.passNum).toNumber() }}
        </text>
        <text class="color-5a6f82 ml-8 font-28">{{ formatStr(dataItem.productUnit, 5) }}</text>
        <image
          v-if="showEdit"
          src="/static/images/icon_edit.svg"
          :class="{ 'stock-experience': isExperience }"
          @tap.stop="editNum(dataItem)"
          class="icon-32 ml-24"
        />
        <image
          v-if="showExplainIcon && new BigNumber(dataItem.ngNum).plus(dataItem.passNum).toNumber() < 0"
          :src="formatImage('icon_tips_tanhao_red', 'svg')"
          @tap.stop="emitShowExplain()"
          class="icon-32 ml-16"
        />
      </view>
    </view>
    <view class="flex align-center mt-24">
      <h-text-display :width="212" :text="dataItem.processName" class="color-5a6f82 font-28" />
      <word-icon v-if="dataItem.isLastProcess === '0'" class="ml-16" text="尾" />
      <word-icon v-if="dataItem.isFirstProcess === '0'" class="ml-16" text="首" />
      <view class="flex-1" />
      <area-date-info :dataItem="{ pass: dataItem.passNum, ng: dataItem.ngNum, productUnit: dataItem.productUnit }" />
    </view>
    <view v-if="showEdit" class="edit-area" @tap.stop="editNum(dataItem)" />
    <view
      v-if="showExplainIcon && new BigNumber(dataItem.ngNum).plus(dataItem.passNum).toNumber() < 0"
      class="edit-area"
      @tap.stop="emitShowExplain(dataItem)"
    />
  </view>
</template>

<script setup lang="ts">
import AreaDateInfo from '@/pages/report-production/components/area-date-info.vue'
import { BigNumber } from 'bignumber.js'
import { $store, formatImage, formatStr } from '@/utils/common'
import { computed } from 'vue'

const props = defineProps({
  dataItem: {
    type: Object,
    default: () => {}
  },
  showEdit: {
    type: Boolean,
    default: true
  },
  showExplainIcon: {
    type: Boolean,
    default: false
  }
})
const emit = defineEmits(['editNum', 'toHistory', 'explainPopup'])
function editNum(dataItem) {
  emit('editNum', dataItem)
}
function toHistory(dataItem) {
  if (isExperience.value) return
  emit('toHistory', dataItem)
}
function emitShowExplain(dataItem) {
  emit('explainPopup', dataItem)
}
// 检查是否是体验
const isExperience = computed(() => {
  return $store.getters.isExperience
})
</script>

<style lang="scss">
.report-production__list-item {
  background-color: #fff;
  padding: px2vw(24) px2vw(32) !important;
  .top__status--shen {
    background-color: #ff4655;
    border-radius: 50%;
    color: #fff;
    font-size: px2vw(24);
    line-height: px2vw(24);
    padding: px2vw(4);
  }
  .stock-experience {
    animation: scaleStockAnimation 3s infinite;
  }
  //体验特效
  @keyframes scaleStockAnimation {
    0% {
      transform: scale(1.3);
    }
    50% {
      transform: scale(0.9);
    }
    100% {
      transform: scale(1.3);
    }
  }

  .edit-area {
    position: absolute;
    right: 0;
    top: 0;
    width: px2vw(100);
    height: 100%;
  }
}
</style>
