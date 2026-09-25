<template>
  <view
    class="bg-fff box pt-32 pl-32 mx-16 mb-8 relative"
    :class="checkStatus && !dataItem.remark ? 'pb-32' : 'pb-24'"
    @tap="handleJumpDetail"
  >
    <view class="absolute-l-t flex align-center">
      <template v-if="checkIsUser && dataItem.warnFlagStaff === '1'">
        <h-exception-label v-if="dataItem.negativeStockFlag === '0'" class="mr-8" text="超报风险" bg="#FE9F00" />
        <h-exception-label v-if="dataItem.lowPassRateFlag === '0'" class="mr-8" text="良品率偏低" bg="#FE9F00" />
        <h-exception-label
          v-if="dataItem.overProductiveCapacityFlag === '0'"
          class="mr-8"
          text="记工数超产能"
          bg="#9BADBC"
        />
      </template>
      <template v-else>
        <h-exception-label v-if="checkWarnMark" text="警" bg="#ff0000" />
      </template>
      <h-exception-label v-if="dataItem.checkStatus === 1" text="待检" bg="#24a8ff" />
      <h-exception-label v-if="dataItem.checkStatus === 2" text="已检" bg="#00BFA5" />
    </view>
    <!--报产人/日期-->
    <view class="flex align-center justify-between pr-16">
      <view v-if="dataItem.id" class="flex align-center">
        <view class="mark-ebf0f5">记工</view>
        <h-text-display :text="dataItem.submitNickName" :width="120" class="ml-16 font-28 color-5a6f82" />
        <template v-if="dataItem.expiredRecordFlag === 0">
          <view class="font-28 color-5a6f82 ml-16">{{ formatDate(dataItem.submitDay, 'MM-DD') }}</view>
          <word-icon class="ml-16" text="补" />
        </template>
        <template v-else>
          <view class="color-5a6f82 font-28 ml-16">{{ formatDate(dataItem.createdDate) }}</view>
        </template>
      </view>
      <view v-else class="flex align-center">
        <view class="font-28 color-5a6f82">{{ dataItem.submitDay }}</view>
        <word-icon class="ml-16" text="补" v-if="dataItem.expiredRecordFlag === 0" />
      </view>
      <view class="flex-1" />
      <image src="/static/images/icon_edit.svg" class="icon-32 mr-32" v-if="checkIsUser && checkStatus && !checkAll" />
      <h-checkbox v-if="checkAll" :checked="dataItem.checked" @checkedChange="handleChecked" class="mr-16" />
      <view class="w-1 h-48" />
      <h-button
        height="48"
        width="96"
        text="审核"
        font="font-24"
        @tap.stop="handleAudit"
        v-if="checkStatus"
        v-show="!checkAll"
      />
      <h-button
        height="48"
        width="96"
        text="撤销"
        font="font-24"
        @tap.stop="handleRevoke"
        v-if="revokeAble && !isExperience && dataItem.isComplete === '1'"
      />
      <view v-if="dataItem.isComplete === '0'" class="font-24 color-5a6f82">
        已入库<text v-if="revokeAble">，无法撤销</text>
      </view>
    </view>
    <!--产品名称/数量-->
    <view class="mt-20 flex align-center pr-32">
      <view class="font-32 color-333 flex-1 bold overflow-hidden">
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
      <view class="mark-ebf0f5-rounded">报</view>
      <view class="ml-8 font-28 color-5a6f82">
        <h-text-display :text="dataItem.operateProcessName" :width="192" />
      </view>
      <word-icon v-if="dataItem.isLastProcess?.includes('0')" class="ml-16" text="尾" />
      <word-icon v-if="dataItem.isFirstProcess?.includes('0')" class="ml-16" text="首" />
      <view class="flex-1" />
      <area-date-info
        :dataItem="{
          pass: checkStatus && dataItem.checkStatus !== 2 ? dataItem.passNum : dataItem.checkPassNum,
          ng: checkStatus && dataItem.checkStatus !== 2 ? dataItem.ngNum : dataItem.checkNgNum
        }"
      />
    </view>
    <view class="flex align-center mt-24 mr-32" v-if="dataItem.preProcessName">
      <view class="mark-ebf0f5-rounded">前</view>
      <view class="ml-8 font-28 color-5a6f82">
        <h-text-display :text="dataItem.preProcessName.replace(/,/gm, '，')" :width="512" />
      </view>
    </view>
    <!--备注信息-->
    <view class="flex align-center mt-24 pt-24 b-t-1 border-f5f5f5 mr-32" v-if="dataItem.remark">
      <view class="flex align-center overflow-hidden">
        <view class="font-28 color-5a6f82">备注：</view>
        <view class="font-28 color-5a6f82 flex-1 single">{{ dataItem.remark }}</view>
      </view>
    </view>
    <!--审产信息-->
    <view class="flex align-center mt-24 pt-24 b-t-1 border-f5f5f5 mr-32" v-if="!checkStatus">
      <word-icon text="审" />
      <view class="ml-8 font-28 color-5a6f82 flex-1">{{ dataItem.checkNickName }}</view>
      <view class="font-28 color-5a6f82">{{ formatDate(dataItem.checkDate) }}</view>
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
import HExceptionLabel from '@/components/h-exception-label.vue'

