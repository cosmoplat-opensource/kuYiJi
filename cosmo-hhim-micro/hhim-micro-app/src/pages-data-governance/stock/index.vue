<template>
  <view class="h-full bg-f3f3f5 box flex flex-col overflow-hidden">
    <uni-nav-bar />
    <h-status-header title="在制品库存治理" />
    <!--检测列表-->
    <scroll-view enable-flex scroll-y class="flex-1 flex flex-col overflow-hidden">
      <view class="py-24 px-48 flex align-center">
        <circle-progress :value="circleSmallPer" :widths="widthsSmall" :breadth="breadthSmall">
          <view class="color-333 font-32 bold">{{ circleSmallPer }}%</view>
        </circle-progress>
        <!--顶部区域-->
        <view class="ml-32">
          <!--数据健康度-->
          <view class="flex align-center font-28">
            <image src="/static/images/icon_health_green.svg" class="icon-32" />
            <text class="color-5a6f82 font-28 ml-8">库存健康度</text>
            <text class="color-333 font-28 ml-8 bold">{{ circleSmallPer }}%</text>
          </view>
          <!--产品总数-->
          <view class="flex align-center mt-24">
            <view class="mark-fe9f00">异常</view>
            <view class="ml-16 font-28">
              <text class="color-333 bold">{{ totalData.ngNum || 0 }}</text>
              <text class="ml-8 color-333 bold">条</text>
            </view>
          </view>
        </view>
      </view>
      <view class="mt-16 box relative">
        <!--负库存-->
        <template v-if="negativeStock.length">
          <view
            class="flex-center mx-16 pt-32 pb-24 px-32 b-b-1 overflow-hidden border-f5f5f5 bg-fff"
            @tap="handleStockShow"
          >
            <view class="flex-1 font-28 color-5a6f82 bold">负库存 </view>
            <view class="flex-center px-16 h-32 bg-EBF0F5 rounded-16 font-24 color-5a6f82">
              <text>共</text>
              <text class="color-ff0000 mx-8 bold">{{ dataTotalStock }}</text>
              <text>条记录</text>
            </view>
            <view class="icon-32 flex-center ml-32">
              <image src="/static/images/icon_unfold.svg" class="icon-32" :class="{ 'icon-rotate-y': stockShow }" />
            </view>
          </view>
          <scroll-view scroll-y v-if="stockShow" @scrolltolower="onLoadMore" enable-flex class="scroll_h">
            <view
              v-for="(item, index) in negativeStock"
              :key="index"
              :class="{ 'b-t-1 border-f5f5f5': index > 0 }"
              class="flex flex-col p-32 box bg-fff tap mx-16"
              @tap="toHistory(item)"
            >
              <view class="flex align-center mid">
                <view class="color-333 font-28 bold">
                  <h-text-display :text="item.productName" :width="212" />
                </view>
                <view class="color-999 font-28 flex-1 overflow-hidden bold flex"
                  >(<h-text-display :text="item.productCode" />)</view
                >
                <view class="flex align-center">
                  <word-icon class="mr-8" text="共" />
                  <text class="font-28" :class="item.ngNum > 0 ? 'color-5a6f82' : 'color-ff0000'">
                    {{ new BigNumber(item.ngNum).plus(item.passNum).toNumber() }}
                  </text>
                  <text class="font-28 color-5a6f82 ml-8">{{ formatStr(item.productUnit, 5) }}</text>
                </view>
              </view>
              <view class="flex align-center mt-24">
                <h-text-display :width="240" :text="item.processName" class="font-28 color-5a6f82" />
                <view class="flex-1" />
                <view class="area-date-info font-28">
                  <view class="area-date-info--l">良</view>
                  <text :class="item.passNum > 0 ? 'color-333' : 'color-ff0000'">{{ item.passNum || 0 }}</text>
                  <template v-if="item.ngNum">
                    <view class="mark-ebf0f5 ml-24">不良</view>
                    <text :class="item.ngNum > 0 ? 'color-333' : 'color-ff0000'">{{ item.ngNum || 0 }}</text>
                  </template>
                </view>
              </view>
            </view>
            <uni-load-more :status="dataNoMoreStock" />
          </scroll-view>
        </template>
        <!--不良品预警-->
        <template v-if="ngList.length">
          <view
            class="flex-center pt-32 pb-24 px-32 b-b-1 overflow-hidden border-f5f5f5 mt-16 bg-fff mx-16"
            @tap="handleNgShow"
          >
            <view class="flex-1 font-28 color-5a6f82 bold">不良品预警 </view>
            <view class="flex-center px-16 h-32 bg-EBF0F5 rounded-16 font-24 color-5a6f82">
              <text>共</text>
              <text class="color-ff0000 mx-8 bold">{{ dataTotalNg }}</text>
              <text>条记录</text>
            </view>
            <view class="icon-32 flex-center ml-32">
              <image src="/static/images/icon_unfold.svg" class="icon-32" :class="{ 'icon-rotate-y': ngShow }" />
            </view>
          </view>
          <scroll-view scroll-y v-if="ngShow" @scrolltolower="onLoadMore" enable-flex class="scroll_h">
            <view
              v-for="(item, index) in ngList"
              :key="index"
              :class="{ 'b-t-1 border-f5f5f5': index > 0 }"
              class="flex flex-col p-32 box bg-fff tap mx-16"
              @tap="toHistory(item)"
            >
              <view class="flex align-center mid">
                <view class="color-333 font-28 bold">
                  <h-text-display :text="item.productName" :width="212" />
                </view>
                <view class="color-999 font-28 flex-1 overflow-hidden bold flex">
                  (<h-text-display :text="item.productCode" />)
                </view>
                <view class="flex align-center">
                  <word-icon class="mr-8" text="共" />
                  <text class="font-28" :class="item.ngNum > 0 ? 'color-5a6f82' : 'color-ff0000'">
                    {{ new BigNumber(item.ngNum).plus(item.passNum).toNumber() }}
                  </text>
                  <text class="font-28 color-5a6f82 ml-8">{{ formatStr(item.productUnit, 5) }}</text>
                </view>
              </view>
              <view class="flex align-center mt-24">
                <h-text-display :width="240" :text="item.processName" class="font-28 color-5a6f82" />
                <view class="flex-1" />
                <view class="area-date-info font-28">
                  <view class="area-date-info--l">良</view>
                  <text :class="item.passNum > 0 ? 'color-333' : 'color-ff0000'">{{ item.passNum || 0 }}</text>
                  <template v-if="item.ngNum">
                    <view class="mark-ebf0f5 ml-24">不良</view>
                    <text :class="item.ngNum > 0 ? 'color-333' : 'color-ff0000'">{{ item.ngNum || 0 }}</text>
                  </template>
                </view>
              </view>
            </view>
            <uni-load-more :status="dataNoMoreNg" />
          </scroll-view>
        </template>
      </view>
    </scroll-view>
    <h-status-footer />
    <h-popup />
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, reactive } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import CircleProgress from '../circle-progress.vue'
import { _get } from '@/utils/common-request'
import { formatPx2Vw, formatStr } from '@/utils/common'
import { BigNumber } from 'bignumber.js'
import HExceptionLabel from '@/components/h-exception-label.vue'
import dayjs from 'dayjs'

