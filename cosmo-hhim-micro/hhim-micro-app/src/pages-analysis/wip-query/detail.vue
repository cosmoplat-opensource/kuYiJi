<template>
  <view class="w-full h-full bg-f3f3f5 relative flex flex-col">
    <uni-nav-bar />
    <h-status-header title="在制品明细" />
    <!-- 工序列表 -->
    <view class="flex justify-between align-center pl-48 pr-48 pt-24 pb-24 box">
      <view class="font-28 bold flex">
        <h-text-display :text="productInfo.productName" :width="180" />
        <view class="color-999 font-28 ml-8 flex"
          >(<h-text-display :text="productInfo.productCode" :width="180" />)</view
        >
      </view>
      <view class="flex-1"></view>
      <view class="font-24 flex align-center">
        <word-icon class="mr-8" text="共" />{{ new BigNumber(productInfo.passNum).plus(productInfo.ngNum).toNumber()
        }}<text class="ml-8 color-5a6f82">{{ formatStr(productInfo.productUnit, 5) }}</text>
      </view>
    </view>
    <!-- 筛选输入框 -->
    <list-search @search="searchList" :needSelect="false" />
    <scroll-view
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
      :refresher-triggered="triggered"
      scroll-y
      refresher-enabled
      enable-flex
      class="w-100 pr-16 box flex-1 mt-16 overflow-hidden"
    >
      <!-- 工序样式 -->
      <h-empty v-if="pageEmpty" tipsWord="查无此工序，请重新搜索" class-name="pt-240" />
      <template v-else>
        <list-item-process
          v-for="(item, index) in ProcessList"
          :key="index"
          :dataItem="item"
          :idx="index + 1"
          :showExplainIcon="true"
          @toHistory="toHistory"
        />
      </template>
      <uni-load-more v-if="!pageEmpty" :status="dataNoMore" />
    </scroll-view>
    <h-status-footer />
    <h-popup />
  </view>
</template>
<script setup lang="ts">
import { onMounted, ref, computed, reactive } from 'vue'
import { BigNumber } from 'bignumber.js'
import { _get } from '@/utils/common-request'
import { formatStr } from '@/utils/common'
import ListSearch from '@/components/process-search/list-search.vue'
import ListItemProcess from '@/pages-analysis/components/list-item-process.vue'
import { $state } from '@/utils/common'
import HEmpty from '@/components/h-empty.vue'
import { onLoad, onShow } from '@dcloudio/uni-app'

onShow(() => {
  getProductInfo()
  getConditionList()
})

onLoad((options) => {
  productSeq.value = options.productSeq
  getProductInfo()
  getConditionList()
})
// 加载测试数据
const checkMock = computed(() => $state.mock.mockOpen)
const productInfo = ref({})
const productSeq = ref('')
function getProductInfo() {
  _get({
    url: '/storage/product/list',
    data: {
      productSeq: productSeq.value,
      pageNum: 1,
      pageSize: 1
    }
  }).then((res: IResponseType<{}>) => {
    productInfo.value = res.rows[0] ?? {}
  })
}
//搜索部分
const processNameOrCode = ref('')
function searchList(str) {
  processNameOrCode.value = str
  getConditionList()
}
// 用来计算比例的数据，排行第一的数据和
const total = ref(0)
//获取排行数据
const ProcessList = ref([])
function getConditionList() {
  let mockParams = {
    url: '/storage/condition/list',
    data: {
      productSeq: productSeq.value,
      processNameOrCode: processNameOrCode.value,
      ...pages
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
      if (pages.pageNum === 1) {
        ProcessList.value = []
      }
      if (checkMock.value) {
        if (res.data) {
          ProcessList.value.push(...res.data)
        }
      } else {
        if (res.rows && res.rows[0]) {
          ProcessList.value.push(...res.rows)
        }
      }
      dataTotal.value = res.total
    })
    .finally(() => {
      triggered.value = false
      pageEmpty.value = !ProcessList.value.length
      dataNoMore.value = dataEnd.value ? 'noMore' : 'more'
    })
}
function toHistory(data, type) {
  uni.navigateTo({
    url: '/pages/stock-list/change-history?item=' + JSON.stringify(data) + '&type=' + type
  })
}
//数据加载，刷新部分
const triggered = ref(false)
const pages = reactive({ pageSize: 10, pageNum: 1 })
const dataTotal = ref(0)
const dataEnd = computed(() => pages.pageNum * pages.pageSize >= dataTotal.value)
const pageEmpty = ref(false)
const dataNoMore = ref('')
function onRefresh() {
  triggered.value = true
  pages.pageNum = 1
  getConditionList()
}
function onLoadMore() {
  if (dataEnd.value) {
    return
  }
  pages.pageNum++
  getConditionList()
}
</script>
<style lang="scss" scoped>
.number-mark {
  background: #ebf0f5;
  border-radius: 0 px2vw(4) px2vw(4) 0;
  height: px2vw(32);
  line-height: px2vw(32);
  position: absolute;
  z-index: 2;
  left: px2vw(-48);
  top: px2vw(24);
}
</style>
