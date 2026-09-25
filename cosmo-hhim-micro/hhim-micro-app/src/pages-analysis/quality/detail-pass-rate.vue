<template>
  <view class="bg-f3f3f5 box flex flex-col">
    <uni-nav-bar />
    <h-status-header title="良品率明细" />
    <view class="py-24 px-32 flex align-center">
      <view class="w-m-192 single overflow-hidden color-333 font-28 bold">
        <h-text-display :text="tabProduct ? dataItem.productName : dataItem.processName" :width="180" />
      </view>
      <view class="flex-1 color-999 font-28 bold">
        <text class="color-999 font-28 bold">({{ tabProduct ? dataItem.productCode : dataItem.processCode }})</text>
      </view>
      <view class="ml-16">
        <text class="font-24 color-5a6f82">良品率：</text>
      </view>
      <view :class="passRateColor">
        <text class="font-24 color-5a6f82">{{ getNumberRate }}</text>
      </view>
    </view>
    <view class="mx-16 box pt-32 bg-fff rounded-16">
      <view class="flex align-center">
        <card-mark />
        <view class="font-28 color-5a6f82 bold ml-16">{{
          tabProduct ? '产品各工序良品率' : '该工序下产品良品率'
        }}</view>
      </view>
      <!-- H5 用 h-echart(echarts,DOM 渲染) -->
      <!-- #ifdef H5 -->
      <h-echart ref="chart" class="w-100" :custom-style="chartStyle" @finished="chartInit" />
      <!-- #endif -->
      <!-- 小程序/App 用 qiun-data-charts(uCharts,canvas 兼容模式;canvas2d 在开发者工具/部分基础库不渲染) -->
      <!-- #ifndef H5 -->
      <view class="w-100" style="height: 69.44vw;">
        <qiun-data-charts type="bar" :canvas2d="false" :ontouch="true" :chart-data="uChartData" :opts="uChartOpts" />
      </view>
      <!-- #endif -->
    </view>
    <h-status-footer />
    <h-popup />
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { $state } from '@/utils/common'
import * as echarts from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'
import { TooltipComponent, GridComponent, LegendComponent } from 'echarts/components'
import CardMark from '@/pages-analysis/components/card-mark.vue'
import bigNumber, { BigNumber } from 'bignumber.js'
import { _get } from '@/utils/common-request'
import { onLoad } from '@dcloudio/uni-app'

echarts.use([BarChart, CanvasRenderer, TooltipComponent, GridComponent, LegendComponent])

// 加载测试数据
const checkMock = computed(() => $state.mock.mockOpen)
const params = computed(() => {
  return $state.analysis.params
})
const dataItem = computed(() => {
  return $state.analysis.dataItem
})
const passRateColor = computed(() => {
  return dataItem.value.passRate > 0.8
    ? 'color-19aa8d'
    : dataItem.value.passRate > 0.6
    ? 'color-e7a11a'
    : 'color-ff0000'
})

const getNumberRate = computed(() => {
  return `${new bigNumber(dataItem.value.passRate).times(100)}%`
})

const tabProduct = ref(true)

watch(
  () => params.value,
  (val) => {
    if (!val.startDate || !val.endDate) return
    tabProduct.value = val.tabIndex === 0
    getList(val)
  },
  { immediate: true, deep: true }
)

// 图表模块
const chart = ref(null)
// 图表高度随类别数自适应(横向条形图,每类约 40px 行高 + 标题/轴区),数据多时页面可滚动查看完整
const chartHeight = ref(320)
const chartStyle = computed(() => `height: ${chartHeight.value}px;`)
// 小程序/App 端:qiun-data-charts(uCharts)数据与配置,与 H5 的 h-echart 数据一致
const uChartData = ref({ categories: [], series: [] })
const uChartOpts = ref({})
function chartInit(list) {
  if (!list || !list.length) return
  chartHeight.value = Math.max(list.length * 40 + 140, 320)
  const xData = []
  const pass = []
  list.forEach((item) => {
    xData.push(tabProduct.value ? item.operateProcessName : item.productName)
    pass.push(new BigNumber(item.passRate).multipliedBy(100).toNumber())
  })
  const chartOption = {
    grid: {
      top: 14,
      left: 15,
      right: 40,
      bottom: 32,
      containLabel: true
    },
    xAxis: {
      type: 'value',
      axisLabel: {
        show: false
      },
      splitLine: {
        lineStyle: {
          color: '#e5e5e5',
          type: 'dashed'
        }
      }
    },
    yAxis: {
      type: 'category',
      data: xData,
      axisTick: {
        show: false
      },
      axisLine: {
        show: false,
        lineStyle: {
          color: '#333'
        }
      },
      axisLabel: {
        color: '#333',
        overflow: 'breakAll',
        width: 100
      }
    },
    series: [
      {
        name: '2011',
        type: 'bar',
        data: pass,
        itemStyle: {
          color: '#19aa8d'
        },
        label: {
          show: true,
          position: 'right',
          formatter: '{c}%'
        }
      }
    ]
  }
  // #ifdef H5
  chart.value.init(echarts, (chart) => {
    chart.setOption(chartOption)
  })
  // #endif
  // #ifndef H5
  // 小程序/App 端:uCharts 横向条形图(categories=y轴类别, series.data=x轴值),数据与 H5 一致
  uChartData.value = {
    categories: xData,
    series: [
      {
        name: '良品率',
        data: pass,
        formatter: (val) => val + '%',
        textSize: 10,
        textColor: '#333333'
      }
    ]
  }
  uChartOpts.value = {
    color: ['#19aa8d'],
    padding: [14, 40, 32, 15],
    fontSize: 10,
    fontColor: '#666666',
    legend: {
      show: false
    },
    xAxis: {
      axisLine: true,
      axisLineColor: '#333333',
      gridType: 'dash',
      dashLength: 2,
      gridColor: '#e5e5e5'
    },
    yAxis: {
      fontSize: 10,
      fontColor: '#333333'
    },
    extra: {
      bar: {
        width: 20,
        activeType: 'none'
      }
    }
  }
  // #endif
}

function getList(data) {
  let mockParams = {
    url: '/submit/completedProductInformation',
    data
  }
  if (checkMock.value) {
    Object.assign(mockParams, {
      url: '/analysis/loadDisplayData',
      data: { apiUrl: mockParams.url + (tabProduct.value ? '/product' : '/process') }
    })
  }
  _get(mockParams)
    .then((res) => {
      chartInit(res.data)
    })
    .catch(() => {
      // 接口失败兜底：提示，避免图表区静默空白（H5 弱网/后端异常时更易发生）
      uni.showToast({ title: '数据加载失败，请稍后重试', icon: 'none' })
    })
}
</script>

<style lang="scss"></style>
