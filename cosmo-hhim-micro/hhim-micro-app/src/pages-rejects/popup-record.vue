<template>
  <uni-popup ref="popupRecord" type="bottom" @maskClick="handleCancel" :safe-area="false">
    <view class="bg-fff flex flex-col justify-start font-28 rounded-16-top">
      <view class="p-24 box flex align-center b-b-1 border-f0f0f0">
        <view class="w-56" />
        <view class="py-12 px-16 flex-1 text-center">
          <text class="font-28 color-5a6f82 bold">质检记录</text>
        </view>
        <image @click="handleCancel" src="/static/images/icon_close_666.svg" class="icon-48" />
      </view>
      <scroll-view scroll-y enable-flex class="m-h-500 flex flex-col">
        <view v-for="(item, index) in dataList" :key="index" class="flex flex-col px-32 py-32 box border-bottom-dashed">
          <view class="flex align-center">
            <view class="mark-24a8ff">质检</view>
            <view class="ml-16 color-333 font-28">{{ item.qcNickName }}</view>
            <view class="flex-1 ml-16 color-333 font-28">{{ formatDate(item.qcDate, 'YYYY-MM-DD HH:mm:ss') }}</view>
            <word-icon class="mr-8" text="共" />
            <view class="color-333 font-28 bold">{{ item.totalNgNum }}</view>
            <view class="color-333 font-28 ml-8 bold">件</view>
          </view>
          <rejects-type-displpy :dataList="item.ngProductDetailInfoList" :unit="item.productUnit || '件'" />
        </view>
        <view class="font-24 color-b6c0c9 py-48 box text-center">已显示全部</view>
      </scroll-view>
    </view>
  </uni-popup>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import WordIcon from '@/components/word-icon.vue'
import HStatusFooter from '@/components/h-status-footer.vue'
import { formatDate } from '../utils/common'
import RejectsTypeDisplpy from '@/components/rejects-type-displpy.vue'
const popupRecord = ref(null)
const props = defineProps({
  dataList: {
    type: Array,
    default: () => []
  }
})

function open() {
  popupRecord.value.open('bottom')
}
function handleCancel() {
  popupRecord.value?.close('bottom')
}

defineExpose({ open })
</script>
<style lang="scss">
.mark-index {
  box-sizing: border-box;
  padding: px2vw(6) px2vw(8);
  background-color: #ebf0f5;
  color: #5a6f82;
  font-size: px2vw(20);
  line-height: px2vw(20);
  border-radius: 0 px2vw(4) px2vw(4) 0;
}
</style>
