<template>
  <view>
    <view class="bg-fff pt-32 mx-16 box rounded-16 mt-16 flex flex-col">
      <view class="flex align-center px-32">
        <view class="font-28 bold color-5a6f82 flex-1">生产质量趋势</view>
      </view>
      <h-empty v-if="chartEmpty" class-name="pt-120 pb-48" />
      <!-- #ifdef H5 -->
      <h-echart ref="chart" class="w-100" custom-style="height: 69.44vw;" @finished="chartInit" />
      <!-- #endif -->
      <!-- #ifndef H5 -->
      <!-- 数据就绪后才挂载；:key 在数据更新时强制重建（canvas 重新初始化，规避延迟挂载不绘制） -->
      <view v-if="uChartData && uChartData.series && uChartData.series.length > 0" class="w-100" style="height: 69.44vw;">
        <qiun-data-charts :key="chartKey" type="line" :canvas2d="false" :ontouch="true" :chart-data="uChartData" :opts="uChartOpts" />
      </view>
      <view v-else class="w-100" style="height: 69.44vw;"></view>
      <!-- #endif -->
    </view>
    <view class="bg-fff pt-32 mx-16 box rounded-16 mt-16 flex flex-col mb-48">
      <view class="flex align-center px-32">
        <view class="font-28 bold color-5a6f82 flex-1">生产良品率</view>
        <view class="flex align-center">
          <btn-export @exportToMail="exportToMail" />
        </view>
      </view>
      <h-empty v-if="chartEmpty" class-name="pt-120 pb-48" />
      <view v-else>
        <view v-for="(item, index) in dataList" :key="index" class="mx-32 py-24 trend-list">
          <view class="flex align-center">
            <view class="color-333 font-28 bold">
              <h-text-display :text="item.productName" :width="200" />
            </view>
            <view class="ml-4 color-999 font-28 flex"
              >( <h-text-display :text="item.productCode" :width="200" />)
            </view>
            <view class="flex-1 text-right color-5a6f82 font-24 ml-8">良品率：</view>
            <view class="font-28" :class="passRateColor(item.passRate)">{{ `${getPassNumRate(item.passRate)}%` }}</view>
          </view>
          <view class="flex align-center mt-24">
            <h-text-display :text="item.operateProcessName" :width="200" class="color-5a6f82 font-28" />
            <view class="flex-1" />
            <area-date-info :dataItem="{ pass: 1, ng: 2 }" :unit="item.unit" />
          </view>
        </view>
        <view class="font-24 color-b6c0c9 py-48 text-center">已到底部</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
// #ifdef H5
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'
import { TooltipComponent, GridComponent, LegendComponent } from 'echarts/components'
// #endif
import { computed, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { _get } from '@/utils/common-request'
import CardMark from '@/pages-analysis/components/card-mark.vue'
import bigNumber from 'bignumber.js'
import { $state } from '@/utils/common'
import HEmpty from '@/components/h-empty.vue'
import BtnExport from '@/components/btn-export.vue'
import AreaDateInfo from '@/pages/report-production/components/area-date-info.vue'

// #ifdef H5
echarts.use([LineChart, CanvasRenderer, TooltipComponent, GridComponent, LegendComponent])
// #endif

const props = defineProps({
  params: {
    type: Object,
    default: () => ({})
  },
  dateType: {
    type: Number,
    default: 1
  }
})

// 加载测试数据
const checkMock = computed(() => $state.mock.mockOpen)

// 图表状态（必须声明在 watch 之前：immediate watch 回调会同步执行 getChart()，
// 其第一行访问 chartReady，靠后声明会导致 H5 端 TDZ 报错整页白屏）
const chartEmpty = ref(false)
// 图表模块
const chart = ref(null)
const chartReady = ref(false)

watch(
  () => props.params,
  (val) => {
    if (!val.startDate || !val.endDate) return
    getList()
    getChart()
  },
  { immediate: true, deep: true }
)
function chartInit() {
  // #ifdef H5
  // 无数据不初始化:组件常驻渲染,@finished 在数据到达前已触发,等待数据就绪后由 getChart 主动调用,避免空数据重复初始化导致闪烁
  if (!dataChart.value || !dataChart.value.length) {
    return
  }
  const list = dataChart.value || []
  const xData = []
  const pass = []
  list.forEach((item) => {
    if (props.dateType === 2) {
      xData.push(dayjs(item.submitDay).format('MM月'))
    } else {
      xData.push(dayjs(item.submitDay).format('MM.DD'))
    }
    pass.push(new bigNumber(item.passRate).times(100).toNumber())
  })
  const chartOption = {
    legend: {
      data: ['良品率'],
      bottom: 8
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
      bottom: 38,
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
      data: xData
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
        name: '良品率',
        type: 'line',
        label: {
          show: true,
          position: 'top',
          formatter: '{c}%'
        },
        symbolSize: 8,
        itemStyle: {
          color: '#19aa8d'
        },
        data: pass
      }
    ]
  }
  chart.value.init(echarts, (chart) => {
    chart.setOption(chartOption)
  })
  // #endif
  // #ifndef H5
  updateUChart()
  // #endif
}

