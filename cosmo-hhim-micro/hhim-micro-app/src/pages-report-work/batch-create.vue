<template>
  <view class="h-full bg-f3f3f5 flex flex-col">
    <uni-nav-bar />
    <h-status-header title="记工" />
    <view class="flex-1 flex flex-col overflow-hidden">
      <!--      顶部信息-->
      <view class="mx-16 box mt-16 bg-fff py-18 px-32 rounded-16 flex align-center mb-16">
        <view class="mark-f3f3f5 border">记工日期</view>
        <view class="ml-16 font-28 color-5a6f82 flex-1">{{ dateSelected }}</view>
        <view class="color-5a6f82 font-24">产品：</view>
        <view class="color-ff0000 font-24 bold">{{ productList.length }}</view>
      </view>
      <template v-if="!productList.length">
        <!--      空数据提示-->
        <view class="box pt-240 pb-132">
          <h-empty :tipsWord="tipWord" />
        </view>
        <!--      添加产品按钮-->
        <view class="mx-48 box rounded-16 bg-fff py-24 flex justify-center btn-hover" @tap="handleProductOpen">
          <image :src="formatImage('icon_add_0066ff', 'svg')" class="icon-48" />
        </view>
      </template>
      <scroll-view v-else scroll-y enable-flex class="flex-1 flex flex-col overflow-hidden">
        <view v-for="(item, index) in productList" :key="index" class="mx-16 box mb-16">
          <view class="flex align-center bg-f7f8e7 py-24 box pr-32">
            <view class="block" />
            <view class="color-333 font-28 bold ml-16">
              <h-text-display :text="item.itemName" :width="200" />
            </view>
            <view class="color-999 font-28 line-height-32 bold flex-1">({{ item.itemCode }})</view>
            <image @tap="handleProductDel(index)" src="/static/images/icon_clear.svg" class="icon-32 ml-32" />
          </view>
          <!--工序列表-->
          <view v-for="(process, processIndex) in item.processList" :key="processIndex" class="bg-fff p-32 box mt-8">
            <view class="flex align-center">
              <view class="color-5a6f82 font-28 bold">{{ process.itemName }}</view>
              <view class="flex-1" />
              <image
                @tap="handleProcessDel(index, processIndex)"
                :src="formatImage('icon_del_32_fce0de', 'svg')"
                class="icon-32"
              />
            </view>
            <view class="flex align-center font-28 mt-32">
              <view class="color-19aa8d">良品</view>
              <input
                class="flex-1 h-64 ml-16 border-2 border-f3f3f5 rounded-8 box px-24 text-right bold"
                placeholder-class="bold color-b8b8b8"
                v-model="process.passNum"
                placeholder="0"
                type="digit"
              />
              <view class="color-333 ml-32">不良品</view>
              <input
                class="flex-1 h-64 ml-16 border-2 border-f3f3f5 rounded-8 box px-24 text-right bold"
                placeholder-class="bold color-b8b8b8"
                v-model="process.ngNum"
                placeholder="0"
                type="digit"
              />
            </view>
          </view>
          <view
            class="mt-8 bg-fff flex-center py-26 box color-b6c0c9 font-24"
            v-if="!item.processList || !item.processList.length"
          >
            暂无推荐工序
          </view>
          <view class="mt-8 bg-fff flex-center py-26 box color-0066ff font-28 btn-hover" @tap="handleProcessAdd(index)">
            添加报工工序
          </view>
        </view>
      </scroll-view>
    </view>
    <!--    底部操作区-->
    <view class="flex align-end justify-center py-32">
      <h-button width="296" height="72" text="取消" type="bg-fff color-333" @tap.stop="handleCancel" />
      <h-button width="296" height="72" text="下一步" class="ml-32" @tap.stop="handleBtnNext" />
    </view>
    <h-status-footer />
    <h-popup-product ref="refProduct" @productSelected="handleProductSelected" />
    <h-popup-process ref="refProcess" @processSelected="handleProcessSelected" title="报工工序" />
    <h-popup />
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import HEmpty from '@/components/h-empty.vue'
import { formatImage } from '@/utils/common'
import HPopupProduct from '@/pages-report-work/components/h-popup-product.vue'
import HTextDisplay from '@/components/h-text-display.vue'
import { _get, _post } from '@/utils/common-request'
import HPopupProcess from '@/pages-report-work/components/h-popup-process.vue'
import HooksPopup from '@/hooks/popup'
import HPopup from '@/components/h-popup.vue'

