<template>
  <view class="finish-report-detail">
    <uni-nav-bar />
    <h-status-header title="完工单详情" />
    <view class="bg-fff px-32 box mt-16 mb-8 mx-16 rounded-16-top">
      <view class="flex flex-col pt-34 pb-24 box">
        <view class="flex justify-between align-center word-break box">
          <view class="font-28 relative green-title flex-1 bold">
            <text>{{ dataItem.reportNo }}</text>
          </view>
        </view>
        <view class="flex align-center font-28 color-5a6f82 mt-24">
          <view class="mark-ebf0f5">操作员</view>
          <text class="ml-16">{{ dataItem.completeUserNickName }}</text>
          <text class="ml-16">{{ formatDate(dataItem.completeTime) }}</text>
        </view>
        <view class="flex align-center font-28 mt-24">
          <view class="flex align-center">
            <view class="mark-ebf0f5">产品</view>
            <text class="ml-12 bold mx-8">{{ dataItem.productCategoryTotalNum }}</text>
            <text>款</text>
          </view>
          <view class="flex align-center ml-28">
            <view class="mark-ebf0f5">数量</view><text class="ml-12 bold">{{ dataItem.productTotalNum }}</text>
          </view>
          <view class="flex-1"></view>
        </view>
      </view>
    </view>
    <scroll-view scroll-y class="area-scroll">
      <view v-for="(item, idx) in detailList" :key="idx" class="relative flex flex-col px-32 box bg-fff mx-16">
        <view class="pt-32 pb-24 border-bottom-f5f5f5">
          <view class="flex align-center justify-between">
            <view class="font-28 relative flex-1 bold">
              <text>{{ item.productName }}</text>
              <text class="color-999">({{ item.productSeq }})</text>
            </view>
            <area-date-info :dataItem="{ pass: item.completeNum, productUnit: item.productUnit }" :showNg="false"
          /></view>
          <view class="flex align-center font-28 color-5a6f82 mt-24">
            <view class="mark-ebf0f5">记工</view>
            <text class="ml-16">{{ item.submitWorkNickName }}</text>
            <text class="ml-16">{{ formatDate(item.submitWorkTime) }}</text>
          </view>
        </view>
      </view>
      <view class="font-24 color-b6c0c9 text-center w-100 mt-48 bottomWord">已到底部</view>
    </scroll-view>
  </view>
</template>
<script setup lang="ts">
import { onLoad, onShow } from '@dcloudio/uni-app'
import { computed, onMounted, reactive, ref } from 'vue'
import { $state, $store, formatDate } from '@/utils/common'
import { _get } from '@/utils/common-request'
import AreaDateInfo from '@/pages/report-production/components/area-date-info.vue'
const dataItem = reactive({})
onLoad((e) => {
  Object.assign(dataItem, JSON.parse(e.orderInfo))
  getDetailList()
})
const detailList = ref([])
function getDetailList() {
  let mockParams = {
    url: `/complete/report/${dataItem.reportNo}`
  }

  _get(mockParams)
    .then((res) => {
      detailList.value = res.data
    })
    .finally(() => {})
}
</script>
<style scoped lang="scss">
.bottomWord {
  margin-bottom: calc(48rpx + constant(safe-area-inset-bottom));
  margin-bottom: calc(48rpx + env(safe-area-inset-bottom));
}

.finish-report-detail {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f3f3f5;
  overflow: hidden;
  .area-scroll {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }
}
.number-mark {
  background: #00bfa5;
  border-radius: 0px px2vw(4) px2vw(4) 0px;
  position: absolute;
  left: px2vw(-16);
  top: px2vw(34);
}
</style>
