<template>
  <view class="flex align-center bg-f7f8e7 px-32 py-16">
    <image src="/static/images/icon_tips.svg" class="icon-32" />
    <view class="flex-1 ml-16 font-24 single">
      <template v-if="dataItem.days">
        <text class="color-5a6f82">{{ dataItem.name }} 近期记工出错 </text>
        <text class="color-ff0000">{{ dataItem.days }}</text>
        <text class="color-5a6f82"> 次，请仔细核对</text>
      </template>
      <template v-if="dataItem.count">
        <text class="color-5a6f82">已超过工序日产能(</text>
        <text class="color-ff0000">{{ formatCount }}</text>
        <text class="color-5a6f82">)，请仔细核对</text>
      </template>
      <!--记工风险监控跳转到审核列表的提示条-->
      <template v-if="dataItem.governCount">
        <text class="color-5a6f82">存在{{ dataItem.governType }}的共</text>
        <text class="color-ff0000">{{ dataItem.governCount }}</text>
        <text class="color-5a6f82">条，请核对</text>
      </template>
    </view>
    <h-button
      type="border-1 border-0066ff color-0066ff bg-fff"
      font="font-24"
      height="48"
      width="128"
      text="全部员工"
      @tap="handleClose"
    />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
const props = defineProps({
  dataItem: {
    type: Object,
    default: 0
  }
})
const emits = defineEmits(['clearSearch'])

const formatCount = computed(() => {
  return Math.floor(props.dataItem.count)
})
function handleClose() {
  emits('clearSearch')
}
</script>

<style lang="scss" scoped></style>
