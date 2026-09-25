<template>
  <view class="h-88 flex">
    <view class="flex align-center px-32" @click="toMy">
      <image :src="userInfo.avatar || '/static/images/img_default.svg'" class="icon-56 rounded-full" />
      <text class="ml-16 font-32" :class="textColor">{{ userInfo.nickName }}</text>
      <text class="ml-16 font-24 color-b8b8b8">{{ tenantName }}</text>
    </view>
    <view class="flex-1" />
    <view
      v-if="uuc"
      class="flex align-center h-64 w-80 flex align-center pr-32 justify-center px-24 bg-fff border-2 border-cad7eb rounded-32"
      @tap="handleQuit"
    >
      <image src="/static/images/icon_2circle.svg" class="icon-40" />
    </view>
  </view>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'
import { $state } from '@/utils/common'
import { uuc, formatImage } from '@/utils/common'
const props = defineProps({
  textColor: {
    type: String,
    default: 'color-333'
  }
})
const userInfo = computed(() => {
  return $state.user.userInfo
})
const tenantName = ref(uni.getStorageSync('micro_tenantName') || '')

function toMy() {
  uni.navigateTo({
    url: '/pages-my-center/my-reviewer'
  })
}
function handleQuit() {
  plus.runtime.quit()
}
</script>
