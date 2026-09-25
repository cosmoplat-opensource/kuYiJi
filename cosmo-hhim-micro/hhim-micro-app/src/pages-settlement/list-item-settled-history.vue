<template>
  <view v-for="(item, index) in dataList" :key="index" class="flex flex-col box mb-16">
    <view class="flex align-center bg-f7f8e7 h-64">
      <view class="block" />
      <view class="color-333 font-28 bold ml-16">{{ item.employeeName }}</view>
    </view>
    <template v-if="item.showMore">
      <view
        v-for="(itemDetail, detailIdx) in item.detailList.filter((v, i) => i < 5)"
        :key="detailIdx"
        class="card box p-32 bg-fff mt-8"
        @tap="toDetail(item, itemDetail)"
      >
        <view class="flex align-center justify-between">
          <view class="flex align-center font-28">
            <h-text-display class="bold color-333" :text="itemDetail.productName" :width="200" />
            <view class="color-999 bold">({{ itemDetail.productCode }})</view>
          </view>
          <image src="/static/images/icon_detail.svg" class="icon-32" />
        </view>
        <view class="flex align-center mt-24 box">
          <h-text-display :text="itemDetail.operateProcessName" :width="256" class="color-5a6f82 font-28" />
          <view class="font-28 color-333 flex-1 flex justify-end">
            <view class="mark-ebf0f5">共调整</view>
            <view class="bold flex align-center">
              <text class="mx-12">{{ Number(itemDetail.totalAdjustedNum.replace(/^(.*\..{4}).*$/, '$1')) }}</text>
              <h-text-display :text="itemDetail.productUnit" :width="60" />
            </view>
          </view>
        </view>
      </view>
    </template>
    <template v-else>
      <view
        v-for="(itemDetail, detailIdx) in item.detailList"
        :key="detailIdx"
        class="card box p-32 bg-fff mt-8"
        @tap="toDetail(item, itemDetail)"
      >
        <view class="flex align-center justify-between">
          <view class="flex align-center font-28">
            <h-text-display class="bold color-333" :text="itemDetail.productName" :width="200" />
            <view class="color-999 bold">({{ itemDetail.productCode }})</view>
          </view>
          <image src="/static/images/icon_detail.svg" class="icon-32" />
        </view>
        <view class="flex align-center justify-between mt-24 box">
          <h-text-display :text="itemDetail.operateProcessName" :width="200" class="color-5a6f82 font-28" />
          <view class="font-28 color-333 flex justify-end">
            <view class="mark-ebf0f5">共调整</view>
            <view class="bold flex align-center">
              <text class="mx-8">{{ Number(itemDetail.totalAdjustedNum.replace(/^(.*\..{4}).*$/, '$1')) }}</text>
              <h-text-display :text="itemDetail.productUnit" :width="60" />
            </view>
          </view>
        </view>
      </view>
    </template>
    <view
      v-if="item.showMore"
      @tap="showMoreData(item)"
      class="w-100 h-80 color-0066ff font-28 bg-fff flex justify-center align-center mt-8 check"
      >查看更多</view
    >
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { $store } from '@/utils/common'

const dataList = computed(() => {
  let list = $store.state.settlement.dataList
  list.forEach((item) => {
    if (item.detailList.length > 5) {
      item.showMore = true
    } else {
      item.showMore = false
    }
  })
  return list
})
function showMoreData(item) {
  item.showMore = false
}
function toDetail(item, itemDeatil) {
  let employeeData = {
    employeeId: item.employeeId,
    employeeName: item.employeeName,
    ...itemDeatil
  }
  uni.navigateTo({
    url: `/pages-settlement/detail-history-item?employeeData=${JSON.stringify(employeeData)}`
  })
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
  left: px2vw(-32);
  top: px2vw(4);
}
.check {
  &:active {
    background-color: #dbeaff;
  }
}
.card {
  &:active {
    background-color: #f3f3f5;
  }
}
</style>
