<template>
  <view class="flex flex-col h-100 overflow-hidden bg-f3f3f5" @touchstart="touchStart" @touchend="touchEnd">
    <view class="flex flex-col relative">
      <view class="h-80 pl-8 pr-16 box mb-24 mt-16 flex align-center justify-between pr-24 box bg-fff rounded-16 mx-16">
        <h-tabs
          ref="hTabs"
          wrapperClass="pl-0"
          itemClass="px-24"
          :tabData="tabs"
          :activeIndex="activeIndex"
          :config="defaultConfig"
          @tabClick="tabClick"
        />
        <view class="flex align-center" v-if="!Role_Staff">
          <h-button
            width="128"
            height="48"
            font="font-24"
            text="导入导出"
            type="border-1 border-0066ff color-0066ff bg-fff"
            @tap.stop="exportToMail"
          />
          <h-button width="128" font="font-24" height="48" text="变动库存" class="ml-16" @tap.stop="addStockChange" />
        </view>
      </view>
      <view v-if="activeIndex === 0" class="flex align-center">
        <list-product-search
          @search="searchProductList"
          :needSelect="false"
          class="flex-1"
          wrapper-padding-right="pr-0"
        />
        <h-page-video :videoArr="[10]" page-name="车间库存-产品库存" class="mx-24" />
      </view>
      <view v-else class="flex align-center">
        <list-search @search="searchList" :focusAble="false" class="flex-1" />
        <h-page-video :videoArr="[10]" page-name="车间库存-工序库存" class="mr-24" />
      </view>
    </view>
    <system-notice-area v-model="systemNotice" />
    <!--数据列表滚动区域-->
    <scroll-view
      :style="activeIndex === 1 ? '' : 'display:none'"
      scroll-y
      :scroll-into-view="scrollTop"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
      :refresher-triggered="triggered"
      refresher-enabled
      class="area-scroll mt-16"
      enable-flex
    >
      <h-empty v-if="pageEmpty" tipsWord="暂无库存，报产审核后的可在此显示" class-name="pt-240" />
      <template v-else>
        <list-item
          class="area-scroll__list-item"
          v-for="(item, index) in dataList"
          :id="`scroll-stock-${index}`"
          :key="index"
          :dataItem="item"
          :showEdit="false"
          :showExplainIcon="true"
          @editNum="editNum"
          @toHistory="toHistory"
          @explainPopup="explainPopup"
        />
        <uni-load-more :status="dataNoMore" />
      </template>
    </scroll-view>
    <scroll-view
      :style="activeIndex === 0 ? '' : 'display:none'"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
      :scroll-into-view="scrollTop"
      :refresher-triggered="triggeredProduct"
      enable-flex
      refresher-enabled
      scroll-y
      class="px-16 box mt-16 flex-1 overflow-hidden"
    >
      <!-- 产品列表 -->
      <h-empty v-if="pageEmptyProduct" className="pt-320" tipsWord="暂无产品" />
      <template v-else>
        <view
          v-for="(dataItem, index) in ProductList"
          :key="index"
          :id="`scroll-product-${index}`"
          @tap="toDetail(dataItem)"
          class="bg-fff flex justify-between align-center p-32 box mb-8"
        >
          <view class="font-28 relative flex-1 bold flex align-center">
            <h-text-display :text="dataItem.productName" :width="200" />
            <text class="color-999">({{ dataItem.productCode }})</text>
          </view>
          <word-icon class="mr-8" text="共" />
          <view class="font-28 text-right flex align-center">
            <text class="color-ff0000 word-break">
              {{ new BigNumber(dataItem.passNum).plus(dataItem.ngNum).toNumber() }}
            </text>
            <text class="ml-8">{{ dataItem.productUnit }}</text>
          </view>
        </view>
        <uni-load-more :status="dataNoMoreProduct" />
      </template>
    </scroll-view>
    <view class="health-data" v-if="!Role_Staff">
      <btn-data-health :value="circleSmallPer" url="/pages-data-governance/stock/index" keyWord="库存健康度" />
    </view>
    <ExportEmail
      ref="exportEmail"
      :exportLoading="exportLoading"
      :needImport="true"
      @confirmPopup="confirmExportPopup"
    />
    <h-status-footer v-if="isExperience" />
  </view>
</template>
<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import ListItem from '@/pages/stock-list/components/list-item.vue'
import ListSearch from '@/pages/stock-list/components/list-search.vue'
import ListProductSearch from '@/components/product-search/list-search.vue'
import { BigNumber } from 'bignumber.js'
import { _get } from '@/utils/common-request'
import { onShow } from '@dcloudio/uni-app'
import { $store, collectClick, collectPV, Role_Staff, setGio } from '@/utils/common'
import HTabs from '@/components/h-tabs.vue'
import HEmpty from '@/components/h-empty.vue'

