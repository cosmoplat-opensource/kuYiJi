<template>
  <view class="no-data flex flex-col w-100 align-center" :class="className">
    <image src="/static/images/img_tips_noresult.svg" class="img mb-64" />
    <text class="font-24 color-b6c0c9">{{ tipsWord }}</text>
  </view>
</template>
<script setup lang="ts">
const props = defineProps({
  tipsWord: {
    type: String,
    default: '暂无数据'
  },
  className: {
    type: String,
    default: ''
  }
})
</script>
<style lang="scss" scoped>
.no-data {
  .img {
    width: px2vw(480);
    height: px2vw(156);
  }
}
</style>