const props = defineProps({
  dataItem: {
    type: Object as PropType<IQualityTestingItem>,
    default: () => {}
  },
  checkAll: {
    type: Boolean,
    default: false
  },
  tabType: {
    type: Boolean,
    default: false
  },
  revokeAble: {
    type: Boolean,
    default: false
  }
})

// true 未审核 false 已审核
const checkStatus = computed(() => {
  return props.dataItem.submitStatus !== 0
})

const checkIsUser = computed(() => {
  return !props.tabType
})

const checkWarnMark = computed(() => {
  return (
    props.dataItem.negativeStockFlag === '0' ||
    props.dataItem.lowPassRateFlag === '0' ||
    props.dataItem.overProductiveCapacityFlag === '0'
  )
})

const formatNum = computed(() => {
  const { passNum, ngNum, unit, submitStatus, checkStatus, checkPassNum, checkNgNum } = props.dataItem
  if (submitStatus === 0 || checkStatus === 2) {
    return new bigNumber(checkPassNum || 0).plus(checkNgNum || 0).toString() + (unit || '')
  } else {
    return new bigNumber(passNum || 0).plus(ngNum || 0).toString() + (unit || '')
  }
})

const emits = defineEmits(['auditCheck', 'itemCheck', 'itemRevoke'])
function handleAudit() {
  emits('auditCheck', props.dataItem)
}
function handleChecked() {
  emits('itemCheck', props.dataItem)
}
function handleRevoke() {
  emits('itemRevoke', props.dataItem)
}
function handleJumpDetail() {
  if (isExperience.value) {
    return
  }
  if (checkIsUser.value || !checkStatus.value) {
    let url = `/pages-audit/detail-info?id=${props.dataItem.id}&from=auditListUser&type=edit`
    if (!checkStatus.value) {
      url += '&readOnly=true'
    }
    url += `&negativeStockFlag=${props.dataItem.negativeStockFlag}`
    url += `&preProcessStockNum=${props.dataItem.preProcessStockNum}`
    url += `&preProcessWaitCheckPassNum=${props.dataItem.preProcessWaitCheckPassNum}`
    url += `&lowPassRateFlag=${props.dataItem.lowPassRateFlag}`
    url += `&passRate=${props.dataItem.passRate}`
    url += `&avgPassRateByDay=${props.dataItem.avgPassRateByDay}`
    url += `&overProductiveCapacityFlag=${props.dataItem.overProductiveCapacityFlag}`
    url += `&avgProductionCapacityByDay=${props.dataItem.avgProductionCapacityByDay}`
    url += `&todaySubmitNum=${props.dataItem.todaySubmitNum}`
    url += `&overProductiveCapacityExceptionRecordIds=${props.dataItem.overProductiveCapacityExceptionRecordIds}`
    uni.navigateTo({ url })
  }
}

const isExperience = computed(() => {
  return $store.getters.isExperience
})
</script>

<style lang="scss" scoped></style>
