<!--页面顶部安全区域-->
<template>
  <view class="h-status-header" :style="{ backgroundColor }">
    <view class="p-16">
      <image :src="formatImage('icon_return', 'svg')" class="img-back" @tap="handleBack" v-show="showBack" />
    </view>
    <view class="title">
      <view class="flex align-center">
        <image :src="formatImage(iconOptions.icon, iconOptions.iconType)" class="icon-48 icon_robot" />
        <view class="font-32 color-333 ml-16 title_text">{{ title }}</view>
      </view>
    </view>
  </view>
</template>
<script setup lang="ts">
import { formatImage } from '@/utils/common'
import { PropType } from 'vue'

type TIcon = {
  icon: string
  iconType: string
}

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  showBack: {
    type: Boolean,
    default: true
  },
  backgroundColor: {
    type: String,
    default: '#f3f3f5'
  },
  iconOptions: {
    type: Object as PropType<TIcon>,
    default: {
      icon: '',
      iconType: ''
    }
  }
})

function handleBack() {
  uni.navigateBack()
}
</script>
<style lang="scss">
.h-status-header {
  height: px2vw(88);
  display: flex;
  align-items: center;
  padding-left: px2vw(8);
  padding-right: px2vw(24);
  position: relative;

  .img-back {
    width: px2vw(48);
    height: px2vw(48);
  }

  .title {
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
    font-size: px2vw(32);
    color: #333;
    font-weight: bold;
    &_text {
      font-weight: 700;
    }
  }
}

.icon_robot {
  animation: bounce-down 1.5s infinite;
  background-color: white;
  border-radius: 50%;
  padding: px2vw(5) px2vw(2);
}

@keyframes bounce-down {
  15% {
    transform: translateY(px2vw(-4));
  }
  30% {
    transform: translateY(0);
  }
  45% {
    transform: translateY(px2vw(4));
  }
  60% {
    transform: translateY(0);
  }
}
</style>