// -------------------页面初始化---------------------
const dateSelected = ref('')
const tipWord = '请点击"+"号添加记工产品'
onLoad((options) => {
  dateSelected.value = options.date
})

onMounted(() => {
  if (uni.getStorageSync('batch-create-record')) {
    productList.value = JSON.parse(uni.getStorageSync('batch-create-record'))
  } else {
    handleProductOpen()
  }
})

// --------------------产品操作----------------------
const productList = ref([])
const refProduct = ref(null)
// 打开添加产品的弹窗
function handleProductOpen() {
  refProduct.value?.open()
}
// 批量选择产品弹窗回调
function handleProductSelected(products) {
  const params = []
  productList.value = products.map((v) => {
    v.processList = []
    params.push({ productSeq: v.itemSeq, standard: v.standard })
    return v
  })
  _post({ url: '/submit/multiRecommend', data: params }).then((res) => {
    productList.value.forEach((v, i) => {
      v.processList = res.data[v.itemSeq] || []
    })
  })
}
// 删除产品
function handleProductDel(index) {
  const product = productList.value[index]
  uni.showModal({
    title: '提示',
    content: `是否确认删除${product.itemName}?`,
    success: (res) => {
      if (res.confirm) {
        productList.value.splice(index, 1)
      }
    }
  })
}

// ---------------------工序操作----------------------
const refProcess = ref(null)
const productIndex = ref(0)
function handleProcessAdd(index) {
  productIndex.value = index
  const product = productList.value[index]
  refProcess.value?.open(product)
}
// 批量选择工序弹窗回调
function handleProcessSelected(processList) {
  const arr_old = productList.value[productIndex.value].processList
  processList.forEach((v) => {
    const item = arr_old.find((item) => item.itemSeq === v.itemSeq)
    if (item) {
      v.passNum = item.passNum
      v.ngNum = item.ngNum
    }
  })
  productList.value[productIndex.value].processList = processList
}
// 删除工序
function handleProcessDel(pIndex, cIndex) {
  productList.value[pIndex].processList.splice(cIndex, 1)
}

// --------------------页面底部按钮操作---------------
function handleCancel() {
  uni.navigateBack()
}
const { popupOpen } = HooksPopup()
function handleBtnNext() {
  let errorTag = ''
  let count = 0
  if (productList.value.length === 1) {
    if (productList.value[0].processList.every((v) => !v.passNum && !v.ngNum)) {
      popupOpen('请至少录入一个工序的良品数量/不良品数量')
      return
    }
  }
  productList.value.forEach((product) => {
    product.processList = product.processList.filter((v) => v.passNum || v.ngNum)
    product.processList.forEach((process) => {
      count++
      // '良品数量/不良品数量请至少录入一个'
      if (process.passNum == 0 && process.ngNum == 0) {
        errorTag = `${product.itemName}中${process.itemName}的良品数量/不良品数量不能同时为0`
        return
      } else if (process.passNum && !/^\d+(\.\d{1,4})?$/.test(process.passNum)) {
        errorTag = `${product.itemName}中${process.itemName}的良品数量应为非负数且最多保留4位小数`
        return
      } else if (process.ngNum && !/^\d+(\.\d{1,4})?$/.test(process.ngNum)) {
        errorTag = `${product.itemName}中${process.itemName}的不良品数量应为非负数且最多保留4位小数`
        return
      }
    })
  })
  if (errorTag) {
    popupOpen(errorTag)
    return
  }
  if (!count) {
    popupOpen('请添加产品和工序')
    return
  }
  uni.navigateTo({
    url: `/pages-report-work/batch-create-step2?productList=${JSON.stringify(productList.value)}&date=${
      dateSelected.value
    }`
  })
}
</script>

<style lang="scss" scoped>
.btn-hover:active {
  background-color: #ebf0f5;
}
</style>
