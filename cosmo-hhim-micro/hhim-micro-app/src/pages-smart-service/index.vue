<template>
  <view class="w-full h-full bg-f3f3f5 flex flex-col overflow-hidden">
    <uni-nav-bar />
    <h-status-icon-header title="海云客服" :iconOptions="{ icon: 'icon_robot', iconType: 'png' }" />
    <view class="px-16 pt-16 pb-8">
      <view class="h-80 px-24 bg-fff rounded-16 flex justify-between align-center">
        <view class="flex align-center" @tap="phoneCall">
          <view class="font-28 color-333">400-135-7277</view>
          <image :src="formatImage('icon_phone', 'svg')" class="icon-40 ml-16" />
        </view>
        <view class="font-24 color-0066ff" @tap="toFeedback">历史反馈</view>
      </view>
    </view>
    <scroll-view
      id="commentContainer"
      scroll-y
      enable-flex
      class="flex-1 overflow-hidden mt-8 px-16 box scroll-wrapper"
      :scroll-with-animation="true"
      :scroll-top="commentScrollTop"
      @refresherrefresh="onRefresh"
      :refresher-triggered="triggered"
      :refresher-enabled="refresherEnabled"
    >
      <view id="commentContent">
        <date-time date-time="下拉加载更多历史记录" v-if="refresherEnabled" />
        <list-history :historyList="historyList" />
        <answer-init />
        <list-chat />
      </view>
    </scroll-view>
    <view class="px-24 pt-16 pb-32 relative">
      <!--输入框上方推荐列表-->
      <view class="flex overflow-auto py-16 gap-16" v-if="inputSelect.length">
        <view class="select-item" v-for="(item, index) in inputSelect" :key="index" @tap="handleSendMessage(item)">
          {{ item.question }}
        </view>
      </view>
      <view class="flex gap-24">
        <input
          v-model="inputVal"
          @input="handleInput"
          placeholder="请输入..."
          placeholder-style="color: #B8B8B8"
          cursor-spacing="30"
          class="flex-1 h-64 bg-fff rounded-16 font-28 color-333 line-height-64 px-24"
        />
        <view class="py-18 px-24 bg-0066ff rounded-16 font-28 color-fff" @tap="handleInputSearch">发送</view>
      </view>
    </view>
    <h-status-footer />
    <h-popup />
  </view>
</template>

<script setup lang="ts">
import { getCurrentInstance, nextTick, onBeforeUnmount, ref } from 'vue'
import { $store, formatImage } from '@/utils/common'
import HStatusIconHeader from './components/h-status-icon-header.vue'
import AnswerInit from './components/answer-init.vue'
import { _get } from '@/utils/common-request'
import ListChat from '@/pages-smart-service/list-chat.vue'
import ListHistory from '@/pages-smart-service/list-history.vue'
import { onReady } from '@dcloudio/uni-app'
import DateTime from '@/pages-smart-service/components/date-time.vue'

/**
 * 输入框外加推荐列表
 * */
const inputVal = ref('')
const inputSelect = ref([])
function handleInput(e) {
  getSegmentList(e.detail.value)
}
function getSegmentList(content) {
  _get({ url: '/faq/segment', data: { content } }).then((res: IResponseType<[]>) => {
    inputSelect.value = res.data
  })
}
function handleSendMessage(item) {
  $store.commit('service/chatAdd', item)
  uni.$emit('scrollToBottom')
  inputSelect.value = []
}
function handleInputSearch() {
  handleSendMessage({ ask: inputVal.value, answerType: 'TEXT' })
  setTimeout(() => {
    _get({ url: '/faq/post', data: { content: inputVal.value } }).then((res: IResponseType<[]>) => {
      if (res.data) {
        handleSendMessage(res.data)
      } else {
        handleSendMessage({ answer: '我们已收到您的反馈', answerType: 'TEXT', showIcon: true })
      }
      inputVal.value = ''
      inputSelect.value = []
    })
  }, 500)
}

/**
 * 辅助功能
 * */
// 历史反馈
function toFeedback() {
  uni.navigateTo({
    url: '/pages-my-center/my-feedback/feedback-list'
  })
}
//拨打电话
function phoneCall() {
  uni.makePhoneCall({
    phoneNumber: '400-135-7277'
  })
}

/**
 * 自动滚动到列表下方
 * */
onReady(() => {
  scrollToBottom()
})
const commentScrollTop = ref(0)
const instance = getCurrentInstance()
function scrollToBottom() {
  nextTick(() => {
    let query = uni.createSelectorQuery().in(instance)
    query.select('#commentContainer').boundingClientRect()
    query.select('#commentContent').boundingClientRect()
    query.exec((res) => {
      //如果子元素高度大于父元素高度(显示高度)
      if (res[1].height > res[0].height) {
        //计算出二者差值就是需要滚动的距离
        commentScrollTop.value = res[1].height - res[0].height
      }
    })
  })
}

/**
 * 事件注册销毁
 */
uni.$on('scrollToBottom', scrollToBottom)
uni.$on('getSegmentList', getSegmentList)
onBeforeUnmount(() => {
  uni.$off('scrollToBottom', scrollToBottom)
  uni.$off('getSegmentList', getSegmentList)
  $store.commit('service/resetChatList')
})

/**
 * 下拉刷新
 */
const triggered = ref(false)
const pageNum = ref(0)
const historyList = ref([])
const refresherEnabled = ref(true)
function onRefresh() {
  pageNum.value++
  triggered.value = true
  getHistoryList()
}
function getHistoryList() {
  _get({ url: '/faq/chatHistory', data: { pageNum: pageNum.value, pageSize: 10 } }).then((res: IResponseType<[]>) => {
    if (res.rows?.length) {
      historyList.value.unshift(...res.rows.reverse())
    }
    triggered.value = false
    if (res.total <= pageNum.value * 10) {
      setTimeout(() => {
        refresherEnabled.value = false
      }, 500)
    }
  })
}
</script>

<style lang="scss" scoped>
.select-item {
  padding: 0 px2vw(24);
  height: px2vw(48);
  background: #fff;
  border-radius: px2vw(24);
  font-size: px2vw(24);
  color: #333;
  font-weight: 500;
  line-height: px2vw(48);
  flex-shrink: 0;
}
.scroll-wrapper {
  ::-webkit-scrollbar {
    width: px2vw(6) !important;
    height: px2vw(6) !important;
    background-color: #f3f3f5;
  }

  /*定义滚动条轨道 内阴影+圆角*/
  ::-webkit-scrollbar-track {
    border-radius: px2vw(3);
    background-color: #f3f3f5;
  }

  /*定义滑块 内阴影+圆角*/
  ::-webkit-scrollbar-thumb {
    border-radius: px2vw(3);
    background: rgba(61, 130, 234, 0.3);
  }
}
</style>
