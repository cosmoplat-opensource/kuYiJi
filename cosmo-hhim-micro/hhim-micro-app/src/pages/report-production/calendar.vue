<template>
  <view class="flex-1 bg-f3f3f5 overflow-hidden flex flex-col">
    <view class="w-full rounded-16 bg-fff flex-1">
      <view class="flex justify-between align-center mt-32 mb-24 px-32 box">
        <view class="mark-24a8ff">工作日历</view>
        <view class="font-28 flex align-center">
          <image
            @tap="minusMonth"
            src="/static/images/icon_input_arr.svg"
            class="icon-48"
            style="transform: rotate(90deg)"
          />
          <picker
            mode="date"
            fields="month"
            :value="queryDate"
            :start="startDatePicker"
            :end="endDatePicker"
            @change="bindDateChange"
            class="mx-8 bold"
          >
            <text>{{ showDate }}</text>
          </picker>
          <image
            @tap="addMonth"
            src="/static/images/icon_input_arr.svg"
            class="icon-48"
            style="transform: rotate(-90deg)"
          />
        </view>
      </view>
      <view class="flex h-136 font-24 px-16 box">
        <view class="flex-1 flex flex-col align-center justify-center tap" @tap="changeType('00')">
          <text class="font-32 bold">{{ showInfo.productSum }}</text>
          <text class="mt-16">产品种类</text>
          <view class="tab-line mt-16"></view>
        </view>
        <view class="flex-1 flex flex-col align-center justify-center tap" @tap="changeType('10')">
          <text class="font-32 bold">{{ showInfo.workSubmitSum }}</text>
          <text class="mt-16">记工总数</text>
          <view :class="['tab-line mt-16', statisticType === '10' ? 'bg-0066ff' : '']"></view>
        </view>
        <view class="flex-1 flex flex-col align-center justify-center tap" @tap="changeType('20')">
          <text class="font-32 color-ff0000 bold">{{ showInfo.waitCheckSum }}</text>
          <text class="mt-16">待审核</text>
          <view :class="['tab-line mt-16', statisticType === '20' ? 'bg-0066ff' : '']"></view>
        </view>
        <view
          v-if="showInfo.settledNum"
          class="flex-1 flex flex-col align-center justify-center tap"
          @tap="changeType('30')"
        >
          <text class="font-32 bold">{{ showInfo.settledNum }}</text>
          <text class="mt-16">已结算</text>
          <view :class="['tab-line mt-16']"></view>
        </view>
      </view>
      <view class="area-week">
        <text v-for="(item, index) in weekArr" :key="index" class="area-week__label">{{ item }}</text>
      </view>
      <calendar-month :date="queryDate" :statisticType="statisticType" :calendarDetail="showCalendarDetail" />
      <view class="w-100 px-32 box">
        <view class="line w-100 px-32 box"></view>
        <view class="flex justify-between my-24">
          <view class="mark-ebf0f5">出勤天数</view><view class="font-28">{{ showInfo.workDays }} 天</view>
        </view>
        <template v-if="showInfo.openNum">
          <view class="line w-100 px-32 box"></view>
          <view @tap="showPopup" class="flex justify-between my-24">
            <view class="mark-ebf0f5">记工待结算</view>
            <view class="font-28 flex align-center color-ff0000">
              {{ showInfo.openNum }}
              <image src="/static/images/icon_detail.svg" class="icon-32 ml-24" />
            </view>
          </view>
        </template>
        <view class="line w-100 px-32 box"></view>
        <view class="flex justify-between my-24">
          <view class="mark-ebf0f5">平均日产能</view>
          <view class="font-28 color-ff0000">{{ showInfo.averageCapacityOfDay }}</view>
        </view>
        <view class="line w-100 px-32 box"></view>
        <view class="flex justify-between mt-24 mb-32">
          <view class="mark-ebf0f5">平均良品率</view>
          <view class="font-28 color-19aa8d">
            {{ new bigNumber(showInfo.averageGoodProductRatioOfDay).multipliedBy(100).toNumber() }}%
          </view>
        </view>
      </view>
    </view>
    <settle-list-popup ref="popupSettled" :searchDate="queryDate" />
  </view>
</template>

<script setup lang="ts">
import CalendarMonth from './calendar-month.vue'
import { onMounted, ref, computed } from 'vue'
import { _get } from '@/utils/common-request'
import dayjs from 'dayjs'
import bigNumber from 'bignumber.js'
import SettleListPopup from '@/pages/report-production/components/settle-list-popup.vue'
const startDatePicker = computed(() => {
  return getDate('start')
})
const endDatePicker = computed(() => {
  return getDate('end')
})
const showDate = computed(() => {
  let date = queryDate.value.split('-')
  return date[1] + '月 ' + date[0]
})
const weekArr = ['日', '一', '二', '三', '四', '五', '六']
const monthArr = ref([])

const queryDate = ref(dayjs().startOf('month').format('YYYY-MM'))
function bindDateChange(e) {
  queryDate.value = e.detail.value
  getAll()
}
function getAll() {
  getInfo()
  getCalendarDetail()
}
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
  return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
}
const showInfo = ref<Partial<TShowInfo>>({})
const showCalendarDetail = ref([])
function getInfo() {
  _get({ url: '/staff/calendar/statistic', data: { queryDate: queryDate.value } }).then((res: IResponseType<{}>) => {
    showInfo.value = res.data
  })
}
function getCalendarDetail() {
  showCalendarDetail.value = []
  _get({
    url: '/staff/calendar/calendarDetail',
    data: { queryDate: queryDate.value, statisticType: statisticType.value }
  }).then((res: IResponseType<[]>) => {
    showCalendarDetail.value = res.data
  })
}
function minusMonth() {
  queryDate.value = dayjs(queryDate.value).subtract(1, 'month').format('YYYY-MM')
  getAll()
}
function addMonth() {
  queryDate.value = dayjs(queryDate.value).add(1, 'month').format('YYYY-MM')
  getAll()
}
// 切换
const statisticType = ref('10')
function changeType(type) {
  if (type === '00') {
    uni.navigateTo({
      url: '/pages-report/my-product?queryDate=' + queryDate.value
    })
  } else if (type === '30') {
    //展示已结算清单
    settledType.value = 0
    popupSettled.value.open(settledType.value)
  } else {
    statisticType.value = type
    getCalendarDetail()
  }
}
function showPopup() {
  //展示待结算清单
  settledType.value = 1
  popupSettled.value.open(settledType.value)
}

//结算清单弹窗
const popupSettled = ref(null)
const settledType = ref(0) //0 已结算 1 待结算

onMounted(() => {
  getInfo()
  if (statisticType.value !== '00') {
    getCalendarDetail()
  }

  // let index = 0
  // while (1 === 1) {
  //   const item = dayjs().subtract(index, 'month').format('YYYY-MM')
  //   monthArr.value.push(item)
  //   if (item === '2022-10') {
  //     return
  //   }
  //   index++
  // }
})
</script>

<style lang="scss" scoped>
.tab-line {
  width: px2vw(32);
  height: px2vw(8);
  border-radius: px2vw(4);
}

.line {
  height: px2vw(2);
  background: #f5f5f5;
}

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
.number-mark {
  border-radius: 0 px2vw(4) px2vw(4) 0;
  height: px2vw(32);
  line-height: px2vw(32);
  position: absolute;
  z-index: 2;
  left: px2vw(-48);
  top: px2vw(4);
}
</style>
