<template>
  <view class="change-history-list bg-f3f3f5 flex flex-col h-full">
    <uni-nav-bar />
    <h-status-header title="变动历史" />
    <view class="h-88 bg-fff flex align-center mid mx-16 px-32 box rounded-16-top">
      <view class="color-333 font-32 bold">
        <h-text-display :text="dataItem.productName" :width="300" />
      </view>
      <view class="color-999 font-32 flex-1 overflow-hidden bold flex"
        >(<h-text-display :text="dataItem.productCode" />)</view
      >
      <view class="flex align-center">
        <word-icon class="mr-8" text="共" />
        <text class="color-5a6f82 font-28">{{ dataItem.num }}</text>
      </view>
    </view>
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
        <btn-export @exportToMail="exportToMail" />
      </view>
      <view class="h-16 bg-fff" />
      <!--数据列表滚动区域-->
      <scroll-view scroll-y enable-flex class="area-scroll overflow-auto border-box font-28 flex-1">
        <view class="flex w-100 relative pl-16 pr-32 box bg-ffffff">
          <view class="step-line"></view>
          <view class="flex-1 flex flex-col">
            <template v-for="(item, index) in historyList" :key="index">
              <view class="font-28 bold flex align-top mb-36 pl-40 relative">
                <image src="/static/images/icon_cursor_1.svg" class="icon-32 mr-8 day-tip" />
                <text class="mr-16 shrink-0 font-28">{{ item.date }}</text>
                <view class="flex flex-wrap"
                  >入库
                  <text class="color-ff0000">{{ item.inBoundNum }}</text>
                  , 出库
                  <text class="color-ff0000">{{ Math.abs(item.outBoundNum) }}</text>
                  , 库存结余
                  <text class="color-ff0000">{{ item.finishChangeNum }}</text></view
                >
              </view>
              <view v-for="(itemChild, itemIdx) in item.storageHistoryList" :key="itemIdx" class="mb-40">
                <view class="color-999 relative pl-40 flex align-center space-between">
                  <view class="font-28">{{ formatData(itemChild.changeTime) }}</view>
                  <view class="ml-16 overflow-hidden text flex-1">
                    <h-text-display :text="itemChild.changeUserNickName" />
                  </view>
                  <view v-if="itemChild.changeType === '10'" class="mark-ebf0f5"> 完工入库 </view>
                  <view v-if="itemChild.changeType === '20'" class="mark-ebf0f5"> 完工撤销 </view>
                  <view v-if="itemChild.changeType === '30'" class="mark-ebf0f5"> 库存变动 </view>
                  <view v-if="itemChild.changeType === '40'" class="mark-ebf0f5"> 成品出库 </view>
                  <view class="step-over icon-24 rounded-50"></view>
                </view>
                <!-- 良品 -->
                <view class="flex justify-between align-center mt-24 pl-40">
                  <view class="flex align-center">
                    <view
                      v-if="itemChild.changeNum > 0"
                      class="h-32 flex-center font-24 bg-F26A60 rounded-4 color-fff px-4"
                      >入库</view
                    >
                    <view v-else class="h-32 flex-center font-24 bg-19AA8D rounded-4 color-fff px-4">出库</view>
                    <view class="ml-8 font-32">{{ Math.abs(itemChild.changeNum) }}</view>
                  </view>
                  <view class="flex align-center">
                    <view class="mark-ebf0f5 mr-12">库存</view>
                    <view class="color-333">{{ itemChild.finishChangeNum }}</view>
                  </view>
                </view>

                <view v-if="itemChild.changeReason" class="color-5a6f82 font-28 pl-40 flex flex-col box">
                  <view class="line bg-f5f5f5 mt-24 mb-24"></view>
                  <view class="flex overflow-hidden">
                    <view>变动原因：</view>
                    <view class="flex-1 overflow-hidden">
                      <text>{{ itemChild.changeReason }}</text>
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
    <view class="flex justify-center w-100 pb-32 bg-f3f3f5 px-36 box">
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
  </view>
</template>
<script setup lang="ts">
import { onMounted, ref, computed, reactive } from 'vue'
import { BigNumber } from 'bignumber.js'
import { onLoad } from '@dcloudio/uni-app'
import { _get, _post } from '@/utils/common-request'
import dayjs from 'dayjs'
import EditStock from '@/pages-work-center/finish-product-storage/components/edit-stock.vue'
import ExportEmail from '@/components/export-email.vue'

import { Role_Staff } from '@/utils/common'
import BtnExport from '@/components/btn-export.vue'
const dataItem = ref({})
const historyList = ref([])
const startDate = ref('')
const endDate = ref('')
onLoad((e) => {
  dataItem.value = JSON.parse(e.item)

  getHistory()
})
const pages = reactive({ pageSize: 10, pageNum: 1 })

const startDatePicker = computed(() => {
  return getDate('start')
})
const endDatePicker = computed(() => {
  return getDate('end')
})

onMounted(() => {})

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
    url: '/storage/finish/history/list',
    data: {
      productSeq: dataItem.value.productSeq,
      startDate: startDate.value ? startDate.value + ' 00:00:00' : startDate.value,
      endDate: endDate.value ? endDate.value + ' 23:59:59' : endDate.value
    }
  }
  _get(mockParams).then((res) => {
    let rows = res.data ?? []
    historyList.value = rows
    //及时更新调整后的数量
    dataItem.value.num = historyList.value[0].finishChangeNum
  })
}

function formatData(createdDate) {
  return dayjs(createdDate).format('YYYY-MM-DD HH:mm:ss')
}
const currentItem = ref({})
const editStock = ref(null)
function edit() {
  let data = { ...dataItem.value }

  data.passToNum = data.num //	良品变化后数量

  currentItem.value = data
  editStock.value.openEdit()
}
function closePopup() {
  currentItem.value = {}
}
function confirmPopup(newItem) {
  dataItem.value.passNum = newItem.passToNum
  uni.$emit('storageFinishStatisticRefresh')
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
    startDate: startDate.value ? startDate.value : dayjs().subtract(3, 'month').format('YYYY-MM-DD'),
    endDate: endDate.value ? endDate.value : dayjs().format('YYYY-MM-DD'),
    receivedBy: receivedBy
  }
  _get({
    url: '/storage/finish/history/export',
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
    .catch(() => {
      exportLoading.value = false
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
.tips1 {
  // width: px2vw(104);
  height: px2vw(32);
  background: #def2ff;
  border: px2vw(2) solid #64b5ea;
}
.tips-special {
  width: px2vw(104);
  height: px2vw(32);
  background: #fff5e3;
  border: px2vw(2) solid #f4bb5b;
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
