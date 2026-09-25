<template>
  <view class="w-full h-full bg-f3f3f5 flex flex-col">
    <uni-nav-bar />
    <h-status-header title="记工明细" />
    <view class="py-24 px-48 box flex align-center justify-between">
      <view class="font-28 color-5a6f82 flex-1 flex align-center">
        <h-text-display :text="userInfo.nickName" :width="190" />
        (<h-text-display :text="userInfo.userName" :width="190" class="color-999" />)
      </view>
      <view class="font-24">{{ startDate }} ~ {{ dayjs(endDate).format('MM-DD') }}</view>
    </view>
    <view class="flex-1 flex flex-col rounded-16 mb-24 overflow-hidden">
      <scroll-view
        @refresherrefresh="onRefresh"
        @scrolltolower="onLoadMore"
        :refresher-triggered="triggered"
        refresher-enabled
        enable-flex
        scroll-y
        class="w-100 flex-1 overflow-hidden"
      >
        <view class="py-128 bg-fff mx-16" v-if="pageEmpty">
          <h-empty tipsWord="未查到产品记录" />
        </view>
        <template v-else>
          <view
            v-for="(dataItem, idx) in ProductList"
            :key="idx"
            class="px-32 pb-16 box bg-fff mx-16 relative list-item"
          >
            <view class="flex align-center mid box">
              <view class="color-333 font-28 bold">
                <h-text-display :text="dataItem.productName" :width="200" />
              </view>
              <view class="color-999 font-28 flex-1 flex bold">
                ( <h-text-display :text="dataItem.productCode" :width="200" />)
              </view>
              <view class="flex align-center">
                <view class="color-5a6f82 font-24 flex align-center">
                  记工
                  <text class="color-ff0000 bold font-28 ml-8">{{ dataItem.totalNums }}</text>
                </view>
              </view>
            </view>
            <view
              v-for="(item, itemIdx) in dataItem.recordForProductAndProcessList"
              :key="itemIdx"
              class="flex align-center py-32"
              :class="itemIdx + 1 === dataItem.recordForProductAndProcessList.length ? '' : 'border-bottom-f5f5f5'"
            >
              <h-text-display :text="item.processName" class="color-5a6f82 font-28" :width="212" />
              <view class="flex-1" />
              <area-date-info :dataItem="{ pass: item.passNum, ng: item.ngNum, productUnit: item.productUnit }" />
            </view>
          </view>

          <uni-load-more :status="dataNoMore" />
        </template>
      </scroll-view>
    </view>
    <h-status-footer />
    <h-popup />
  </view>
</template>
<script setup lang="ts">
import { computed, onMounted, ref, reactive } from 'vue'
import { BigNumber } from 'bignumber.js'
import { _get } from '@/utils/common-request'
import AreaDateInfo from '@/pages/report-production/components/area-date-info.vue'
import ListSearch from '@/components/product-search/list-search.vue'
import { $state, $store, setGio } from '@/utils/common'
import HEmpty from '@/components/h-empty.vue'
import WordIcon from '@/components/word-icon.vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import DateSelect from '@/pages-analysis/components/date-select.vue'
import dayjs from 'dayjs'
onLoad((e) => {
  startDate.value = e.startDate
  endDate.value = e.endDate
  Object.assign(userInfo, JSON.parse(e.userInfo))
})

//刷新,加载
const pageEmpty = ref(false)
const dataNoMore = ref('')
const triggered = ref(false)
const pages = reactive({ pageSize: 10, pageNum: 1 })
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
const startDate = ref('')
const endDate = ref('')
const userInfo = reactive({})
//获取产品数据
const ProductList = ref([])
function getProductList() {
  let mockParams = {
    url: '/analysis/showProductList',
    data: {
      userName: userInfo.userName,
      startDate: startDate.value,
      endDate: endDate.value,
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
      dataTotal.value = res?.total || 0
    })
    .finally(() => {
      triggered.value = false
      pageEmpty.value = !ProductList.value.length
      dataNoMore.value = dataEnd.value ? 'noMore' : 'more'
    })
}

const dateSelect = ref(null)
onMounted(() => {
  getProductList()
})
</script>
<style lang="scss" scoped>
.number-mark {
  background: #ebf0f5;
  border-radius: 0 px2vw(4) px2vw(4) 0;
  padding: px2vw(6) px2vw(8);
  position: absolute;
  left: px2vw(-50);
  top: px2vw(0);
}
.list-item {
  padding-top: px2vw(48);
  &:first-of-type {
    padding-top: px2vw(32);
  }
  &:not(:last-of-type) {
    border-bottom: 1px dashed #e5e5e5;
  }
}
</style>
