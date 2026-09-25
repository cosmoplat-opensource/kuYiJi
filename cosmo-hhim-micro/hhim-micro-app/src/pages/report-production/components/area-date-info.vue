<template>
  <view class="area-date-info">
    <template v-if="props.showPass">
      <view class="area-date-info--l">良</view>
      <text class="bold font-28">{{ props.dataItem.pass || 0 }}{{ unit }}</text>
    </template>
    <template v-if="props.showNg">
      <view class="mark-ebf0f5 ml-24 mr-12">不良</view>
      <text class="bold font-28">{{ props.dataItem.ng || 0 }}{{ unit }}</text>
    </template>
  </view>
</template>

<script setup>
const props = defineProps({
  dataItem: {
    type: Object,
    default: () => {}
  },
  showNg: {
    type: Boolean,
    default: true
  },
  showPass: {
    type: Boolean,
    default: true
  },
  unit: {
    type: String,
    default: ''
  }
})
</script>

<style lang="scss">
.area-date-info {
  display: flex;
  align-items: center;
  &--l {
    border-radius: 50%;
    background: #19aa8d;
    font-size: px2vw(24);
    margin-right: px2vw(8);
    color: #fff;
    width: px2vw(32);
    height: px2vw(32);
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
