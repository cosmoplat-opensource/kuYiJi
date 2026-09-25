<!--选择日期-->
<template>
  <view class="report-production__calendar-select flex flex-col overflow-hidden">
    <uni-nav-bar />
    <h-status-header title="选择日期" />
    <view class="area-week">
      <text v-for="(item, index) in weekArr" :key="index" class="area-week__label">{{ item }}</text>
    </view>
    <scroll-view scroll-y enable-flex class="area-calendar flex-1 overflow-hidden">
      <calendar-month v-for="(item, index) in monthArr" :date="item" :key="index" />
    </scroll-view>
    <h-status-footer />
  </view>
</template>

<script setup lang="ts">
import CalendarMonth from '../calendar-month.vue'
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'

const weekArr = ['日', '一', '二', '三', '四', '五', '六']
const monthArr = ref([])

onMounted(() => {
  let index = 0
  while (1 === 1) {
    const item = dayjs().subtract(index, 'month').format('YYYY-MM')
    monthArr.value.push(item)
    if (item === '2022-10') {
      return
    }
    index++
  }
})
</script>

<style lang="scss">
.report-production__calendar-select {
  background-color: #f3f3f5;
  height: 100vh;

  .area-week {
    background: #ffffff;
    border-radius: px2vw(16);
    padding: px2vw(32);
    display: flex;
    align-items: center;

    &__label {
      flex: 1;
      color: #999;
      font-size: px2vw(28);
      text-align: center;

      + .area-week__label {
        margin-left: px2vw(16);
      }
    }
  }
}
</style>
