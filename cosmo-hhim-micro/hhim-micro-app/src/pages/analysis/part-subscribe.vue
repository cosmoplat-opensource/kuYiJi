<template>
  <view
    class="pr-24 box bg-fff rounded-16 flex align-center border-bottom-f5f5f5"
    :class="analysis ? 'pl-24 py-16' : 'pl-32 py-32'"
    @tap="handleSubscribe"
  >
    <image :src="formatImage('icon_rss', 'svg')" class="icon-48" />
    <view class="flex-1 font-28 bold" :class="analysis ? 'ml-8' : 'ml-16'">生产日报</view>
    <view v-if="subscribeStatusShow" class="mark-ebf0f5 mr-32"> 已订阅 </view>
    <h-button type="border-1 border-0066ff color-0066ff bg-fff" font="font-24" width="80" height="48" text="订阅" />
  </view>
</template>

<script setup lang="ts">
import HooksSubscribe from '@/hooks/subscribe'
import { formatImage } from '@/utils/common'
import { onMounted } from 'vue'

const { checkSubscribeStatus, subscribeStatusShow, handleSubscribe } = HooksSubscribe()

const props = defineProps({
  analysis: {
    type: Boolean,
    default: false
  }
})

onMounted(() => {
  checkSubscribeStatus()
})
</script>

<style lang="scss" scoped>
.btn-rss {
  height: px2vw(48);
  line-height: px2vw(48);
  text-align: center;
  background: rgba(61, 130, 234, 0);
  border-radius: px2vw(32);
  border: px2vw(2) solid #0066ff;
  color: #0066ff;
  &:active {
    background-color: #0066ff;
    color: #fff;
  }
}
</style>
