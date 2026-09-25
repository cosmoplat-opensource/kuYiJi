<template>
  <view class="w-full h-full bg-f3f3f5 relative flex flex-col overflow-hidden">
    <uni-nav-bar />
    <h-status-header title="完工产品" />
    <view class="flex-1 flex flex-col overflow-hidden">
      <!-- 时间选择 -->
      <date-select ref="dateSelect" dateType="month" @dateChange="dateChange" />
      <!-- 排行列表 -->
      <view class="flex-1 flex flex-col overflow-hidden">
        <view class="bg-fff rounded-16 pt-24 pb-32 mx-16 box flex-1 flex flex-col overflow-hidden">
          <view class="flex justify-between align-center px-32 box">
            <view class="relative green-title">
              <text class="color-5a6f82 font-28 bold">完工数量统计</text>
            </view>
            <view class="flex align-center">
              <btn-export @exportToMail="exportToMail" />
              <image
                @click="changeShowType"
                :src="showType === 'table' ? '/static/images/icon_chart.svg' : '/static/images/icon_table.svg'"
                class="icon-48 ml-32"
              />
            </view>
          </view>
          <!-- 表格样式 -->
          <scroll-view
            v-show="showType === 'table'"
            @refresherrefresh="onRefresh"
            :refresher-triggered="triggered"
            refresher-enabled
            scroll-y
            enable-flex
            class="mt-8 box flex-1 overflow-hidden"
          >
            <template v-if="FinishList.length > 0">
              <view
                v-for="(item, index) in FinishList"
                :key="index"
                class="w-100 h-80 flex justify-between align-center relative pl-48 pr-32 box"
                :class="{ 'border-bottom-f5f5f5': index === FinishList.length - 1 }"
              >
                <view class="number-mark pl-8 pr-8 box flex align-center justify-center">
                  <text class="color-5a6f82 font-20">{{ index + 1 > 9 ? index + 1 : '0' + (index + 1) }}</text>
                </view>
                <view class="font-28 flex bold">
                  <h-text-display :text="item.productName" :width="200" />
                  <view class="color-999 flex">( <h-text-display :text="item.productSeq" :width="240" />) </view>
                </view>
                <view class="font-24">
                  <text class="color-ff0000">{{ item.completeTotalNum }}</text>
                  <text> {{ item.unit ? formatStr(item.unit, 5) : '' }}</text>
                </view>
              </view>
            </template>
            <template v-else>
              <h-empty className="pt-240" tipsWord="暂无数据" />
            </template>
          </scroll-view>
          <!-- 图表样式:H5 用 h-echart(echarts,DOM tooltip),小程序/App 用 qiun(画笔 tooltip) -->
          <view v-if="showType === 'chart'" class="charts-box mx-16 box">
            <template v-if="FinishChart.length > 0">
              <!-- #ifdef H5 -->
              <h-echart ref="chart" class="w-100" custom-style="height: 83.33vw;" @finished="init" />
              <!-- #endif -->
              <!-- #ifndef H5 -->
              <view class="w-100" style="height: 83.33vw;">
                <qiun-data-charts type="line" :canvas2d="false" :ontouch="true" :chart-data="uChartData" :opts="uChartOpts" />
              </view>
              <!-- #endif -->
            </template>
            <template v-else>
              <h-empty className="pt-240" tipsWord="暂无数据" />
            </template>
          </view>
        </view>
      </view>
    </view>
    <ExportEmail ref="exportEmail" :exportLoading="exportLoading" @confirmPopup="confirmPopup" />
    <h-status-footer />
    <h-popup />
  </view>
