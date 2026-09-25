<template>
  <view class="h-full bg-f3f3f5 flex flex-col">
    <uni-nav-bar />
    <h-status-header title="记工" />
    <view class="flex-1 flex flex-col overflow-hidden">
      <!--      顶部信息-->
      <view class="mx-16 box mt-16 bg-fff py-18 px-32 mb-16 rounded-16 flex align-center">
        <view class="mark-f3f3f5 border">记工日期</view>
        <view class="ml-16 font-28 color-5a6f82 flex-1">{{ dateSelected }}</view>
        <view class="color-5a6f82 font-24">产品：</view>
        <view class="color-ff0000 font-24 bold">{{ productList.length }}</view>
      </view>
      <scroll-view scroll-y enable-flex class="flex-1 flex flex-col overflow-hidden">
        <view v-for="(product, index) in productList" :key="index" class="mx-16 box mb-16">
          <view class="flex align-center bg-f7f8e7 py-24 box pr-32">
            <view class="block" />
            <view class="color-333 font-28 bold ml-16">
              <h-text-display :text="product.itemName" :width="240" />
            </view>
            <view class="color-999 font-28 bold flex-1">({{ product.itemCode }})</view>
          </view>
          <!--工序列表-->
          <view
            v-for="(process, processIndex) in product.processList"
            :key="processIndex"
            class="bg-fff px-32 pt-32 pb-16 box mt-8"
          >
            <view class="flex align-center">
              <h-text-display :text="process.itemName" :width="240" class="font-28 color-5a6f82 mr-16" />
              <word-icon class="mr-8" text="首" v-if="process.isFirstProcess === '0'" />
              <word-icon class="mr-8" text="尾" v-if="process.isLastProcess === '0'" />
              <view class="flex-1" />
              <area-date-info :dataItem="{ pass: process.passNum, ng: process.ngNum }" :unit="product.itemUnit" />
            </view>
            <!--不是标准工艺的首尾序可变更-->
            <view class="flex align-center mt-32 pb-8" v-if="!product.standard">
              <view class="flex align-center" @tap="handleIsLastProcess(product, process)">
                <h-checkbox-icon :checked="process.isLastProcess === '0'" />
                <view class="ml-16" :class="process.isLastProcess === '0' ? 'color-333' : 'color-5a6f82'">
                  <text class="font-24">我是最后一道工序</text>
                </view>
              </view>
              <view class="flex align-center ml-48" @tap="handleIsFirstProcess(product, process)">
                <h-checkbox-icon :checked="process.isFirstProcess === '0'" />
                <view class="ml-16" :class="process.isFirstProcess === '0' ? 'color-333' : 'color-5a6f82'">
                  <text class="font-24">我是首序</text>
                </view>
              </view>
            </view>
            <!--前工序-->
            <template v-if="process.preSeqs">
              <view class="mt-16 pt-24 pb-16 box b-t-1 border-f5f5f5 flex align-center">
                <view class="mark-ebf0f5 border">前工序</view>
                <view class="color-333 bold font-28 ml-16">
                  <h-text-display :text="process.preNames" :width="212" />
                </view>
                <view class="flex-1 flex flex-wrap justify-end font-24" v-if="!process.preSeqs.includes(',')">
                  <view class="flex align-center">
                    <view class="color-5a6f82">未审核</view>
                    <view class="color-333 bold mx-8">{{ process.unapprovedNum }}</view>
                    <view class="color-333">{{ product.itemUnit }}</view>
                  </view>
                  <view class="ml-24 flex align-center">
                    <view class="color-5a6f82">库存</view>
                    <view class="color-333 bold mx-8">{{ process.totalStockNum }}</view>
                    <view class="color-333">{{ product.itemUnit }}</view>
                  </view>
                </view>
              </view>
            </template>
            <template v-else>
              <view
                class="mt-16 bg-f3f3f5 flex align-center py-16 pr-16 box mb-16"
                v-if="process.isFirstProcess !== '0'"
              >
                <view class="font-28 color-5a6f82 w-176 pl-16 box">前工序</view>
                <view class="flex-1 overflow-hidden" v-if="process.preNames">
                  <h-text-display :text="process.preNames" />
                </view>
                <input
                  v-else
                  class="flex-1"
                  placeholder="选择前工序"
                  placeholder-class="font-28 color-b8b8b8"
                  disabled
                  :value="process.preProcessName"
                  @tap="handleChangePreProcess(index, processIndex, product, process)"
                />
                <image v-if="!process.preNames" :src="formatImage('icon_input_arr', 'svg')" class="icon-48" />
              </view>
              <view class="flex font-24" v-if="process.preProcessName">
                <view class="w-176" />
                <view class="color-5a6f82">未审核</view>
                <view class="color-333 bold mx-8">{{ process.unapprovedNum }}</view>
                <view class="color-333">{{ product.itemUnit }}</view>
                <view class="ml-24 color-5a6f82">库存</view>
                <view class="color-333 bold mx-8">{{ process.totalStockNum }}</view>
                <view class="color-333">{{ product.itemUnit }}</view>
              </view>
            </template>
          </view>
        </view>
        <view class="font-24 color-b6c0c9 pt-32 pb-48 box text-center">已到底部</view>
      </scroll-view>
    </view>
    <!--    底部操作区-->
    <view class="flex align-end justify-center gap-32 py-32 relative px-48">
      <h-button
        width="100%"
        height="72"
        text="上一步"
        type="bg-fff color-333"
        class="flex-1"
        @tap.stop="handleCancel"
      />
      <h-button
        v-if="checkSubmitInspect && btnCheckShow"
        width="100%"
        height="72"
        text="送检"
        :type="btnRecordShow ? 'border-1 border-0066ff color-0066ff bg-fff' : 'bg-0066ff color-fff'"
        class="flex-1"
        @tap.stop="handleBtnNext(true)"
      />
      <h-button
        v-if="btnRecordShow"
        width="100%"
        class="flex-1"
        height="72"
        text="确认并记工"
        @tap.stop="handleBtnNext(false)"
      />
      <view v-if="!btnRecordShow || (!btnCheckShow && checkSubmitInspect)" class="btn-float" @tap="handleBtnDisplayAll">
        <text class="font-24 color-0066ff">{{ !btnRecordShow ? '记工' : '送检' }}</text>
      </view>
    </view>
    <h-status-footer />
    <h-popup-pre-process ref="refProcess" @processSelected="handleProcessSelected" />
  </view>
  <h-popup />
