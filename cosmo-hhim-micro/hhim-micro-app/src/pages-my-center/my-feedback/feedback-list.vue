<template>
  <view class="bg-F3F3F5 h-full flex flex-col">
    <uni-nav-bar />
    <h-status-header title="历史反馈" />
    <scroll-view
      @refresherrefresh="pageListRefresh"
      @scrolltolower="pageListLoadMore"
      :refresher-triggered="refreshTag"
      refresher-enabled
      scroll-y
      class="overflow-auto mt-8 px-16 pt-16 box flex-1"
      enable-flex
    >
      <template v-if="pageEmpty">
        <h-empty className="pt-240" tipsWord="暂无反馈，新增后的可在此显示" />
      </template>
      <template v-else>
        <view v-for="(item, index) in dataList" :key="index" class="p-32 box bg-fff mb-16" @tap="toDetail(item)">
          <view class="font-24 flex justify-between"
            ><text class="color-5a6f82">{{ item.createTime }}</text>
            <text v-if="item.status === '未处理'" class="color-ff0000">{{ item.status }}</text>
            <text v-else-if="item.status === '处理中'" class="color-e7a11a">{{ item.status }}</text>
            <text v-else-if="item.status === '已处理'" class="color-999">{{ item.status }}</text>
          </view>
          <view class="font-28 mt-32"><text class="color-5a6f82">问题描述：</text>{{ item.content }}</view>
        </view>

        <uni-load-more :status="dataNoMore" />
      </template>
    </scroll-view>
    <view class="px-48 pb-32 relative" v-if="btnFeedBackShow">
      <view class="tab-bar-shadow" />
      <h-button width="100%" height="72" text="我要反馈" @tap="jumpTpFeedBackSubmit" />
    </view>
  </view>
</template>

<script setup lang="ts">
import HEmpty from '@/components/h-empty.vue'
import HooksPageList from '@/hooks/page-list'
import { onReady, onShow } from '@dcloudio/uni-app'
import { ref } from 'vue'
const { dataList, pageListRefresh, pageTotal, dataNoMore, refreshTag, pageEmpty, pageListLoadMore, pageListSetParams } =
  HooksPageList('/suggestion/list', { run: false })
function toDetail(item) {
  uni.navigateTo({
    url: '/pages-my-center/my-feedback/feedback-detail?data=' + JSON.stringify(item)
  })
}
function jumpTpFeedBackSubmit() {
  uni.navigateTo({
    url: '/pages-my-center/my-feedback/submit-feedback'
  })
}
const btnFeedBackShow = ref(false)
onReady(() => {
  if (getCurrentPages()[getCurrentPages().length - 2].route === 'pages-smart-service/index') {
    btnFeedBackShow.value = true
  }
})
onShow(() => {
  pageListRefresh()
})
</script>

<style lang="scss" scoped>
.tab-bar-shadow {
  position: absolute;
  left: 0;
  width: 100%;
  bottom: px2vw(104);
  height: px2vw(64);
  background: linear-gradient(180deg, #f3f3f500, #f3f3f5 100%);
  border-radius: 0;
}
</style>
