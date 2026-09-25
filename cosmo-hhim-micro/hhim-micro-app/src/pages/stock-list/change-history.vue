<template>
  <view class="change-history-list bg-f3f3f5 flex flex-col h-full">
    <uni-nav-bar />
    <h-status-header title="变动历史" />
    <list-item
      class="area-scroll__list-item mx-16"
      :dataItem="dataItem"
      :showEdit="false"
      :showExplainIcon="true"
      @explainPopup="explainPopup"
    />
    <view class="main-content bg-f3f3f5 mx-16 mt-16 flex flex-col flex-1 overflow-hidden pb-32 box">
      <view class="time-content flex align-center justify-between px-32 box w-100 color-333 font-24 bg-ffffff">
        <view class="flex align-center">
          <picker
            mode="date"
            :value="startDate"
            :start="startDatePicker"
            :end="endDatePicker"
            @change="bindDateChangeStart"
            class="date bg-f3f3f5 rounded-24 mr-8 pl-16 pr-16"
          >
            <text v-if="startDate">{{ startDate }}</text>
            <text v-else class="color-999">起始日期</text>
          </picker>
          ~
          <picker
            mode="date"
            :value="endDate"
            :start="startDatePicker"
            :end="endDatePicker"
            @change="bindDateChangeEnd"
            class="date bg-f3f3f5 rounded-24 ml-8 pl-16 pr-16"
          >
            <text v-if="endDate">{{ endDate }}</text>
            <text v-else class="color-999">结束日期</text>
          </picker>
        </view>
        <btn-export v-if="!Role_Staff" @exportToMail="exportToMail" />
      </view>
      <view class="h-16 bg-fff" />
      <!--数据列表滚动区域-->
      <scroll-view scroll-y enable-flex class="area-scroll overflow-auto border-box font-28 flex-1">
        <view class="flex w-100 relative pl-16 pr-32 box bg-ffffff">
          <view class="step-line"></view>
          <view class="flex-1 flex flex-col">
            <template v-for="(item, index) in historyList" :key="index">
              <view class="font-28 bold flex align-center mb-36 pl-40 relative">
                <image src="/static/images/icon_cursor_1.svg" class="icon-32 mr-8 day-tip" />
                <text class="mr-16 font-28">{{ item.createdDate }}</text>
                <text>良品</text>
                <text class="color-ff0000">{{ item.passNum }}</text>
                <text>{{ formatStr(item.productUnit, 5) }}，不良品</text>
                <text class="color-ff0000">{{ item.ngNum }}</text>
                <text>{{ formatStr(item.productUnit, 5) }}</text>
              </view>
              <!-- :class="index == historyList.length - 1 ? '' : 'mb-40'" -->
              <view v-for="(itemChild, itemIdx) in item.changeList" :key="itemIdx" class="mb-40">
                <view class="color-999 relative pl-40 flex space-between align-center">
                  <view class="font-28">{{ formatData(itemChild.createdDate) }}</view>
                  <view class="ml-16 overflow-hidden text flex-1">
                    <h-text-display :text="itemChild.createdBy" />
                  </view>
                  <view class="mark-ebf0f5">{{ itemChild.operateNode }}</view>
                  <view class="step-over icon-24 rounded-50"></view>
                </view>
                <!-- 良品 -->
                <view
                  v-if="calNum(itemChild.passFromNum, itemChild.passToNum) > 0"
                  class="flex justify-between align-center mt-24 pl-40"
                >
                  <view class="flex align-center">
                    <view class="w-90 flex">
                      <view class="font-24 bg-19AA8D color-fff rounded-50 icon-32 text-center line-32">良</view>
                    </view>
                    <view v-if="calNumchange(itemChild.passFromNum, itemChild.passToNum)" class="mark-f26a60">
                      增加
                    </view>
                    <view v-if="!calNumchange(itemChild.passFromNum, itemChild.passToNum)" class="mark-fe9f00">
                      减少
                    </view>
                    <view class="ml-8 font-32">
                      {{ calNum(itemChild.passFromNum, itemChild.passToNum) }}
                      {{ formatStr(itemChild.productUnit, 5) }}</view
                    >
                  </view>
                  <view class="flex align-center">
                    <view class="mark-ebf0f5 ml-52 mr-12">库存</view>
                    <view class="color-333">{{ itemChild.passToNum }}{{ formatStr(itemChild.productUnit, 5) }}</view>
                  </view>
                </view>
                <!-- 不良品 -->
                <view
                  v-if="calNum(itemChild.ngFromNum, itemChild.ngToNum) > 0"
                  class="flex justify-between align-center mt-24 pl-40"
                >
                  <view class="flex align-center">
                    <view class="w-90 flex">
                      <view class="mark-ebf0f5 mr-24">不良</view>
                    </view>
                    <view v-if="calNumchange(itemChild.ngFromNum, itemChild.ngToNum)" class="mark-f26a60">增加</view>
                    <view v-if="!calNumchange(itemChild.ngFromNum, itemChild.ngToNum)" class="mark-fe9f00">扣减</view>
                    <view class="ml-8 font-32">
                      {{ calNum(itemChild.ngFromNum, itemChild.ngToNum) }}
                      {{ formatStr(itemChild.productUnit, 5) }}
                    </view>
                  </view>
                  <view class="flex align-center">
                    <view class="mark-ebf0f5 mr-12">库存</view>
                    <view class="color-333">{{ itemChild.ngToNum }}{{ formatStr(itemChild.productUnit, 5) }}</view>
                  </view>
                </view>
                <view v-if="itemChild.remark" class="color-5a6f82 font-28 pl-40 flex flex-col box">
                  <view class="line bg-f5f5f5 mt-24 mb-24"></view>
                  <view class="flex overflow-hidden">
                    <view>变动原因：</view>
                    <view class="flex-1 overflow-hidden">
                      <text>{{ itemChild.remark }}</text>
                    </view>
                  </view>
                </view>
              </view>
            </template>
          </view>
        </view>
        <view class="w-100 h-72 bg-f3f3f5 font-24 color-b6c0c9 flex align-end justify-center">已到底部</view>
      </scroll-view>
    </view>
    <view class="flex justify-center w-100 pb-32 bg-f3f3f5 px-36 box" v-if="!Role_Staff">
      <h-button width="624" height="72" text="编辑库存信息" @tap.stop="edit" />
    </view>
    <edit-stock
      ref="editStock"
      :currentItem="currentItem"
      @closePopup="closePopup"
      @confirmPopup="confirmPopup"
      @changeData="changeData"
    />
    <ExportEmail ref="exportEmail" :exportLoading="exportLoading" @confirmPopup="confirmExport" />
    <h-status-footer />
    <h-popup />
    <!--w10-库存异常弹窗-->
    <h-popup-dialog ref="refPopupDialog" :showFooter="false" :showHeader="false">
      <image @click="closePopupDialog" src="/static/images/icon_close_666.svg" class="icon-48 absolute r-8 t-8" />
      <view class="m-h-800 flex flex-col bg-fff">
        <view class="flex-1 overflow-auto">
          <view class="fit-content h-32 flex-center px-4 font-24 color-fff bg-FE9F00 rounded-4">库存异常分析</view>
          <view v-if="errorValue.message" class="mt-24 color-5a6f82 line-height-48 font-28">
            1. {{ errorValue.message }};
          </view>
          <view class="color-5a6f82 line-height-48 font-28">
            {{ errorValue.message ? '2. ' : '' }}最近发现异常的日期为
            <text class="color-333 bold">{{ errorValue.occurredDate }}</text
            >。</view
          >
          <view class="mt-32 fit-content h-32 flex-center px-4 font-24 color-fff bg-19AA8D rounded-4 font-24"
            >建议</view
          >
          <view class="mt-24 color-333 line-height-48 font-28"> 1. 盘点该产品实物库存，并编辑库存修正数据; </view>
          <view class="color-333 line-height-48 font-28">
            2. 以产品维度进行审产，注意预警，保障日常审产的准确性。
          </view>
        </view>
        <view class="flex-center mt-40">
          <h-button width="174" height="64" text="我知道了" @tap.stop="closePopupDialog" />
        </view>
      </view>
    </h-popup-dialog>
  </view>
