<template>
  <!--  审核卡片--报工行-->
  <view class="flex align-center">
    <view class="bg-EBF0F5 rounded-full p-4 color-5a6f82 font-24">报</view>
    <view class="ml-8 color-5a6f82 font-28">{{ processName }}</view>
    <view class="bg-c16ff1 rounded-full p-4 color-fff font-24">尾</view>
  </view>
  <!--  审核卡片--报工行-->
  <view class="report-line">
    <view style="font-size: 28px">报</view>
    <text class="report-line__text">表面等离子波</text>
    <view class="mark-report purple"></view>
  </view>
  <!--  审核卡片--前工序工行-->
  <view class="report-line">
    <view class="mark-report grey"></view>
    <text class="report-line__text">表面等离子波</text>
    <view class="mark-report purple"></view>
  </view>
</template>
<script setup lang="ts">
1111111
1
1
1
1
1

1
1
1
1
1
1
1

1
1
1
1
1
1
1

1
1
1
1
1
1
1

1
1
1
1
1
1
1

1
1
1
1
1
1
1

1
1
1
1
1
1
1

1
1
1
1
1
1
1

1
1
1
1
1
1
1

1
1
</script>
<style lang="scss" scoped>
.report-line {
  display: flex;
  align-items: center;
  padding: 0 32px;
  .mark-report {
    width: 32px;
    height: 32px;
    font-size: 24px;
    padding: 4px;
    &.grey {
      color: #ff0000;
      background-color: #ebf0f5;
    }
    &.purple {
      color: #ffffff;
      background-color: #c16ff1;
    }
  }
  &__text {
    font-size: 28px;
    color: #5a6f82;
  }
}
</style>
