<template>
  <view class="bg-fff rounded-16 flex flex-col">
    <view class="flex h-136 align-center border-bottom-f5f5f5">
      <view class="flex-1 flex flex-col align-center" @tap="toReport">
        <text class="font-32 bold">{{ indexData.submitUserNum }}<text class="font-20 color-999 ml-8">人</text></text
        ><text class="font-24 mt-8">记工</text>
      </view>
      <view class="flex-1 flex flex-col align-center" @tap="toProduct">
        <text class="font-32 bold">{{ indexData.productNum }}<text class="font-20 color-999 ml-8">款</text></text
        ><text class="font-24 mt-8">产品</text>
      </view>
      <view class="flex-1 flex flex-col align-center" @tap="toProduct">
        <text class="font-32 bold">{{ indexData.totalNum || 0 }}</text>
        <text class="font-24 mt-8">报产总数</text>
      </view>
      <view class="flex-1 flex flex-col align-center">
        <view class="relative">
          <image
            :src="`/static/images/icon_trendarr_${indexData.momCheckProgress > 0 ? 'up' : 'down'}.svg`"
            class="icon-16 ml-8 mr-4 position-icon"
          />
          <text class="font-32 bold">{{ indexData.momCheckProgressShow }}%</text>
        </view>
        <text class="font-24 mt-8">环比</text>
      </view>
    </view>
    <template v-if="Role_Admin || Role_Review">
      <view class="flex-1 px-32 box h-100 flex flex-col justify-center mt-24">
        <view class="w-100 flex align-center justify-between font-24">
          <view class="flex align-center">
            <view class="mark-ebf0f5">审产进度</view>
            <text class="ml-16">{{ indexData.checkProgress }}%</text>
          </view>
          <view class="flex align-center">
            <text>已审</text>
            <text class="ml-12">{{ indexData.checkNum }}</text>
            <text>，待审 </text>
            <text class="color-ff0000 ml-12">{{ indexData.waitCheckNum }}</text>
          </view>
        </view>
        <view class="w-100 h-32 bg-f5f5f5 rounded-16 mt-20">
          <view class="h-100 bg-0066ff rounded-16" :style="{ width: indexData.checkProgress + '%' }"></view>
        </view>
      </view>
      <view class="h-80 mt-24 border-top-f5f5f5 flex align-center px-32 box">
        <view class="mark-ebf0f5">良品率</view>
        <view class="font-28 bold ml-16 text-right color-19aa8d">
          {{ `${new BigNumber(indexData.passRate || 0).multipliedBy(100).toNumber()}%` }}
        </view>
        <view class="flex-1"></view>
        <view class="flex align-center">
          <text class="color-5a6f82 font-24">环比</text>
          <image
            :src="`/static/images/icon_trendarr_${indexData.momPassRate > 0 ? 'up' : 'down'}.svg`"
            class="icon-16 ml-8 mr-4"
          />
          <text class="font-28 bold">{{ indexData.momPassRateShow }}%</text>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { BigNumber } from 'bignumber.js'
import { computed } from 'vue'
import { $state, Role_Admin, Role_Review } from '@/utils/common'
import HTextRun from '@/components/h-text-run.vue'

const props = defineProps({
  indexData: {
    type: Object,
    default: () => {}
  },
  startDate: {
    type: String,
    default: ''
  },
  endDate: {
    type: String,
    default: ''
  },
  dateType: {
    type: Number,
    default: 0
  }
})

const typeArr = ['day', 'month', 'year', 'other']
const getParams = computed(() => {
  return `startDate=${props.startDate}&endDate=${props.endDate}&dateType=${typeArr[props.dateType]}`
})
function toProduct() {
  uni.navigateTo({ url: `/pages-analysis/product-analysis/product-list?${getParams.value}` })
}
function toReport() {
  uni.navigateTo({ url: `/pages-analysis/product-analysis/report-work-list?${getParams.value}` })
}

const userInfo = computed(() => {
  return $state.user.userInfo
})
</script>
<style lang="scss" scoped>
.position-icon {
  position: absolute;
  left: px2vw(-32);
  bottom: 0;
}
</style>
