<template>
  <view class="h-full bg-f3f3f5 box flex flex-col">
    <uni-nav-bar />
    <h-status-header title="异常分析" />
    <scroll-view enable-flex scroll-y class="flex-1 mt-16 flex flex-col justify-start font-28">
      <view class="p-32 box bg-fff">
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
      <view class="bg-fff mt-8 px-32 pt-24 pb-32 box" v-if="flagData.negativeStockFlag === '0'">
        <view class="flex">
          <view class="p-4 bg-FE9F00 rounded-4 color-fff font-24">超报风险</view>
        </view>
        <view class="color-333 font-28 mt-24 line-height-44">
          <text>报工工序</text>
          <text class="color-c682ee px-8 bold">{{ dataItem.operateProcessName }}</text>
          <text>的累计产出</text>
          <text class="color-ff0000 px-8 bold">大于</text>
          <text>前工序</text>
          <text class="color-418CBD px-8 bold">{{ dataItem.preProcessName }}</text>
          <text>的累计产出！</text>
        </view>
        <view class="flex mt-32">
          <view class="p-4 bg-19AA8D rounded-4 color-fff font-24">建议</view>
        </view>
        <view class="font-28 color-5a6f82 mt-24 line-height-44">
          <view>
            1. 如工序<text class="color-333 px-8">{{ dataItem.operateProcessName }}</text
            >超报，可直接修正报工数量；
          </view>
          <view>
            2. 如记工无误，可确认工序<text class="color-333 px-8">{{ dataItem.preProcessName }}</text
            >的历史库存及当日产出。
          </view>
        </view>
      </view>
      <!-- 良品率偏低 -->
      <view class="bg-fff mt-8 px-32 pt-24 pb-32 box" v-if="flagData.lowPassRateFlag === '0'">
        <view class="flex">
          <view class="p-4 bg-FE9F00 rounded-4 color-fff font-24">良品率偏低</view>
        </view>
        <view class="flex align-center mt-24">
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
      <!-- 记工数超产能 -->
      <view class="bg-fff mt-8 px-32 pt-24 box" v-if="flagData.overProductiveCapacityFlag === '0'">
        <view class="flex">
          <view class="p-4 bg-9badbc rounded-4 color-fff font-24">记工数超产能</view>
        </view>
        <view class="flex align-center mt-24">
          <view class="ml-8 color-5a6f82 font-28">记工数量已超工序日均产能</view>
        </view>
        <view class="mt-32 flex align-center">
          <view class="flex align-center">
            <view class="p-4 bg-DEF2FF rounded-4 color-418CBD font-24">今日记工总数</view>
            <view class="ml-16 color-333 font-28 bold">{{ flagData.todaySubmitNum }}</view>
          </view>
          <view class="flex align-center ml-36">
            <view class="p-4 bg-DEF2FF rounded-4 color-418CBD font-24">日均产能</view>
            <view class="ml-16 color-333 font-28">{{ flagData.avgProductionCapacityByDay }}</view>
          </view>
        </view>
        <view class="flex justify-between align-center h-96 mt-32 border-top-f5f5f5">
          <view class="font-24 color-333"
            >涉及
            <text class="color-ff0000 bold">{{
              flagData.overProductiveCapacityExceptionRecordIds?.split(',')?.length
            }}</text>
            条待审核数据</view
          >
          <h-button width="120" height="48" text="去审核" font="font-24" @tap.stop="handleAduit" />
        </view>
      </view>
      <view class="flex-1"></view>
      <view class="flex justify-center mt-40 mb-32">
        <h-button width="296" height="72" text="去确认报工数量" @tap.stop="handleCancel" />
      </view>
    </scroll-view>
    <h-status-footer />
  </view>
</template>

<script setup lang="ts">
import bigNumber from 'bignumber.js'
import AreaDateInfo from '@/pages/report-production/components/area-date-info.vue'
import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { $store } from '@/utils/common'
const dataItem = ref({})
const flagData = ref({})
onLoad((options) => {
  dataItem.value = JSON.parse(options.dataItem)
  flagData.value = JSON.parse(options.flagData)
})

function handleCancel() {
  uni.navigateBack()
}
function handleAduit() {
  let filterData = {
    productNameOrCode: dataItem.value.productName,
    processNameOrCode: dataItem.value.operateProcessName,
    submitNickName: dataItem.value.submitNickName,
    startDate: dataItem.value.submitDay,
    endDate: dataItem.value.submitDay
  }
  let options = {
    switchChange: false,
    ids: flagData.value.overProductiveCapacityExceptionRecordIds
  }
  $store.commit('audit/setFilterData', filterData)
  $store.commit('audit/setCrossPageData', options)
  $store.commit('tabBar/setTabActive', 1)
  uni.reLaunch({ url: '/pages/main' })
}
function formatPercent(value: number) {
  return `${new bigNumber(value).multipliedBy(100).toNumber()}%`
}
function formatPercentColor(value: number) {
  return value > 0.9 ? '#19aa8d' : value > 0.6 ? '#E7A11A' : '#ff0000'
}
</script>

<style lang="scss" scoped>
.item-border {
  border-bottom: px2vw(2) dotted #f5f5f5;
}
</style>