</template>
<script setup lang="ts">
import { onMounted, ref, computed, reactive } from 'vue'
import ListItem from '@/pages/stock-list/components/list-item.vue'
import { BigNumber } from 'bignumber.js'
import { onLoad } from '@dcloudio/uni-app'
import { _get } from '@/utils/common-request'
import { $state, formatStr, Role_Staff } from '@/utils/common'
import dayjs from 'dayjs'
import EditStock from '@/pages/stock-list/components/edit-stock.vue'
import ExportEmail from '@/components/export-email.vue'
import HPopupDialog from '@/components/h-popup-dialog.vue'

import BtnExport from '@/components/btn-export.vue'
const dataItem = ref({})
const historyList = ref([])
const startDate = ref('')
const endDate = ref('')
const isAnalysis = ref('')
// 加载测试数据
const checkMock = computed(() => $state.mock.mockOpen)
onLoad((e) => {
  dataItem.value = JSON.parse(e.item)
  isAnalysis.value = e.type
  getStockList()
  getHistory()
})
const pages = reactive({ pageSize: 10, pageNum: 1 })
function getStockList() {
  _get({
    url: '/storage/list',
    data: { productSeq: dataItem.value.productSeq, processSeq: dataItem.value.processSeq, ...pages }
  })
    .then((res) => {
      dataItem.value = res.rows[0]
    })
    .finally(() => {})
}
const startDatePicker = computed(() => {
  return getDate('start')
})
const endDatePicker = computed(() => {
  return getDate('end')
})