</template>
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { $state, formatStr, setGio } from '@/utils/common'
import DateSelect from '@/pages-analysis/components/date-select.vue'
import { LineChart } from 'echarts/charts'
import { _get } from '@/utils/common-request'
import * as echarts from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { TooltipComponent, GridComponent, LegendComponent } from 'echarts/components'
import HEmpty from '@/components/h-empty.vue'
import { onShow } from '@dcloudio/uni-app'
import ExportEmail from '@/components/export-email.vue'
import BtnExport from '@/components/btn-export.vue'
echarts.use([LineChart, CanvasRenderer, TooltipComponent, GridComponent, LegendComponent])
onShow(() => {
  setGio('jr_wgcp')
})
onMounted(() => {
  dateSelect.value.selectDataType('month')
})
// 图表:echarts 折线,显示全部数据点(移除会崩的 dataZoom)
const chart = ref(null)
const option = ref({
  legend: {
    data: ['完工数量'],
    bottom: 0
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'line',
      label: {
        backgroundColor: '#6a7985'
      }
    }
  },
  grid: {
    top: 34,
    left: 15,
    right: 16,
    bottom: 65,
    containLabel: true
  },
  xAxis: {
    type: 'category',
    axisTick: {
      show: false
    },
    boundaryGap: [5, 5],
    axisLine: {
      show: false,
      lineStyle: {
        color: '#333'
      }
    },
    data: []
  },
  yAxis: {
    type: 'value',
    axisLine: {
      lineStyle: {
        color: '#333'
      }
    },
    splitLine: {
      lineStyle: {
        color: '#e5e5e5',
        type: 'dashed'
      }
    }
  },
  series: [
    {
      name: '完工数量',
      type: 'line',
      label: {
        show: true,
        position: 'top',
        formatter: '{c}'
      },
      symbolSize: 8,
      itemStyle: {
        color: '#19aa8d'
      },
      data: []
    }
  ]
})
function init() {
  chart.value?.init(echarts, (chart) => {
    chart.setOption(option.value)
  })
}
// 小程序/App 端:qiun-data-charts(uCharts)折线,显示全部数据点
const uChartData = ref({ categories: [], series: [] })
const uChartOpts = ref({})
function updateUChart() {
  if (!FinishChart.value || !FinishChart.value.length) return
  const xData = []
  const dataChart = []
  FinishChart.value.forEach((item) => {
    xData.push(item.statDate)
    dataChart.push(item.completeTotalNum)
  })
  uChartData.value = {
    categories: xData,
    series: [
      {
        name: '完工数量',
        data: dataChart,
        formatter: (val) => String(val),
        textSize: 10,
        textColor: '#333333'
      }
    ]
  }
  uChartOpts.value = {
    color: ['#19aa8d'],
    padding: [34, 16, 38, 15],
    fontSize: 10,
    fontColor: '#666666',
    legend: {
      show: true,
      position: 'bottom',
      fontSize: 10,
      lineHeight: 20
    },
    xAxis: {
      disableGrid: true,
      boundaryGap: xData.length <= 1 ? 'center' : 'justify',
      axisLine: false,
      fontSize: 10,
      fontColor: '#666666'
    },
    yAxis: {
      gridType: 'dash',
      dashLength: 2,
      gridColor: '#e5e5e5'
    },
    extra: {
      line: {
        type: 'straight',
        width: 2,
        activeType: 'hollow'
      }
    }
  }
}
// 加载测试数据
const checkMock = computed(() => $state.mock.mockOpen)

const dateSelect = ref(null)
//显示类型
const showType = ref('chart')
function changeShowType() {
  if (showType.value === 'table') {
    showType.value = 'chart'
    getFinishData()
  } else {
    showType.value = 'table'
    getFinishTableData()
  }
}
//日期选择
const startDate = ref('')
const endDate = ref('')
function dateChange(start, end) {
  startDate.value = start
  endDate.value = end
  if (showType.value === 'chart') {
    getFinishData()
  } else {
    showType.value = 'table'
    getFinishTableData()
  }
}
//获取排行数据
const FinishList = ref([])
const FinishChart = ref([])
function getFinishData() {
  let mockParams = {
    url: '/complete/report/statistics/day',
    data: {
      startDate: startDate.value,
      endDate: endDate.value
    }
  }
  if (checkMock.value) {
    Object.assign(mockParams, {
      url: '/analysis/loadDisplayData',
      data: { apiUrl: mockParams.url }
    })
  }
  _get(mockParams)
    .then((res) => {
      if (res?.data) {
        FinishChart.value = res.data
        const dataChart = []
        const xData = []
        FinishChart.value.forEach((item) => {
          xData.push(item.statDate)
          dataChart.push(item.completeTotalNum)
        })
        option.value.series[0].data = dataChart
        option.value.xAxis.data = xData
        if (dataChart.length > 0) {
          chart.value?.init(echarts, (chart) => {
            chart.setOption(option.value)
          })
          // 小程序/App 端走 uCharts
          updateUChart()
        }
      }
    })
    .finally(() => {})
}
function getFinishTableData() {
  let mockParams = {
    url: '/complete/report/statistics/product',
    data: {
      startDate: startDate.value,
      endDate: endDate.value
    }
  }
  if (checkMock.value) {
    Object.assign(mockParams, {
      url: '/analysis/loadDisplayData',
      data: { apiUrl: mockParams.url }
    })
  }
  _get(mockParams)
    .then((res) => {
      if (res?.rows) {
        FinishList.value = res.rows
      }
    })
    .finally(() => {
      triggered.value = false
    })
}
const triggered = ref(false)
function onRefresh() {
  triggered.value = true
  getFinishTableData()
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
      '/complete/report/statistics/export?startDate=' +
      startDate.value +
      '&&endDate=' +
      endDate.value +
      '&&receivedBy=' +
      receivedBy
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
</script>
<style lang="scss" scoped>
.green-title:before {
  top: px2vw(10);
}
.charts-box {
  height: 300px;
}
.number-mark {
  background: #ebf0f5;
  border-radius: 0 px2vw(4) px2vw(4) 0;
  height: px2vw(32);
  position: absolute;
  z-index: 2;
  left: px2vw(0);
  top: px2vw(24);
}
.export-btn {
  width: px2vw(80);
  height: px2vw(44);
  line-height: px2vw(44);
  background: #ffffff;
  border: px2vw(2) solid #0066ff;
  &:active {
    background-color: #0066ff;
    color: #ffffff;
  }
}
</style>
