<template>
  <!-- 时间选择 -->
  <!-- #ifdef H5 -->
  <!-- H5：date-select-root 配合 style 块禁止纵向 flex-grow（根因修复，见下方样式注释）；
       height:auto 让日期条按内容高度排列；overflow:hidden 兜底裁剪异常撑高 -->
  <view
    class="date-select-root flex font-24 color-999 justify-between align-center w-100 pl-16 box"
    style="height: auto; overflow: hidden"
  >
  <!-- #endif -->
  <!-- #ifndef H5 -->
  <view class="date-select-root flex-1 flex font-24 color-999 justify-between align-center w-100 h-84 pl-16 box">
  <!-- #endif -->
    <view v-if="dateType" class="flex">
      <view
        v-if="showDay"
        :class="['date-common text-center rounded-24', dateType === 'day' ? ' bg-dbeaff color-0066ff' : '']"
        @tap="selectDataType('day')"
      >
        今日
      </view>
      <view
        :class="[
          'date-common text-center rounded-24',
          dateType === 'month' ? ' bg-dbeaff color-0066ff' : '',
          showDay ? 'ml-16' : ''
        ]"
        @tap="selectDataType('month')"
      >
        本月
      </view>
      <view
        :class="['date-common text-center rounded-24 ml-16', dateType === 'year' ? ' bg-dbeaff color-0066ff' : '']"
        @tap="selectDataType('year')"
      >
        本年
      </view>
      <view class="flex-1"></view>
    </view>
    <view class="time-content flex align-center justify-end flex-1 color-333 font-24">
      <picker
        mode="date"
        :value="startDate"
        :start="startDatePicker"
        :end="endDatePicker"
        @change="bindDateChangeStart"
        :class="['date text-center rounded-24 mr-8 px-16 box', bgClass]"
      >
        <text v-if="startDate">{{ dayjs(startDate).format('YY-MM-DD') }}</text>
        <text v-else class="color-999">起始日期</text>
      </picker>
      ~
      <picker
        mode="date"
        :value="endDate"
        :start="startDatePicker"
        :end="endDatePicker"
        @change="bindDateChangeEnd"
        :class="['date text-center rounded-24 ml-8 px-16 box', bgClass]"
      >
        <text v-if="endDate">{{ dayjs(endDate).format('YY-MM-DD') }}</text>
        <text v-else class="color-999">结束日期</text>
      </picker>
    </view>
  </view>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import { _get, _post } from '@/utils/common-request'
import { onShow } from '@dcloudio/uni-app'
import dayjs from 'dayjs'
const props = defineProps({
  dateType: {
    type: String,
    default: ''
  },
  bgClass: {
    type: String,
    default: 'bg-fff'
  },
  showDay: {
    type: Boolean,
    default: false
  }
})
const emits = defineEmits(['dateChange'])

//日期选择
const dateType = ref(props.dateType)
const startDate = ref('')
const endDate = ref('')

