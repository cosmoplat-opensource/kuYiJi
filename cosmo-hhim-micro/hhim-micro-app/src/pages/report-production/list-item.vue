<template>
  <view class="bg-fff" @longpress="handleLongTap" @tap="handleJumpDetail">
    <uni-swipe-action>
      <uni-swipe-action-item
        :right-options="checkStatus ? options2 : []"
        :show="isOpened"
        :auto-close="false"
        @change="handleDeleteAreaChange"
        @click="handleDeleteAreaClick"
      >
        <view class="content-box px-32 pt-8 pb-24">
          <view class="flex align-center pt-16">
            <view class="mark-24a8ff border" v-if="dataItem.submitStatus === 1">待审</view>
            <view class="mark-fe9f00 border" v-else-if="dataItem.submitStatus === 2">驳回</view>
            <view class="mark-ebf0f5 border" v-else>已审</view>
            <view class="mark-24a8ff border ml-16" v-if="dataItem.checkStatus === 1">待检</view>
            <view class="mark-00bfa5 border ml-16" v-if="dataItem.checkStatus === 2">已检</view>
            <view class="flex-1 ml-8 color-5a6f82 font-28 flex align-center">
              <!--是否为补录-->
              <template v-if="dataItem.expiredRecordFlag === 0">
                <text>{{ props.dataItem.createdDate }}</text>
                <view class="icon-32 flex-center bg-FE9F00 rounded-full ml-16 color-fff font-24">补</view>
              </template>
              <text v-else>{{ formatDate(props.dataItem.createdDate) }}</text>
            </view>
            <h-checkbox v-if="checkAble" :checked="props.dataItem.checked" @checkedChange="handleItemCheck" />
            <view v-if="checkStatus && !checkAble">
              <image src="/static/images/icon_edit.svg" class="icon-32" />
            </view>
            <view
              class="bg-FE9F00 color-fff icon-32 flex align-center justify-center rounded-full font-24"
              v-if="dataItem.dataStatus === 0"
              >改</view
            >
          </view>
          <view class="flex align-center mt-24 mid">
            <view class="color-333 font-32 bold">
              <h-text-display :text="props.dataItem.productName" :width="192" />
            </view>
            <text class="color-999 font-32 flex-1 bold">({{ props.dataItem.productCode }})</text>
            <view class="ml-24 icon-32 color-5a6f82 bg-EBF0F5 rounded-full font-24 flex align-center justify-center"
              >共</view
            >
            <text class="color-5a6f82 font-28 ml-8">{{ getTotalCount }}</text>
          </view>
          <view class="flex align-center mt-24">
            <view class="color-5a6f82 font-28">{{ dataItem.operateProcessName }}</view>
            <word-icon v-if="props.dataItem.isLastProcess === '0'" class="ml-16" text="尾" />
            <word-icon v-if="props.dataItem.isFirstProcess === '0'" class="ml-16" text="首" />
            <view class="flex-1" />
            <area-date-info
              v-if="props.dataItem.submitStatus === 0"
              :dataItem="{ pass: props.dataItem.checkPassNum, ng: props.dataItem.checkNgNum }"
            />
            <area-date-info v-else :dataItem="{ pass: props.dataItem.passNum, ng: props.dataItem.ngNum }" />
          </view>
        </view>
      </uni-swipe-action-item>
    </uni-swipe-action>
  </view>
</template>

<script setup lang="ts">
import AreaDateInfo from './components/area-date-info.vue'
import { computed, ref } from 'vue'
import { _delete } from '@/utils/common-request'
import HNameplate from '@/components/h-nameplate.vue'
import bigNumber from 'bignumber.js'
import { formatDate } from '@/utils/common'

const props = defineProps({
  dataItem: {
    type: Object,
    default: () => {}
  },
  checkAble: {
    type: Boolean,
    default: false
  }
})

const getTotalCount = computed(() => {
  const { passNum, ngNum, unit, submitStatus, checkPassNum, checkNgNum } = props.dataItem
  if (submitStatus === 0) {
    return new bigNumber(checkPassNum).plus(checkNgNum).toString() + (unit || '')
  } else {
    return new bigNumber(passNum).plus(ngNum).toString() + (unit || '')
  }
})

const checkStatus = computed(() => {
  return props.dataItem.submitStatus !== 0
})

function handleJumpDetail() {
  if (props.checkAble) {
    handleItemCheck()
  } else {
    let url = `/pages-report-work/edit?id=${props.dataItem.id}&from=report&date=${props.dataItem.submitDay}`
    if (!checkStatus.value || props.dataItem.checkStatus === 2) {
      url += '&readOnly=true'
    }
    uni.navigateTo({ url })
  }
}

// 滑动删除模块1
const options2 = [
  {
    text: '删除',
    style: {
      backgroundColor: '#ff0000'
    }
  }
]
const show = ref(false)
const isOpened = ref('none')
const emits = defineEmits(['openDeleteBatch', 'itemCheck', 'deleteItem'])
function handleLongTap() {
  emits('openDeleteBatch')
}
function handleDeleteAreaChange(e) {
  if (['right', 'none'].includes(e)) {
    isOpened.value = e
  }
}
function handleDeleteAreaClick(e) {
  if (e.content.text === '删除') {
    emits('deleteItem', props.dataItem.id)
  }
}
function handleItemCheck() {
  emits('itemCheck')
}
</script>
