<template>
  <view class="h-full bg-f3f3f5 box flex flex-col overflow-hidden">
    <uni-nav-bar />
    <h-status-header :title="getPageTitle" />
    <data-notice-area
      v-if="pageSingleType"
      text="指定首序/尾序后，可减少记工误操作"
      class="mt-16 mx-16 rounded-16 overflow-hidden"
    />
    <data-notice-area v-else text="多个尾序会影响分析数据的准确性" class="mt-24 mx-16 rounded-16 overflow-hidden" />
    <scroll-view
      @refresherrefresh="onRefresh"
      :refresher-triggered="triggered"
      refresher-enabled
      enable-flex
      scroll-y
      class="flex-1 flex flex-col overflow-hidden"
    >
      <!--无首序-->
      <view class="mx-16 bg-fff rounded-16 mt-8" v-if="pageSingleType && hasNoLastList.length">
        <view
          class="flex-center p-32 top-deliver overflow-hidden"
          :class="{ hide: !hasNoFirstShow }"
          @tap="handleFirstShow"
        >
          <view class="flex-1">
            <text class="color-5a6f82 font-28 bold">无首序</text>
          </view>
          <view class="flex-center px-16 h-32 bg-EBF0F5 rounded-16 font-24 color-5a6f82">
            <text>共</text>
            <text class="color-ff0000 mx-8 bold">{{ hasNoFirstList.length }}</text>
            <text>款产品</text>
          </view>
          <image
            src="/static/images/icon_unfold.svg"
            class="icon-32 ml-32"
            :class="{ 'icon-rotate-y': hasNoFirstShow }"
          />
        </view>
        <view v-if="hasNoFirstShow">
          <view
            v-for="(item, index) in hasNoFirstList"
            :key="index"
            :class="{ 'b-t-1 border-f5f5f5': index > 0 }"
            class="flex-center p-32 box"
          >
            <view class="flex flex-col flex-1">
              <h-text-display :text="item.warnProductName" :width="328" class="font-28 color-333 bold" />
              <text class="mt-16 font-24 color-999">{{ item.warnProductCode }}</text>
            </view>
            <h-button height="48" width="96" text="指定" class="ml-24" font="font-24" @tap="handleOpenPopup(item, 1)" />
          </view>
        </view>
      </view>
      <!--无尾序-->
      <view class="mx-16 bg-fff rounded-16 mt-8" v-if="pageSingleType && hasNoLastList.length">
        <view
          class="flex-center p-32 top-deliver overflow-hidden"
          :class="{ hide: !hasNoLastShow }"
          @tap="handleListShow"
        >
          <view class="flex-1">
            <text class="color-5a6f82 font-28 bold">无尾序</text>
          </view>
          <view class="flex-center px-16 h-32 bg-EBF0F5 rounded-16 font-24 color-5a6f82">
            <text>共</text>
            <text class="color-ff0000 mx-8 bold">{{ hasNoLastList.length }}</text>
            <text>款产品</text>
          </view>
          <image
            src="/static/images/icon_unfold.svg"
            class="icon-32 ml-32"
            :class="{ 'icon-rotate-y': hasNoLastShow }"
          />
        </view>
        <view v-if="hasNoLastShow">
          <view
            v-for="(item, index) in hasNoLastList"
            :key="index"
            :class="{ 'b-t-1 border-f5f5f5': index > 0 }"
            class="flex-center p-32 box"
          >
            <view class="flex flex-col flex-1">
              <h-text-display :text="item.warnProductName" :width="328" class="font-28 color-333 bold" />
              <text class="mt-16 font-24 color-999">{{ item.warnProductCode }}</text>
            </view>
            <h-button height="48" width="96" text="指定" class="ml-24" font="font-24" @tap="handleOpenPopup(item, 2)" />
          </view>
        </view>
      </view>
      <!--多尾序-->
      <view
        class="bg-fff rounded-16 mt-8 pt-32 flex flex-col mx-16 box"
        v-if="hasMoreLastList.length && !pageSingleType"
      >
        <!--标题区域-->
        <view class="flex align-center justify-between px-32 pb-24 top-deliver">
          <view class="color-5a6f82 font-28 bold">多尾序</view>
          <view class="flex-center px-16 h-32 bg-EBF0F5 rounded-16 font-24 color-5a6f82">
            <text>共</text>
            <text class="color-ff0000 mx-8 bold">{{ hasMoreLastList.length }}</text>
            <text>款产品</text>
          </view>
        </view>
        <template v-for="(item, index) in hasMoreLastList" :key="index">
          <view class="deliver-line mx-32 box" v-if="index > 0" />
          <view class="flex align-start pb-16 px-32 box pt-24 overflow-hidden" @tap="handleMoreListShow(item)">
            <view class="flex flex-col">
              <h-text-display :text="item.warnProductName" :width="384" class="color-333 font-28 bold" />
              <text class="mt-16 color-999 font-24">{{ item.warnProductCode }}</text>
            </view>
            <view class="flex-1" />
            <view class="flex-center px-16 h-32 bg-EBF0F5 rounded-16 font-24 color-5a6f82">
              <text class="color-ff0000 mx-8 bold">{{ item.processWarnList.length }}</text>
              <text>个尾序</text>
            </view>
            <view class="icon-32 flex-center ml-32">
              <image src="/static/images/icon_unfold.svg" class="icon-32" :class="{ 'icon-rotate-y': item.showList }" />
            </view>
          </view>
          <view v-if="item.showList" class="flex flex-col">
            <view
              v-for="(obj, obj_index) in item.processWarnList"
              :key="obj_index"
              class="relative flex align-center py-32 mx-32 box b-t-1 border-f5f5f5"
            >
              <h-text-display :text="obj.warnProcessName" :width="328" class="font-28 color-333 bold" />
              <word-icon text="尾" class="ml-16" />
            </view>
            <view class="flex justify-end b-t-1 border-f5f5f5 p-32">
              <h-button
                height="48"
                width="96"
                text="指定"
                class="ml-24"
                font="font-24"
                @tap="handleOpenPopup(item, 3)"
              />
            </view>
          </view>
        </template>
      </view>
    </scroll-view>
    <popup-assign-process ref="popupAssignProcess" @refreshList="initData" />
    <h-status-footer />
    <h-popup />
  </view>
