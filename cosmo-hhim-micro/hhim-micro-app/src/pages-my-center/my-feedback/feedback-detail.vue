<template>
  <view class="bg-F3F3F5 h-full">
    <uni-nav-bar />
    <h-status-header title="反馈详情" />
    <view class="rounded-16 p-32 box font-28 bg-fff">
      <view class="flex justify-between align-center font-24">
        <view class="color-5a6f82"
          >处理时间：<text class="color-333">{{ dataItem.status === '未处理' ? '-' : dataItem.updateTime }}</text></view
        >
        <view v-if="dataItem.status === '未处理'" class="color-ff0000 flex-1 text-right">{{ dataItem.status }}</view>
        <view v-else-if="dataItem.status === '处理中'" class="color-e7a11a flex-1 text-right">{{
          dataItem.status
        }}</view>
        <view v-else="dataItem.status === '已处理'" class="color-999 flex-1 text-right">{{ dataItem.status }}</view>
      </view>
      <view class="color-5a6f82 mt-32"
        >问题描述：<text class="color-333">{{ dataItem.content }}</text></view
      >
      <view v-if="imageList" class="w-100 flex flex-wrap">
        <view
          class="addImg flex align-center justify-center mt-24 mr-24 relative"
          v-for="(item, index) in imageList"
          :key="index"
          @tap="previewImg(imageList, index)"
        >
          <image :src="item" class="icon-170 rounded-16" mode="aspectFill" />
        </view>
      </view>
      <template v-if="dataItem.status === '已处理'">
        <view class="line w-100 mt-48"></view>
        <view class="res rounded-4 text-center color-5a6f82 font-24 mt-48">处理结果</view>
        <view class="mt-24">{{ dataItem.dealResult }}</view>
      </template>
    </view>
  </view>
</template>

<script setup lang="ts">
import { _get } from '@/utils/common-request'
import { onMounted, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import HooksUploadImage from '@/hooks/upload-image'

onMounted(() => {})
const dataItem = ref<Partial<TDataItem>>({})
const imageList = ref([])

onLoad((e) => {
  dataItem.value = JSON.parse(e.data)
  imageList.value = []
  if (dataItem.value.attachFiles) {
    dataItem.value.attachFiles.forEach((file) => {
      imageList.value.push(file.filePath)
    })
  }
})
const { previewImg } = HooksUploadImage(5)
</script>

<style lang="scss" scoped>
.res {
  width: px2vw(104);
  height: px2vw(32);
  line-height: px2vw(32);
  background: #ebf0f5;
  border: px2vw(2) solid #d3dfeb;
}
.line {
  height: px2vw(0);
  border: px2vw(1) dashed #e5e5e5;
}
</style>
