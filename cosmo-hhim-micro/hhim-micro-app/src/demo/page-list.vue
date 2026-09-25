<!--注意顶部节点和scroll-view添加overflow-hidden,否则列表的刷新等功能会出现异常-->
<template>
  <view class="bg-F3F3F5 h-full flex flex-col overflow-hidden">
    <uni-nav-bar />
    <h-status-header title="列表演示" />
    <view class="p-16 color-0066ff" @tap="handleChangeEmpty">点击切换空数据,下拉刷新重新填充数据</view>
    <scroll-view
      @refresherrefresh="pageListRefresh"
      @scrolltolower="pageListLoadMore"
      :refresher-triggered="refreshTag"
      refresher-enabled
      scroll-y
      class="flex-1 overflow-hidden"
      enable-flex
    >
      <template v-if="pageEmpty">
        <h-empty className="pt-240" tipsWord="暂无反馈，新增后的可在此显示" />
      </template>
      <template v-else>
        <view v-for="(item, index) in dataPageList" :key="index" class="p-32 box bg-fff mb-16">
          <view class="font-24 flex justify-between"
            ><text class="color-5a6f82">{{ item.createTime }}</text>
            <text v-if="item.status === '未处理'" class="color-ff0000">{{ item.status }}</text>
            <text v-else-if="item.status === '处理中'" class="color-e7a11a">{{ item.status }}</text>
            <text v-else class="color-999">{{ item.status }}</text>
          </view>
          <view class="font-28 mt-32"><text class="color-5a6f82">问题描述：</text>{{ item.content }}</view>
        </view>
        <!--下拉加载更多-->
        <uni-load-more :status="dataNoMore" />
      </template>
    </scroll-view>
    <h-status-footer />
  </view>
</template>

<script setup lang="ts">
import HEmpty from '@/components/h-empty.vue'
import HooksPageList from '@/hooks/page-list'
import { computed } from 'vue'

/** 如果需要对返回的数组进行封装,采用computed方式,不需要二次封装的直接在view中用dataList*/
const dataPageList = computed(() => {
  return [...dataList.value, ...dataList.value, ...dataList.value]
})

/** 初始化分页hooks*/
const { dataList, pageListRefresh, pageTotal, dataNoMore, refreshTag, pageEmpty, pageListLoadMore, pageListSetParams } =
  HooksPageList('/suggestion/list')
// 默认调用采用get方式
// HooksPageList('/suggestion/list', { data: {页面初始参数}, method: 'post' })

function handleChangeEmpty() {
  // demo样例,实际不要这么直接给可用变量赋值,改变传参用pageListSetParams
  dataList.value = []
  pageEmpty.value = true
}
</script>

<style lang="scss" scoped></style>
