<template>
  <view class="p-16 flex align-center">
    <h-text-display :text="productName" :width="424" class="color-5a6f82 font-28" />
    <view class="flex-1" />
    <h-button
      height="48"
      width="128"
      text="复制工艺"
      font="font-24"
      @tap="handleProductShow"
      type="border-1 border-0066ff color-0066ff bg-fff mr-16"
    />
  </view>
  <view class="bg-fff mb-16 mx-16 box py-16 flex align-center">
    <view class="block" />
    <view class="ml-16 font-28 color-5a6f82 bold">{{ sortType }}生产</view>
    <view class="ml-32 rounded-8 bg-f3f3f5 color-0066ff font-24 py-12 px-16 btn-sort" @tap="handleSortChange">
      转为{{ sortType === '顺序' ? '并序' : '顺序' }}
    </view>
  </view>
  <view class="box flex align-center mx-16 bg-f7f8e7 px-16 h-64 mb-8" v-if="sortType === '并序'">
    <image src="/static/images/icon_tips.svg" class="icon-32" />
    <view class="flex-1 ml-16">
      <text class="font-24 color-5a6f82">保存后，记工工序只能在以下工序内选择</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import HTextDisplay from '@/components/h-text-display.vue'
const props = defineProps({
  sortType: {
    type: String,
    default: '顺序'
  },
  productName: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['productShow', 'sortChange'])

function handleProductShow() {
  emit('productShow')
}

function handleSortChange() {
  emit('sortChange')
}
</script>