import HTextDisplay from '@/components/h-text-display.vue'
import HPopup from '@/components/h-popup.vue'

const widthsSmall = ref(formatPx2Vw(160))
const breadthSmall = ref(formatPx2Vw(48))

onShow(() => {
  initData()
})
const stockShow = ref(false)
function handleStockShow() {
  stockShow.value = !stockShow.value
  pagesStock.pageNum = 1
  pagesNg.pageNum = 1
  if (stockShow.value) {
    getStockList()
    ngShow.value = false
  }
}
const ngShow = ref(false)
function handleNgShow() {
  ngShow.value = !ngShow.value
  pagesStock.pageNum = 1
  pagesNg.pageNum = 1
  if (ngShow.value) {
    getWarnNg()
    stockShow.value = false
  }
}
const dataTotalStock = ref(0)
const pagesStock = reactive({ pageSize: 15, pageNum: 1 })
const dataEndStock = computed(() => pagesStock.pageNum * pagesStock.pageSize >= dataTotalStock.value)
const dataNoMoreStock = ref('')
const dataTotalNg = ref(0)
const pagesNg = reactive({ pageSize: 15, pageNum: 1 })
const dataEndNg = computed(() => pagesNg.pageNum * pagesNg.pageSize >= dataTotalNg.value)
const dataNoMoreNg = ref('')
function onLoadMore() {
  if (stockShow.value) {
    if (dataEndStock.value) {
      return
    }
    pagesStock.pageNum++
    getStockList()
  }
  if (ngShow.value) {
    if (dataEndNg.value) {
      return
    }
    pagesNg.pageNum++
    getWarnNg()
  }
}
const negativeStock = ref([])
const ngList = ref([])
const totalData = ref({})
function initData() {
  pagesStock.pageNum = 1
  pagesNg.pageNum = 1
  _get({ url: '/warn/storage/health' }).then((res) => {
    totalData.value = res.data
  })

  getStockList()
  getWarnNg()
}
function getStockList() {
  // 库存-负库存
  _get({ url: '/warn/negativeStock', data: { pageNum: pagesStock.pageNum, pageSize: pagesStock.pageSize } })
    .then((res) => {
      if (pagesStock.pageNum === 1) {
        negativeStock.value = []
      }
      negativeStock.value.push(...res.rows)
      dataTotalStock.value = res.total || 0
    })
    .finally(() => {
      dataNoMoreStock.value = dataEndStock.value ? 'noMore' : 'more'
    })
}
function getWarnNg() {
  // 库存-不良品
  _get({ url: '/warn/ng', data: { pageNum: pagesNg.pageNum, pageSize: pagesNg.pageSize } })
    .then((res) => {
      if (pagesNg.pageNum === 1) {
        ngList.value = []
      }
      ngList.value.push(...res.rows)
      dataTotalNg.value = res.total || 0
    })
    .finally(() => {
      dataNoMoreNg.value = dataEndNg.value ? 'noMore' : 'more'
    })
}
function toHistory(data) {
  uni.navigateTo({
    url: '/pages/stock-list/change-history?item=' + JSON.stringify(data)
  })
}
// 数据健康度圆环
const circleSmallPer = computed(() => {
  if (!totalData.value.total || !totalData.value.ngNum) {
    return 100
  }
  let data = new BigNumber(totalData.value.total).minus(totalData.value.ngNum)
  const num = Math.floor(new BigNumber(data).div(totalData.value.total).times(100).toNumber()) || 0
  return Math.min(num, 100)
})
</script>

<style lang="scss" scoped>
.area-date-info {
  display: flex;
  align-items: center;
  &--l {
    border-radius: 50%;
    background: #19aa8d;
    font-size: px2vw(24);
    margin-right: px2vw(8);
    color: #fff;
    width: px2vw(32);
    height: px2vw(32);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  &--bl {
    margin-left: px2vw(24);
    margin-right: px2vw(12);
    background: #ebf0f5;
    border-radius: px2vw(4);
    border: px2vw(2) solid #d3dfeb;
    width: px2vw(56);
    height: px2vw(32);
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: px2vw(24);
    color: #5a6f82;
  }
}
.scroll_h {
  max-height: px2vw(900);
}
</style>