function selectDataType(type) {
  dateType.value = type
  const date = new Date()
  if (type === 'day') {
    startDate.value = dayjs().format('YYYY-MM-DD')
    endDate.value = dayjs().format('YYYY-MM-DD')
  }
  if (type === 'month') {
    startDate.value = dayjs().startOf('month').format('YYYY-MM-DD')
    endDate.value = dayjs().endOf('month').format('YYYY-MM-DD')
  }
  if (type === 'year') {
    startDate.value = dayjs().startOf('year').format('YYYY-MM-DD')
    endDate.value = dayjs().endOf('year').format('YYYY-MM-DD')
  }
  emits('dateChange', startDate.value, endDate.value)
}
function timePlus0(time) {
  if (time > 9) {
    return time
  } else {
    return '0' + time
  }
}
//设置可选择的起始时间
const startDatePicker = computed(() => {
  return getDate('start')
})
const endDatePicker = computed(() => {
  return getDate('end')
})
function getDate(type) {
  const date = new Date()
  let year = date.getFullYear()
  let month = date.getMonth() + 1
  let day = date.getDate()

  if (type === 'start') {
    year = year - 60
  } else if (type === 'end') {
    year = year + 2
  }
  month = month > 9 ? month : '0' + month
  day = day > 9 ? day : '0' + day
  return `${year}-${month}-${day}`
}
//选择自定义时间
function bindDateChangeStart(e) {
  dateType.value = 'other'
  if (endDate.value) {
    let before = dayjs(e.detail.value).isBefore(endDate.value)
    let same = dayjs(e.detail.value).isSame(dayjs(endDate.value))
    if (before || same) {
      startDate.value = e.detail.value
      emits('dateChange', startDate.value, endDate.value)
    } else {
      uni.showToast({ title: '开始日期必须晚于结束日期', icon: 'none', duration: 2000 })
    }
  } else {
    startDate.value = e.detail.value
    emits('dateChange', startDate.value, endDate.value)
  }
}
function bindDateChangeEnd(e) {
  dateType.value = 'other'
  if (startDate.value) {
    let after = dayjs(e.detail.value).isAfter(startDate.value)
    let same = dayjs(e.detail.value).isSame(dayjs(startDate.value))
    if (after || same) {
      endDate.value = e.detail.value
      emits('dateChange', startDate.value, endDate.value)
    } else {
      uni.showToast({ title: '开始日期必须晚于结束日期', icon: 'none', duration: 2000 })
    }
  } else {
    endDate.value = e.detail.value
    emits('dateChange', startDate.value, endDate.value)
  }
}
function setDate(start, end, type) {
  startDate.value = start
  endDate.value = end
  if (type) {
    dateType.value = type
  }
}
defineExpose({ selectDataType, setDate })
</script>
<style lang="scss" scoped>
/* #ifdef H5 */
/* H5 根因修复：date-select 根 class 原本含 .flex-1（flex:1），在 flex-direction:column 的
   父容器（如记工排行页 100vh 内容区）中被解析为纵向 flex-grow:1，与兄弟列表容器平分剩余高度，
   导致日期条被撑高、内容居中、上下出现 150~200px 大留白。
   这里 H5 下禁止纵向 flex-grow（flex:none），日期条恢复按内容高度紧凑排列（w-100 仍保证横向撑满）。 */
.date-select-root {
  flex: none;
  /* 合理的上下内边距（≈12px）：既有呼吸感又不空旷，高度接近小程序 h-84，两端视觉一致 */
  padding-top: px2vw(24);
  padding-bottom: px2vw(24);
}
/* #endif */
.date-common {
  width: px2vw(80);
  height: px2vw(44);
  line-height: px2vw(44);
}
.bg-tip {
  width: px2vw(104);
  height: px2vw(32);
  line-height: px2vw(32);
  background: #ebf0f5;
  border-radius: px2vw(4);
  border: px2vw(2) solid #d3dfeb;
}
.bottom-btn {
  height: px2vw(152);
  border-radius: 0px 0px 0px 0px;
  opacity: 1;
  .cancel {
    width: px2vw(296);
    height: px2vw(88);
    background: linear-gradient(180deg, #f5f7fa 0%, #ffffff 100%);
    box-shadow: px2vw(8) px2vw(8) px2vw(16) px2vw(1) rgba(0, 0, 0, 0.1);
    border-radius: px2vw(44);
  }
  .confirm {
    width: px2vw(296);
    height: px2vw(88);
    background: linear-gradient(180deg, #629bf0 0%, #89b4f5 100%);
    box-shadow: px2vw(8) px2vw(8) px2vw(16) px2vw(1) rgba(0, 0, 0, 0.1);
    border-radius: px2vw(44);
  }
}
.main-content {
  height: px2vw(451);
  border-radius: px2vw(16) px2vw(16) 0 0;
  position: absolute;
  left: 0;
  bottom: 0;
}
.export {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 2;
  background: rgba(0, 0, 0, 0.3);
}
.line-h-40 {
  line-height: px2vw(40);
}
.crown {
  width: px2vw(48);
  height: px2vw(38);
  position: absolute;
  z-index: 2;
  left: px2vw(40);
  top: px2vw(-38);
}
.rank2 {
  position: absolute;
  z-index: 2;
  left: px2vw(40);
  bottom: px2vw(-16);
}
.rank1 {
  left: px2vw(44);
}

.time-content {
  //   height: px2vw(100);
  .date {
    // width: px2vw(155);
    height: px2vw(44);
    line-height: px2vw(44);
  }
}
/* #ifdef H5 */
/* H5：去掉固定高度 h-84 导致的日期条底部留白，让日期条与下方标题栏/内容贴合（与小程序视觉一致） */
.date-select-root {
  height: auto !important;
}
/* #endif */
</style>
