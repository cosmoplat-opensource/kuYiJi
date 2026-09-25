<template>
  <view class="h-exception-label" :class="[border, className]" :style="getStyle">
    <text>{{ text }}</text>
  </view>
</template>

<script setup lang="ts">
import { computed, defineComponent } from 'vue'
defineComponent({ name: 'h-exception-label' })

const props = defineProps({
  text: {
    type: String,
    default: ''
  },
  bg: {
    type: String,
    default: '#F26A60'
  },
  textColor: {
    type: String,
    default: '#fff'
  },
  border: {
    type: String,
    default: 'list'
  },
  className: {
    type: String,
    default: 'px-8 py-2'
  }
})

const getStyle = computed(() => {
  return {
    backgroundColor: props.bg,
    color: props.textColor
  }
})
</script>

<style lang="scss" scoped>
.h-exception-label {
  font-size: px2vw(24);
  line-height: px2vw(24);
  color: #ffffff;
  // 卡片边框弧度
  &.list {
    border-radius: 0 0 px2vw(8) 0;
  }
  &.detail {
    border-radius: px2vw(8);
  }
  &.detail-4 {
    border-radius: px2vw(4);
  }
}
</style>
