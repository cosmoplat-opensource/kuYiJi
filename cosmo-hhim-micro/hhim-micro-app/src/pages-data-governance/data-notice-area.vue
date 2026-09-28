<!--
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author cosmo-hhim-open Team
-->
<template>
  <view class="flex align-center bg-f7f8e7 px-32 py-16">
    <image src="/static/images/icon_tips.svg" class="icon-32" />
    <view class="flex-1 ml-16">
      <text class="font-24 color-5a6f82">{{ text }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
const props = defineProps({
  text: {
    type: String,
    default: ''
  }
})
</script>

<style lang="scss" scoped></style>