</template>

<script setup lang="ts">
import { h, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import HTextDisplay from '@/components/h-text-display.vue'
import { _get, _post } from '@/utils/common-request'
import AreaDateInfo from '@/pages/report-production/components/area-date-info.vue'
import HCheckboxIcon from '@/components/h-checkbox-icon.vue'
import WordIcon from '@/components/word-icon.vue'
import { $store, collectClick, formatImage, Role_Experience } from '@/utils/common'
import HPopupPreProcess from '@/pages-report-work/components/h-popup-pre-process.vue'
import HooksPopup from '@/hooks/popup'
import HPopup from '@/components/h-popup.vue'

import HooksSettingConfig from '@/hooks/setting-config'
import HProcessStock from '@/components/h-process-stock.vue'

// -------------------页面初始化---------------------
const dateSelected = ref('')
const productList = ref([])
const { checkSubmitInspect } = HooksSettingConfig()
onLoad((options) => {
  dateSelected.value = options.date
  uni.setStorageSync('batch-create-record', options.productList)
  const arr = JSON.parse(options.productList)
  productList.value = arr.filter((v) => v.processList.length)
  formatProcessList()
  if ($store.state.user.userInfo.personalRecommend === 1) {
    initRecommendButton()
  }
})

// 获取按钮显示权限
const btnCheckShow = ref(true)
const btnRecordShow = ref(true)
function initRecommendButton() {
  const arr = []
  productList.value.forEach((product) => {
    product.processList.forEach((process) => {
      arr.push({ productSeq: product.itemSeq, operateProcessSeq: process.itemSeq })
    })
  })
  _post({ url: '/user/recommendButton', data: arr }).then((res: IResponseType<[]>) => {
    if (res.data?.length) {
      btnCheckShow.value = res.data.find((v) => v === '送检')
      btnRecordShow.value = res.data.find((v) => v === '记工')
    } else {
      handleBtnDisplayAll()
    }
  })
}
function handleBtnDisplayAll() {
  btnCheckShow.value = true
  btnRecordShow.value = true
}

// --------------------产品操作----------------------
// 循环遍历确认工序的首尾序
function formatProcessList() {
  const arr = []
  productList.value.forEach((product) => {
    product.processList.forEach((process) => {
      arr.push({ productSeq: product.itemSeq, operateProcessSeq: process.itemSeq, standard: product.standard || false })
    })
  })
  const maps = {}
  _post({ url: '/process/selectPre', data: arr }).then((res: IResponseType<any[]>) => {
    res.data.forEach((v) => {
      maps[`${v.productSeq}-${v.operateProcessSeq}`] = v
    })
    productList.value.forEach((product) => {
      product.processList.forEach((process) => {
        const {
          isFirstProcess,
          isLastProcess,
          preProcessName,
          preProcessCode,
          preProcessSeq,
          totalStockNum,
          unapprovedNum
        } = maps[`${product.itemSeq}-${process.itemSeq}`]
        process.isFirstProcess = isFirstProcess ? '0' : '1'
        process.isLastProcess = isLastProcess ? '0' : '1'
        process.preProcessName = preProcessName
        process.preProcessCode = preProcessCode
        process.preProcessSeq = preProcessSeq
        process.totalStockNum = totalStockNum
        process.unapprovedNum = unapprovedNum
        if (product.standard && preProcessSeq) {
          process.preNames = preProcessName
          process.preCodes = preProcessCode
          process.preSeqs = preProcessSeq
        }
      })
    })
  })
}

// ---------------------工序操作----------------------
const refProcess = ref(null)
const productIndex = ref(0)
const processIndex = ref(0)
// 批量选择工序弹窗回调
function handleProcessSelected(preProcess) {
  const product = productList.value[productIndex.value]
  productList.value[productIndex.value].processList[processIndex.value].preProcessName = preProcess.itemName
  productList.value[productIndex.value].processList[processIndex.value].preProcessCode = preProcess.itemCode
  productList.value[productIndex.value].processList[processIndex.value].preProcessSeq = preProcess.itemSeq
  _post({ url: '/process/selectStock', data: { productSeq: product.itemSeq, processList: [preProcess.itemSeq] } }).then(
    (res: IResponseType<selectStock[]>) => {
      const { totalStockNum, unapprovedNum } = res.data[0]
      productList.value[productIndex.value].processList[processIndex.value].totalStockNum = totalStockNum
      productList.value[productIndex.value].processList[processIndex.value].unapprovedNum = unapprovedNum
    }
  )
}
// 尾序变更
function handleIsLastProcess(product, process) {
  if (product.standard) {
    uni.showModal({
      title: '提示',
      content: '已规定标准工艺路线,不可更改勾选信息',
      showCancel: false
    })
    return
  }
  process.isLastProcess = process.isLastProcess === '1' ? '0' : '1'
}
// 首序变更
function handleIsFirstProcess(product, process) {
  if (product.standard) {
    uni.showModal({
      title: '提示',
      content: '已规定标准工艺路线,不可更改勾选信息',
      showCancel: false
    })
    return
  }
  process.isFirstProcess = process.isFirstProcess === '1' ? '0' : '1'
}
// 选择前工序
function handleChangePreProcess(pIndex, cIndex, product, process) {
  productIndex.value = pIndex
  processIndex.value = cIndex
  const pre = {
    itemName: process.preProcessName,
    itemCode: process.preProcessCode,
    itemSeq: process.preProcessSeq
  }
  refProcess.value?.open(pre, product, process)
}

// --------------------页面底部按钮操作---------------
function handleCancel() {
  uni.navigateBack()
}
const { popupOpen } = HooksPopup()

function handleBtnNext(check) {
  const arr = []
  let errorTag = ''
  productList.value.forEach((product) => {
    product.processList.forEach((process) => {
      // '良品数量/不良品数量请至少录入一个'
      if (process.isFirstProcess !== '0' && !process.preProcessName && !product.standard) {
        errorTag = `${product.itemName}中${process.itemName}的前工序不能为空`
        return
      } else if (process.isFirstProcess !== '0' && process.preProcessName === process.itemName) {
        errorTag = `${product.itemName}中${process.itemName}的前工序和报工工序不能为同一道工序`
        return
      }
      if (process.isFirstProcess === '0') {
        process.preProcessName = ''
        process.preProcessCode = ''
        process.preProcessSeq = ''
      }
      arr.push({
        productName: product.itemName,
        productCode: product.itemCode,
        productSeq: product.itemSeq,
        preProcessName: process.preNames || process.preProcessName,
        preProcessCode: process.preCodes || process.preProcessCode,
        preProcessSeq: process.preSeqs || process.preProcessSeq,
        operateProcessName: process.itemName,
        operateProcessCode: process.itemCode,
        operateProcessSeq: process.itemSeq,
        isLastProcess: process.isLastProcess || '1',
        isFirstProcess: process.isFirstProcess || '1',
        submitDay: dateSelected.value,
        passNum: process.passNum || 0,
        ngNum: process.ngNum || 0
      })
      collectClick(check ? '送检' : '记工', '批量记工页', '记工列表', `${product.itemSeq}&${process.itemSeq}`)
    })
  })
  if (errorTag) {
    popupOpen(errorTag)
    return
  }
  const params = {
    submitWay: check ? 2 : 1,
    multiMixedList: arr
  }
  _post({ url: '/submit/multiMixed', data: params }).then((res) => {
    $store.commit('tabBar/setTabActiveByName', '记工')
    uni.removeStorageSync('batch-create-record')
    uni.navigateTo({ url: '/pages/main' })
  })
}
</script>
<style lang="scss" scoped>
.btn-float {
  position: absolute;
  right: px2vw(-16);
  bottom: px2vw(152);
  width: px2vw(96);
  height: px2vw(64);
  background: #dbeaff;
  border-radius: px2vw(32) 0 0 px2vw(32);
  text-align: center;
  line-height: px2vw(64);
}
</style>
