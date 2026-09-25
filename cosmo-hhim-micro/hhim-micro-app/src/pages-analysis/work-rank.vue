<template>
  <view class="w-full h-full bg-f3f3f5 relative flex flex-col">
    <uni-nav-bar />
    <h-status-header title="记工排行" />
    <view class="pl-16 pr-16 box flex flex-col flex-1 overflow-hidden">
      <!-- 时间选择 -->
      <date-select ref="dateSelect" dateType="month" @dateChange="dateChange" />
      <!-- 排行列表 -->
      <view class="flex-1 flex flex-col overflow-hidden rounded-16-top">
        <view class="flex align-center pr-32 py-24 box bg-fff">
          <view class="block" />
          <view class="flex-1 color-5a6f82 font-28 bold ml-16">记工排行</view>
          <h-page-video :videoArr="[13]" page-name="记工排行" class="mr-24" />
          <btn-export @exportToMail="exportToMail" />
        </view>
        <view class="overflow-auto flex flex-col bg-fff pb-8 rounded-16-bottom box m-h-100">
          <!-- 大于3个显示1，2，3样式 -->
          <view v-if="RankList?.length >= 3" class="flex mx-32 justify-around align-end mt-40 pb-28 box">
            <view class="font-24 flex flex-col align-center">
              <view class="relative">
                <image
                  :src="RankList[1]?.avatar ? RankList[1].avatar : '/static/images/img_default.svg'"
                  class="icon-112 rounded-50 border-2"
                />
                <image class="rank2 icon-32" src="/static/images/icon_no2.png" />
              </view>
              <view class="mt-32 bold"><h-text-display :text="RankList[1]?.nickName" :width="160" /></view>
              <view class="mt-8 color-999"><h-text-display :text="RankList[1]?.userName" :width="160" /></view>
              <view class="mt-16 flex align-center bold">
                <image src="/static/images/icon_num_ee6258.svg" class="icon-32 mr-8" /><text>{{
                  RankList[1]?.submitNum
                }}</text>
              </view>
            </view>
            <view class="font-24 flex flex-col align-center">
              <view class="relative">
                <image class="crown" src="/static/images/icon_crown_48.svg" />
                <image
                  :src="RankList[0]?.avatar ? RankList[0].avatar : '/static/images/img_default.svg'"
                  class="icon-128 rounded-50 border-2"
                />
                <image class="rank2 rank1 icon-32" src="/static/images/icon_no1.png" />
              </view>
              <view class="mt-32 font-28 bold"><h-text-display :text="RankList[0]?.nickName" :width="160" /></view>
              <view class="mt-8 color-999"><h-text-display :text="RankList[0]?.userName" :width="160" /></view>
              <view class="mt-16 flex align-center bold">
                <image src="/static/images/icon_num_ee6258.svg" class="icon-32 mr-8" /><text>{{
                  RankList[0]?.submitNum
                }}</text>
              </view>
            </view>
            <view class="font-24 flex flex-col align-center">
              <view class="relative">
                <image
                  :src="RankList[2]?.avatar ? RankList[2].avatar : '/static/images/img_default.svg'"
                  class="icon-112 rounded-50 border-2"
                />
                <image class="rank2 icon-32" src="/static/images/icon_no3.png" />
              </view>
              <view class="mt-32 bold"><h-text-display :text="RankList[2]?.nickName" :width="160" /></view>
              <view class="mt-8 color-999"><h-text-display :text="RankList[2]?.userName" :width="160" /></view>
              <view class="mt-16 flex align-center bold">
                <image src="/static/images/icon_num_ee6258.svg" class="icon-32 mr-8" /><text>{{
                  RankList[2]?.submitNum
                }}</text>
              </view>
            </view>
          </view>
          <!-- 行列表样式 -->
          <view v-if="RankList?.length && RankList?.length != 3" class="mx-32 bg-fff box">
            <template v-if="RankList?.length > 3">
              <view
                v-for="(item, index) in RankList.slice(3, RankList.length)"
                :key="index"
                class="flex py-24 box align-center justify-between border-top-f5f5f5"
              >
                <view class="flex align-center">
                  <view class="font-24 bg-f5f5f5 color-999 icon-40 rounded-50 text-center line-h-40">
                    {{ index + 4 }}
                  </view>
                  <view class="font-28 ml-16 flex bold">
                    <h-text-display :text="item.nickName" :width="160" />
                    <view class="color-999 flex">( <h-text-display :text="item.userName" :width="160" />) </view>
                  </view>
                </view>
                <view class="flex font-24">
                  <image src="/static/images/icon_num_24a8ff.svg" class="icon-32 mr-8" />
                  <text class="bold">{{ item.submitNum }}</text>
                </view>
              </view>
            </template>
            <template v-else>
              <view
                v-for="(item, index) in RankList"
                :key="index"
                class="flex py-24 box align-center justify-between border-top-f5f5f5"
              >
                <view class="flex align-center">
                  <view class="font-24 bg-f5f5f5 color-999 icon-40 rounded-50 text-center line-h-40">
                    {{ index + 1 }}
                  </view>
                  <view class="font-28 ml-16 flex bold">
                    <h-text-display :text="item.nickName" :width="160" />
                    <view class="color-999 flex">( <h-text-display :text="item.userName" :width="160" />) </view>
                  </view>
                </view>
                <view class="flex font-24">
                  <image src="/static/images/icon_num_24a8ff.svg" class="icon-32 mr-8" />
                  <text>{{ item.submitNum }}</text>
                </view>
              </view>
            </template>
          </view>
        </view>
        <view class="font-24 color-b6c0c9 flex align-end justify-center py-48">已到底部</view>
      </view>
    </view>
    <!-- 导出 -->
    <ExportEmail ref="exportEmail" :exportLoading="exportLoading" @confirmPopup="confirmExport" />
    <h-status-footer />
    <h-popup />
  </view>
