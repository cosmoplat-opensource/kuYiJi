<template>
  <view class="audit-switch" @tap="handleSwitchChange">
    <view class="text left" :class="{ active: check && tabActive === 1, disable: tabActive === 0 }">产品</view>
    <view class="text right" :class="{ active: !check && tabActive === 1, disable: tabActive === 0 }">员工</view>
  </view>
</template>

<script setup lang="ts">
const props = defineProps({
  check: {
    type: Boolean,
    default: false
  },
  tabActive: {
    type: Number,
    default: 1
  }
})

const emits = defineEmits(['switchChange'])

function handleSwitchChange() {
  if (props.tabActive === 0) return
  emits('switchChange', !props.check)
}
</script>

<style lang="scss">
.audit-switch {
  display: flex;
  .text {
    width: px2vw(80);
    height: px2vw(48);
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: px2vw(24);
    color: #5a6f82;
    background-color: #ebf0f5;
    border: 1px solid #d3dfeb;
    &.left {
      border-radius: px2vw(32) 0 0 px2vw(32);
      left: px2vw(16);
      border-right: 0;
      &.active {
        border: 1px solid #51c5ff;
      }
    }
    &.right {
      border-radius: 0 px2vw(32) px2vw(32) 0;
      left: px2vw(52);
      border-left: 0;
      &.active {
        border: 1px solid #51c5ff;
      }
    }
    &.active {
      color: #fff;
      background-color: #51c5ff;
    }
    &.disable {
      color: #9badbc;
      background: #ebf0f5;
      border: 1px solid #d3dfeb;
      &.left {
        border-right: 0;
      }
    }
  }
}
</style>
