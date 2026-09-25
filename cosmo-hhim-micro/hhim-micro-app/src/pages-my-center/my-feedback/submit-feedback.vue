<template>
  <view class="bg-F3F3F5 h-full flex flex-col">
    <uni-nav-bar />
    <h-status-header title="联系我们" />
    <view class="p-16 box">
      <view class="rounded-16 bg-fff p-32 box">
        <view class="flex-ac justify-between mb-24">
          <view class="font-28 color-5a6f82"><text class="color-ff0000">*</text>问题描述</view>
          <view class="flex-ac" @tap="phoneCall"
            ><text class="font-28 color-333">400-135-7277</text
            ><image :src="formatImage('icon_phone')" class="icon-40 ml-16"
          /></view>
        </view>
        <h-textarea v-model="postData.content" :maxlength="255" placeholder="输入问题描述" />
      </view>
      <view class="rounded-16 bg-fff p-32 box flex flex-col mt-8">
        <view class="font-28 color-5a6f82">上传图片({{ uploadImageList.length }}/5)</view>
        <view class="w-100 flex flex-wrap">
          <view
            class="addImg flex align-center justify-center mt-24 mr-24 relative"
            v-for="(item, index) in uploadImageList"
            :key="index"
            @tap="previewImg(uploadImageList, index)"
          >
            <image :src="item" class="icon-170 rounded-16" mode="aspectFill" />
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
      <view class="h-112 rounded-16 bg-fff px-32 box flex align-center mt-8">
        <view class="font-28 color-5a6f82"><text class="color-ff0000">*</text>联系电话</view>
        <input
          v-model="postData.contactNumber"
          class="flex-1 ml-64 font-28"
          placeholder-class="font-28 color-b8b8b8"
          type="number"
          placeholder="输入联系电话"
        />
      </view>
    </view>
    <view class="bottom-btn px-48 py-32 box flex justify-center align-end font-32 flex-1 gap-32">
      <h-button
        v-if="btnViewListShow"
        width="100%"
        height="72"
        text="查看历史反馈"
        class="flex-1"
        type="border-1 border-0066ff color-0066ff"
        @tap.stop="toFeedback"
      />
      <h-button width="100%" height="72" text="提交" class="flex-1" @tap.stop="submit" />
    </view>
    <canvas
      v-for="idx in 5"
      :key="idx"
      :canvas-id="'compress_canvas' + idx"
      :id="'compress_canvas' + idx"
      class="compress_canvas"
      :style="{ width: '800px', height: '800px' }"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, getCurrentInstance, computed } from 'vue'
import { $state, formatImage } from '@/utils/common'
import { _post } from '@/utils/common-request'
import HTextarea from '@/components/h-textarea.vue'
import HooksUploadImage from '@/hooks/upload-image'
import { onReady } from '@dcloudio/uni-app'
const postData = ref({
  content: '',
  attachFiles: [],
  contactNumber: ''
})
const phoneNumber = computed(() => {
  return $state.user.userInfo.phonenumber
})
const btnViewListShow = ref(true)
onReady(() => {
  //默认号码
  postData.value.contactNumber = phoneNumber.value
  if (getCurrentPages()[getCurrentPages().length - 2].route === 'pages-my-center/my-feedback/feedback-list') {
    btnViewListShow.value = false
  }
})
// 上传图片模块
const wCompress = ref(null)
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
const submitLoading = ref(false)
function submit() {
  if (submitLoading.value) {
    return
  }

  if (!postData.value.content) {
    uni.showToast({
      title: '请先填写问题描述',
      icon: 'none',
      duration: 1500
    })
    return
  }
  if (!postData.value.contactNumber) {
    uni.showToast({
      title: '请留下您的联系电话',
      icon: 'none',
      duration: 1500
    })
    return
  }
  submitLoading.value = true
  uni.showLoading({
    title: '加载中'
  })
  uploadImage().then((res: string[]) => {
    let data = { ...postData.value }
    data.attachFiles = []
    if (res) {
      res.forEach((file) => {
        let arr = file.split('/')
        let str = arr[arr.length - 1]
        let fileItem = {
          fileName: str.split('.')[0],
          fileType: str.split('.')[1],
          fileSize: '',
          filePath: file
        }
        data.attachFiles.push(fileItem)
      })
    }

    uni.showLoading({
      title: '加载中'
    })
    _post({ url: '/suggestion/add', data: data })
      .then((res: IResponseType<unknown>) => {
        if (res.code === 200) {
          uni.showToast({
            title: '提交成功',
            icon: 'none',
            duration: 1500
          })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        }
        uni.hideLoading()
      })
      .finally(() => {
        setTimeout(() => {
          submitLoading.value = false
        }, 1500)
      })
  })
}
function toFeedback() {
  uni.navigateTo({
    url: '/pages-my-center/my-feedback/feedback-list'
  })
}
//拨打电话
function phoneCall() {
  uni.makePhoneCall({
    phoneNumber: '400-135-7277'
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
.bottom-btn {
  height: px2vw(152);
  border-radius: 0px 0px 0px 0px;
  opacity: 1;
}
.compress_canvas {
  position: absolute;
  left: 10000px;
}
</style>
