<template>
  <view class="btn-wrapper flex-center box" :class="[type, `active-${active}`, font]" :style="sizeStyle">
    <slot>
      <view class="single">{{ props.text }}</view>
    </slot>
  </view>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { formatPx2Vw } from '@/utils/common'

const props = defineProps({
  active: {
    type: String,
    default: 'blue' //blue  red
  },
  type: {
    type: String,
    default: 'bg-0066ff color-fff'
  },
  width: {
    type: [String, Number],
    default: 272
  },
  height: {
    type: [String, Number],
    required: true,
    default: 72
  },
  text: {
    type: String,
    default: '按钮'
  },
  font: {
    type: String,
    default: 'font-28'
  }
})

const sizeStyle = computed(() => {
  let width = ''
  if (typeof props.width === 'string') {
    if (props.width.includes('%')) {
      width = props.width
    } else {
      width = `${formatPx2Vw(Number(props.width))}vw`
    }
  } else {
    width = `${formatPx2Vw(props.width)}vw`
  }
  const height = Number(props.height)
  // 圆角固定为高度的一半
  return {
    width,
    height: `${formatPx2Vw(height)}vw`,
    borderRadius: `${formatPx2Vw(height / 2)}vw`
  }
})
</script>
<style lang="scss" scoped>
.btn-wrapper {
  &.active-blue:active {
    color: #ffffff;
    background-color: #0044e7;
  }
  &.active-red:active {
    color: #ffffff;
    background-color: #cc0000;
  }
  &.active-grey:active {
    color: #ffffff;
    background-color: #b6c0c9;
  }
}
</style>
