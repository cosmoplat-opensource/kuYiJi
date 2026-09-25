<template>
  <view class="h-full bg-f3f3f5 box flex flex-col">
    <uni-nav-bar />
    <h-status-header title="良品率分析" />
    <!--搜索区域-->
    <view class="box flex align-center pt-24 pb-16">
      <list-search @search="searchList" needExact :focusAble="false" class="flex-1" wrapper-padding-right="pr-0" />
      <h-page-video :videoArr="[20]" page-name="良品率分析" class="ml-24 mr-32" />
    </view>
    <!-- 时间选择 -->
    <view class="flex font-24 color-999 justify-between align-center pt-8 pb-24 mx-32 box">
      <view class="date-common" :class="{ active: dateType === 1 }" @tap="selectDataType(1)">本月</view>
      <view class="date-common ml-16" :class="{ active: dateType === 2 }" @tap="selectDataType(2)">本年</view>
      <view class="flex-1 justify-end flex align-center pl-32 color-333 font-24">
        <picker
          mode="date"
          :value="startDate"
          :start="startDatePicker"
          :end="endDate"
          @change="bindDateChangeStart"
          class="h-44 line-height-44 text-center bg-fff rounded-24 mr-8 px-16"
        >
          <text v-if="startDate">{{ startDate }}</text>
          <text v-else class="color-999">起始日期</text>
        </picker>
        ~
        <picker
          mode="date"
          :value="endDate"
          :start="startDate"
          :end="endDatePicker"
          @change="bindDateChangeEnd"
          class="h-44 line-height-44 text-center bg-fff rounded-24 ml-8 px-16"
        >
          <text v-if="endDate">{{ endDate }}</text>
          <text v-else class="color-999">结束日期</text>
        </picker>
      </view>
    </view>
    <!--滚动区域-->
    <scroll-view
      @refresherrefresh="onRefresh"
      :refresher-triggered="triggered"
      scroll-y
      refresher-enabled
      enable-flex
      class="flex-1 overflow-hidden flex flex-col"
    >
      <!--良品率分析-->
      <list-pass-rate :params="params" :dimension="dimension" ref="listPassRate" @changeTriggered="changeTriggered" />
    </scroll-view>
    <h-status-footer />
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { _get } from '@/utils/common-request'
import ListPassRate from '@/pages-analysis/quality/list-pass-rate.vue'
import ListSearch from '@/components/product-search/list-search.vue'
import bigNumber from 'bignumber.js'
import { $state, setGio } from '@/utils/common'
import { onShow, onLoad } from '@dcloudio/uni-app'
onShow(() => {
  setGio('jr_lpv')
})
// 问一问"查看图表"跳转参数：?params=<encodeURIComponent(JSON)>
// { startDate, endDate, dimension: product|process|employee }
// 历史问题：本页原先没有 onLoad，跳进来永远按"本月 + 产品 tab"，与答案的时间范围/维度不一致
const dimension = ref('')
let externalLoaded = false
onLoad((e: any) => {
  let p: any = {}
  try {
    p = e.params ? JSON.parse(decodeURIComponent(e.params)) : {}
  } catch (err) {}
  if (p.dimension) {
    dimension.value = p.dimension
  }
  const start = p.startDate || e.startDate
  const end = p.endDate || e.endDate
  if (start && end) {
    externalLoaded = true
    startDate.value = start
    endDate.value = end
    dateType.value = 3
  }
})
// 初始化
onMounted(() => {
  if (!externalLoaded) selectDataType(1)
})
// 加载测试数据
const checkMock = computed(() => $state.mock.mockOpen)

// 日期模块
const dateType = ref(0)
const startDate = ref('')
const endDate = ref('')
function selectDataType(type) {
  dateType.value = type
  switch (type) {
    case 0:
      startDate.value = dayjs().format('YYYY-MM-DD')
      endDate.value = dayjs().format('YYYY-MM-DD')
      break
    case 1:
      startDate.value = dayjs().startOf('month').format('YYYY-MM-DD')
      endDate.value = dayjs().endOf('month').format('YYYY-MM-DD')
      break
    case 2:
      startDate.value = dayjs().startOf('year').format('YYYY-MM-DD')
      endDate.value = dayjs().endOf('year').format('YYYY-MM-DD')
      break
    default:
      break
  }
}
const startDatePicker = computed(() => {
  return dayjs().subtract(60, 'year').format('YYYY-MM-DD')
})
const endDatePicker = computed(() => {
  return dayjs().add(2, 'year').format('YYYY-MM-DD')
})
function bindDateChangeStart(e) {
  startDate.value = e.detail.value
  dateType.value = 3
}
function bindDateChangeEnd(e) {
  endDate.value = e.detail.value
  dateType.value = 3
}

const params = computed(() => {
  return {
    startDate: startDate.value,
    endDate: endDate.value,
    productNameOrCode: seqKey.value
  }
})

const seqKey = ref('')
function searchList(item) {
  seqKey.value = item?.itemSeq || ''
}

const triggered = ref(false)
const listPassRate = ref(null)
function onRefresh() {
  triggered.value = true
  listPassRate.value.getList()
}
function changeTriggered() {
  triggered.value = false
}
</script>

<style lang="scss" scoped>
.input-area {
  height: px2vw(64);
  background: #ebecee;
  box-shadow: 0 px2vw(1) 0 px2vw(1) #ffffff, inset 0 px2vw(8) px2vw(12) px2vw(1) #ccd1d9;
  border-radius: px2vw(16);
  padding: 0 px2vw(24) 0 px2vw(32);
}

.date-common {
  width: px2vw(80);
  height: px2vw(44);
  line-height: px2vw(44);
  text-align: center;
  border-radius: px2vw(24);
  &.active {
    background-color: #dbeaff;
    color: #0066ff;
  }
}
.card-mark {
  width: px2vw(16);
  height: px2vw(24);
  background: #00bfa5;
  border-radius: 0 px2vw(4) px2vw(4) 0;
}
</style>
