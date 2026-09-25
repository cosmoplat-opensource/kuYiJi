<template>
  <view
    class="bg-fff box pt-24 pl-32 mx-16 mb-8 relative"
    :class="!itemIsChecked && !dataItem.remark ? 'pb-32' : 'pb-24'"
    @tap="handleJumpDetail"
  >
    <!--报产人/日期-->
    <view class="flex align-center justify-between pr-16">
      <view v-if="dataItem.id" class="flex align-center flex-1">
        <view class="mark-ebf0f5">记工</view>
        <h-text-display :text="dataItem.submitNickName" :width="120" class="ml-16 font-28 color-5a6f82" />
        <view class="color-5a6f82 font-28 ml-16">{{ formatDate(dataItem.submitDate) }}</view>
      </view>
      <template v-if="!itemIsChecked">
        <h-checkbox v-if="checkAll" :checked="dataItem.checked" @checkedChange="handleChecked" class="mr-16" />
        <h-button v-else height="48" width="96" text="质检" font="font-24"
      /></template>
    </view>
    <!--产品名称/数量-->
    <view class="mt-20 flex align-center pr-32">
      <view class="font-28 color-333 flex-1 bold overflow-hidden">
        <h-text-display :text="dataItem.productName" />
      </view>
      <word-icon class="ml-24" text="共" />
      <view class="ml-8 color-5a6f82 font-28">
        {{ formatNum }}
      </view>
    </view>
    <!--产品编码-->
    <view class="mt-12 font-24 color-999">{{ dataItem.productCode }}</view>
    <!--工序信息-->
    <view class="flex align-center mt-24 mr-32">
      <view class="flex align-center">
        <view class="mark-ebf0f5-rounded">报</view>
        <h-text-display :text="dataItem.processName" :width="150" class="ml-8 font-28 color-5a6f82" />
        <word-icon v-if="dataItem.isLastProcess?.includes('0')" class="ml-16" text="尾" />
        <word-icon v-if="dataItem.isFirstProcess?.includes('0')" class="ml-16" text="首" />
      </view>
      <view class="area-date-info flex-1 flex flex-wrap justify-end gap-16-24">
        <view class="flex align-center">
          <view class="area-date-info--l">良</view>
          <text class="bold font-28">{{ dataItem.passNum || 0 }}{{ dataItem.productUnit }}</text>
        </view>
        <view class="flex align-center">
          <view class="mark-ebf0f5 border mr-12">不良</view>
          <text class="bold font-28">{{ dataItem.ngNum || 0 }}{{ dataItem.productUnit }}</text>
        </view>
      </view>
    </view>
    <view class="flex align-center mt-24" v-if="dataItem.preProcessName">
      <view class="mark-ebf0f5-rounded">前</view>
      <h-text-display :text="dataItem.preProcessName" :width="520" class="ml-8 font-28 color-5a6f82" />
    </view>
    <!--备注信息-->
    <view class="flex align-center mt-24 pt-24 b-t-1 border-f5f5f5 mr-32" v-if="dataItem.remark">
      <view class="flex align-center overflow-hidden">
        <view class="font-28 color-5a6f82">备注：</view>
        <view class="font-28 color-5a6f82 flex-1 single">{{ dataItem.remark }}</view>
      </view>
    </view>
    <!--质检信息-->
    <view class="flex align-center mt-24 pt-24 b-t-1 border-f5f5f5 mr-32" v-if="itemIsChecked">
      <word-icon text="检" />
      <view class="ml-8 font-28 color-5a6f82 flex-1">{{ dataItem.qcNickName }}</view>
      <view class="font-28 color-5a6f82">{{ formatDate(dataItem.qcDate) }}</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue'
import { $store, formatDate } from '@/utils/common'
import bigNumber from 'bignumber.js'
import HNameplate from '@/components/h-nameplate.vue'
import AreaDateInfo from '@/pages/report-production/components/area-date-info.vue'

import HTextDisplay from '@/components/h-text-display.vue'

const props = defineProps({
  dataItem: {
    type: Object as PropType<IQualityTestingItem>,
    default: () => {}
  },
  tabType: {
    type: Boolean,
    default: false
  },
  checkAll: {
    type: Boolean,
    default: false
  }
})

// true 未检 false 已检
const itemIsChecked = computed(() => {
  return props.dataItem.checkStatus === 2
})

const formatNum = computed(() => {
  const { passNum, ngNum, productUnit, checkPassNum, checkNgNum } = props.dataItem
  return new bigNumber(passNum).plus(ngNum).toString() + (productUnit || '')
})

function handleJumpDetail() {
  $store.commit('qualityTesting/setDataItem', props.dataItem)
  let url = `/pages/quality-testing/detail?submitId=${props.dataItem.id}&readonly=${itemIsChecked.value}&recordRemark=${
    props.dataItem.remark || ''
  }`
  uni.navigateTo({ url })
}
const emits = defineEmits(['itemCheck'])
function handleChecked() {
  emits('itemCheck', props.dataItem)
}
</script>

<style lang="scss" scoped>
.audit-card-mark {
  position: absolute;
  left: px2vw(-32);
  width: px2vw(16);
  height: px2vw(24);
  background: #00bfa5;
  border-radius: 0 px2vw(4) px2vw(4) 0;
}
.audit-experience {
  animation: scaleAuditAnimation 3s infinite;
}

//体验特效
@keyframes scaleAuditAnimation {
  0% {
    transform: scale(1.1);
  }
  50% {
    transform: scale(0.9);
  }
  100% {
    transform: scale(1.1);
  }
}
.area-date-info {
  display: flex;
  align-items: center;
  &--l {
    border-radius: 50%;
    background: #19aa8d;
    font-size: px2vw(24);
    margin-right: px2vw(12);
    color: #fff;
    width: px2vw(32);
    height: px2vw(32);
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
