<template>
  <view class="h-full bg-f3f3f5 relative flex flex-col overflow-hidden">
    <uni-nav-bar />
    <h-status-header title="库存分析" />
    <!-- 筛选输入框 -->
    <list-search @search="searchList" :needExact="true" class="mt-24" />
    <scroll-view
      scroll-y
      @refresherrefresh="onRefresh"
      :refresher-triggered="triggered"
      refresher-enabled
      class="flex-1 flex flex-col box rounded-16 overflow-hidden my-24"
    >
      <!-- 排行列表 -->
      <view class="flex justify-between align-center pr-32 pt-32 pb-8 bg-fff box mx-16">
        <view class="block" />
        <view class="color-5a6f82 font-28 bold ml-16 box flex-1">
          {{ !productSeq ? '库存排行(Top10)' : '工序库存排行' }}
        </view>
        <image
          @click="changeShowType"
          :src="showType === 'table' ? '/static/images/icon_chart.svg' : '/static/images/icon_table.svg'"
          class="icon-48"
        />
      </view>
      <view class="flex-1 overflow-hidden flex flex-col">
        <!-- 产品 表格样式 -->
        <view v-if="showType === 'table' && !productSeq" class="overflow-auto flex-1 px-16 box">
          <list-item-product
            v-for="(item, index) in StockList"
            :key="index"
            :dataItem="item"
            :idx="index + 1"
            class="bg-fff"
          />
          <view class="font-24 color-b6c0c9 py-48 box text-center">已到底部</view>
        </view>
        <!-- 产品 图表样式 -->
        <view
          v-show="showType === 'chart' && !productSeq"
          class="font-20 px-32 box overflow-hidden mx-16 flex-1 bg-fff"
        >
          <view v-for="(item, index) in StockList" :key="index" class="flex align-start pt-32">
            <view class="w-100px flex flex-col">
              <h-text-display :text="item.productName" :width="100" />
              <h-text-display :text="item.processName" :width="100" class="color-999" />
            </view>
            <h-tooltip ref="too" class="flex-1">
              <view class="flex h-32 align-center ml-8" @tap="showDetail(index)">
                <view
                  :style="{ width: item.passRate + '%' }"
                  :class="['h-32 color-fff justify-center flex-ac', item.passNum >= 0 ? 'bg-00bfa5' : 'bg-68a2f8']"
                  >{{ item.passNum }}</view
                >
                <view
                  :style="{ width: item.ngRate + '%' }"
                  :class="['h-32 color-fff justify-center flex-ac', item.ngNum >= 0 ? 'bg-F26A60' : 'bg-f4bb5b']"
                  >{{ item.ngNum }}</view
                >
                <text class="ml-8 w-56">{{ formatStr(item.total, 2) }}</text>
              </view>
              <template v-slot:content>
                <view>产品：{{ item.productName }}</view>
                <view>工序：{{ item.processName }}</view>
                <view>良：{{ item.passNum }}，不良：{{ item.ngNum }}</view>
              </template>
            </h-tooltip>
          </view>
          <view v-show="showType === 'chart'" class="flex align-center justify-between font-24 px-56 box mt-40 mb-32">
            <view class="flex align-center"> <view class="blockChart bg-00bfa5 mr-16"></view>良品 </view>
            <view class="flex align-center"> <view class="blockChart bg-F26A60 mr-16"></view>不良品 </view>
            <view class="flex align-center"> <view class="blockChart bg-68a2f8 mr-16"></view>良品(负) </view>
            <view class="flex align-center"> <view class="blockChart bg-f4bb5b mr-16"></view>不良品(负) </view>
          </view>
          <view class="font-24 color-b6c0c9 py-48 box text-center">已到底部</view>
        </view>
        <!-- 工序 表格样式 -->
        <view v-if="showType === 'table' && productSeq" class="overflow-hidden flex-1 px-16 box">
          <list-item-process
            v-for="(item, index) in StockList"
            :key="index"
            :dataItem="item"
            :idx="index + 1"
            :isLast="StockList.length === index + 1"
            class="bg-fff"
          />
          <view class="font-24 color-b6c0c9 py-48 box text-center">已到底部</view>
        </view>
        <!-- 工序 图表样式 -->
        <view v-show="showType === 'chart' && productSeq" class="font-20 px-24 mx-16 box overflow-hidden bg-fff">
          <view v-for="(item, index) in StockList" :key="index" class="flex align-start pt-32 flex-1">
            <view class="w-100px flex flex-col">
              <h-text-display :text="item.processName" :width="100" />
            </view>
            <h-tooltip ref="tooProcess" class="flex-1">
              <view class="flex-1 flex h-32 align-center ml-8" @tap="showDetailProcess(index)">
                <view
                  :style="{ width: item.passRate + '%' }"
                  :class="['h-32 color-fff text-center', item.passNum >= 0 ? 'bg-00bfa5' : 'bg-68a2f8']"
                  >{{ item.passNum }}</view
                >
                <view
                  :style="{ width: item.ngRate + '%' }"
                  :class="['h-32 color-fff text-center', item.ngNum >= 0 ? 'bg-F26A60' : 'bg-f4bb5b']"
                  >{{ item.ngNum }}</view
                >
                <text class="ml-8 w-56">{{ formatStr(item.total, 2) }}</text>
              </view>
              <template v-slot:content>
                <view>工序：{{ item.processName }}</view>
                <view>良：{{ item.passNum }}，不良：{{ item.ngNum }}</view>
              </template>
            </h-tooltip>
          </view>
          <view v-show="showType === 'chart'" class="flex align-center justify-between font-24 px-56 box mt-40 mb-32">
            <view class="flex align-center"> <view class="blockChart bg-00bfa5 mr-16"></view>良品 </view>
            <view class="flex align-center"> <view class="blockChart bg-F26A60 mr-16"></view>不良品 </view>
            <view class="flex align-center"> <view class="blockChart bg-68a2f8 mr-16"></view>良品(负) </view>
            <view class="flex align-center"> <view class="blockChart bg-f4bb5b mr-16"></view>不良品(负) </view>
          </view>
          <view class="font-24 color-b6c0c9 py-48 box text-center">已到底部</view>
        </view>
      </view>
    </scroll-view>
    <h-status-footer />
    <h-popup />
  </view>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import dayjs from 'dayjs'
