<template>
  <view class="bigBg">
    <uni-nav-bar backgroundColor="#00000000" color="#ffffff" />
    <!--人名头像,体验用户才显示联系我们-->
    <h-user-info textColor="color-fff" />
    <!-- 良品-不良品 -->
    <view class="flex-ac color-fff mt-24 px-16 box">
      <view class="w-288 flex align-center" @tap="toPass">
        <image :src="formatImage('icon_liangpin_color')" mode="aspectFit" class="icon-96 mt-12" />
        <view class="flex flex-col ml-16 justify-center">
          <h-text-run class="font-32 bold" :numbers="statPassNum" :size="6" />
          <text class="font-24 mt-8">良品数</text>
        </view>
      </view>
      <view class="w-288 flex align-center" @tap="toNgPass">
        <image :src="formatImage('icon_buliang_color')" mode="aspectFit" class="icon-96 mt-12" />
        <view class="flex flex-col ml-16 justify-center">
          <h-text-run class="font-32 bold" :numbers="statNgNum" :size="6" />
          <text class="font-24 mt-8">不良品数</text>
        </view>
      </view>
      <view v-if="!Role_Experience" class="flex-center icon-80 feedback" @tap="toFeedback">
        <image :src="formatImage('icon_service')" class="icon-48" />
      </view>
    </view>
    <view class="pl-16 pr-16 box flex-1 flex flex-col overflow-hidden mt-4">
      <!-- 时间选择 -->
      <view class="flex font-24 color-fff justify-between align-center py-24 pl-16 pr-16 box">
        <view class="flex-1 flex justify-start align-center">
          <view
            class="date-common text-center rounded-24 box px-16 single"
            :class="dateType === 0 ? 'bg-fff-90 color-0066ff' : 'bg-fff-16'"
            @tap="selectDataType(0)"
          >
            今日
          </view>
          <view
            class="date-common text-center rounded-24 ml-16 box px-16 single"
            :class="dateType === 1 ? 'bg-fff-90 color-0066ff' : 'bg-fff-16'"
            @tap="selectDataType(1)"
          >
            本月
          </view>
          <view
            class="date-common text-center rounded-24 ml-16 box px-16 single"
            :class="dateType === 2 ? 'bg-fff-90 color-0066ff' : 'bg-fff-16'"
            @tap="selectDataType(2)"
          >
            本年
          </view>
        </view>
        <view class="shrink-1 time-content flex align-center color-fff font-24">
          <picker
            mode="date"
            :value="startDate"
            :start="startDatePicker"
            :end="endDatePicker"
            @change="bindDateChangeStart"
            class="shrink-1 date text-center bg-fff-90 color-333 rounded-24 mr-8 px-16 box"
          >
            {{ dayjs(startDate).format('YY-MM-DD') }}
          </picker>
          ~
          <picker
            mode="date"
            :value="endDate"
            :start="startDatePicker"
            :end="endDatePicker"
            @change="bindDateChangeEnd"
            class="shrink-1 date text-center bg-fff-90 color-333 rounded-24 ml-8 px-16 box"
          >
            {{ dayjs(endDate).format('YY-MM-DD') }}
          </picker>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { formatImage, Role_Experience } from '@/utils/common'
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import HTextRun from '@/components/h-text-run.vue'
/** 跳转不良品（带上当前选中的时间段：进页面即走"报工口径"，与卡片数字同源、可对上） */
function toNgPass() {
  uni.navigateTo({
    url: `/pages-analysis/quality/ng-pass-list?startDate=${startDate.value}&endDate=${endDate.value}`
  })
}
/** 跳转良品（同上；不传日期会落到"在制库存口径"，数字与卡片不可比 → 用户会以为数错） */
function toPass() {
  uni.navigateTo({
    url: `/pages-analysis/quality/pass-list?startDate=${startDate.value}&endDate=${endDate.value}`
  })
}

