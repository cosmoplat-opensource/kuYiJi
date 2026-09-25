<template>
  <view class="flex flex-col mt-40">
    <view class="color-5a6f82">产品图片({{ uploadImageList.length }}/5)</view>
    <view class="flex flex-wrap">
      <view
        class="addImg flex align-center justify-center mt-24 mr-24 relative"
        v-for="(item, index) in uploadImageList"
        :key="index"
        @tap="previewImg(uploadImageList, index)"
      >
        <image :src="item" class="icon-170 rounded-16" />
        <view @tap.stop="deleteImage(index)" class="icon-72 tap-view">
          <view class="del flex align-center justify-center">
            <image src="/static/images/icon_delete_24.svg" class="icon-24" />
          </view>
        </view>
      </view>
      <view
        v-if="uploadImageList.length <= 4"
        class="addImg border-2-dashed flex align-center justify-center mt-24"
        @tap="handleSelectImages"
      >
        <image src="/static/images/icon_camera.svg" class="icon-72" />
      </view>
    </view>
  </view>
  <canvas
    v-for="idx in 5"
    :key="idx"
    :canvas-id="'compress_canvas' + idx"
    :id="'compress_canvas' + idx"
    class="compress_canvas"
    :style="{ width: '800px', height: '800px' }"
  />
</template>

<script lang="ts" setup>
// 上传图片模块
import { getCurrentInstance, ref } from 'vue'
import HooksUploadImage from '@/hooks/upload-image'

const { choseImage, uploadImageList, uploadImage, setImageList, previewImg, removeImage } = HooksUploadImage(5)
function handleSelectImages() {
  choseImage(getCurrentInstance(), { width: 800, height: 800, zip: true }).then((res) => {
  })
}
function deleteImage(index) {
  uni.showModal({
    title: '提示',
    content: '确认要删除这张图片吗',
    success: function (res) {
      if (res.confirm) {
        removeImage(index)
      } else if (res.cancel) {
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.addImg {
  width: px2vw(170);
  height: px2vw(170);
  background: #ffffff;
  border-radius: px2vw(16);
  opacity: 1;
  .tap-view {
    position: absolute;
    right: 0;
    top: 0;
  }
  .del {
    width: px2vw(40);
    height: px2vw(40);
    background: rgba(0, 0, 0, 0.3);
    border-radius: 0px px2vw(16) 0px px2vw(16);
    position: absolute;
    right: 0;
    top: 0;
  }
}
.compress_canvas {
  position: absolute;
  left: 10000px;
}
</style>
