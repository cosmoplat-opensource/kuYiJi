<template>
  <view class="flex flex-col gap-32">
    <view v-for="(item, index) in historyList" :key="index">
      <date-time :dateTime="dayjs(item.sortDate).format('YYYY-MM-DD HH:mm:ss')" />
      <!--      用户-->
      <default-answer :text="item.question" module-type="1" v-if="!item.answer" />
      <!--      客服-->
      <default-answer :text="item.answer" :refer="item.question" module-type="0" v-else />
    </view>
  </view>
</template>

<script setup lang="ts">
import DefaultAnswer from '@/pages-smart-service/components/default-answer.vue'
import DateTime from '@/pages-smart-service/components/date-time.vue'
import dayjs from 'dayjs'

const props = defineProps({
  historyList: {
    type: Array,
    default: []
  }
})
</script>

<style scoped lang="scss"></style>