/** 异常原因弹窗模块 */
const refPopupDialog = ref(null)
const errorValue = ref({})
function closePopupDialog() {
  refPopupDialog.value.close()
}
function explainPopup() {
  uni.showLoading({
    title: '加载中'
  })
  let model = {
    productSeq: dataItem.value.productSeq,
    processSeq: dataItem.value.processSeq,
    startDate: startDate.value,
    endDate: endDate.value
  }
  _get({
    url: '/storage/negativeStockAnalysis',
    data: model
  })
    .then((res) => {
      errorValue.value = res.data
      refPopupDialog.value.open()
    })
    .finally(() => {
      uni.hideLoading()
    })
}

onMounted(() => {
  if (isAnalysis.value && isAnalysis.value === 'error') {
    explainPopup()
  }
})
function calNum(from, to) {
  let num = 0
  num = new BigNumber(from).minus(to).toNumber()
  num = Math.abs(num)
  return num
}
function calNumchange(from, to) {
  let num = new BigNumber(to).minus(from).toNumber()
  if (num > 0) {
    return true
  }
  if (num < 0) {
    return false
  }
  return
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
  month = month > 9 ? month : '0' + month
  day = day > 9 ? day : '0' + day
  return `${year}-${month}-${day}`
}
function bindDateChangeStart(e) {
  if (endDate.value) {
    let before = dayjs(e.detail.value).isBefore(endDate.value)
    let same = dayjs(e.detail.value).isSame(dayjs(endDate.value))
    if (before || same) {
      startDate.value = e.detail.value
      getHistory()
    } else {
      uni.showToast({ title: '开始日期必须晚于结束日期', icon: 'none', duration: 2000 })
    }
  } else {
    startDate.value = e.detail.value
    getHistory()
  }
}
function bindDateChangeEnd(e) {
  if (startDate.value) {
    let after = dayjs(e.detail.value).isAfter(startDate.value)
    let same = dayjs(e.detail.value).isSame(dayjs(startDate.value))
    if (after || same) {
      endDate.value = e.detail.value
      getHistory()
    } else {
      uni.showToast({ title: '开始日期必须晚于结束日期', icon: 'none', duration: 2000 })
    }
  } else {
    endDate.value = e.detail.value
    getHistory()
  }
}
function getHistory() {
  let mockParams = {
    url: '/storage/history',
    data: {
      processSeq: dataItem.value.processSeq,
      productSeq: dataItem.value.productSeq,
      startDate: startDate.value ? startDate.value + ' 00:00:00' : startDate.value,
      endDate: endDate.value ? endDate.value + ' 23:59:59' : endDate.value
    }
  }
  if (checkMock.value) {
    Object.assign(mockParams, {
      url: '/analysis/loadDisplayData',
      data: { apiUrl: mockParams.url }
    })
  }
  _get(mockParams).then((res) => {
    let rows = res.rows ?? []
    if (checkMock.value) {
      rows = res.data ?? []
    }
    let newList = []
    rows.forEach((item) => {
      let has = false
      let idx = 0
      for (let i in newList) {
        if (newList[i].createdDate === formatDate2(item.createdDate)) {
          has = true
          idx = i
          break
        }
      }
      if (has) {
        newList[idx].changeList.push(item)
      } else {
        let data = {
          createdDate: formatDate2(item.createdDate),
          passNum: item.passToNum,
          ngNum: item.ngToNum,
          productUnit: item.productUnit,
          changeList: [item]
        }
        newList.push(data)
      }
    })
    historyList.value = newList
  })
}
function formatDate2(createdDate) {
  var d = new Date(createdDate)
  var datetime = d.getFullYear() + '-' + timePlus0(d.getMonth() + 1) + '-' + timePlus0(d.getDate())
  return datetime
}
function formatData(createdDate) {
  var d = new Date(createdDate)
  var datetime =
    d.getFullYear() +
    '-' +
    timePlus0(d.getMonth() + 1) +
    '-' +
    timePlus0(d.getDate()) +
    ' ' +
    timePlus0(d.getHours()) +
    ':' +
    timePlus0(d.getMinutes()) +
    ':' +
    timePlus0(d.getSeconds())
  return datetime
}
function timePlus0(time) {
  if (time > 9) {
    return time
  } else {
    return '0' + time
  }
}
const currentItem = ref({})
const editStock = ref(null)
function edit() {
  let data = { ...dataItem.value }
  data.ngFromNum = data.ngNum //不良品变化前数量(赋值原库存不良品数量)
  data.ngToNum = data.ngNum //不良品变化后数量
  data.passFromNum = data.passNum //	良品变化前数量(赋值原库存良品数量)
  data.passToNum = data.passNum //	良品变化后数量
  data.totalNumPre = new BigNumber(data.ngNum).plus(data.passNum).toNumber()
  data.totalNum = new BigNumber(data.ngNum).plus(data.passNum).toNumber()
  delete data.ngNum
  delete data.passNum
  currentItem.value = data
  editStock.value.openEdit()
}
function closePopup() {
  currentItem.value = {}
}
function confirmPopup(newItem) {
  dataItem.value.passNum = newItem.passToNum
  dataItem.value.ngNum = newItem.ngToNum
  getHistory()
}
function changeData(data, value) {
  currentItem.value[data] = value
}
//导出
const exportEmail = ref(null)
function exportToMail() {
  let start = ''
  let end = ''
  if (!startDate.value && !endDate.value) {
    end = dayjs().format('YYYY-MM-DD')
    start = dayjs().subtract(3, 'month').format('YYYY-MM-DD')
  } else if (!startDate.value) {
    end = endDate.value
    start = dayjs(end).subtract(3, 'month').format('YYYY-MM-DD')
  } else if (!endDate.value) {
    start = startDate.value
    end = dayjs(start).add(3, 'month').format('YYYY-MM-DD')
  } else {
    start = startDate.value
    let end3 = dayjs(start).add(3, 'month')
    if (dayjs(endDate.value).isSame(end3) || dayjs(endDate.value).isBefore(end3)) {
      end = endDate.value
    } else {
      uni.showToast({
        title: '只能导出三个月内的记录哦',
        duration: 2000,
        icon: 'none'
      })
      return
    }
  }

  exportEmail.value.openExportToMail({ startDate: start, endDate: end })
}
const exportLoading = ref(false)
function confirmExport(receivedBy) {
  uni.showLoading({
    title: '发送中'
  })
  exportLoading.value = true
  let model = {
    productSeq: dataItem.value.productSeq,
    processSeq: dataItem.value.processSeq,
    startDate: startDate.value,
    endDate: endDate.value,
    receivedBy: receivedBy
  }
  _get({
    url: '/export/stockChangeRecord',
    data: model
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
.text {
  width: px2vw(160);
}
.change-history-list {
  height: 100vh;
}
.main-content {
}
.time-content {
  height: px2vw(100);
  .date {
    height: px2vw(44);
    line-height: px2vw(44);
  }
}
.step-current {
  background: #ffffff;
  border: px2vw(2) solid #0066ff;
  position: absolute;
  left: 0;
  top: px2vw(4);
}
.step-over {
  background: #f5f5f5;
  border: px2vw(2) solid #e5e5e5;
  position: absolute;
  left: 0;
  top: px2vw(2);
}
.day-tip {
  position: absolute;
  left: 0;
  top: px2vw(-2);
}
.line {
  height: px2vw(1);
}
.step-line {
  width: px2vw(2);
  height: 100%;
  background: #f5f5f5;
  position: absolute;
  top: 0;
  left: px2vw(30);
  z-index: 0;
}
.tips2 {
  border: px2vw(2) solid #d3dfeb;
}
.area-scroll {
}
</style>
