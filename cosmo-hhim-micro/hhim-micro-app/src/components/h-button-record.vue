<template>
  <view class="h-button--record" :class="size">
    <slot>
      <text class="text">{{ props.text }}</text>
    </slot>
  </view>
</template>
<script setup lang="ts">
const props = defineProps({
  text: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    default: 'icon-144'
  }
})
</script>
<style lang="scss" scoped>
.h-button--record {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #0066ff;
  border-radius: 50%;
  .text {
    color: #fff;
    font-size: px2vw(40);
    line-height: px2vw(40);
    font-weight: 500;
  }
  &:active {
    background-color: #0044e7;
  }
}
.icon-144 {
  width: 72px;
  height: 72px;
}
.icon-112 {
  width: 56px;
  height: 56px;
}
</style>
