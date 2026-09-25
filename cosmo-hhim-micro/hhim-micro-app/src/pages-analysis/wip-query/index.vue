<template>
  <view class="w-full h-full bg-f3f3f5 flex flex-col">
    <uni-nav-bar />
    <h-status-header title="在制品查询" />
    <!-- 筛选输入框 -->
    <list-search @search="searchList" :needSelect="false" class="mt-24" />
    <scroll-view
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
      :refresher-triggered="triggered"
      refresher-enabled
      enable-flex
      scroll-y
      class="w-100 pl-16 pr-16 box mt-16 flex-1 overflow-hidden"
    >
      <!-- 产品列表 -->
      <h-empty v-if="pageEmpty" className="pt-320" tipsWord="暂无产品" />
      <template v-else>
        <view
          v-for="(dataItem, index) in ProductList"
          :key="index"
          @tap="toDetail(dataItem)"
          class="w-100 bg-fff flex justify-between align-start py-32 px-32 box mb-8 word-break"
        >
          <view class="font-28 relative flex-1 bold flex align-center">
            <h-text-display :text="dataItem.productName" :width="212" />
            <text class="color-999">({{ dataItem.productCode }})</text>
          </view>
          <view class="font-28 w-176 flex align-center justify-end">
            <word-icon class="mr-8" text="共" />
            <text class="color-ff0000 mr-8">{{ new BigNumber(dataItem.passNum).plus(dataItem.ngNum).toNumber() }}</text>
            <text>{{ dataItem.productUnit }}</text>
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
import ListSearch from '@/components/product-search/list-search.vue'
import { $state, $store, setGio } from '@/utils/common'
import HEmpty from '@/components/h-empty.vue'

import { onShow } from '@dcloudio/uni-app'
import HTextDisplay from '@/components/h-text-display.vue'
onShow(() => {
  setGio('jr_zzpcx')
  getProductList()
})
onMounted(() => {
  getProductList()
})
// 加载测试数据
const checkMock = computed(() => $state.mock.mockOpen)

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
//获取产品数据
const ProductList = ref([])
function getProductList() {
  let mockParams = {
    url: '/storage/product/list',
    data: {
      productNameOrCode: productNameOrCode.value,
      ...pages
    }
  }
  if (checkMock.value) {
    Object.assign(mockParams, {
      url: '/analysis/loadDisplayData',
      data: { apiUrl: mockParams.url }
    })
  }
  dataNoMore.value = 'loading'
  _get(mockParams)
    .then((res) => {
      if (pages.pageNum === 1) {
        ProductList.value = []
      }
      if (checkMock.value) {
        // ProductList.value = res?.data ?? []
        ProductList.value.push(...res.data)
      } else {
        // ProductList.value = res?.rows ?? []
        ProductList.value.push(...res.rows)
      }
      dataTotal.value = res?.total || 0
    })
    .finally(() => {
      triggered.value = false
      pageEmpty.value = !ProductList.value.length
      dataNoMore.value = dataEnd.value ? 'noMore' : 'more'
    })
}
//明细
function toDetail(dataItem) {
  uni.navigateTo({ url: `/pages-analysis/wip-query/detail?productSeq=${dataItem.productSeq}` })
}
</script>
<style lang="scss" scoped>
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
