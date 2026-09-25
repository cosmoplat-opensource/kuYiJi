<template>
  <view class="flex flex-col relative b-t-1 border-ececec">
    <view class="tab-bar-shadow w-full" />
    <view class="h-112 flex align-center">
      <view
        v-for="(item, index) in tabList"
        :key="item.pagePath"
        class="flex-1 flex flex-col align-center justify-center"
        @tap="handleTabSelect(index)"
      >
        <image :src="formatImage(item.checked ? item.selectedIconPath : item.iconPath, 'svg')" class="icon-48" />
        <view class="mt-8 font-24" :class="item.checked ? 'color-0066ff' : 'color-333'">{{ item.text }}</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { $store, formatImage } from '@/utils/common'

const tabList = computed(() => $store.state.tabBar.tabList)

function handleTabSelect(index) {
  $store.commit('tabBar/setTabActive', index)
}
</script>

<style lang="scss" scoped>
.tab-bar-shadow {
  position: absolute;
  bottom: px2vw(112);
  height: px2vw(32);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0) 0%, #ffffff 100%);
  border-radius: 0;
}
</style>
