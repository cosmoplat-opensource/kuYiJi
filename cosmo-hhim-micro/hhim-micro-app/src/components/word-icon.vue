<template>
  <view class="icon-32 rounded-full font-24 flex align-center justify-center" :class="defaultClass">{{ text }}</view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  text: {
    type: String,
    default: '共'
  },
  colorClass: {
    //文字颜色样式名称
    type: String,
    default: ''
  },
  backgroundClass: {
    //文字背景样式名称
    type: String,
    default: ''
  }
})

const defaultClass = computed(() => {
  switch (props.text) {
    case '共':
      return [props.colorClass || 'color-5a6f82', props.backgroundClass || 'bg-EBF0F5']
    case '尾':
      return [props.colorClass || 'color-fff', props.backgroundClass || 'bg-c16ff1']
    case '首':
      return [props.colorClass || 'color-fff', props.backgroundClass || 'bg-24a8ff']
    case '补':
    case '修':
      return [props.colorClass || 'color-fff', props.backgroundClass || 'bg-FE9F00']
    case '审':
    case '检':
      return [props.colorClass || 'color-5a6f82', props.backgroundClass || 'bg-EBF0F5']
    case '改':
      return [props.colorClass || 'color-fff', props.backgroundClass || 'bg-FE9F00']
    case '警':
      return [props.colorClass || 'color-fff', props.backgroundClass || 'bg-FE9F00', 'rounded-4-imp']
    case '废':
      return [props.colorClass || 'color-fff', props.backgroundClass || 'bg-ff0000']
    case '让':
      return [props.colorClass || 'color-fff', props.backgroundClass || 'bg-19AA8D']
    case '转':
      return [props.colorClass || 'color-fff', props.backgroundClass || 'bg-24a8ff']
    case '新':
      return [props.colorClass || 'color-fff', props.backgroundClass || 'bg-FE9F00']
    case '前':
      return [props.colorClass || 'color-fff', props.backgroundClass || 'bg-64B5EA']
    default:
      return [props.colorClass || 'color-5a6f82', props.backgroundClass || 'bg-EBF0F5']
  }
})
</script>

<style lang="scss" scoped>
.rounded-4-imp {
  border-radius: px2vw(4) !important;
}
</style>
