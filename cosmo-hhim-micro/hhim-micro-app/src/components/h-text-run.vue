<template>
  <view class="marquee-root">
    <view class="marquee-content" :style="animationCss" v-if="String(numbers).length > size">{{ numbers }}</view>
    <view v-else>{{ numbers }}</view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  numbers: {
    type: Number,
    default: ''
  },
  size: {
    type: Number,
    default: 4
  }
})
const animationCss = computed(() => {
  return { animationDuration: String(props.numbers)?.length * 0.2 + 5 + 's' }
})
</script>

<style lang="scss" scoped>
@keyframes marqueeAnim {
  0% {
    transform: translateX(100%);
  }
  100% {
    transform: translateX(-100%);
  }
}

.marquee-content {
  white-space: nowrap;
  display: inline-block;
  animation: marqueeAnim 5s linear 0s infinite;
}

.marquee-root {
  width: 100%;
  text-align: left;
  overflow: hidden;
}
</style>
