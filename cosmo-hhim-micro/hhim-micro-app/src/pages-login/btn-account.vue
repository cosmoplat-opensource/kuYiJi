<template>
  <view class="btn-account wrapper-outer">
    <text class="btn-account__text">{{ props.text }}</text>
  </view>
</template>
<script setup lang="ts">
const props = defineProps({
  text: {
    type: String,
    default: ''
  }
})
</script>
<style lang="scss">
.btn-account {
  &.wrapper-outer {
    display: flex;
    align-items: center;
    justify-content: center;
    width: px2vw(528);
    height: px2vw(74);
    border-radius: px2vw(37) px2vw(37) px2vw(37) px2vw(37);
    border: px2vw(2) solid #24a8ff;
  }

  &.wrapper-outer:active {
    background: #24a8ff;
    .btn-account__text {
      color: #fff;
    }
  }

  &__text {
    color: #24a8ff;
    font-size: px2vw(32);
    line-height: px2vw(32);
  }
}
</style>
