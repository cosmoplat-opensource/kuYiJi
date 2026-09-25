<!--基础的单页面-->
<template>
  <view class="h-full bg-f3f3f5 flex flex-col">
    <uni-nav-bar />
    <h-status-header title="不良明细" />
    <view class="flex-1 mx-16 box flex flex-col overflow-hidden">
      <view class="mt-16 rounded-16-top pt-32 px-32 pb-24 box bg-fff">
        <view class="flex align-center">
          <view class="font-28 color-333 bold">{{ productItem.name }}</view>
          <view class="font-28 color-999 bold flex-1">({{ productItem.code }})</view>
        </view>
        <view class="mt-24">
          <h-text-display :text="processItem.name" :width="512" class="color-5a6f82 font-28" />
        </view>
      </view>
      <scroll-view scroll-y enable-flex class="flex-1 mt-8">
        <view
          v-for="(u, index) in userList"
          :key="index"
          :class="{ 'border-bottom-dashed': index !== userList.length - 1 }"
          class="bg-fff flex align-center box py-24 px-32"
        >
          <view class="flex-1 font-28 color-333 bold">{{ u.submitNickName }}</view>
          <view class="color-5a6f82 font-24">不良</view>
          <view class="color-ff0000 font-28 ml-8">{{ u.ngNum }}</view>
          <h-button width="144" height="48" text="返修复核" font="font-24" class="ml-32" @tap.stop="handleRepair(u)" />
        </view>
        <view class="font-24 color-b6c0c9 py-32 box text-center">已显示全部</view>
      </scroll-view>
    </view>
    <h-status-footer />
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { _get } from '@/utils/common-request'
import { onLoad, onShow } from '@dcloudio/uni-app'

import HTextDisplay from '@/components/h-text-display.vue'
import { $store } from '@/utils/common'
import PopupStaff from '@/pages/audit/popup-staff.vue'

type detailType = {
  productSeq: 'P000002'
  productCode: 'P000002'
  productName: '缸体2'
  processSeq: 'GX000001'
  processName: '下料'
  submitUser: 181
  submitNickName: null
  ngNum: 4.0
}

const item = ref({})
const productItem = reactive({})
const processItem = reactive({})
const userList = ref<detailType[]>([])

function initData(productSeq, processSeq) {
  _get({ url: '/ngProduct/manage/list/product/user', data: { productSeq, processSeq } }).then(
    (res: IResponseType<detailType[]>) => {
      userList.value = res.data
    }
  )
}
onLoad((options) => {
  const { product, process } = options
  Object.assign(productItem, JSON.parse(product))
  Object.assign(processItem, JSON.parse(process))
  initData(productItem.seq, processItem.seq)
})

onShow(() => {
  initData(productItem.seq, processItem.seq)
})

function handleRepair(item) {
  const params = {
    productSeq: productItem.seq,
    processSeq: processItem.seq,
    submitUser: item.submitUser
  }
  $store.commit('qualityTesting/setRepairItem', params)
  uni.navigateTo({ url: '/pages-rejects/repair' })
}
</script>

<style lang="scss" scoped></style>
