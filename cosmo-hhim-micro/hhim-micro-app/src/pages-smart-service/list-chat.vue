<template>
  <view class="flex flex-col gap-32">
    <view v-for="(item, index) in dataList" :key="index">
      <template v-if="item.answerType === 'TEXT'">
        <default-answer :text="item.answer" show-icon v-if="item.showIcon" />
        <default-answer :text="item.ask" module-type="1" v-else-if="item.ask" />
        <default-answer :text="item.answer" v-else />
      </template>
      <template v-if="item.answerType === 'PICTURE'">
        <image :src="item.answer" mode="aspectFit" lazy-load @tap="handlePreview(item.answer)" />
      </template>
      <template v-if="item.answerType === 'VIDEO'">
        <video :src="item.answer" />
      </template>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { $store } from '@/utils/common'
import DefaultAnswer from '@/pages-smart-service/components/default-answer.vue'
import HooksPopup from '@/hooks/popup'

const dataList = computed(() => {
  return $store.state.service.chatList
})
const { popupOpenImage } = HooksPopup()
function handlePreview(url) {
  popupOpenImage(url)
}
</script>

<style scoped lang="scss">
video {
  width: px2vw(360);
  max-height: px2vw(480);
}
image {
  width: px2vw(360);
  max-height: px2vw(480);
  will-change: transform;
}
</style>
