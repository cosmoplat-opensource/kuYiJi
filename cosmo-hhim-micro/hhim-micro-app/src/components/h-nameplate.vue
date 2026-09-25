<template>
  <view class="h-nameplate">
    <view class="rounded-4 font-24 label overflow-hidden" :class="type">
      <h-text-display :text="label" :width="width" />
    </view>
    <view class="rounded-4 font-24 value overflow-hidden" :class="type">
      <h-text-display :text="value" :width="width" />
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps({
  label: {
    type: String,
    default: ''
  },
  value: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: 'process'
  },
  width: {
    type: Number,
    default: 204
  }
})
</script>

<style lang="scss" scoped>
.h-nameplate {
  display: flex;
  .label {
    height: px2vw(32);
    display: flex;
    align-items: center;
    justify-content: center;
    padding-left: px2vw(4);
    padding-right: px2vw(4);
    &.process {
      background-color: #64b5ea;
      // border: 1px solid #64b5ea;
      color: #fff;
    }
    &.product {
      background-color: #def2ff;
      color: #418cbd;
      // border: 1px solid #64b5ea;
    }
    &.purple {
      background-color: #c16ff1;
      color: #ffffff;
      // border: 1px solid #c16ff1;
    }
  }
  .value {
    height: px2vw(32);
    display: flex;
    align-items: center;
    justify-content: center;
    padding-left: px2vw(8);
    padding-right: px2vw(4);
    max-width: px2vw(204);
    &.process {
      background-color: #def2ff;
      color: #418cbd;
    }
    &.product {
      color: #5a6f82;
    }
    &.purple {
      background-color: #00000000;
      color: #333333;
      // border: 1px solid #00000000;
    }
  }
}
</style>
