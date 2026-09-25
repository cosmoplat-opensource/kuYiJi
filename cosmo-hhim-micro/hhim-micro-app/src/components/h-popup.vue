<template>
  <uni-popup :ref="setItemRef" class="h-popup">
    <view class="flex justify-center" v-if="content">
      <view class="text box">{{ content }}</view>
    </view>
    <view class="p-16" v-if="imagePath">
      <image :src="imagePath" class="popupImage" mode="aspectFit" />
    </view>
  </uni-popup>
</template>

<script setup lang="ts">
// 弹窗
import { computed } from 'vue'
import { $state, $store } from '@/utils/common'
import { onUnload } from '@dcloudio/uni-app'

const setItemRef = (el) => {
  if (el) {
    $store.commit('popup/addPopupRef', {
      name: routeName.value,
      ref: el
    })
  }
}

const routeName = computed<string>(() => {
  return getCurrentPages()[getCurrentPages().length - 1]?.route
})

const content = computed(() => {
  return $state.popup.popupContent
})

const imagePath = computed(() => {
  return $state.popup.popupImagePath
})

onUnload(() => {
  $state.popup.popupRefList[routeName.value] = null
})
</script>

<style lang="scss" scoped>
.h-popup {
  .text {
    background-color: #fff;
    max-width: 80vw;
    padding: px2vw(16);
    border-radius: px2vw(16);
    word-break: break-word;
  }
  .popupImage {
    width: 80vw;
    height: 80vh;
  }
}
</style>
