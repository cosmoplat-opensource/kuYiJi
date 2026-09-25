<!--4大首页用着套,h-user-info带着顶部的用户模块和分享功能-->
<template>
  <view class="h-full bg-f3f3f5 flex flex-col">
    <!--状态栏,占位-->
    <uni-nav-bar />
    <!--用户模块-->
    <h-user-info />
    <!--内容区域-->
    <view class="flex-1">123</view>
    <!--底部安全区域,ios的x之后会留白-->
    <h-status-footer />
    <h-share />
  </view>
</template>

<script setup lang="ts">
/** 首页添加分享模块,实现小程序右上角的分享功能 */
import HooksShare from '@/hooks/share'
import { onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'
const { shareTimeline, shareAppMessage } = HooksShare()
onShareAppMessage(() => shareAppMessage)
onShareTimeline(() => shareTimeline)
/** 首页添加分享模块,实现小程序右上角的分享功能 */
</script>

<style lang="scss" scoped></style>
