<template>
  <view class="finish-report-detail">
    <uni-nav-bar />
    <h-status-header title="完工明细" />
    <view class="bg-fff p-32 box mt-16 mb-8 mx-16 rounded-16-top">
      <view class="font-28 relative flex-1 bold mb-24">
        <text>{{ dataItem.productName }}</text>
        <text class="color-999">({{ dataItem.productSeq }})</text>
      </view>
      <time-select :start="startDate" :end="endDate" @search="searchCompleteDetail" bg="bg-f3f3f5" />
    </view>
    <scroll-view scroll-y class="area-scroll">
      <view v-for="(item, idx) in detailList" :key="idx" class="relative flex flex-col p-32 box bg-fff mx-16">
        <view class="flex align-center justify-between">
          <view class="flex align-center font-28 color-333">
            <view class="mark-ebf0f5">记工</view>
            <text class="ml-16">{{ item.submitUser }}</text>
            <text class="ml-16">{{ formatDate(item.submitTime) }}</text>
          </view>
          <area-date-info :dataItem="{ pass: item.completeNum, productUnit: item.productUnit }" :showNg="false"
        /></view>
        <view class="flex align-center font-24 color-5a6f82 mt-24">
          <view class="">操作员：</view>
          <text class="ml-8">{{ item.completeUser }}</text>
          <text class="ml-8">{{ formatDate(item.completeTime) }}</text>
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
import TimeSelect from '@/pages-work-center/completion-report/components/time-select.vue'
import AreaDateInfo from '@/pages/report-production/components/area-date-info.vue'
const dataItem = reactive({})
const startDate = ref('')
const endDate = ref('')
onLoad((e) => {
  Object.assign(dataItem, JSON.parse(e.orderInfo))
  startDate.value = e.startDate
  endDate.value = e.endDate
  getDetailList()
})
function searchCompleteDetail(start, end) {
  startDate.value = start
  endDate.value = end
  getDetailList()
}
const detailList = ref([])
function getDetailList() {
  let mockParams = {
    url: `/complete/report/productSide/detailList`,
    data: { productSeq: dataItem.productSeq, startDate: startDate.value, endDate: endDate.value }
  }

  _get(mockParams)
    .then((res) => {
      detailList.value = res.rows
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