import HooksPageSwiper from '@/hooks/pages-swiper'
import BtnDataHealth from '@/components/btn-data-health.vue'
import ExportEmail from '@/components/export-email.vue'
import SystemNoticeArea from '@/components/system-notice-area.vue'
import HooksSystemNotice from '@/hooks/system-notice'

const dataList = ref([])
const seqKey = ref('')
const currentItem = ref({}) //当前编辑的产品信息
const tabs = ref([
  {
    state: 0,
    name: '产品库存'
  },
  {
    state: 1,
    name: '工序库存'
  }
])
const activeIndex = ref(0)
const pageEmpty = ref(false)
const pageEmptyProduct = ref(false)
const dataNoMore = ref('')
const dataNoMoreProduct = ref('')

const defaultConfig = ref({
  fontSize: 28,
  color: '#999999',
  activeBold: 'bold',
  activeColor: '#333333',
  underLineHeight: 8,
  underLineColor: '#0066ff'
})

onMounted(() => {
  collectPV('产品库存', '车间库存-产品库存', '车间库存', 0.5)
  collectPV('工序库存', '车间库存-工序库存', '车间库存', 0.5)
})

function tabClick(event) {
  activeIndex.value = event
  if (event === 1) {
    getStockList()
    collectPV('工序库存', '车间库存-工序库存', '车间库存', 0.5)
  }
  if (event === 0) {
    getProductList()
    collectPV('产品库存', '车间库存-产品库存', '车间库存', 0.5)
  }
}
//搜索在制品部分
const productNameOrCode = ref('')
function searchProductList(str) {
  pagesProduct.pageNum = 1
  productNameOrCode.value = str
  getProductList()
}
type TProduct = {
  productCode: string
  productName: string
  productUnit: string
  passNum: number
  ngNum: number
}
//获取产品数据
const ProductList = ref<TProduct[]>([])
function getProductList() {
  dataNoMoreProduct.value = 'loading'
  let mockParams = {
    url: '/storage/product/list',
    data: {
      productNameOrCode: productNameOrCode.value,
      ...pagesProduct
    }
  }

  _get(mockParams)
    .then((res: IResponseType<TProduct>) => {
      if (pagesProduct.pageNum === 1) {
        ProductList.value = []
      }
      ProductList.value.push(...res.rows)
      dataTotalProduct.value = res?.total || 0
      if (pagesProduct.pageNum === 1 && ProductList.value.length) {
        setTimeout(() => {
          scrollTop.value = 'scroll-product-0'
        }, 200)
      }
    })
    .finally(() => {
      triggeredProduct.value = false
      pageEmptyProduct.value = !ProductList.value.length
      dataNoMoreProduct.value = dataEndProduct.value ? 'noMore' : 'more'
    })
}
//明细
function toDetail(dataItem) {
  uni.navigateTo({ url: `/pages-analysis/wip-query/detail?productSeq=${dataItem.productSeq}` })
}

const { systemNotice, getSystemTips } = HooksSystemNotice(30)
onShow(() => {
  setGio('jr_kcbd_edit')
  // 清理所有的弹窗
  $store.commit('popup/closeAllPopup')
  getCirclePer()
  getSystemTips()
})

// 页面左右滑动
const scrollTop = ref('')
const hTabs = ref(null)
const { touchStart, touchEnd } = HooksPageSwiper((direction) => {
  if (direction === 'left') {
    pages.pageNum = 1
    hTabs.value.tabClick(0)
  } else if (direction === 'right') {
    pagesProduct.pageNum = 1
    hTabs.value.tabClick(1)
  }
})

// 下拉刷新
const triggered = ref(false)
const triggeredProduct = ref(false)
const pages = reactive({ pageSize: 20, pageNum: 1 })
const pagesProduct = reactive({ pageSize: 20, pageNum: 1 })
const dataTotal = ref(0)
const dataTotalProduct = ref(0)
const dataEnd = computed(() => pages.pageNum * pages.pageSize >= dataTotal.value)
const dataEndProduct = computed(() => pagesProduct.pageNum * pagesProduct.pageSize >= dataTotalProduct.value)
function onRefresh() {
  if (activeIndex.value === 1) {
    triggered.value = true
    pages.pageNum = 1
    getStockList()
  }
  if (activeIndex.value === 0) {
    triggeredProduct.value = true
    pagesProduct.pageNum = 1
    getProductList()
  }
}
function onLoadMore() {
  if (activeIndex.value === 1) {
    if (dataEnd.value) {
      return
    }
    pages.pageNum++
    getStockList()
  }
  if (activeIndex.value === 0) {
    if (dataEndProduct.value) {
      return
    }
    pagesProduct.pageNum++
    getProductList()
  }
}

