<template>
  <view class="help-center mt-16">
    <view class="flex align-center justify-between b-b-1 border-f5f5f5 pb-24">
      <view class="flex-center">
        <image :src="formatImage('icon_help', 'svg')" class="icon-48 mr-16" />
        <view class="font-28 color-333 bold">帮助中心</view>
      </view>
      <image :src="formatImage('icon_extend', 'svg')" class="icon-32" @tap="handleJump" />
    </view>
    <view
      class="pt-32"
      :class="{ 'pb-32 b-b-2-dashed': index + 1 !== helpList.length }"
      v-for="(item, index) in helpList"
      :key="index"
      @tap="handleJump(item.videoIndex)"
    >
      <text class="font-28 desc-chunk relative">{{ item.desc }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
// 帮助中心
import { ref } from 'vue'
import { formatImage } from '@/utils/common'

const helpList = ref<{ desc?: string; videoIndex: number }[]>([
  { desc: '审产有防错，可预防超报、漏报', videoIndex: 7 },
  { desc: '在制品库存了如执掌，生产调度更轻松', videoIndex: 8 },
  { desc: '如何新增产品，新增工艺', videoIndex: 22 }
])

function handleJump(index = 0) {
  uni.navigateTo({ url: '/pages-novice-guidance/index?videoIndex=' + index })
}
</script>

<style lang="scss" scoped>
.help-center {
  padding: px2vw(24) px2vw(32) px2vw(32);
  background: #ffffff;
  border-radius: px2vw(16);
  .b-b-2-dashed {
    border-bottom: px2vw(2) dashed #e5e5e5;
  }
  .desc-chunk {
    color: #333333;
    &:active {
      color: #0066ff;
    }

    &::after {
      content: '';
      width: px2vw(32);
      height: px2vw(32);
      background-image: url('/static/micro_app/icon_pointer.svg');
      background-size: 100%;
      position: absolute;
      bottom: px2vw(4);
      margin-left: px2vw(16);
    }
  }
}
</style>
