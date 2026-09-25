<template>
  <view class="bg-f3f3f5 box flex flex-col">
    <uni-nav-bar />
    <h-status-header title="质量趋势" />
    <!--搜索区域-->
    <view class="box flex align-center pt-24">
      <list-search @search="searchList" :initial-value="seqKey" needExact :focusAble="false" class="flex-1" wrapper-padding-right="pr-0" />
      <h-page-video :videoArr="[21]" page-name="质量趋势" class="ml-24 mr-32" @open="videoOpen = true" @close="videoOpen = false" />
    </view>
    <!-- 时间选择 -->
    <view class="flex font-24 color-999 justify-between align-center py-24 mx-32 box">
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
    <!--头部数据区域-->
    <view class="bg-fff p-32 mx-16 box rounded-16 flex align-center">
      <view class="flex-1 flex flex-col align-center">
        <view class="font-32 bold color-333">{{ topData.totalCounts }}</view>
        <view class="font-24 mt-16 color-333">记工总数</view>
      </view>
      <view class="flex-1 flex flex-col align-center ml-16" @tap="toPass">
        <view class="font-32 bold color-19aa8d">{{ topData.totalPassNum }}</view>
        <view class="font-24 mt-16 color-333">良品</view>
      </view>
      <view class="flex-1 flex flex-col align-center ml-16" @tap="toNgPass">
        <view class="font-32 bold color-ff0000">{{ topData.totalNgNum }}</view>
        <view class="font-24 mt-16 color-333">不良品</view>
      </view>
      <view class="flex-1 flex flex-col align-center ml-16">
        <view class="font-32 bold color-19aa8d">{{
          `${new bigNumber(topData.passRate).times(100)}%`
        }}</view>
        <view class="font-24 mt-16 color-333">良品率</view>
      </view>
    </view>
    <!--滚动区域:临时改为页面级滚动(测试 canvas 悬浮是否与 scroll-view 相关)-->
    <!-- 视频弹窗打开时隐藏图表(原生 canvas),避免盖住视频弹窗 -->
    <chart-quality-trend v-show="!videoOpen" ref="chartRef" :params="params" :dateType="dateType" @exportToMail="exportToMail" />
    <ExportEmail ref="exportEmail" :exportLoading="exportLoading" @confirmPopup="confirmPopup" />
    <h-status-footer />
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { _get } from '@/utils/common-request'
import ChartQualityTrend from '@/pages-analysis/quality/chart-quality-trend.vue'
import ListSearch from '@/components/product-search/list-search.vue'
import bigNumber from 'bignumber.js'
import { $state, setGio } from '@/utils/common'
import { onShow, onPullDownRefresh, onLoad } from '@dcloudio/uni-app'
import ExportEmail from '@/components/export-email.vue'
onShow(() => {
  setGio('jr_zlqs')
})
// 下拉刷新:重新拉取顶部统计 + 图表 + 列表
onPullDownRefresh(() => {
  initData()
  chartRef.value?.getChart?.()
  chartRef.value?.getList?.()
  // 等数据请求返回后收起下拉刷新动画(500ms 为保险等待,避免过早收起)
  setTimeout(() => uni.stopPullDownRefresh(), 500)
})
// 初始化
let externalLoaded = false
// 问一问"查看图表"跳转参数：?params=<encodeURIComponent(JSON)>（兼容旧 startDate/endDate query）
onLoad((e: any) => {
  let p: any = {}
  try {
    p = e.params ? JSON.parse(decodeURIComponent(e.params)) : {}
  } catch (err) {}
  const start = p.startDate || e.startDate
  const end = p.endDate || e.endDate
  const product = p.productNameOrCode
  if (start && end) {
    externalLoaded = true
    startDate.value = start
    endDate.value = end
    dateType.value = 3
  }
  if (product) {
    externalLoaded = true
    seqKey.value = product
  }
  if (externalLoaded) initData()
})
onMounted(() => {
  if (!externalLoaded) selectDataType(1)
})
// 加载测试数据
const checkMock = computed(() => $state.mock.mockOpen)
// 子组件 ref:下拉刷新时调用其刷新方法
const chartRef = ref(null)
// 视频弹窗状态:打开时隐藏图表 canvas,避免原生组件盖住视频弹窗
const videoOpen = ref(false)

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
  initData()
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
  initData()
}
function bindDateChangeEnd(e) {
  endDate.value = e.detail.value
  dateType.value = 3
  initData()
}

// 数据获取
const topData = reactive({ totalCounts: 0, totalNgNum: 0, totalPassNum: 0, passRate: 0 })
const params = computed(() => {
  return {
    startDate: startDate.value,
    endDate: endDate.value,
    productNameOrCode: seqKey.value
  }
})
function initData() {
  let mockParams = {
    url: '/analysis/showTotalCount',
    data: params.value
  }
  if (checkMock.value) {
    Object.assign(mockParams, {
      url: '/analysis/loadDisplayData',
      data: { apiUrl: mockParams.url }
    })
  }
  // 质量分析 - 报工数量统计展示
  _get(mockParams)
    .then((res) => {
      const { totalCounts, totalNgNum, totalPassNum, passRate } = res.data || {}
      topData.totalCounts = parseInt(totalCounts || 0)
      topData.totalNgNum = parseInt(totalNgNum || 0)
      topData.totalPassNum = parseInt(totalPassNum || 0)
      topData.passRate = passRate || 0
    })
}
const seqKey = ref('')
function searchList(item) {
  seqKey.value = item?.itemSeq || ''
  initData()
}

//导出
const exportEmail = ref(null)
function exportToMail() {
  exportEmail.value.openExportToMail({ startDate: startDate.value, endDate: endDate.value })
}
const exportLoading = ref(false)
function confirmPopup(receivedBy) {
  uni.showLoading({
    title: '发送中'
  })
  exportLoading.value = true
  _get({
    url:
      '/export/qualityTrends?startDate=' + startDate.value + '&&endDate=' + endDate.value + '&&receivedBy=' + receivedBy
  })
    .then((res) => {
      exportEmail.value.closePopup()
      uni.showToast({
        title: '导出成功',
        duration: 2000,
        icon: 'none'
      })
    })
    .finally(() => {
      uni.hideLoading()
      exportLoading.value = false
    })
}
//不良品
function toNgPass() {
  uni.navigateTo({
    url: '/pages-analysis/quality/ng-pass-list?startDate=' + startDate.value + '&endDate=' + endDate.value
  })
}
//良品
function toPass() {
  uni.navigateTo({
    url: '/pages-analysis/quality/pass-list?startDate=' + startDate.value + '&endDate=' + endDate.value
  })
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
