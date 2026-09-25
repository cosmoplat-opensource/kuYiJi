<template>
  <view class="py-16 px-16 box h-full bg-f3f3f5 flex flex-col overflow-hidden">
    <uni-nav-bar />
    <h-status-header title="调整详情" />
    <view class="flex flex-col pt-32 pb-24 px-32 box bg-fff rounded-16-top">
      <view class="flex align-center color-5a6f82 font-28">
        <view class="mark-ebf0f5">记工</view>
        <view class="ml-16">{{ employeeData.employeeName }}</view>
      </view>
      <view class="flex align-center justify-between mt-24">
        <view class="flex align-center font-28">
          <h-text-display class="bold color-333" :text="employeeData.productName" :width="200" />
          <view class="color-999 bold">({{ employeeData.productCode }})</view>
        </view>
      </view>
      <view class="flex align-center mt-24">
        <h-text-display :text="employeeData.operateProcessName" :width="256" class="color-5a6f82 font-28" />
        <view class="font-28 color-333 flex-1 flex justify-end">
          <view class="mark-ebf0f5">共调整</view>
          <text class="mx-12">{{ Number(employeeData.totalAdjustedNum?.replace(/^(.*\..{4}).*$/, '$1')) }}</text>
          <h-text-display :text="employeeData.productUnit" :width="60" />
        </view>
      </view>
    </view>
    <scroll-view scroll-y class="flex-1 overflow-hidden mt-8" enable-flex>
      <view class="flex relative pl-16 pr-32 pt-32 box bg-ffffff">
        <view class="step-line"></view>
        <view class="flex flex-col">
          <view v-for="(itemChild, itemIdx) in historyData" :key="itemIdx" class="mb-40">
            <view class="color-999 font-28 relative pl-40 flex space-between">
              <view>{{ itemChild.createdDate }}</view>
              <view class="ml-16 overflow-hidden text flex-1 flex"
                ><view class="shrink-1">调整人：</view><h-text-display :text="itemChild.createdByName" :width="180"
              /></view>
              <view class="step-over icon-24 rounded-50"></view>
            </view>

            <view class="flex align-center mt-24 pl-40 box">
              <view class="flex align-center">
                <!-- <view class="font-28 color-333 flex align-center">
                  <view class="font-24 bg-EBF0F5 color-5a6f82 rounded-4 text-center px-4 mr-12">调整前</view>
                  {{ itemIdx + 1 === historyData.length ? itemChild.adjustedNum : historyData[itemIdx + 1].adjustedNum
                  }}{{ formatStr(itemChild.productUnit, 5) }}
                </view>
                <template v-if="!(itemIdx + 1 === historyData.length)"
                  ><view class="ml-24 font-24 bg-FE9F00 rounded-4 color-fff pl-4 pr-4 mr-12">减少</view>
                  <view class="font-32"
                    >{{ new BigNumber(historyData[itemIdx + 1].adjustedNum).minus(itemChild.adjustedNum).toNumber()
                    }}{{ formatStr(itemChild.productUnit, 5) }}</view
                  ></template
                > -->
                <view class="mark-fe9f00 font-28">减少</view>
                <view class="mx-12 font-28">{{ -itemChild.adjustedNum }}</view>
                <view class="font-28">{{ formatStr(itemChild.productUnit, 5) }}</view>
              </view>
            </view>

            <view v-if="itemChild.remark" class="color-5a6f82 font-28 pl-40 flex flex-col box">
              <view class="line bg-f5f5f5 mt-24 mb-24"></view>
              <view class="flex overflow-hidden">
                <view>调整说明：</view>
                <view class="flex-1 overflow-hidden">
                  <text>{{ itemChild.remark }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
      <view class="color-b6c0c9 font-24 my-48 text-center">已到底部</view>
    </scroll-view>
    <h-popup />
  </view>
</template>
<script setup lang="ts">
import { _get } from '@/utils/common-request'
import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'
import dayjs from 'dayjs'
import { BigNumber } from 'bignumber.js'
import { formatStr } from '@/utils/common'
onLoad((e) => {
  employeeData.value = JSON.parse(e.employeeData)
  getDetail()
})
const employeeData = ref({})
const historyData = ref([])
function getDetail() {
  _get({
    url: `/settlement/history/detail`,
    data: {
      userId: employeeData.value.employeeId,
      productSeq: employeeData.value.productSeq,
      operateProcessSeq: employeeData.value.operateProcessSeq
    }
  }).then((res) => {
    historyData.value = res.rows
  })
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
  left: px2vw(0);
  top: px2vw(40);
}
.step-over {
  background: #f5f5f5;
  border: px2vw(2) solid #e5e5e5;
  position: absolute;
  left: 0;
  top: px2vw(2);
}
.step-line {
  width: px2vw(2);
  height: 100%;
  background: #f5f5f5;
  position: absolute;
  top: px2vw(16);
  left: px2vw(30);
  z-index: 0;
}
.line {
  height: px2vw(1);
}
</style>
