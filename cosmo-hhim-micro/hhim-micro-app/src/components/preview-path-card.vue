<template>
  <view
    class="relative path-card flex-center px-16 mx-16 box"
    :style="cardStyle"
    :class="{ line: !isLast && !halfLine, 'half-line': !isLast && halfLine }"
  >
    <!--顶部-->
    <view class="top-area">
      <word-icon text="首" v-if="isFirst" />
      <view class="icon-16 bg-fff rounded-full border-1 border-b6c0c9" v-else></view>
    </view>
    <!--底部-->
    <view class="bottom-area">
      <word-icon text="尾" v-if="isLast" />
      <view class="icon-24 bg-fff rounded-full border-1 border-b6c0c9 flex-center" v-else>
        <img src="/static/images/icon_arr_point.svg" class="icon-16" />
      </view>
    </view>
    <h-text-display :text="item.processName" :width="220" />
  </view>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue'
import { formatPx2Vw } from '@/utils/common'

const props = defineProps({
  item: {
    type: Object as PropType<IProcessPath>,
    default: () => ({})
  },
  halfLine: {
    type: Boolean,
    default: false
  }
})

const isFirst = computed(() => props.item.isFirstProcess === '0' || props.item.parentProcessId === 0)
const isLast = computed(() => props.item.isLastProcess === '0')

const cardStyle = computed(() => {
  return {
    width: `${formatPx2Vw(288)}vw`,
    height: `${formatPx2Vw(80)}vw`,
    background: '#F7F8E7'
  }
})
</script>

<style lang="scss" scoped>
.more-head {
  position: absolute;
  right: px2vw(-16);
  top: 50%;
  transform: translateY(-50%) translateX(100%);
  padding: px2vw(8) px2vw(16);
  background-color: #ffffff;
  border-radius: px2vw(24);
  border: px2vw(2) solid #0066ff;
  font-size: px2vw(28);
  font-weight: bold;
  color: #0066ff;
  &:active {
    color: #ffffff;
    background-color: #0066ff;
    border-radius: px2vw(24);
    border: px2vw(2) solid #0066ff;
  }
}
.path-card {
  border-radius: px2vw(16);
  border: px2vw(2) solid #e5e5e5;
  color: #333;
  font-size: px2vw(28);
  z-index: 1;
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
.line:after {
  content: ' ';
  width: px2vw(1);
  background-color: #fff;
  height: px2vw(48);
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(px2vw(-1));
  border-left: px2vw(2) dashed #cccccc;
  z-index: 1;
}
.half-line:after {
  content: ' ';
  width: px2vw(1);
  background-color: #fff;
  height: px2vw(24);
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(px2vw(-1));
  border-left: px2vw(2) dashed #cccccc;
  z-index: 1;
}
</style>
