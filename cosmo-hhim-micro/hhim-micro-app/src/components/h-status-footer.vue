<!--页面底部安全区域-->
<template>
  <view class="h-status-footer" :style="formatStyle" />
</template>
<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  backgroundColor: {
    type: String,
    default: '#f3f3f5'
  }
})

const formatStyle = computed(() => {
  return {
    backgroundColor: props.backgroundColor
  }
})
</script>
<style lang="scss">
.h-status-footer {
  width: 100%;
  height: env(safe-area-inset-bottom);
}
</style>
