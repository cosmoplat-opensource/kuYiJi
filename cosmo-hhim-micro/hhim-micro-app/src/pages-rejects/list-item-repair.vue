<template>
  <view v-for="(dataItem, index) in dataList" :key="`p-${index}`" class="mb-8 bg-fff" @tap="jumpDetail(dataItem)">
    <view class="box pt-32 px-32 flex align-center" :class="{ 'border-bottom-dashed': dataItem.listShow }">
      <view class="flex-1">
        <text v-if="dataItem.submitNickName">{{ dataItem.submitNickName }}</text>
        <template v-else>
          <text class="color-333 font-28 bold">{{ dataItem.productName }}</text>
          <text class="color-999 font-28 bold">({{ dataItem.productCode }})</text>
        </template>
      </view>
      <word-icon class="mr-8 ml-32" text="共" />
      <view class="color-5a6f82 font-28">
        {{ dataItem.repairNum + dataItem.concessionNum + dataItem.abandonedNum }}
      </view>
      <view class="ml-8 font-28 color-5a6f82">{{ dataItem.productUnit || '件' }}</view>
    </view>
    <!--工序-->
    <view class="mt-24 px-32">
      <h-text-display :text="dataItem.processName" :width="512" class="font-28 color-5a6f82" />
    </view>
    <!--    返修-->
    <view class="flex align-center mt-24 px-32 box">
      <view class="mark-ebf0f5">返修</view>
      <view class="ml-16 color-333 font-28 flex-1">{{ dataItem.repairNickName }}</view>
      <word-icon class="ml-24" text="修" />
      <view class="color-333 font-28 bold ml-8">{{ dataItem.repairNum }}{{ dataItem.productUnit }}</view>
      <word-icon class="ml-24" text="让" />
      <view class="color-333 font-28 bold ml-8">{{ dataItem.concessionNum }}{{ dataItem.productUnit }}</view>
      <word-icon class="ml-24" text="废" />
      <view class="color-333 font-28 bold ml-8">{{ dataItem.abandonedNum }}{{ dataItem.productUnit }}</view>
    </view>
    <!--    复核-->
    <view class="flex align-center mt-24 px-32 pb-24 box">
      <view class="mark-ebf0f5">复核</view>
      <view class="ml-16 color-333 font-28">{{ dataItem.reviewNickName }}</view>
      <view class="ml-16 color-333 font-28">{{ formatDate(dataItem.reviewDate, 'YYYY-MM-DD HH:mm:ss') }}</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { $store, formatDate } from '@/utils/common'
import WordIcon from '@/components/word-icon.vue'
import HTextDisplay from '@/components/h-text-display.vue'

const dataList = computed(() => {
  return $store.state.settlement.dataList
})

function jumpDetail(item) {
  uni.navigateTo({
    url: `/pages-rejects/repair?repairNo=${item.repairNo}`
  })
}
</script>

<style lang="scss" scoped></style>
