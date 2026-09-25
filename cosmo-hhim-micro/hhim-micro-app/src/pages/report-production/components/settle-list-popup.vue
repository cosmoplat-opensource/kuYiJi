<template>
  <uni-popup ref="popup" type="bottom" :safe-area="false">
    <view class="bg-ffffff rounded-16-top h-800 flex flex-col">
      <view class="h-96 w-100 flex align-center relative">
        <text class="color-5a6f82 font-28 flex-1 text-center bold"
          >{{ settledType === 0 ? '已结算' : '待结算' }}清单</text
        >
        <image @click="closePopup" src="/static/images/icon_close_666.svg" class="icon-48 close" />
      </view>
      <view
        v-if="settledList?.totalSettledNum || settledList?.totalAdjustedNum"
        class="bg-f7f8e7 h-56 px-48 box flex justify-between align-center font-24 color-5a6f82"
      >
        <view v-if="settledType === 0">
          月份：
          <text class="color-333 bold">{{ dayjs(props.searchDate).startOf('month').format('YYYY-MM') }}</text>
        </view>
        <view v-if="settledList?.totalSettledNum || settledList?.totalAdjustedNum">
          {{ settledType === 0 ? '已' : '待' }}结算
          <text v-if="settledType === 0" class="color-ff0000 bold">
            {{ String(settledList?.totalSettledNum).replace(/^(.*\..{4}).*$/, '$1') }}
          </text>
          <text v-else class="color-ff0000 bold">
            {{ String(settledList?.totalAdjustedNum).replace(/^(.*\..{4}).*$/, '$1') }}
          </text>
        </view>
      </view>
      <scroll-view scroll-y class="flex-1 overflow-hidden">
        <view v-for="(itemDetail, detailIdx) in settledList?.detailList" :key="detailIdx" class="box px-48">
          <view
            class="pb-24 pt-32 box"
            :class="detailIdx < settledList.detailList.length - 1 ? 'border-bottom-dashed pb-24' : 'pb-32'"
          >
            <view class="flex align-center relative box">
              <h-text-display class="bold color-333 font-28" :text="itemDetail.productName" :width="200" />
              <view class="font-28 color-999 bold">({{ itemDetail.productCode }})</view>
            </view>
            <view class="flex align-center justify-between mt-24 box">
              <h-text-display :text="itemDetail.operateProcessName" :width="256" class="color-5a6f82 font-28" />
              <view class="flex align-center">
                <view class="mark-ebf0f5 ml-32">数量</view>
                <view class="color-333 font-28 bold ml-12">
                  {{ settledType === 0 ? itemDetail.settledNum : itemDetail.adjustedNum }}
                  {{ itemDetail.productUnit }}
                </view>
              </view>
            </view>
          </view>
        </view>
        <template v-if="historyList.length > 0">
          <view class="border-top-f5f5f5 pb-8 pt-32 px-48">
            <view class="flex">
              <view class="mark-00bfa5">调整记录</view>
            </view>
            <view
              v-for="(itemHistory, historyIdx) in historyList"
              :key="historyIdx"
              class="pb-24 pt-32 box"
              :class="{ 'border-bottom-dashed': historyIdx < historyList.length - 1 }"
            >
              <view class="flex align-center relative box">
                <h-text-display class="bold color-333 font-28" :text="itemHistory.productName" :width="200" />
                <view class="font-28 color-999 bold">({{ itemHistory.productCode }})</view>
              </view>
              <view class="flex align-center mt-24 box">
                <h-text-display :text="itemHistory.operateProcessName" :width="256" class="color-5a6f82 font-28" />
                <view class="flex align-center">
                  <view class="mark-fe9f00 ml-32">减少</view>
                  <view class="color-333 font-28 bold ml-12">
                    {{ -itemHistory.adjustedNum }}{{ itemHistory.productUnit }}
                  </view>
                </view>
              </view>
              <view v-if="itemHistory.remark" class="color-5a6f82 font-28 flex flex-col box mb-24">
                <view class="w-100 border-bottom-dashed mt-24 mb-24" style="height: 0"></view>
                <view class="flex overflow-hidden">
                  <view>调整说明：</view>
                  <view class="flex-1 overflow-hidden">
                    <text>{{ itemHistory.remark }}</text>
                  </view>
                </view>
              </view>
              <view v-if="itemHistory.remark" class="line bg-f5f5f5"></view>
              <view class="flex align-center mt-24"
                ><view class="mark-ebf0f5 mr-16">操作员</view>
                <view class="color-333 font-28">{{
                  itemHistory.createdByName + '  ' + itemHistory.createdDate
                }}</view></view
              >
            </view>
          </view>
        </template>
        <view class="color-b6c0c9 font-24 py-48 w-100 text-center border-top-f5f5f5">已到底部</view>
      </scroll-view>
    </view>
  </uni-popup>
</template>

<script setup>
import { $store } from '@/utils/common'
import { ref } from 'vue'
import dayjs from 'dayjs'
import { _get } from '@/utils/common-request'
const props = defineProps({
  searchDate: {
    type: String,
    default: ''
  }
})
const settledType = ref(-1)
function open(type) {
  settledType.value = type
  settledList.value = []
  if (settledType.value === 1) {
    //展示待结算清单
    _get({
      url: '/settlement/open/user',
      data: {
        userId: $store.state.user.userInfo.id
      }
    }).then((res) => {
      settledList.value = res?.rows[0]
      openPopup()
    })
  } else {
    //展示已结算清单
    getDetail()
    _get({
      url: '/settlement/settled/user',
      data: {
        searchDate: dayjs(props.searchDate).startOf('month').format('YYYY-MM-DD'),
        userId: $store.state.user.userInfo.id
      }
    }).then((res) => {
      settledList.value = res?.rows[0]
      openPopup()
    })
  }
}
function getDetail() {
  _get({
    url: `/settlement/history/detail`,
    data: {
      userId: $store.state.user.userInfo.id,
      searchDate: dayjs(props.searchDate).startOf('month').format('YYYY-MM-DD')
    }
  }).then((res) => {
    historyList.value = res.rows
  })
}
//结算清单弹窗
const popup = ref(null)
const settledList = ref({})
const historyList = ref([])
function openPopup() {
  popup.value.open('bottom')
}
function closePopup() {
  popup.value.close('bottom')
}
defineExpose({ open })
</script>

<style lang="scss">
.number-mark {
  border-radius: 0 px2vw(4) px2vw(4) 0;
  height: px2vw(32);
  line-height: px2vw(32);
  position: absolute;
  z-index: 2;
  left: px2vw(-48);
  top: px2vw(4);
}
.line {
  height: px2vw(2);
  background: #f5f5f5;
}
.close {
  position: absolute;
  top: px2vw(24);
  right: px2vw(24);
}
</style>
