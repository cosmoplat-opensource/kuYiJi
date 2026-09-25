<!--选择日期-->
<template>
  <view class="h-full bg-f3f3f5 flex flex-col">
    <uni-nav-bar />
    <h-status-header title="我的记工" />
    <calendar class="flex-1 overflow-auto" />
    <h-status-footer />
    <h-popup />
  </view>
</template>

<script setup lang="ts">
import Calendar from '@/pages/report-production/calendar.vue'
</script>

<style lang="scss"></style>
