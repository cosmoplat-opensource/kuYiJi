<template>
  <view class="calendar-month">
    <view class="display-date">{{ props.date }}</view>
    <view v-if="rowIndex > 1" class="calendar-month__wrapper">
      <view v-for="(row, rowI) in rowIndex" :key="`row-${rowI}`" class="flex align-center">
        <view
          v-for="(col, colI) in 7"
          :key="`col-${colI}`"
          class="flex-1 text-center flex flex-col align-center"
          @tap="handleDateSelect(row, col)"
        >
          <view class="item__wrapper" :class="formatDateColor(maps[`${row}-${col}`])">
            <image src="/static/images/icon_cursor.svg" class="icon-top" v-if="maps[`${row}-${col}`]?.max" />
            <text class="item__text" :class="{ 'color-0066ff': maps[`${row}-${col}`]?.label === '今天' }">{{
              maps[`${row}-${col}`]?.label
            }}</text>
            <image src="/static/images/icon_cursor.svg" class="icon-bot" v-if="maps[`${row}-${col}`]?.min" />
          </view>
          <view class="item__value-area h-24 mt-4">
            <view class="icon-12 bg-19AA8D rounded-full mr-4" v-if="maps[`${row}-${col}`]?.count" />
            <view
              class="item__value-area__text single w-m-60"
              :class="formatDateColor(maps[`${row}-${col}`])"
              v-if="maps[`${row}-${col}`]?.count"
              >{{ maps[`${row}-${col}`]?.count || '' }}</view
            >
            <view v-else>
              <view v-if="maps[`${row}-${col}`]?.before" class="border-1 border-b6c0c9 box icon-12 rounded-full"></view>
              <view v-else></view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import { computed, onMounted, ref } from 'vue'
import { _get } from '@/utils/common-request'

const props = defineProps({ date: { type: String, default: '' } })
// 指定月份总天数
const totalDays = dayjs(props.date).endOf('month').date()
// 指定月份第一天是周几
const start = dayjs(props.date).startOf('month').day()
// 日历集合
const maps = ref({})

function formatDateColor(item) {
  if (!item) return ''
  if (item.max) {
    return 'yellow'
  } else if (item.min) {
    return 'grey'
  } else {
    return item.before ? 'color-999999' : 'color-b8b8b8'
  }
}

// 行,列,日期格式优化
const rowIndex = ref(1)
let colIndex = start + 1
function dateInit() {
  for (let i = 1; i <= totalDays; i++) {
    maps.value[`${rowIndex.value}-${colIndex}`] = { label: i, count: 0 }
    if (colIndex === 7) {
      colIndex = 1
      rowIndex.value++
    } else {
      colIndex++
    }
  }
}

onMounted(() => {
  dateInit()
  getList()
})

const checkYearMonth = computed(() => {
  return dayjs().format('YYYY-MM')
})

function getList() {
  const nowDay = dayjs().date()
  _get({ url: '/submit/countByMonth', data: { monthOfYear: props.date } }).then((res) => {
    const data = res.data
    let max = 0
    let maxDate = ''
    let min = Number.MAX_SAFE_INTEGER
    let minDate = ''
    for (const k in data) {
      if (data[k] && data[k] > max) {
        max = data[k]
        maxDate = k
      }
      if (data[k] && data[k] < min) {
        min = data[k]
        minDate = k
      }
    }
    for (const key in maps.value) {
      const str = `${props.date}-${String(maps.value[key].label).padStart(2, '0')}`
      if (dayjs(str).isBefore(dayjs())) {
        maps.value[key].before = true
      }
      if (str === maxDate) {
        maps.value[key].max = true
      } else if (str === minDate) {
        maps.value[key].min = true
      }
      maps.value[key].count = parseInt(data[str] || 0)
      if (checkYearMonth.value === props.date && maps.value[key].label === nowDay) {
        maps.value[key].label = '今天'
      }
    }
  })
}

function handleDateSelect(row, col) {
  let str = ''
  if (maps.value[`${row}-${col}`].label === '今天') {
    str = dayjs().format('YYYY-MM-DD')
  } else {
    str = `${props.date}-${String(maps.value[`${row}-${col}`].label).padStart(2, '0')}`
  }
  if (dayjs(str).isBefore(dayjs())) {
    uni.$emit('topDateChange', str)
    uni.navigateBack()
  } else {
    uni.showToast({ title: '只能查看今天之前的记工记录', icon: 'none' })
  }
}
</script>

<style lang="scss">
.calendar-month {
  .display-date {
    padding: px2vw(24) 0;
    text-align: center;
    font-weight: bold;
    color: #000;
    font-size: px2vw(28);
    line-height: px2vw(28);
  }
  &__wrapper {
    background-color: #ffffff;
    border-radius: px2vw(16);
    padding: px2vw(16) px2vw(32);
  }
  .item {
    &__wrapper {
      position: relative;
      width: px2vw(64);
      height: px2vw(64);
      line-height: px2vw(64);
      border-radius: 50%;
      &:active {
        background-color: #0066ff;
        &.yellow {
          background-color: #0066ff;
        }
        &.grey {
          background-color: #0066ff;
        }
        .item__text {
          color: #fff;
        }
      }
      &.yellow {
        background-color: #f4bb5b;
        .item__text {
          color: #fff;
        }
      }
      &.blue {
        background-color: #0066ff;
      }
      &.grey {
        background-color: #b6c0c9;
        .item__text {
          color: #fff;
        }
      }
      .icon-top {
        width: px2vw(16);
        height: px2vw(16);
        position: absolute;
        top: 0;
        left: 50%;
        transform: translateX(-50%);
      }
      .icon-bot {
        width: px2vw(16);
        height: px2vw(16);
        position: absolute;
        top: px2vw(46);
        left: 50%;
        transform: translateX(-50%) rotate(180deg);
      }
    }
    &__text {
      font-size: px2vw(28);
      line-height: px2vw(28);
    }
    &__value-area {
      display: flex;
      align-items: center;
      &__text {
        color: #b8b8b8;
        font-size: px2vw(24);
        line-height: px2vw(24);
        &.yellow {
          color: #e7a11a;
        }
        &.grey {
          color: #5a6f82;
        }
      }
    }
  }
}
</style>
