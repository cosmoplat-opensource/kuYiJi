<template>
  <view class="bg-F3F3F5 h-full flex flex-col">
    <uni-nav-bar />
    <h-status-header title="购买引导" />
    <scroll-view scroll-y class="flex-1 overflow-hidden">
      <image mode="widthFix" :show-menu-by-longpress="true" :src="formatImage('img_buyguide')" class="w-100" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { formatImage } from '@/utils/common'
</script>

<style lang="scss" scoped></style>