</template>

<script setup lang="ts">
import DataNoticeArea from '@/pages-data-governance/data-notice-area.vue'
import { computed, onMounted, ref } from 'vue'
import { _get } from '@/utils/common-request'
import HTextDisplay from '@/components/h-text-display.vue'

import PopupAssignProcess from '@/pages-data-governance/process/popup-assign-process.vue'
import { onLoad } from '@dcloudio/uni-app'

// 列表数组模块
const hasNoFirstList = ref([])
const hasNoLastList = ref([])
const hasMoreLastList = ref([])

const getPageTitle = ref('')
const pageSingleType = ref(true)

const triggered = ref(false)
function onRefresh() {
  triggered.value = true
  initData()
}

function initData() {
  _get({ url: '/warn/product/notHaveFirstProcess' }).then((res) => {
    hasNoFirstList.value = res.data
  })
  _get({ url: '/warn/product/notHaveLastProcess' })
    .then((res) => {
      hasNoLastList.value = res.data
    })
    .finally(() => {
      triggered.value = false
    })
  _get({ url: '/warn/product/multiLastProcess' })
    .then((res) => {
      hasMoreLastList.value = res.data
    })
    .finally(() => {
      triggered.value = false
    })
}
onMounted(() => {
  initData()
})
onLoad((options) => {
  getPageTitle.value = options.type === '1' ? '无首序/尾序处理' : '多尾序处理'
  pageSingleType.value = options.type === '1'
})

// 列表是否展示模块
const hasNoFirstShow = ref(false)
const hasNoLastShow = ref(false)
function handleFirstShow() {
  if (hasNoFirstShow.value) {
    hasNoFirstShow.value = false
  } else {
    closeAllList()
    hasNoFirstShow.value = true
  }
}

function handleListShow() {
  if (hasNoLastShow.value) {
    hasNoLastShow.value = false
  } else {
    closeAllList()
    hasNoLastShow.value = true
  }
}
function handleMoreListShow(item) {
  if (item.showList) {
    closeAllList()
  } else {
    closeAllList()
    item.showList = true
  }
}
function closeAllList() {
  hasNoFirstShow.value = false
  hasNoLastShow.value = false
  hasMoreLastList.value.forEach((item) => {
    item.showList = false
  })
}

// 弹窗指定模块
const popupAssignProcess = ref(null)
const popupWarnType = ref(1)
const popupDataItem = ref({})
function handleOpenPopup(item, type) {
  popupDataItem.value = item
  popupWarnType.value = type
  popupAssignProcess.value.openEdit(item, popupWarnType.value)
}
</script>

<style lang="scss" scoped>
.top-deliver {
  border-bottom: px2vw(1) solid #f5f5f5;
  &.hide {
    border-bottom: px2vw(0) solid #fff;
  }
}
.deliver-line {
  border-top: px2vw(1) dashed #f5f5f5;
}
</style>
