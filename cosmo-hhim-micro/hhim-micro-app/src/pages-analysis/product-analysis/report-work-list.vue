<template>
  <view class="w-full h-full bg-f3f3f5 flex flex-col">
    <uni-nav-bar />
    <h-status-header title="生产分析" />
    <!-- 筛选输入框 -->
    <!--搜索区域-->
    <view class="box flex align-center pt-24 pl-32 pr-32">
      <h-search class="flex-1" placeholder="输入员工姓名" @searchInput="handleInput" />
    </view>
    <!-- 时间选择 -->
    <view class="px-16 box">
      <date-select ref="dateSelect" :showDay="true" @dateChange="dateChange" />
    </view>
    <view class="flex-1 flex flex-col rounded-16 mb-24 overflow-hidden">
      <view class="flex align-center pt-32 pb-16 box border-bottom-f5f5f5 bg-fff mx-16 bg-fff">
        <view class="block" />
        <view class="ml-16 color-5a6f82 font-28 bold">产出统计</view>
      </view>
      <scroll-view
        @refresherrefresh="onRefresh"
        @scrolltolower="onLoadMore"
        :refresher-triggered="triggered"
        refresher-enabled
        enable-flex
        scroll-y
        class="flex-1 overflow-hidden"
      >
        <!-- 产品列表 -->
        <view class="py-128 bg-fff mx-16" v-if="pageEmpty">
          <h-empty tipsWord="未查到产出记录" />
        </view>
        <template v-else>
          <view
            v-for="(dataItem, idx) in ProductList"
            :key="idx"
            class="px-32 box tap bg-fff mx-16"
            @tap="toDetail(dataItem)"
          >
            <view class="py-32 box relative" :class="{ 'list-item': idx !== ProductList.length - 1 }">
              <view class="flex align-center mid box">
                <view class="color-333 font-28 bold">
                  <h-text-display :text="dataItem.nickName" :width="200" />
                </view>
                <view class="color-999 font-28 flex-1 flex bold">
                  ( <h-text-display :text="dataItem.userName" :width="200" />)
                </view>
                <view class="flex align-center">
                  <view class="color-5a6f82 font-24 flex align-center">
                    记工
                    <text class="color-ff0000 font-28 ml-8 bold">{{ dataItem.totalSubmitNum }}</text>
                  </view>
                  <image src="/static/images/icon_extend.svg" class="icon-32 ml-16" />
                </view>
              </view>
            </view>
          </view>
          <uni-load-more :status="dataNoMore" />
        </template>
      </scroll-view>
    </view>
    <h-status-footer />
    <h-popup />
  </view>
</template>
<script setup lang="ts">
import { computed, onMounted, ref, reactive } from 'vue'
import { BigNumber } from 'bignumber.js'
import { _get } from '@/utils/common-request'
import { $state, $store, setGio } from '@/utils/common'
import HEmpty from '@/components/h-empty.vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import DateSelect from '@/pages-analysis/components/date-select.vue'
onLoad((e) => {
  startDate.value = e.startDate
  endDate.value = e.endDate
  dateType.value = e.dateType
})
const dateType = ref('')
// 输入框模组
const submitNickName = ref('')
function handleInput(str) {
  submitNickName.value = str
  getWorkerList()
}
function handleInputClear() {
  submitNickName.value = ''
  getWorkerList()
}

//刷新,加载
const pageEmpty = ref(false)
const dataNoMore = ref('')
const triggered = ref(false)
const pages = reactive({ pageSize: 10, pageNum: 1 })
const dataTotal = ref(0)
const dataEnd = computed(() => pages.pageNum * pages.pageSize >= dataTotal.value)
function onRefresh() {
  triggered.value = true
  pages.pageNum = 1
  getWorkerList()
}
function onLoadMore() {
  if (dataEnd.value) {
    return
  }
  pages.pageNum++
  getWorkerList()
}
//日期选择
const startDate = ref('')
const endDate = ref('')
function dateChange(start, end) {
  startDate.value = start
  endDate.value = end
  pages.pageNum = 1
  getWorkerList()
}
//获取产品数据
const ProductList = ref([])
function getWorkerList() {
  let mockParams = {
    url: '/analysis/showWorkerList',
    data: {
      submitNickName: submitNickName.value,
      startDate: startDate.value,
      endDate: endDate.value,
      ...pages
    }
  }

  dataNoMore.value = 'loading'
  _get(mockParams)
    .then((res) => {
      if (pages.pageNum === 1) {
        ProductList.value = []
      }
      ProductList.value.push(...res.rows)
      dataTotal.value = res?.total || 0
    })
    .finally(() => {
      triggered.value = false
      pageEmpty.value = !ProductList.value.length
      dataNoMore.value = dataEnd.value ? 'noMore' : 'more'
    })
}

const dateSelect = ref(null)
onMounted(() => {
  dateSelect.value.setDate(startDate.value, endDate.value, dateType.value)
  getWorkerList()
})
function toDetail(dataItem) {
  let userInfo = {
    nickName: dataItem.nickName,
    userName: dataItem.userName
  }
  uni.navigateTo({
    url:
      '/pages-analysis/product-analysis/report-detail?startDate=' +
      startDate.value +
      '&endDate=' +
      endDate.value +
      '&userInfo=' +
      JSON.stringify(userInfo)
  })
}
</script>
<style lang="scss" scoped>
.input-area {
  height: px2vw(64);
  background: #ebecee;
  box-shadow: 0 px2vw(1) 0 px2vw(1) rgba(255, 255, 255, 1), inset 0 px2vw(8) px2vw(12) px2vw(1) rgba(204, 209, 217, 1);
  border-radius: px2vw(16);
  padding: 0 px2vw(24) 0 px2vw(32);
}
.date-sel {
  height: px2vw(76);
}
.list-item {
  border-bottom: 1px dashed #e5e5e5;
}
.green-title:before {
  top: px2vw(2);
}
</style>
