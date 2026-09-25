<template>
  <view class="open-filter bg-f3f3f5 flex flex-col justify-start font-28">
    <uni-nav-bar />
    <h-status-header title="异常分析" />

    <view class="p-32 box b-b-1 border-f5f5f5 bg-fff rounded-16-top">
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
    <view class="flex align-start bg-fff mt-8" v-if="flagData.negativeStockFlag === '0'">
      <view class="ml-16 mr-32 py-32 box flex-1 item-border">
        <text class="color-fff font-24 bg-FE9F00 line-height-32 bold px-4">超报风险</text>
        <view class="color-5a6f82 font-28 mt-24 flex align-center"
          >审核后将导致 报工工序 库存为 <text class="color-ff0000">-21</text
          ><image src="/pages-audit/static/images/icon_explain.svg" class="icon-32 ml-8" @tap="showDetail('1')"
        /></view>
      </view>
    </view>
    <!-- 超报风险 -->
    <view class="flex align-start bg-fff mt-8" v-if="flagData.negativeStockFlag === '0'">
      <view class="ml-16 mr-32 py-32 box flex-1 item-border">
        <text class="color-fff font-24 bg-FE9F00 line-height-32 bold px-4">超报风险</text>
        <view class="color-5a6f82 font-28 mt-24 flex align-center"
          >审核后将导致 前工序 库存为 <text class="color-ff0000">-21</text
          ><image src="/pages-audit/static/images/icon_explain.svg" class="icon-32 ml-8" @tap="showDetail('2')"
        /></view>
        <view class="mt-32 flex align-center">
          <view class="p-4 bg-DEF2FF rounded-4 color-418CBD font-24">前工序</view>
          <h-text-display :text="dataItem.preProcessName" :width="192" class="ml-8 font-28 color-333" />
          <view class="flex-1" />
        </view>
      </view>
    </view>
    <!-- 良品率偏低 -->
    <view class="flex align-start bg-fff mt-8" v-if="flagData.lowPassRateFlag === '0'">
      <view class="ml-16 mr-32 py-32 box flex-1 item-border">
        <text class="color-fff font-24 line-height-32 bg-FE9F00 rounded-4 px-4">良品率偏低</text>
        <view class="color-5a6f82 font-28 mt-24">记工数量低于工序平均良品率</view>
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
    <view class="flex align-start bg-fff mt-8" v-if="flagData.overProductiveCapacityFlag === '0'">
      <view class="ml-16 mr-32 py-32 box flex-1">
        <text class="color-333 font-32 line-height-32 bold px-4">记工数超产能</text>
        <view class="flex align-center mt-24">
          <view class="color-5a6f82 font-28">记工数量已超工序日均产能</view>
        </view>
        <view class="mt-32 flex align-center">
          <view class="p-4 bg-DEF2FF rounded-4 color-418CBD font-24">日均产能</view>
          <view class="ml-16 color-333 font-28">{{ flagData.avgProductionCapacityByDay }}</view>
        </view>
      </view>
    </view>
    <uni-popup
      ref="popup"
      :isMaskClick="true"
      type="center"
      backgroundColor="#ffffff"
      @maskClick="handleCancel"
      :safe-area="true"
    >
      <view class="p-32 box bg-fff rounded-16 w-480 flex flex-col">
        <template v-if="showType === '1'">
          <view class="font-28 bold">报工工序库存( <text class="color-ff0000">-21</text> ) =</view>
          <view class="font-28 mt-16"
            >当前库存( <text class="color-ff0000">20</text> ) + 本次记工( <text class="color-ff0000">51</text> )
          </view>
          <view class="w-104 color-fff font-24 line-height-32 bg-FE9F00 rounded-4 text-center mt-24">可能原因</view>
          <view class="font-24 color-5a6f82 mt-16">1. 后一道工序的记工数据可能存在多报的情况或本条记工有异常； </view>
          <view class="font-24 color-5a6f82 mt-16">2. 原车间库存数量异常，可前往车间库存进行核对、调整。</view>
        </template>
        <template v-else>
          <view class="font-28 bold">前工序库存( <text class="color-ff0000">-21</text> ) =</view>
          <view class="font-28 mt-16"
            >待审核( <text class="color-ff0000">10</text> ) + 当前库存( <text class="color-ff0000">20</text> ) -
            本次记工( <text class="color-ff0000">51</text> )
          </view>
          <view class="w-104 color-fff font-24 line-height-32 bg-FE9F00 rounded-4 text-center mt-24">可能原因</view>
          <view class="font-24 color-5a6f82 mt-16">1. 这一条记工数据可能存在多报的情况，请审核前认真核对数据； </view>
          <view class="font-24 color-5a6f82 mt-16">2. 前一道工序的记工数量可能有异常，请关注前工序的记工数；</view>
          <view class="font-24 color-5a6f82 mt-16">3. 原车间库存数量异常，可前往车间库存进行核对、调整。</view>
        </template>
        <view class="flex justify-center mt-40">
          <h-button width="296" height="64" text="我知道了" @tap.stop="handleCancel" />
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import bigNumber from 'bignumber.js'
import AreaDateInfo from '@/pages/report-production/components/area-date-info.vue'

const dataItem = reactive({})
const flagData = reactive({})
onLoad((options) => {
  Object.assign(dataItem, JSON.parse(options.dataItem))
  Object.assign(flagData, JSON.parse(options.flagData))
})

function formatPercent(value: number) {
  return `${new bigNumber(value).multipliedBy(100).toNumber()}%`
}
function formatPercentColor(value: number) {
  return value > 0.9 ? '#19aa8d' : value > 0.6 ? '#E7A11A' : '#ff0000'
}
const showType = ref('')
function showDetail(type) {
  showType.value = type
  open()
}
const popup = ref(null)
function open() {
  popup.value.open('center')
}
function handleCancel() {
  popup.value?.close('center')
}
defineExpose({})
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
