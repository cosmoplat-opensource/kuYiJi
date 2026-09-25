<template>
  <view class="py-16 flex" :class="moduleType === '0' ? '' : 'justify-end'">
    <view
      class="flex flex-col w-content max-w-560 rounded-16"
      :class="[showIcon ? 'py-16 pl-16 pr-24' : 'py-32 px-24', moduleType === '0' ? 'bg-fff' : 'bg-dbeaff justify-end']"
    >
      <view class="mb-24 flex align-center" v-if="refer">
        <view class="block" />
        <view class="ml-8 color-999 font-20">
          回复：
          <text class="single w-200">{{ refer }}</text>
        </view>
      </view>
      <view class="flex align-center">
        <image :src="formatImage('icon_logo_64', 'svg')" class="icon-64 mr-16" v-if="showIcon" />
        <view class="flex-1 line-height-42">
          <text class="font-28 color-333 text-wrap" :class="[moduleType === '0' ? 'bold' : '']">{{ text }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { formatImage } from '@/utils/common'
const props = defineProps({
  showIcon: {
    type: Boolean,
    default: false
  },
  moduleType: {
    type: String,
    default: '0'
  },
  text: {
    type: String,
    default: ''
  },
  refer: {
    type: String,
    default: ''
  }
})
</script>
<style lang="scss" scoped>
.block {
  width: px2vw(4);
  height: px2vw(20);
  background: #999999;
  border-radius: px2vw(2);
}
</style>
