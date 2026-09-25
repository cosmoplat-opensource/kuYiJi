<template>
  <view class="h-full flex flex-col bg-f3f3f5 overflow-hidden">
    <view class="flex flex-col relative">
      <uni-nav-bar />
      <h-status-header title="产成品库存" />
      <view class="my-16 flex align-center justify-between pr-32 box">
        <list-product-search
          @search="searchProductList"
          :needSelect="false"
          class="flex-1"
          wrapper-padding-right="pr-0"
        />
        <h-page-video :videoArr="[22, 23]" page-name="产成品库存" class="mx-24" />
        <view class="flex align-center">
          <btn-export @exportToMail="exportToMail" />
        </view>
      </view>
    </view>
    <!--数据列表滚动区域-->
    <scroll-view
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
      :scroll-into-view="scrollTop"
      :refresher-triggered="triggeredProduct"
      enable-flex
      refresher-enabled
      scroll-y
      class="px-16 box flex-1 overflow-hidden"
    >
      <!-- 产品列表 -->
      <h-empty v-if="pageEmptyProduct" className="pt-320" tipsWord="暂无产品" />
      <template v-else>
        <view
          v-for="(dataItem, index) in ProductList"
          :key="index"
          :id="`scroll-product-${index}`"
          @tap="toHistory(dataItem)"
          class="bg-fff flex justify-between align-center py-32 px-32 box mb-8"
        >
          <view class="font-28 flex-1 bold flex align-center">
            <h-text-display :text="dataItem.productName" :width="200" />
            <text class="color-999">({{ dataItem.productCode }})</text>
          </view>
          <word-icon class="mr-8" text="共" />
          <view class="font-28 text-right flex align-center">
            <text class="color-ff0000 word-break">{{ dataItem.num }}</text>
            <text v-if="dataItem.productUnit" class="ml-8">{{ dataItem.productUnit }}</text>
          </view>
        </view>
        <uni-load-more :status="dataNoMoreProduct" />
      </template>
    </scroll-view>
    <view class="flex align-top px-48 box pb-32 relative">
      <view class="tab-bar-shadow" />
      <h-button width="624" height="72" text="出库" @tap.stop="showOutPopup" />
    </view>
    <ExportEmail ref="exportEmail" :exportLoading="exportLoading" @confirmPopup="confirmExportPopup" />
    <OutProduct ref="outProduct" />
    <h-popup tabBar />
  </view>
</template>
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import ListProductSearch from '@/components/product-search/list-search.vue'
import { _get } from '@/utils/common-request'
import { onShow } from '@dcloudio/uni-app'
import HEmpty from '@/components/h-empty.vue'
import ExportEmail from '@/components/export-email.vue'
import BtnExport from '@/components/btn-export.vue'
import OutProduct from '@/pages-work-center/finish-product-storage/components/out-product.vue'
const pageEmptyProduct = ref(false)
const dataNoMoreProduct = ref('')

//搜索在制品部分
const productNameOrCode = ref('')
function searchProductList(str) {
  pagesProduct.pageNum = 1
  productNameOrCode.value = str
  getProductList()
}
//获取产品数据
const ProductList = ref([])
function getProductList() {
  dataNoMoreProduct.value = 'loading'
  let mockParams = {
    url: '/storage/finish/list',
    data: {
      productCodeOrName: productNameOrCode.value,
      ...pagesProduct
    }
  }

  _get(mockParams)
    .then((res: IResponseType<unknown>) => {
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

onShow(() => {
  getProductList()
})

// 页面左右滑动
const scrollTop = ref('')

// 下拉刷新
const triggeredProduct = ref(false)
const pagesProduct = reactive({ pageSize: 15, pageNum: 1 })
const dataTotalProduct = ref(0)
const dataEndProduct = computed(() => pagesProduct.pageNum * pagesProduct.pageSize >= dataTotalProduct.value)
function onRefresh() {
  triggeredProduct.value = true
  pagesProduct.pageNum = 1
  getProductList()
}
function onLoadMore() {
  if (dataEndProduct.value) {
    return
  }
  pagesProduct.pageNum++
  getProductList()
}

function toHistory(data) {
  uni.navigateTo({
    url: '/pages-work-center/finish-product-storage/change-history?item=' + JSON.stringify(data)
  })
}

onMounted(() => {})

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
    url: '/storage/finish/export?receivedBy=' + receivedBy + '&productCodeOrName=' + productNameOrCode.value
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
//出库
const outProduct = ref(null)
function showOutPopup() {
  outProduct.value.open(productNameOrCode.value)
}
</script>
<style lang="scss" scoped>
.export-btn {
  width: px2vw(80);
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
.tab-bar-shadow {
  position: absolute;
  left: 0;
  width: 100%;
  bottom: px2vw(104);
  height: px2vw(64);
  background: linear-gradient(180deg, #f3f3f500, #f3f3f5 100%);
  border-radius: 0;
}
</style>
