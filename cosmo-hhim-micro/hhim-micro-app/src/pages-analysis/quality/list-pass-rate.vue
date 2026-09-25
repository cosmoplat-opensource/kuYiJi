<template>
  <view class="bg-fff py-24 mx-16 box rounded-16 flex flex-col">
    <view class="flex align-center px-32 pb-16 b-b-1 border-f5f5f5 relative">
      <view class="font-28 bold color-5a6f82 flex-1">良品率分析</view>
      <view class="absolute tabChange flex">
        <view class="tab-item" :class="{ active: tabIndex === 0 }" @tap="handleTabChange(0)"> 产品 </view>
        <view class="tab-item" :class="{ active: tabIndex === 1 }" @tap="handleTabChange(1)"> 工序 </view>
        <view class="tab-item" :class="{ active: tabIndex === 2 }" @tap="handleTabChange(2)"> 员工 </view>
        <view class="tab-item" :class="{ active: tabIndex === 3 }" @tap="handleTabChange(3)"> 不良类型 </view>
      </view>
    </view>
    <h-empty class-name="pt-40" v-show="chartEmpty" />
    <view class="px-16 box" v-show="!chartEmpty">
      <template v-if="tabIndex !== 3">
        <view class="pt-32 pl-16 list-item tap" v-for="(item, index) in dataList" :key="index">
          <template v-if="tabIndex < 2">
            <view class="flex align-center pr-16" @tap="handleJumpDetail(item)">
              <view class="font-28 color-333 bold flex-1 single overflow-hidden">{{
                item.processName || item.productName
              }}</view>
              <view class="mark-c2f2ec">良品率</view>
              <view class="font-28 ml-16 text-right" :class="passRateColor(item.passRate)">{{
                `${getPassNumRate(item.passRate)}%`
              }}</view>
            </view>
            <view class="flex align-center justify-between pb-8" @tap="handleJumpDetail(item)">
              <view class="font-24 color-999">{{ item.processCode || item.productCode }}</view>
              <image src="/static/images/icon_extend.svg" class="icon-32 p-16" />
            </view>
          </template>
          <template v-else-if="tabIndex === 2">
            <view class="flex align-center pr-16 pb-32 font-28">
              <view class="color-333 bold single overflow-hidden">{{ item.nickName }}</view>
              <view class="color-999 bold flex-1">({{ item.userName }})</view>
              <view class="mark-c2f2ec">良品率</view>
              <view class="font-28 ml-16 text-right shrink-0" :class="passRateColor(item.passRate)">
                {{ `${getPassNumRate(item.passRate)}%` }}
              </view>
            </view>
          </template>
        </view>
      </template>
      <template v-else>
        <template v-if="qualityCheck">
          <h-echart v-if="dataChart.length > 0" ref="chart" custom-style="height: 69.44vw;" @finished="init" />
        </template>

        <view v-else class="font-24 color-b6c0c9 mt-48 mb-32 text-center">请先开启质检开关</view>
      </template>
      <view v-if="tabIndex !== 3" class="font-24 color-b6c0c9 mt-48 mb-32 text-center">已显示全部</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { _get } from '@/utils/common-request'
import CardMark from '@/pages-analysis/components/card-mark.vue'
import { $state, $store } from '@/utils/common'
import bigNumber from 'bignumber.js'
import HEmpty from '@/components/h-empty.vue'
import * as echarts from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'
import { TooltipComponent, GridComponent, LegendComponent } from 'echarts/components'
echarts.use([PieChart, CanvasRenderer, TooltipComponent, GridComponent, LegendComponent])
const props = defineProps({
  params: {
    type: Object,
    default: () => ({})
  },
  /** 初始维度 tab（问一问"查看图表"跳转用）：product / process / employee */
  dimension: {
    type: String,
    default: ''
  }
})

const dataList = ref([])
const tabIndex = ref(0)
// 初始维度（问一问"查看图表"带 dimension=process/employee 进来时直接开对应 tab，与答案口径一致）
const DIM_TAB: Record<string, number> = { product: 0, process: 1, employee: 2, ngType: 3 }
if (props.dimension && DIM_TAB[props.dimension] !== undefined) {
  tabIndex.value = DIM_TAB[props.dimension]
}

watch(
  () => props.params,
  (val) => {
    const params = { ...val, submitStatus: 0 }
    if (!params.startDate || !params.endDate) return
    getList(params)
  },
  { deep: true }
)

// 首次取数放在 onMounted（不能用 watch 的 immediate：那会在 setup 阶段同步执行，
// 撞上还没初始化的 tabIndex → "Cannot access 'tabIndex' before initialization"，H5 直接空白）
// H5 从问一问跳进来时子组件挂载早于页面 onLoad，参数就绪后由上面的 watch 兜住第二次取数
onMounted(() => {
  if (props.params?.startDate && props.params?.endDate) {
    getList({ ...props.params, submitStatus: 0 })
  }
})

