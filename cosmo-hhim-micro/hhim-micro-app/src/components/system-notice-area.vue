<template>
  <view class="mx-16 mt-8 box flex align-center bg-f7f8e7 px-16 py-16" v-if="newValue">
    <image src="/static/images/icon_tips.svg" class="icon-32 icon-tips" />
    <view class="flex-1 ml-16">
      <view class="font-24 color-5a6f82 notice-text">{{ newValue }}</view>
    </view>
    <image @tap="handleCloseNotice" src="/static/images/icon_close_666.svg" class="icon-48" />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const emits = defineEmits(['update:modelValue'])

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

const newValue = computed({
  get: function () {
    return props.modelValue
  },
  set: function (value) {
    emits('update:modelValue', value)
  }
})

function handleCloseNotice() {
  emits('update:modelValue', '')
}
</script>
<style lang="scss" scoped>
.icon-tips {
  animation: iconNotice 1.2s;
  animation-delay: 1.5s;
}
.notice-text {
  animation: textNotice 1.5s linear;
}
@keyframes iconNotice {
  0% {
    transform: scale(1);
  }
  25% {
    transform: scale(1.3);
  }
  75% {
    transform: scale(0.8);
  }
  100% {
    transform: scale(1);
  }
}
@keyframes textNotice {
  0% {
    transform: translateX(100%);
  }
  100% {
    transform: translateX(0);
  }
}
</style>