import { BigNumber } from 'bignumber.js'
import { _get, _post } from '@/utils/common-request'
import { onShow } from '@dcloudio/uni-app'
import { $state, formatStr, setGio } from '@/utils/common'
import ListSearch from '@/components/product-search/list-search.vue'
import ListItemProduct from '@/pages-analysis/components/list-item-product.vue'
import ListItemProcess from '@/pages-analysis/components/list-item-process.vue'
import HTooltip from '@/components/h-tooltip.vue'
onShow(() => {
  setGio('jr_kcfx')
})
onMounted(() => {
  getStorageRank()
})
// 加载测试数据
const checkMock = computed(() => $state.mock.mockOpen)
//显示类型
const showType = ref('chart')
function changeShowType() {
  if (showType.value === 'table') {
    showType.value = 'chart'
  } else {
    showType.value = 'table'
  }
}
//搜索部分
const productSeq = ref('')
function searchList(item) {
  productSeq.value = item.itemSeq ? item.itemSeq : ''
  getStorageRank()
}
// 用来计算比例的数据，排行第一的数据和
const total = ref(0)
//获取排行数据
const StockList = ref([])
function getStorageRank() {
  let mockParams = {
    url: '/analysis/storageRank',
    data: {
      productSeq: productSeq.value
    }
  }
  if (checkMock.value) {
    Object.assign(mockParams, {
      url: '/analysis/loadDisplayData',
      data: { apiUrl: mockParams.url }
    })
  }
  _get(mockParams)
    .then((res) => {
      if (res?.data) {
        if (!productSeq.value) {
          StockList.value = res.data.splice(0, 10)
        } else {
          StockList.value = res.data
        }
        if (!StockList.value) return
        total.value = new BigNumber(StockList.value[0].passNum).plus(StockList.value[0].ngNum).toNumber()
        StockList.value.forEach((item, index) => {
          if (index === 0) {
            item.passRate = new BigNumber(item.passNum).dividedBy(total.value).multipliedBy(100).dp(0).toNumber()
            item.ngRate = new BigNumber(100).minus(item.passRate).toNumber()
          } else {
            item.passRate = new BigNumber(item.passNum).dividedBy(total.value).multipliedBy(100).dp(0).toNumber()
            item.ngRate = new BigNumber(item.ngNum).dividedBy(total.value).multipliedBy(100).dp(0).toNumber()
          }

          item.total = new BigNumber(item.passNum).plus(item.ngNum).toNumber()
        })
      }
    })
    .finally(() => {
      triggered.value = false
    })
}
//点击显示数据
const too = ref(null)
function showDetail(index) {
  let closeLength = StockList.value.length
  for (let i = 0; i < closeLength; i++) {
    too.value[i].closeTooltip()
  }
  too.value[index].showTooltip()
}
const tooProcess = ref(null)
function showDetailProcess(index) {
  let closeLength = StockList.value.length
  for (let i = 0; i < closeLength; i++) {
    tooProcess.value[i].closeTooltip()
  }
  tooProcess.value[index].showTooltip()
}

const triggered = ref(false)
function onRefresh() {
  triggered.value = true
  getStorageRank()
}
</script>
<style lang="scss" scoped>
.toast-custom {
  position: absolute;
  z-index: 999;
  line-height: px2vw(72);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 px2vw(8) px2vw(16) px2vw(1) rgba(216, 221, 229, 1);
  border: px2vw(1) solid #e5e5e5;
}
.blockChart {
  width: px2vw(24);
  height: px2vw(24);
  border-radius: px2vw(8);
  opacity: 1;
}
.number-mark {
  background: #ebf0f5;
  border-radius: 0 px2vw(4) px2vw(4) 0;
  height: px2vw(32);
  position: absolute;
  z-index: 2;
  left: px2vw(-48);
  top: px2vw(24);
}
</style>
