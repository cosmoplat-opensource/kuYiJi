<template>
  <view class="h-button--record wrapper-outer" :class="props.type">
    <view class="wrapper-middle">
      <view class="wrapper-inner">
        <slot />
      </view>
    </view>
  </view>
</template>
<script lang="ts" setup>
const props = defineProps({
  type: {
    type: String,
    default: 'blue'
  }
})
</script>
<style lang="scss">
.h-button--record {
  &.wrapper-outer {
    display: flex;
    align-items: center;
    justify-content: center;
    background: #ffffff;
    box-shadow: px2vw(8) px2vw(8) px2vw(16) px2vw(1) #d8dde5;
    border-radius: 50%;
    .wrapper-middle {
      padding: px2vw(4);
      display: flex;
      align-items: center;
      justify-content: center;

      box-shadow: px2vw(-8) px2vw(-8) px2vw(16) px2vw(1) #ffffff;
      border-radius: 50%;
      .wrapper-inner {
        padding: px2vw(12);
        display: flex;
        align-items: center;
        justify-content: center;

        border-radius: 50%;
      }
    }
  }
  &.white {
    .wrapper-middle {
      background: linear-gradient(180deg, #f5f7fa 0%, #ffffff 100%);
    }
    .wrapper-inner {
      background: linear-gradient(180deg, #ffffff 0%, #f5f7fa 100%);
    }
  }
  &.blue {
    .wrapper-middle {
      background: linear-gradient(180deg, #629bf0 0%, #89b4f5 100%);
    }
    .wrapper-inner {
      background: linear-gradient(180deg, #89b4f5 0%, #0066ff 100%);
    }
  }
}
</style>
