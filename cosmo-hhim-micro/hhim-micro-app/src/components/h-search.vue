<template>
  <view class="flex align-center rounded-16 pl-32 pr-24" :class="bgClass">
    <image src="/static/images/icon_s_b8.svg" class="icon-24" />
    <input
      v-model="searchValue"
      placeholder-style="color:#B8B8B8"
      class="h-64 pl-16 font-28 box flex-1"
      :placeholder="placeholder"
    />
    <image v-show="searchValue" @tap="handleInputClean" src="/static/images/icon_del.svg" class="icon-48" />
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
const searchValue = ref('')
const props = defineProps({
  placeholder: {
    type: String,
    default: '输入产品编码/名称'
  },
  bgClass: {
    type: String,
    default: 'bg-fff'
  },
  initValue: {
    type: String,
    default: ''
  }
})
const emit = defineEmits(['searchInput'])

function handleInputClean() {
  searchValue.value = ''
}
watch(
  props,
  (val) => {
    searchValue.value = val.initValue
  },
  { immediate: true, deep: true }
)
watch(searchValue, (val) => {
  emit('searchInput', val)
})
</script>

<style lang="scss" scoped>
.select {
  top: px2vw(72);
  left: px2vw(0);
  z-index: 5;
  width: 100vw;
}
</style>
