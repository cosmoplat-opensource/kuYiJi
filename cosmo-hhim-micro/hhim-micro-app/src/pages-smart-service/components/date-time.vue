<!--
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author cosmo-hhim-open Team
-->
<template>
  <view class="flex justify-center py-16">
    <view class="px-24 py-12 bg-fff rounded-24 font-24 color-5a6f82">{{ dateTime }}</view>
  </view>
</template>

<script setup lang="ts">
const props = defineProps({
  dateTime: {
    type: String,
    default: ''
  }
})
</script>

<style lang="scss" scoped></style>
