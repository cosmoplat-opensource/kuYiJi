<template>
  <view class="flex align-center bg-f7f8e7 px-32 py-16">
    <image src="/static/images/icon_tips.svg" class="icon-32" />
    <view class="flex-1 ml-16 font-24">
      <text class="color-5a6f82">该员工近期 </text>
      <text class="color-ff0000">{{ days }}</text>
      <text class="color-5a6f82"> 次记工信息有误，请仔细核对审核</text>
      <!--      今日记工数量已超过员工日均产能(125)，请仔细核对审核!-->
    </view>
    <image src="/static/images/icon_close_666.svg" class="icon-48" @click="handleClose" />
  </view>
</template>

<script setup lang="ts">
defineProps({
  days: {
    type: Number,
    default: 0
  }
})
const emits = defineEmits(['closeNotice'])
function handleClose() {
  emits('closeNotice')
}
</script>

<style lang="scss" scoped></style>
