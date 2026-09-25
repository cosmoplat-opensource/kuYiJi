<template>
  <view class="w-full h-full bg-f3f3f5 flex flex-col">
    <uni-nav-bar />
    <!-- 标题按口径区分：带日期=时间段报工口径；不带日期=在制库存余额口径 -->
    <h-status-header :title="pageTitle" />
    <view v-if="!hasDate" class="px-32 pb-8 font-22 color-999">
      库存口径：各产品在制不良余额（负数=前工序未报工形成的欠账）
    </view>
    <!-- 筛选输入框 -->
    <list-search @search="searchList" :needSelect="false" />
    <!-- 时间选择 -->
    <view v-show="startDate || endDate" class="px-16 box w-100 mt-16">
      <view class="w-100 date-sel rounded-16-top bg-fff box flex align-center">
        <date-select ref="dateSelect" bgClass="bg-f3f3f5" @dateChange="dateChange" />
      </view>
    </view>
    <scroll-view
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
      :refresher-triggered="triggered"
      refresher-enabled
      enable-flex
      scroll-y
      class="mt-16 flex-1 overflow-hidden"
    >
      <!-- 产品列表 -->
      <h-empty v-if="pageEmpty" className="pt-320" tipsWord="未查到产品的不良品记录" />
      <template v-else>
        <view v-for="(dataItem, index) in ProductList" :key="index" class="px-16 box">
          <view class="bg-fff px-32 box mb-8">
            <view class="flex justify-between align-center py-32 box overflow-hidden" @tap="handleListShow(dataItem)">
              <view class="font-28 flex-1 bold flex align-center">
                <h-text-display :text="dataItem.productName" :width="212" />
                <text class="color-999">({{ dataItem.productSeq }})</text>
              </view>
              <view class="font-28 w-176 flex align-center justify-end">
                <word-icon class="mr-8" text="共" />
                <text class="color-ff0000 mr-8">{{ dataItem.totalNgNum }}</text>
                <text>{{ dataItem.productUnit }}</text>
              </view>
              <image
                src="/static/images/icon_unfold.svg"
                class="icon-32 ml-16 shrink-0"
                :class="{ 'icon-rotate-y': dataItem.listShow }"
              />
            </view>
            <view v-if="dataItem.listShow" class="list-item">
              <view
                v-for="(item, idx) in dataItem.ngProductAndProcessList"
                :key="idx"
                class="flex align-center justify-between py-24 box"
                :class="[idx + 1 === dataItem.ngProductAndProcessList.length ? '' : 'border-bottom-f5f5f5']"
                @tap="toHistory(item)"
              >
                <view class="font-28 flex-1 bold color-5a6f82">{{ item.processName }}</view>
                <view class="flex align-center">
                  <view class="area-date-info--bl">不良</view>
                  <text class="color-ff0000 font-28">{{ item.ngNum }}</text>
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
import ListSearch from '@/components/product-search/list-search.vue'
import { $state, $store, setGio } from '@/utils/common'
import HEmpty from '@/components/h-empty.vue'

import { onLoad, onShow } from '@dcloudio/uni-app'
import DateSelect from '@/pages-analysis/components/date-select.vue'
onLoad((e) => {
  // 问一问"查看图表"跳转参数：?params=<encodeURIComponent(JSON)>（兼容旧 startDate/endDate query）
  let p: any = {}
  try {
    p = e.params ? JSON.parse(decodeURIComponent(e.params)) : {}
  } catch (err) {}
  const start = p.startDate || e.startDate
  const end = p.endDate || e.endDate
  const product = p.productNameOrCode
  if (start && end) {
    startDate.value = start
    endDate.value = end
  }
  if (product) productNameOrCode.value = product
})

// 口径区分（同一页面两套数据源）：
//  带日期 → /analysis/showNgProductList（时间段内报工/已审不良统计）
//  无日期 → /analysis/showNgProductListByStock（各产品在制不良余额，可能为负=欠账）
const hasDate = computed(() => !!(startDate.value || endDate.value))
const pageTitle = computed(() => (hasDate.value ? '生产不良品统计' : '在制不良品余额'))

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
function dateChange(start, end) {
  startDate.value = start
  endDate.value = end
  getProductList()
}
//获取产品数据
const ProductList = ref([])
function getProductList() {
  let mockParams = {}
  if (startDate.value || endDate.value) {
    //质量趋势过来
    mockParams = {
      url: '/analysis/showNgProductList',
      data: {
        productNameOrCode: productNameOrCode.value,
        startDate: startDate.value,
        endDate: endDate.value,
        ...pages
      }
    }
  } else {
    //首页过来
    mockParams = {
      url: '/analysis/showNgProductListByStock',
      data: {
        productNameOrCode: productNameOrCode.value,
        ...pages
      }
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
      dataTotal.value = res?.total || 0
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
function toHistory(data) {
  uni.navigateTo({
    url: '/pages/stock-list/change-history?item=' + JSON.stringify(data)
  })
}
const dateSelect = ref(null)
onMounted(() => {
  if (startDate.value || endDate.value) {
    //质量趋势过来
    dateSelect.value.setDate(startDate.value, endDate.value)
  }
  getProductList()
})
</script>
<style lang="scss" scoped>
.number-mark {
  background: #ebf0f5;
  border-radius: 0 px2vw(4) px2vw(4) 0;
  height: px2vw(32);
  display: flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  z-index: 2;
  left: px2vw(-48);
  top: px2vw(24);
}
.green-title:before {
  top: px2vw(0);
}
.date-sel {
  height: px2vw(76);
}
.list-item {
  border-top: 1px dashed #e5e5e5;
}
.area-date-info--bl {
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
</style>