</template>
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { _get } from '@/utils/common-request'
import DateSelect from '@/pages-analysis/components/date-select.vue'
import { $state, setGio } from '@/utils/common'
import { onShow, onLoad } from '@dcloudio/uni-app'
import BtnExport from '@/components/btn-export.vue'
import ExportEmail from '@/components/export-email.vue'
const dateSelect = ref(null)
// 加载测试数据
const checkMock = computed(() => $state.mock.mockOpen)
let externalLoaded = false
// 问一问"查看图表"跳转参数：?params=<encodeURIComponent(JSON)>（兼容旧 startDate/endDate query）
onLoad((e: any) => {
  let p: any = {}
  try {
    p = e.params ? JSON.parse(decodeURIComponent(e.params)) : {}
  } catch (err) {}
  const start = p.startDate || e.startDate
  const end = p.endDate || e.endDate
  if (start && end) {
    externalLoaded = true
    startDate.value = start
    endDate.value = end
    getRankData()
  }
})
onMounted(() => {
  if (!externalLoaded) dateSelect.value.selectDataType('month')
})
onShow(() => {
  setGio('jr_jgph')
})
//日期选择
const startDate = ref('')
const endDate = ref('')
function dateChange(start, end) {
  startDate.value = start
  endDate.value = end
  getRankData()
}

//获取排行数据
const RankList = ref([])
function getRankData() {
  let mockParams = {
    url: '/analysis/submitRecordRank',
    data: {
      startDate: startDate.value,
      endDate: endDate.value
    }
  }
  if (checkMock.value) {
    Object.assign(mockParams, {
      url: '/analysis/loadDisplayData',
      data: { apiUrl: mockParams.url }
    })
  }
  _get(mockParams)
    .then((res: IResponseType<[]>) => {
      if (res.data) {
        RankList.value = res.data
      }
    })
    .finally(() => {
      triggered.value = false
    })
}
//导出
const exportEmail = ref(null)
function exportToMail() {
  exportEmail.value.openExportToMail({ startDate: startDate.value, endDate: endDate.value })
}
const exportLoading = ref(false)
function confirmExport(receivedBy) {
  uni.showLoading({
    title: '发送中'
  })
  setGio('jr_jgph_export')
  exportLoading.value = true
  _get({
    url:
      '/export/submitRecord?startDate=' + startDate.value + '&&endDate=' + endDate.value + '&&receivedBy=' + receivedBy
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

const triggered = ref(false)
function onRefresh() {
  triggered.value = true
  getRankData()
}
</script>
<style lang="scss" scoped>
.border-2 {
  border: px2vw(2) solid #e5e5e5;
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
  left: px2vw(42);
  bottom: px2vw(-12);
}
.rank1 {
  left: px2vw(50);
}
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
</style>
