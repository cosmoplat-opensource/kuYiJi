<template>
  <view class="bg-fff box py-24 pl-32 mx-16 mb-8 relative">
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
        <h-exception-label v-if="dataItem.warnFlag === '0'" text="警" bg="#FE9F00" />
      </template>
    </view>
    <view class="flex pr-16">
      <view class="flex-1 pt-8">
        <!--产品名称/数量-->
        <h-text-display :text="dataItem.productName" :width="512" class="font-28 color-333 bold" />
        <!--产品编码-->
        <view class="mt-16 font-24 color-999 bold">{{ dataItem.productCode }}</view>
        <!--工序信息-->
        <view class="color-5a6f82 font-24 mt-24 flex-ac">
          <text>工序</text>
          <text class="color-333 mx-8 bold">{{ dataItem.processCount || 0 }}</text>
          <text>道</text>
          <text class="mx-16">|</text>
          <text>记工</text>
          <text class="color-333 mx-8 bold">{{ dataItem.recordCount }}</text>
          <text>条</text>
        </view>
      </view>
      <h-button height="48" width="96" text="审核" font="font-24" @tap.stop="handleAudit" />
    </view>
    <!--备注信息-->
    <view class="flex align-center mt-24 pt-24 b-t-1 border-f5f5f5 mr-32" v-if="dataItem.remark">
      <view class="flex align-center overflow-hidden">
        <view class="font-28 color-5a6f82">备注：</view>
        <view class="font-28 color-5a6f82 flex-1 single">{{ dataItem.remark }}</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { $store } from '@/utils/common'

import HTextDisplay from '@/components/h-text-display.vue'
import HExceptionLabel from '@/components/h-exception-label.vue'

const props = defineProps({
  dataItem: {
    type: Object,
    default: () => {}
  },
  tabType: {
    type: Boolean,
    default: false
  }
})

const checkIsUser = computed(() => {
  return !props.tabType
})

const emits = defineEmits(['auditCheck'])
function handleAudit() {
  emits('auditCheck', props.dataItem)
}

const isExperience = computed(() => {
  return $store.getters.isExperience
})
</script>

<style lang="scss" scoped>
.area-date-info--bl {
  margin-right: px2vw(12);
  background: #ebf0f5;
  border-radius: px2vw(4);
  border: px2vw(2) solid #d3dfeb;
  width: px2vw(56);
  height: px2vw(32);
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: px2vw(24);
  color: #5a6f82;
}
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
</style>
