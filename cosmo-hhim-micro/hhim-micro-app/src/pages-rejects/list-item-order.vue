<template>
  <view v-for="(dataItem, index) in dataList" :key="`p-${index}`" class="mb-8 bg-fff">
    <!--通用大列表项-->
    <view
      class="box py-32 px-32 flex align-center overflow-hidden"
      @tap="handleListShow(dataItem)"
      :class="{ 'border-bottom-dashed': dataItem.listShow }"
    >
      <text v-if="dataItem.submitNickName" class="bold font-28">{{ dataItem.submitNickName }}</text>
      <view v-else class="flex align-center">
        <h-text-display :text="dataItem.productName" :width="212" class="color-333 font-28 bold" />
        <text class="color-999 font-28 bold">({{ dataItem.productCode }})</text>
      </view>
      <view class="flex-1" />
      <word-icon class="mr-8 ml-32" text="共" />
      <view class="color-ff0000 font-28">{{ parseInt(dataItem.totalNgNum || '0') }}</view>
      <view class="ml-8 font-28 color-333">{{ dataItem.productUnit }}</view>
      <image
        src="/static/images/icon_unfold.svg"
        class="icon-32 ml-16"
        :class="{ 'icon-rotate-y': dataItem.listShow }"
      />
    </view>
    <!--产品维度小列表-->
    <template v-if="dataItem.listShow && !dataItem.submitNickName">
      <view
        v-for="(item, index) in dataItem.ngProductAndProcessCheckList"
        :key="index"
        class="flex align-center box py-24 mx-32 relative"
        @tap="jumpDetail(dataItem, item)"
        :class="{ 'b-t-1 border-f5f5f5': index !== 0 }"
      >
        <view class="flex-1 color-5a6f82 font-28 bold">{{ item.processName }}</view>
        <view class="mark-ebf0f5 border ml-32">不良</view>
        <view class="color-ff0000 ml-16 font-28">{{ item.ngNum }}</view>
        <image src="/static/images/icon_detail.svg" class="icon-32 ml-16" />
      </view>
    </template>
    <!--用户维度小列表-->
    <template v-if="dataItem.listShow && dataItem.submitNickName">
      <view
        v-for="(item, index) in dataItem.ngProductAndProcessByUserFromAlreadyCheckList"
        :key="index"
        class="flex flex-col box py-24 mx-32 relative"
        :class="{ 'b-t-1 border-f5f5f5': index !== 0 }"
      >
        <view class="flex align-center">
          <view class="font-28 color-333 bold">
            <h-text-display :text="item.productName" :width="212" />
          </view>
          <view class="font-28 color-999 bold flex-1">({{ item.productCode }})</view>
          <word-icon class="ml-32" text="共" />
          <view class="color-333 font-28 ml-8">{{ item.ngNum }}</view>
          <view class="color-5a6f82 font-28 ml-8">{{ item.productUnit }}</view>
        </view>
        <view class="flex align-center mt-24">
          <h-text-display :text="item.processName" :width="212" class="font-24 color-5a6f82" />
          <view class="flex-1" />
          <h-button width="144" height="48" font="font-24" text="返修复核" @tap.stop="handleRepair(item, dataItem)" />
        </view>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { $store } from '@/utils/common'
import WordIcon from '@/components/word-icon.vue'

import HTextDisplay from '@/components/h-text-display.vue'

const dataList = computed(() => {
  return $store.state.settlement.dataList
})

function handleListShow(item) {
  item.listShow = !item.listShow
}

function handleRepair(item, dataItem) {
  const params = {
    productSeq: item.productSeq,
    processSeq: item.processSeq,
    submitUser: dataItem.submitUser
  }
  $store.commit('qualityTesting/setRepairItem', params)
  uni.navigateTo({ url: '/pages-rejects/repair' })
}
function jumpDetail(product, process) {
  const prod = { code: product.productCode, name: product.productName, seq: product.productSeq }
  const proc = { code: process.processCode, name: process.processName, seq: process.processSeq }
  uni.navigateTo({ url: `/pages-rejects/list-detail?product=${JSON.stringify(prod)}&process=${JSON.stringify(proc)}` })
}
</script>

<style lang="scss" scoped></style>
