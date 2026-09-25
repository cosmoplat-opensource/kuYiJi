<template>
  <view class="flex-center btn-data-health px-32" @tap.stop="handleClick">
    <image src="/static/images/icon_health.svg" class="icon-32" />
    <text class="ml-8 font-24 color-fff" v-if="checkAudit">记工风险监控</text>
    <text class="ml-8 font-24 color-fff" v-else>{{ keyWord }}：{{ getValue }}</text>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  value: {
    type: Number,
    default: 0
  },
  url: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: ''
  },
  keyWord: {
    type: String,
    default: '数据健康度'
  }
})

const getValue = computed(() => {
  return Math.min(100, props.value) + '%'
})
const checkAudit = computed(() => {
  return props.type === '审核'
})

function handleClick() {
  uni.navigateTo({ url: props.url })
}
</script>

<style lang="scss" scoped>
.btn-data-health {
  height: px2vw(64);
  background: linear-gradient(180deg, #7ccbff 0%, #24a8ff 100%);
  box-shadow: 0 px2vw(8) px2vw(16) px2vw(1) #d8dde5;
  border-radius: px2vw(32);
  &:active {
    background: linear-gradient(360deg, #7ccbff 0%, #24a8ff 100%);
  }
}
</style>
