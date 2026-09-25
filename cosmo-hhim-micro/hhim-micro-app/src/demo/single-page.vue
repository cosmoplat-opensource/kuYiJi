<!--基础的单页面-->
<template>
  <view class="h-full bg-f3f3f5 flex flex-col">
    <!--状态栏,占位-->
    <uni-nav-bar />
    <!--标题模块,带返回按钮-->
    <h-status-header title="xxxx" />
    <!--内容区域-->
    <view class="flex-1">内容展示</view>
    <!--底部安全区域,ios的x之后会留白-->
    <h-status-footer />
  </view>
</template>

<script setup lang="ts"></script>

<style lang="scss" scoped></style>