// #ifndef H5
// 小程序/App 端:qiun-data-charts(uCharts,Apache-2.0)渲染,与 H5 端 h-echart 效果对齐
const uChartData = ref({ categories: [], series: [] })
const uChartOpts = ref({})
const chartKey = ref(0)
function updateUChart() {
  if (!dataChart.value || !dataChart.value.length) {
    uChartData.value = { categories: [], series: [] }
    return
  }
  const list = dataChart.value || []
  const xData = []
  const pass = []
  list.forEach((item) => {
    if (props.dateType === 2) {
      xData.push(dayjs(item.submitDay).format('MM月'))
    } else {
      xData.push(dayjs(item.submitDay).format('MM.DD'))
    }
    pass.push(new bigNumber(item.passRate).times(100).toNumber())
  })
  uChartData.value = {
    categories: xData,
    series: [
      {
        name: '良品率',
        data: pass,
        // uCharts 2.5.0:标签格式/样式挂在 series 上(opts.dataLabel 仅作开关)
        formatter: (val) => val + '%',
        textSize: 10,
        textColor: '#333333'
      }
    ]
  }
  chartKey.value++ // 强制 qiun 组件重建（canvas 重新初始化，规避延迟挂载不绘制）
  uChartOpts.value = {
    color: ['#19aa8d'],
    padding: [34, 16, 38, 15],
    fontSize: 10,
    fontColor: '#666666',
    legend: {
      show: true,
      position: 'bottom',
      fontSize: 10,
      lineHeight: 20,
      margin: 8
    },
    xAxis: {
      disableGrid: true,
      // 单数据点时用 center 居中(与网页端 echarts boundaryGap [5,5] 单点居中一致);多点用 justify 端点排布
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
// #endif

function passRateColor(num) {
  return num > 0.8 ? 'color-19aa8d' : num > 0.6 ? 'color-e7a11a' : 'color-ff0000'
}
function getPassNumRate(num) {
  return new bigNumber(num).times(100)
}

// 质量分析 - 生产质量趋势分析
const dataList = ref([])
const dataChart = ref([])
function getChart() {
  chartReady.value = false
  let mockParams = {
    url: '/analysis/productionQualityTrend',
    data: { ...props.params, type: props.dateType === 2 ? 0 : 1 }
  }
  if (checkMock.value) {
    Object.assign(mockParams, {
      url: '/analysis/loadDisplayData',
      data: { apiUrl: mockParams.url }
    })
  }
  _get(mockParams)
    .then((res) => {
      dataChart.value = res.data ?? []
      chartEmpty.value = !dataChart.value.length
      chartReady.value = true
      // 组件常驻渲染:@finished 在挂载时已用空数据初始化过,数据就绪后需重新绘制
      if (!chartEmpty.value) {
        chartInit()
      }
    })
    .catch(() => {
      // 接口失败兜底：显示空态，避免页面永久空白（H5 弱网/后端异常时更易发生）
      dataChart.value = []
      chartEmpty.value = true
      chartReady.value = true
    })
}
function getList() {
  let mockParams = {
    url: '/submit/completedProductInformation',
    data: { ...props.params, submitStatus: 0 }
  }
  if (checkMock.value) {
    Object.assign(mockParams, {
      url: '/analysis/loadDisplayData',
      data: { apiUrl: mockParams.url }
    })
  }
  _get(mockParams)
    .then((res) => {
      dataList.value = res.data ?? []
      chartEmpty.value = !dataList.value.length
    })
    .catch(() => {
      dataList.value = []
      chartEmpty.value = true
    })
}
const emits = defineEmits(['exportToMail'])
// 暴露给父组件:下拉刷新时重新拉取图表与列表数据
defineExpose({ getChart, getList })
function exportToMail() {
  emits('exportToMail')
}
</script>
<style lang="scss" scoped>
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
.trend-list {
  border-bottom: 1px solid #f5f5f5;
}
.trend-mark {
  position: absolute;
  left: px2vw(-48);
  top: px2vw(-4);
  width: px2vw(38);
  height: px2vw(32);
  line-height: px2vw(32);
  text-align: center;
  background: #ebf0f5;
  border-radius: 0 px2vw(4) px2vw(4) 0;
  color: #5a6f82;
  font-size: px2vw(20);
}
</style>
