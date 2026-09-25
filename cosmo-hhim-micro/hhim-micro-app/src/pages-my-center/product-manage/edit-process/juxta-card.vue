<template>
  <view class="flex flex-wrap justify-center m-h-100 overflow-auto">
    <view v-for="(item, index) in techData" :key="index" class="box juxta-card relative">
      <h-text-display :text="item.processName" :width="219" />
      <view class="top-area">
        <word-icon text="首" v-if="item.isFirstProcess === '0'" />
      </view>
      <view class="bottom-area">
        <word-icon text="尾" v-if="item.isLastProcess === '0'" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import HTextDisplay from '@/components/h-text-display.vue'

const props = defineProps({
  techData: {
    type: Array,
    default: () => [] as IProcessItem[]
  }
})
</script>

<style lang="scss" scoped>
.juxta-card {
  height: px2vw(80);
  display: flex;
  align-items: center;
  background: #f7f8e7;
  border-radius: px2vw(16);
  border: px2vw(2) solid #e5e5e5;
  padding: 0 px2vw(24);
  margin-right: px2vw(16);
  margin-bottom: px2vw(16);
  margin-top: px2vw(16);
  font-size: px2vw(28);
  color: #333;
}
.top-area {
  position: absolute;
  z-index: 2;
  left: 50%;
  transform: translateX(-50%) translateY(-50%);
  top: 0;
}
.bottom-area {
  position: absolute;
  z-index: 2;
  left: 50%;
  transform: translateX(-50%) translateY(50%);
  bottom: 0;
}
</style>
