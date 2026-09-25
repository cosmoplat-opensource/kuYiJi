<template>
  <view class="staff-switch relative">
    <view class="wrapper" :class="checked ? 'active' : ''"></view>
    <view class="circle" :class="checked ? 'right' : 'left'"></view>
  </view>
</template>

<script setup lang="ts">
const props = defineProps({
  checked: {
    type: Boolean,
    default: false
  }
})
</script>

<style lang="scss" scoped>
.staff-switch {
  .wrapper {
    width: px2vw(88);
    height: px2vw(40);
    background: #fff;
    border: px2vw(4) solid #e5e5e5;
    border-radius: px2vw(32);
    &.active {
      background: #51c5ff;
      border: px2vw(4) solid #51c5ff;
    }
  }
  .circle {
    position: absolute;
    top: px2vw(4);
    width: px2vw(40);
    height: px2vw(40);
    background: #ffffff;
    border-radius: 50%;
    box-shadow: px2vw(-2) px2vw(4) px2vw(4) px2vw(1) rgba(0, 0, 0, 0.1);
    &.left {
      left: px2vw(4);
    }
    &.right {
      right: px2vw(4);
    }
    transition: left 0.2s linear;
  }
}
</style>
