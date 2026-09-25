<template>
  <view class="w-full h-full bg-f3f3f5 flex flex-col">
    <uni-nav-bar />
    <h-status-header title="我记工的产品" />
    <!-- 时间选择 -->
    <view class="mx-16 px-16 bg-fff box h-88 mt-16 flex align-center justify-between rounded-16-top">
      <view class="color-5a6f82 font-28 pl-16">产品种类：</view>
      <text class="color-333 bold font-28">{{ dataTotal }}</text>
      <view class="font-28 flex align-center flex-1 justify-end">
        <image
          @tap="minusMonth"
          src="/static/images/icon_input_arr.svg"
          class="icon-48"
          style="transform: rotate(90deg)"
        />
        <picker
          mode="date"
          fields="month"
          :value="queryDate"
          :start="startDatePicker"
          :end="endDatePicker"
          @change="bindDateChange"
          class="mx-8"
        >
          <text>{{ showDate }}</text>
        </picker>
        <image
          @tap="addMonth"
          src="/static/images/icon_input_arr.svg"
          class="icon-48"
          style="transform: rotate(-90deg)"
        />
      </view>
    </view>
    <scroll-view
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
      :refresher-triggered="triggered"
      refresher-enabled
      enable-flex
      scroll-y
      class="w-100 mt-8 flex-1 overflow-hidden"
    >
      <!-- 产品列表 -->
      <h-empty v-if="pageEmpty" className="pt-320" tipsWord="未查到我记工的产品记录" />
      <template v-else>
        <view v-for="(dataItem, index) in ProductList" :key="index" class="px-16 box">
          <view class="bg-fff pr-32 box mb-8 overflow-hidden">
            <view class="flex align-center py-32 box overflow-hidden" @tap="handleListShow(dataItem)">
              <view class="font-28 flex align-center flex-1 bold pl-32">
                <h-text-display :width="212" :text="dataItem.productName" class="color-333" />
                <h-text-display :width="212" :text="`(${dataItem.productSeq})`" class="color-999" />
              </view>
              <view class="font-28 w-176 flex align-center justify-end">
                <word-icon class="mr-8" text="共" />
                <text class="color-ff0000 mr-8">{{ dataItem.productTotalNum }}</text>
                <text>{{ dataItem.productUnit }}</text>
              </view>
              <image
                src="/static/images/icon_unfold.svg"
                class="icon-32 ml-16"
                :class="{ 'icon-rotate-y': dataItem.listShow }"
              />
            </view>
            <view v-if="dataItem.listShow" class="list-item">
              <view
                v-for="(item, idx) in dataItem.processSeqInfoList"
                :key="idx"
                class="flex align-center justify-between py-24 box"
                :class="[idx + 1 === dataItem.processSeqInfoList.length ? '' : 'border-bottom-f5f5f5']"
              >
                <view class="font-28 flex-1 bold color-5a6f82 pl-32">
                  <h-text-display :text="item.processName" :width="212" />
                </view>
                <view class="flex align-center">
                  <view class="area-date-info--l">良</view>
                  <text class="color-333 font-28">{{ item.totalPassProductNum }}</text>
                </view>
                <view class="flex align-center ml-24">
                  <view class="mark-ebf0f5">不良</view>
                  <text class="color-333 font-28 ml-8">{{ item.totalNgProductNum }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
        <uni-load-more :status="dataNoMore" />
      </template>
    </scroll-view>
    <h-status-footer />
    <h-popup />
  </view>
</template>
<script setup lang="ts">
import { computed, onMounted, ref, reactive } from 'vue'
import { BigNumber } from 'bignumber.js'
import { _get } from '@/utils/common-request'
import { $state, $store, setGio } from '@/utils/common'
import HEmpty from '@/components/h-empty.vue'

import { onLoad, onShow } from '@dcloudio/uni-app'
import dayjs from 'dayjs'
onLoad((e) => {
  queryDate.value = e.queryDate
})

//搜索部分
const productNameOrCode = ref('')
function searchList(str) {
  pages.pageNum = 1
  productNameOrCode.value = str
  getProductList()
}
//刷新,加载
const pageEmpty = ref(false)
const dataNoMore = ref('')
const triggered = ref(false)
const pages = reactive({ pageSize: 20, pageNum: 1 })
const dataTotal = ref(0)
const dataEnd = computed(() => pages.pageNum * pages.pageSize >= dataTotal.value)
function onRefresh() {
  triggered.value = true
  pages.pageNum = 1
  getProductList()
}
function onLoadMore() {
  if (dataEnd.value) {
    return
  }
  pages.pageNum++
  getProductList()
}
//日期选择
const showDate = computed(() => {
  let date = queryDate.value.split('-')
  return date[1] + '月 ' + date[0]
})
const queryDate = ref('')
function minusMonth() {
  queryDate.value = dayjs(queryDate.value).subtract(1, 'month').format('YYYY-MM')
  getProductList()
}
function addMonth() {
  queryDate.value = dayjs(queryDate.value).add(1, 'month').format('YYYY-MM')
  getProductList()
}
//获取产品数据
const ProductList = ref([])
function getProductList() {
  let mockParams = {
    url: '/staff/calendar/processSeqsForProduct',
    data: {
      queryDate: queryDate.value,
      ...pages
    }
  }

  dataNoMore.value = 'loading'
  _get(mockParams)
    .then((res) => {
      if (pages.pageNum === 1) {
        ProductList.value = []
      }

      ProductList.value.push(...res.rows)
      ProductList.value.forEach((item) => {
        item.listShow = false
      })
      dataTotal.value = res.total || 0
    })
    .finally(() => {
      triggered.value = false
      pageEmpty.value = !ProductList.value.length
      dataNoMore.value = dataEnd.value ? 'noMore' : 'more'
    })
}
//明细
function handleListShow(dataItem) {
  dataItem.listShow = !dataItem.listShow
}

onMounted(() => {
  getProductList()
})
</script>
<style lang="scss" scoped>
.date-sel {
  height: px2vw(76);
}
.list-item {
  border-top: 1px dashed #e5e5e5;
}
.area-date-info--l {
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
</style>
