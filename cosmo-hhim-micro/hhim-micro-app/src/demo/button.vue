<template>
  <view class="h-full bg-f3f3f5 flex flex-col">
    <!--状态栏,占位-->
    <uni-nav-bar />
    <!--用户模块-->
    <h-status-header title="按钮demo" />
    <!--内容区域-->
    <view class="flex-1 flex flex-col">
      <view class="mt-32 flex justify-center">
        <h-button width="296" height="72" text="取消" type="bg-fff color-333" />
      </view>
      <view class="mt-32 flex justify-center">
        <h-button width="296" height="72" text="确认" />
      </view>
      <view class="mt-32 flex justify-center">
        <h-button width="296" height="72" text="删除" type="ml-32 bg-ff0000 color-fff" active="red" />
      </view>
    </view>
    <!--底部安全区域,ios的x之后会留白-->
    <h-status-footer />
  </view>
</template>

<script setup lang="ts"></script>

<style lang="scss" scoped></style>