// 维度 prop 可能在 onLoad 之后才到位（uni-app 生命周期时序不稳）→ 变化时同步 tab 并重新取数
watch(
  () => props.dimension,
  (dim) => {
    const idx = DIM_TAB[dim as string]
    if (idx === undefined || idx === tabIndex.value) return
    tabIndex.value = idx
    if (props.params?.startDate && props.params?.endDate) {
      getList({ ...props.params, submitStatus: 0 })
    }
  }
)
// 加载测试数据
const checkMock = computed(() => $state.mock.mockOpen)
function passRateColor(num) {
  return num > 0.8 ? 'color-19aa8d' : num > 0.6 ? 'color-e7a11a' : 'color-ff0000'
}
function getPassNumRate(num) {
  return new bigNumber(num).times(100)
}
function handleTabChange(index) {
  tabIndex.value = index
  getList(props.params)
}
const chartEmpty = ref(false)
async function getList(params = props.params) {
  let mockParams = { url: '', data: { ...params, submitStatus: 0 } }
  let res = null
  switch (tabIndex.value) {
    case 0:
      // 质量分析 - 良品率分析（产品维度）
      mockParams.url = '/analysis/passRateAnalysisByProduct'
      break
    case 1:
      // 质量分析 - 良品率分析（工序维度）
      mockParams.url = '/analysis/passRateAnalysisByProcess'
      break
    case 2:
      // 质量分析 - 良品率分析（员工维度）
      mockParams.url = '/analysis/passRateAnalysisByEmployee'
      break
    case 3:
      // 质量分析 - 良品率分析（不良类型）
      getNgTypeAnalysis(params)
      return
    default:
      break
  }
  if (checkMock.value) {
    Object.assign(mockParams, { url: '/analysis/loadDisplayData', data: { apiUrl: mockParams.url } })
  }
  res = await _get(mockParams)
  dataList.value = res.data
  chartEmpty.value = !res.data.length
  emits('changeTriggered')
}
const dataChart = ref([])
const qualityCheck = ref(false)
function getNgTypeAnalysis(params) {
  _get({
    url: `/setting/getIndividuationConfig`,
    data: {}
  }).then((res) => {
    if (res?.data.submitInspectSwitch === '1') {
      //关闭
      qualityCheck.value = false
      return
    } else {
      qualityCheck.value = true
      let mockParams = { url: '/ngProduct/manage/ngTypeAnalysis', data: { ...params } }
      _get(mockParams).then((res) => {
        let resData = res.data
        dataChart.value = []
        let qita = { name: '其他', value: 0 }
        let num = 0
        resData.forEach((item, idx) => {
          if (item.ngType && num <= 5) {
            num++
            dataChart.value.push({ name: item.ngType, value: item.ngNum })
          } else {
            qita.value += item.ngNum
          }
        })
        if (qita.value) dataChart.value.push(qita)
        option.value.series[0]['data'] = dataChart.value
        if (dataChart.value.length > 0) {
          chart.value?.init(echarts, (chart) => {
            chart.setOption(option.value)
          })
        }
        chartEmpty.value = !dataChart.value.length
      })
    }
  })
}
function handleJumpDetail(item) {
  const params = {
    ...props.params,
    submitStatus: 0,
    tabIndex: tabIndex.value
  }
  if (tabIndex.value) {
    params['operateProcessSeq'] = item.processSeq
  } else {
    params['productSeq'] = item.productSeq
  }
  $store.commit('analysis/setDataItem', item)
  $store.commit('analysis/setParams', params)
  uni.navigateTo({ url: '/pages-analysis/quality/detail-pass-rate?tabIndex=' + tabIndex.value })
}
//饼图设置
const chart = ref(null)
function init() {
  chart.value?.init(echarts, (chart) => {
    chart.setOption(option.value)
  })
}
const option = ref({
  textStyle: {
    fontSize: 10
  },
  label: {
    formatter: function (param) {
      let name = param.name
      let value = param.value
      if (name.length <= 4) {
      } else {
        name = name.slice(0, 4) + '...'
      }
      return name + ': ' + value
    }
  },
  tooltip: {
    trigger: 'item',
    formatter: '{b}: {c}'
  },
  labelLine: {
    length: 6,
    length2: 2
  },
  series: [
    {
      name: 'Chart',
      type: 'pie',
      minAngle: 10,
      radius: [25, 80],
      center: ['50%', '50%'],
      roseType: 'area',
      itemStyle: {
        color: function (params) {
          //自定义颜色
          var colorList = ['#F26A60', '#7792FA', '#00BFA5', '#F4BB5B', '#64B5EA', '#C682EE', '#68A2F8']
          return colorList[params.dataIndex]
        },
        borderRadius: 0,
        borderWidth: 8,
        borderColor: '#ffffff'
      },
      emphasis: {
        itemStyle: {
          normal: {
            color: function (params) {
              //自定义颜色
              var colorList = ['#F26A60', '#7792FA', '#00BFA5', '#F4BB5B', '#64B5EA', '#C682EE', '#68A2F8']
              return colorList[params.dataIndex]
            }
          }
        }
      },
      data: []
    }
  ]
})
const emits = defineEmits(['changeTriggered'])
defineExpose({ getList })
</script>

<style lang="scss" scoped>
.list-item {
  border-bottom: 1px dashed #e5e5e5;
}
.tab-item {
  // width: px2vw(80);
  height: px2vw(56);
  line-height: px2vw(56);
  padding-left: px2vw(16);
  padding-right: px2vw(16);
  text-align: center;
  position: relative;
  font-size: px2vw(24);
  color: #999;
  background: #f3f3f5;
  border-radius: 0 0 px2vw(8) px2vw(8);
  &.active {
    color: #0066ff;
    background: #dbeaff;
  }
  & + .tab-item {
    margin-left: px2vw(16);
  }
}
.tabChange {
  top: px2vw(-24);
  right: px2vw(32);
}
</style>
