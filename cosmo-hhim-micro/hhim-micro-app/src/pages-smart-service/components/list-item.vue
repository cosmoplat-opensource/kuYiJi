<template>
  <view>
    <view>
      <date-time />
      <view class="w-100 flex justify-end">
        <default-answer moduleType="1" text="如何邀请伙伴加入" />
      </view>
    </view>
    <view>
      <date-time />
      <default-answer showIcon text="您好，智能客服助理为您服务！" />
      <video-answer />
    </view>
  </view>
</template>

<script setup lang="ts">
import DateTime from './date-time.vue'
import DefaultAnswer from './default-answer.vue'
import VideoAnswer from './video-answer.vue'

const props = defineProps({
  dataItem: {
    type: Object,
    default: () => {}
  }
})
</script>

<style lang="scss" scoped></style>
