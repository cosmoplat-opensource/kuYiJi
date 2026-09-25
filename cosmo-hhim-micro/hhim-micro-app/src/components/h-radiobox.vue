<template>
  <view class="h-radio-box" @tap="handleChecked">
    <view class="inner-wrapper" v-if="checked" />
  </view>
</template>

<script setup lang="ts">
const props = defineProps({
  checked: {
    type: Boolean,
    default: false
  }
})

const emits = defineEmits(['checkedChange'])

function handleChecked() {
  emits('checkedChange', !props.checked)
}
</script>

<style lang="scss">
.h-radio-box {
  width: px2vw(32);
  height: px2vw(32);
  border-radius: 50%;
  background-color: #fff;
  border: px2vw(2) solid #0066ff;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  .inner-wrapper {
    width: px2vw(22);
    height: px2vw(22);
    border-radius: 50%;
    background-color: #0066ff;
  }
}
</style>
