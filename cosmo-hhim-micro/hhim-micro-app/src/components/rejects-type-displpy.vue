<template>
  <view class="pt-8 mx-32 box flex flex-wrap" :class="className" v-if="formatList.length">
    <view
      v-for="(mark, index) in formatList"
      :key="index"
      class="mr-16 mt-16 flex align-center h-48 px-8 box bg-f3f3f5 rounded-8"
    >
      <view class="color-5a6f82 font-28">{{ mark.ngType }}</view>
      <view class="bold color-333 font-28 ml-8">{{ mark.ngNum }}{{ unit || '件' }}</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  dataList: {
    type: Array,
    default: () => []
  },
  unit: {
    type: String,
    default: ''
  },
  className: {
    type: String,
    default: ''
  }
})

const formatList = computed(() => {
  return props.dataList?.filter((v) => v.ngType) ?? []
})
</script>

<style lang="scss" scoped></style>
