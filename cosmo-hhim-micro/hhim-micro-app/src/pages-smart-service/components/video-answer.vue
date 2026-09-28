<!--
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author cosmo-hhim-open Team
-->
<template>
  <view class="py-16">
    <view class="w-360 h-480 bg-0066ff rounded-16 flex-center overflow-hidden bg-img">
      <view class="flex flex-col align-center">
        <image :src="formatImage('icon_type_video_80', 'svg')" class="icon-80" />
        <view class="font-28 color-fff mt-32">如何邀请伙伴加入</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { formatImage } from '@/utils/common'
</script>

<style lang="scss" scoped>
.bg-img {
  background: url('/static/micro_app/img_video_bg.png') no-repeat;
  background-size: cover;
}
</style>
