<template>
  <view class="py-16 box h-full bg-f3f3f5 flex flex-col overflow-hidden">
    <uni-nav-bar />
    <h-status-header title="产品计件详情" />
    <view class="flex flex-col pt-24 pb-32 px-32 box bg-fff mx-16">
      <view class="flex align-center justify-between">
        <view class="flex align-center">
          <h-text-display class="bold color-333 font-28" :text="productData.productName" :width="200" />
          <view class="font-28 color-999 bold">({{ productData.productCode }})</view>
        </view>
        <picker
          mode="date"
          fields="month"
          :value="selectMonth"
          @change="bindDateChangeMonth"
          class="bg-f3f3f5 color-333 rounded-24 px-16 py-10 box font-24 bold"
        >
          {{ selectMonth }}
        </picker>
      </view>
      <view class="flex align-center mt-16 font-24 color-5a6f82">
        <text>工序</text>
        <text class="color-333 mx-8 bold">{{ productData.processNum || 0 }}</text>
        <text>道</text>
        <text class="mx-16">|</text>
        <text>员工</text>
        <text class="color-333 mx-8 bold">{{ productData.employeeNum || 0 }}</text>
        <text>人</text>
        <text class="mx-16">|</text>
        <text>完工</text>
        <text class="color-333 mx-8 bold">{{ productData.storageNum || 0 }}</text>
        <text>{{ productData.productUnit }}</text>
      </view>
    </view>
    <scroll-view scroll-y class="flex-1 overflow-hidden" enable-flex>
      <view v-for="(item, idx) in processData" :key="idx" class="px-32 pt-32 box bg-fff mt-8 mx-16 relative">
        <view class="flex align-center mb-8 box">
          <view class="mark-ebf0f5 mr-8">工序</view>
          <h-text-display :text="item.operateProcessName" :width="256" class="color-5a6f82 font-28" />
        </view>
        <view
          v-for="(itemDetail, detailIdx) in item.detailList"
          :key="detailIdx"
          class="h-80 flex justify-between align-center"
          :class="[detailIdx + 1 === item.detailList.length ? '' : 'border-bottom-f5f5f5']"
        >
          <view class="font-28 bold">{{ itemDetail.employeeName }}</view>
          <view class="flex"
            ><view class="color-333 font-28 bold ml-12"
              >{{ itemDetail.totalSettledNum }}{{ itemDetail.productUnit }}</view
            ></view
          >
        </view>
      </view>
      <view class="color-b6c0c9 font-24 my-48 w-100 text-center">已到底部</view>
    </scroll-view>
    <h-popup />
  </view>
</template>

<script setup lang="ts">
import { _get } from '@/utils/common-request'
import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'
import dayjs from 'dayjs'
onLoad((e) => {
  productData.value = JSON.parse(e.productData)
  selectMonth.value = e.searchDate
  getDetail()
})
const productData = ref({})
const processData = ref({})
const selectMonth = ref(dayjs().startOf('month').format('YYYY-MM'))
function getDetail() {
  _get({
    url: `/settlement/settled/product/detail`,
    data: {
      searchDate: dayjs(selectMonth.value).startOf('month').format('YYYY-MM-DD'),
      productSeq: productData.value.productSeq
    }
  }).then((res) => {
    processData.value = res.rows
  })
}
function bindDateChangeMonth(event) {
  selectMonth.value = event.detail.value
  getDetail()
}
</script>

<style lang="scss" scoped>
.number-mark {
  background: #00bfa5;
  border-radius: 0 px2vw(4) px2vw(4) 0;
  height: px2vw(32);
  line-height: px2vw(32);
  position: absolute;
  z-index: 2;
  left: px2vw(-16);
  top: px2vw(33);
}
</style>