/**
 * 顶部「良品数 / 不良品数」= 所选时间段内的**已审**良品数 / 不良品数（报工口径）
 *
 * 历史问题：这里原先读 `/analysis/obtainedTotalStock`（= 工序在制品表 `micro_process_storage` 的
 * pass_num/ng_num 之和），而该列在不同写入路径语义不同——当前工序行 += 良品，前工序行 -=（良品+不良），
 * 两边不对称且前工序可能从未被加过 → 求和会出现 -31、-21 这类负数，既不是"良品数"也不随时间选择变化
 * （该接口不接收日期参数）。现改为复用页面已取的 `/analysis/index`（与下方统计区、良品率同源）。
 */
const props = defineProps<{
  indexData?: any
}>()
const statPassNum = computed(() => props.indexData?.passNum ?? 0)
const statNgNum = computed(() => props.indexData?.ngNum ?? 0)

onMounted(() => {
  selectDataType(0)
})

//日期选择
const dateType = ref(0)
const startDate = ref('')
const endDate = ref('')
function selectDataType(type) {
  dateType.value = type
  if (type === 0) {
    startDate.value = dayjs().format('YYYY-MM-DD')
    endDate.value = dayjs().format('YYYY-MM-DD')
  }
  if (type === 1) {
    startDate.value = dayjs().startOf('month').format('YYYY-MM-DD')
    endDate.value = dayjs().endOf('month').format('YYYY-MM-DD')
  }
  if (type === 2) {
    startDate.value = dayjs().startOf('year').format('YYYY-MM-DD')
    endDate.value = dayjs().endOf('year').format('YYYY-MM-DD')
  }
  getIndexData()
}
//设置可选择的起始时间
const startDatePicker = computed(() => {
  return getDate('start')
})
const endDatePicker = computed(() => {
  return getDate('end')
})
function getDate(type) {
  switch (type) {
    case 'start':
      return dayjs().subtract(60, 'year').format('YYYY-MM-DD')
    case 'end':
      return dayjs().add(2, 'year').format('YYYY-MM-DD')
    default:
      return ''
  }
}
//选择自定义时间
function bindDateChangeStart(e) {
  dateType.value = 3
  if (endDate.value) {
    let before = dayjs(e.detail.value).isBefore(endDate.value)
    let same = dayjs(e.detail.value).isSame(dayjs(endDate.value))
    if (before || same) {
      startDate.value = e.detail.value
      getIndexData()
    } else {
      uni.showToast({ title: '开始日期必须晚于结束日期', icon: 'none', duration: 2000 })
    }
  } else {
    startDate.value = e.detail.value
    getIndexData()
  }
}
function bindDateChangeEnd(e) {
  dateType.value = 3
  if (startDate.value) {
    let after = dayjs(e.detail.value).isAfter(startDate.value)
    let same = dayjs(e.detail.value).isSame(dayjs(startDate.value))
    if (after || same) {
      endDate.value = e.detail.value
      getIndexData()
    } else {
      uni.showToast({ title: '开始日期必须晚于结束日期', icon: 'none', duration: 2000 })
    }
  } else {
    endDate.value = e.detail.value
    getIndexData()
  }
}

const emits = defineEmits(['getIndexData'])
function getIndexData() {
  emits('getIndexData', startDate.value, endDate.value, dateType.value)
}
function toFeedback() {
  uni.navigateTo({
    url: '/pages-smart-service/index'
  })
}
defineExpose({ selectDataType })
</script>

<style lang="scss" scoped>
.feedback {
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
}
.bigBg {
  height: px2vw(400);
  background-image: url('/static/images/topbg_analyse.png');
  background-size: 100% 100%;
  border-radius: 0 0 px2vw(16) px2vw(16);
  background-color: #054cf2;
}
.bg-fff-16 {
  background: rgba(255, 255, 255, 0.16);
}
.vertical-line {
  width: px2vw(1);
  height: px2vw(72);
  background: #e5e5e5;
}
.bg-view {
  width: px2vw(104);
  height: px2vw(32);
  line-height: px2vw(32);
}

.date-common {
  height: px2vw(44);
  line-height: px2vw(44);
}

.time-content {
  .date {
    height: px2vw(44);
    line-height: px2vw(44);
  }
}
</style>
