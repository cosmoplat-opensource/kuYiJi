<template>
  <uni-popup
    ref="popup"
    :isMaskClick="false"
    type="bottom"
    backgroundColor="#ffffff"
    @maskClick="handleCancel"
    :safe-area="true"
  >
    <view class="open-filter bg-fff flex flex-col justify-start font-28">
      <view class="title flex align-center px-24 box">
        <text class="color-5a6f82 font-28 bold flex-1 text-center">异常分析</text>
        <image @click="handleCancel" src="/static/images/icon_close_666.svg" class="icon-48" />
      </view>
      <view class="p-32 box b-b-1 border-f5f5f5">
        <!-- 产品 -->
        <view class="flex align-center">
          <h-text-display :text="dataItem.productName" :width="204" class="font-32 color-333 bold" />
          <h-text-display :text="`(${dataItem.productCode})`" :width="204" class="font-32 color-999 bold" />
        </view>
        <!-- 现工序 -->
        <view class="flex align-center mt-24">
          <view class="p-4 bg-efe2f7 rounded-4 color-c682ee font-24">报工工序</view>
          <h-text-display :text="dataItem.operateProcessName" :width="192" class="ml-8 font-28 color-333" />
          <view class="flex-1" />
          <area-date-info :dataItem="{ pass: dataItem.passNum, ng: dataItem.ngNum }" />
        </view>
      </view>
      <!-- 超报风险 -->
      <view class="flex align-start" v-if="flagData.negativeStockFlag === '0'">
        <view class="block mt-36" />
        <view class="ml-16 mr-32 py-32 box flex-1 item-border">
          <view class="color-333 font-32 line-height-32 bold">超报风险</view>
          <view class="flex align-center mt-24">
            <image src="/static/images/icon_tips.svg" class="icon-32" />
            <view class="ml-8 color-5a6f82 font-28">审核后可能会造成前工序负库存</view>
          </view>
          <view class="mt-32 flex align-center">
            <view class="p-4 bg-DEF2FF rounded-4 color-418CBD font-24">前工序</view>
            <h-text-display :text="dataItem.preProcessName" :width="192" class="ml-8 font-28 color-333" />
            <view class="flex-1" />
            <view class="p-4 bg-fce0de rounded-4 color-ff0000 font-24">待审核</view>
            <view class="ml-16 color-333 font-28">{{ flagData.preProcessWaitCheckPassNum }}</view>
            <view class="ml-24 p-4 bg-EBF0F5 rounded-4 color-5a6f82 font-24">库存</view>
            <view class="ml-16 color-333 font-28">{{ flagData.preProcessStockNum }}</view>
          </view>
        </view>
      </view>
      <!-- 良品率偏低 -->
      <view class="flex align-start" v-if="flagData.lowPassRateFlag === '0'">
        <view class="block mt-36" />
        <view class="ml-16 mr-32 py-32 box flex-1 item-border">
          <view class="color-333 font-32 line-height-32 bold">良品率偏低</view>
          <view class="flex align-center mt-24">
            <image src="/static/images/icon_tips.svg" class="icon-32" />
            <view class="ml-8 color-5a6f82 font-28">记工数量低于工序平均良品率</view>
          </view>
          <view class="mt-32 flex align-center">
            <view class="p-4 bg-c2f2ec rounded-4 color-19aa8d font-24">平均良品率</view>
            <view class="ml-16 font-28" :style="{ color: formatPercentColor(flagData.avgPassRateByDay) }">{{
              formatPercent(flagData.avgPassRateByDay)
            }}</view>
            <view class="ml-32 p-4 bg-c2f2ec rounded-4 color-19aa8d font-24">当前良品率</view>
            <view class="ml-16 font-28" :style="{ color: formatPercentColor(flagData.passRate) }">{{
              formatPercent(flagData.passRate)
            }}</view>
          </view>
        </view>
      </view>
      <!-- 记工数超产能 -->
      <view class="flex align-start" v-if="flagData.overProductiveCapacityFlag === '0'">
        <view class="block mt-36" />
        <view class="ml-16 mr-32 py-32 box flex-1">
          <view class="color-333 font-32 line-height-32 bold">记工数超产能</view>
          <view class="flex align-center mt-24">
            <image src="/static/images/icon_tips.svg" class="icon-32" />
            <view class="ml-8 color-5a6f82 font-28">记工数量已超工序日均产能</view>
          </view>
          <view class="mt-32 flex align-center">
            <view class="p-4 bg-DEF2FF rounded-4 color-418CBD font-24">日均产能</view>
            <view class="ml-16 color-333 font-28">{{ flagData.avgProductionCapacityByDay }}</view>
          </view>
        </view>
      </view>
      <view class="flex justify-center mt-40 mb-32">
        <h-button width="296" height="72" text="确定" class="ml-32" @tap.stop="handleCancel" />
      </view>
    </view>
  </uni-popup>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import bigNumber from 'bignumber.js'
import AreaDateInfo from '@/pages/report-production/components/area-date-info.vue'
const props = defineProps({
  dataItem: {
    type: Object,
    default: () => {}
  },
  flagData: {
    type: Object,
    default: () => {}
  }
})

function formatPercent(value: number) {
  return `${new bigNumber(value).multipliedBy(100).toNumber()}%`
}
function formatPercentColor(value: number) {
  return value > 0.9 ? '#19aa8d' : value > 0.6 ? '#E7A11A' : '#ff0000'
}

const popup = ref(null)
function open() {
  popup.value.open('bottom')
}
function handleCancel() {
  popup.value?.close('bottom')
}

defineExpose({
  open
})
</script>

<style lang="scss" scoped>
.uni-popup {
  z-index: 999;
}
.open-filter {
  border-radius: px2vw(16) px2vw(16) 0 0;
  .title {
    height: px2vw(96);
    border-bottom: px2vw(1) solid#F0F0F0;
  }
}
.item-border {
  border-bottom: px2vw(2) dotted #f5f5f5;
}
</style>