function getStockList() {
  dataNoMore.value = 'loading'
  _get({ url: '/storage/list', data: { seqKey: seqKey.value, ...pages } })
    .then((res: IResponseType<unknown>) => {
      if (pages.pageNum === 1) {
        dataList.value = []
      }
      dataList.value.push(...res.rows)
      dataTotal.value = res.total
      if (pages.pageNum === 1 && dataList.value.length) {
        setTimeout(() => {
          scrollTop.value = 'scroll-stock-0'
        }, 200)
      }
    })
    .finally(() => {
      triggered.value = false
      pageEmpty.value = !dataList.value.length
      dataNoMore.value = dataEnd.value ? 'noMore' : 'more'
    })
}
function searchList(item) {
  pages.pageNum = 1
  seqKey.value = item?.itemSeq || ''
  getStockList()
}
const editStock = ref(null)
function editNum(dataItem) {
  let data = { ...dataItem }
  data.ngFromNum = data.ngNum //不良品变化前数量(赋值原库存不良品数量)
  data.ngToNum = data.ngNum //不良品变化后数量
  data.passFromNum = data.passNum //	良品变化前数量(赋值原库存良品数量)
  data.passToNum = data.passNum //	良品变化后数量
  data.totalNumPre = new BigNumber(data.ngNum).plus(data.passNum).toNumber()
  data.totalNum = new BigNumber(data.ngNum).plus(data.passNum).toNumber()
  delete data.ngNum
  delete data.passNum
  currentItem.value = data

  editStock.value.openEdit()
  // popup.value.open('bottom')
  uni.hideTabBar()
}
// function closePopup() {
//   uni.showTabBar()
//   currentItem.value = {}
// }
// function confirmPopup() {
//   getStockList()

//   if (!isExperience.value) {
//     uni.showTabBar()
//   }
// }

// function changeData(data, value) {
//   currentItem.value[data] = value
// }
function toHistory(data) {
  uni.navigateTo({
    url: '/pages/stock-list/change-history?item=' + JSON.stringify(data)
  })
}
function explainPopup(data) {
  uni.navigateTo({
    url: '/pages/stock-list/change-history?item=' + JSON.stringify(data) + '&type=error'
  })
}
function addStockChange() {
  uni.navigateTo({
    url: '/pages/stock-list/add-change'
  })
}

/* 体验模块 */
// 检查是否是体验
const isExperience = computed(() => {
  return $store.getters.isExperience
})
// 是否正在体验中
const experienceLoading = ref(true)
const maskShow = ref(true)

onMounted(() => {
  uni.$on('stockListRefresh', () => {
    getStockList()
  })
  getProductList()
  getCirclePer()
  getSystemTips()
  getStockList()
})

onUnmounted(() => {
  uni.$off('stockListRefresh')
})

const circleSmallPer = ref(0)
function getCirclePer() {
  _get({ url: '/warn/storage/health' }).then((res: IResponseType<{ ngNum: number; total: number }>) => {
    if (!res.data.total || !res.data.ngNum) {
      circleSmallPer.value = 100
    } else {
      let data = new BigNumber(res.data.total).minus(res.data.ngNum)
      const num = Math.floor(new BigNumber(data).div(res.data.total).times(100).toNumber())
      circleSmallPer.value = Math.min(num || 0, 100)
    }
  })
}
//导出
const exportEmail = ref(null)
function exportToMail() {
  exportEmail.value.openExportToMail()
}
const exportLoading = ref(false)
function confirmExportPopup(receivedBy) {
  uni.showLoading({
    title: '发送中'
  })
  exportLoading.value = true
  _get({
    url: '/export/allProcessStorage?receivedBy=' + receivedBy
  })
    .then((res) => {
      exportEmail.value.closePopup()
      uni.showToast({
        title: '导出成功',
        duration: 2000,
        icon: 'none'
      })
    })
    .finally(() => {
      uni.hideLoading()
      exportLoading.value = false
    })
}
</script>
<style lang="scss" scoped>
.export-btn {
  width: px2vw(128);
  height: px2vw(44);
  line-height: px2vw(44);
  background: #ffffff;
  border: px2vw(2) solid #0066ff;
  &:active {
    background-color: #0066ff;
    color: #ffffff;
  }
}
.w-115 {
  width: px2vw(115);
}

.area-scroll {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  &__list-item {
    margin-bottom: px2vw(8);
    padding: 0 px2vw(16);
  }
}

.e-img {
  position: absolute;
  z-index: 101;
  left: 50%;
  top: px2vw(576);
  transform: translate(-50%);
  width: px2vw(498);
  height: px2vw(200);
}
</style>
