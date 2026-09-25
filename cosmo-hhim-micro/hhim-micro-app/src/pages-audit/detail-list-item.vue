<template>
  <view
    class="bg-fff box pt-32 pl-32 mx-16 mb-8 relative"
    :class="dataItem.remark ? 'pb-24' : 'pb-32'"
    @tap="handleEdit"
  >
    <view class="absolute-l-t flex align-center">
      <h-exception-label v-if="dataItem.negativeStockFlag === '0'" class="mr-8" text="超报风险" bg="#FE9F00" />
      <h-exception-label v-if="dataItem.lowPassRateFlag === '0'" class="mr-8" text="良品率偏低" bg="#FE9F00" />
      <h-exception-label
        v-if="dataItem.overProductiveCapacityFlag === '0'"
        class="mr-8"
        text="记工数超产能"
        bg="#9BADBC"
      />
      <h-exception-label v-if="dataItem.checkStatus === 1" text="待检" bg="#24a8ff" />
      <h-exception-label v-if="dataItem.checkStatus === 2" text="已检" bg="#00BFA5" />
    </view>
    <!--报产人/日期-->
    <view class="flex align-center justify-between pr-16">
      <view class="flex align-center flex-1">
        <template v-if="dataItem.expiredRecordFlag === 0">
          <view class="color-5a6f82 font-28">{{ dataItem.submitDay }}</view>
          <view class="icon-32 flex-center bg-FE9F00 rounded-full ml-16 color-fff font-24">补</view>
        </template>
        <template v-else>
          <view class="color-5a6f82 font-28">{{ formatDate(dataItem.createdDate) }}</view>
        </template>
        <view class="color-333 font-28 ml-16 bold">{{ formatStr(dataItem.submitNickName, 12) }}</view>
      </view>
      <h-checkbox v-if="checkAll" :checked="dataItem.checked" @checkedChange="handleChecked" class="mr-16" />
      <image v-else src="/static/images/icon_edit.svg" class="icon-32 mr-16" />
    </view>
    <!--良 不良 共-->
    <view class="flex align-center flex-wrap">
      <area-date-info
        class="mt-32"
        :dataItem="{
          pass: dataItem.checkStatus === 2 ? dataItem.checkPassNum : dataItem.passNum,
          ng: dataItem.checkStatus === 2 ? dataItem.checkNgNum : dataItem.ngNum
        }"
      />
      <view class="mt-32 ml-24 icon-32 color-5a6f82 bg-EBF0F5 rounded-full font-24 flex align-center justify-center">
        <text>共</text>
      </view>
      <view class="mt-32 font-28 color-333 ml-8 bold mr-28">
        <text>
          {{
            dataItem.checkStatus === 2 ? dataItem.checkPassNum + dataItem.checkNgNum : dataItem.passNum + dataItem.ngNum
          }}
          {{ dataItem.unit }}
        </text>
      </view>
    </view>
    <view class="mt-32 flex align-center" v-if="dataItem.preProcessName && dataItem.isFirstProcess !== '0'">
      <view class="mark-ebf0f5-rounded">前</view>
      <view class="ml-8 color-333 font-28">
        <h-text-display :text="dataItem.preProcessName.replace(/,/gm, '，')" :width="212" />
      </view>
    </view>
    <!--首尾序-->
    <view class="mt-32 flex align-center">
      <view class="flex align-center">
        <view class="flex align-center" @tap.stop="handleIsLastProcess">
          <h-checkbox-icon :checked="dataItem.isLastProcess === '0'" />
          <view class="ml-16" :class="dataItem.isLastProcess === '0' ? 'color-333' : 'color-5a6f82'">
            <text class="font-24">我是最后一道工序</text>
          </view>
        </view>
        <view class="flex align-center ml-48" @tap.stop="handleIsFirstProcess">
          <h-checkbox-icon :checked="dataItem.isFirstProcess === '0'" />
          <view class="ml-16" :class="dataItem.isFirstProcess === '0' ? 'color-333' : 'color-5a6f82'">
            <text class="font-24">我是首序</text>
          </view>
        </view>
      </view>
    </view>
    <view
      class="pt-24 mt-24 box b-t-1 border-f5f5f5 single word-break overflow-hidden font-24 color-999 pr-32"
      v-if="dataItem.remark"
    >
      备注：{{ dataItem.remark }}
    </view>
  </view>
</template>

<script setup lang="ts">
import { formatStr, formatDate } from '@/utils/common'
import HCheckboxIcon from '@/components/h-checkbox-icon.vue'
import AreaDateInfo from '@/pages/report-production/components/area-date-info.vue'
import HExceptionLabel from '@/components/h-exception-label.vue'
import HNameplate from '@/components/h-nameplate.vue'

const props = defineProps({
  dataItem: {
    type: Object,
    default: () => {}
  },
  checkAll: {
    type: Boolean,
    default: false
  },
  tabType: {
    type: Boolean,
    default: false
  }
})

const emits = defineEmits(['isLastCheck', 'isFirstCheck', 'itemCheck'])
function handleChecked() {
  emits('itemCheck', props.dataItem)
}
function handleIsLastProcess() {
  emits('isLastCheck', props.dataItem)
}
function handleIsFirstProcess() {
  emits('isFirstCheck', props.dataItem)
}

function handleEdit() {
  if (props.checkAll) {
    handleChecked()
  } else {
    let params = `id=${props.dataItem.id}&isLastProcess=${props.dataItem.isLastProcess}&isFirstProcess=${props.dataItem.isFirstProcess}`
    params += `&negativeStockFlag=${props.dataItem.negativeStockFlag}`
    params += `&preProcessStockNum=${props.dataItem.preProcessStockNum}`
    params += `&preProcessWaitCheckPassNum=${props.dataItem.preProcessWaitCheckPassNum}`
    params += `&lowPassRateFlag=${props.dataItem.lowPassRateFlag}`
    params += `&passRate=${props.dataItem.passRate}`
    params += `&avgPassRateByDay=${props.dataItem.avgPassRateByDay}`
    params += `&overProductiveCapacityFlag=${props.dataItem.overProductiveCapacityFlag}`
    params += `&avgProductionCapacityByDay=${props.dataItem.avgProductionCapacityByDay}`
    params += `&todaySubmitNum=${props.dataItem.todaySubmitNum}`
    params += `&overProductiveCapacityExceptionRecordIds=${props.dataItem.overProductiveCapacityExceptionRecordIds}`
    uni.navigateTo({ url: `/pages-audit/detail-info?${params}&from=audioDetail` })
  }
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
</style>
