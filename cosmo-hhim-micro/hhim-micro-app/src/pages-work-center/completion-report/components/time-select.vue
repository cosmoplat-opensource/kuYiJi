<template>
  <!-- 时间选择 -->
  <view class="flex font-24 color-fff justify-between align-center box">
    <!-- <view class="mark-ebf0f5">完工日期</view> -->
    <view class="shrink-1 time-content flex justify-start align-center color-999 font-24">
      <picker
        mode="date"
        :value="startDate"
        :start="startDatePicker"
        :end="endDatePicker"
        @change="bindDateChangeStart"
        class="shrink-1 date text-center color-333 rounded-24 mr-8 px-16 box"
        :class="bg"
      >
        {{ startDate }}
      </picker>
      ~
      <picker
        mode="date"
        :value="endDate"
        :start="startDatePicker"
        :end="endDatePicker"
        @change="bindDateChangeEnd"
        class="shrink-1 date text-center color-333 rounded-24 ml-8 px-16 box"
        :class="bg"
      >
        {{ endDate }}
      </picker>
    </view>
  </view>
</template>
<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import dayjs from 'dayjs'
const props = defineProps({
  start: {
    type: String,
    default: ''
  },
  end: {
    type: String,
    default: ''
  },
  bg: {
    type: String,
    default: 'bg-fff'
  }
})
const startDate = ref('')
const endDate = ref('')
watch(
  () => [props.start, props.end],
  (val) => {
    startDate.value = val[0]
    endDate.value = val[1]
  },
  { immediate: true, deep: true }
)
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
  if (endDate.value) {
    let before = dayjs(e.detail.value).isBefore(endDate.value)
    let same = dayjs(e.detail.value).isSame(dayjs(endDate.value))
    if (before || same) {
      startDate.value = e.detail.value
      emits('search', startDate.value, endDate.value)
    } else {
      uni.showToast({ title: '开始日期必须晚于结束日期', icon: 'none', duration: 2000 })
    }
  } else {
    startDate.value = e.detail.value
    emits('search', startDate.value, endDate.value)
  }
}
function bindDateChangeEnd(e) {
  if (startDate.value) {
    let after = dayjs(e.detail.value).isAfter(startDate.value)
    let same = dayjs(e.detail.value).isSame(dayjs(startDate.value))
    if (after || same) {
      endDate.value = e.detail.value
      emits('search', startDate.value, endDate.value)
    } else {
      uni.showToast({ title: '开始日期必须晚于结束日期', icon: 'none', duration: 2000 })
    }
  } else {
    endDate.value = e.detail.value
    emits('search', startDate.value, endDate.value)
  }
}
const emits = defineEmits(['search'])
</script>
<style lang="scss" scoped>
.time-content {
  .date {
    height: px2vw(44);
    line-height: px2vw(44);
  }
}
</style>
