<template>
  <view class="h-full bg-f3f3f5 flex flex-col">
    <uni-nav-bar />
    <h-status-header :title="postData.id ? '编辑产品' : '新增产品'" />
    <!--tab栏-->
    <view class="flex align-center px-24 box bg-fff rounded-16 mx-16 box">
      <h-tabs ref="refTabs" :tabData="tabs" :activeIndex="activeIndex" :config="defaultConfig" @tabClick="tabClick" />
    </view>
    <!--基础信息-->
    <view v-if="!activeIndex" class="flex-1 flex flex-col overflow-hidden">
      <view class="mt-8 mx-16 overflow-auto rounded-16 bg-fff px-32 pb-32 box font-28 flex-1">
        <view class="h-96 flex align-center border-bottom-f5f5f5">
          <view class="w-176 color-5a6f82">产品编码</view>
          <input
            v-model="postData.productCode"
            class="flex-1"
            maxlength="30"
            placeholder-class="font-28 color-b8b8b8"
            placeholder="如未填写则自动生成"
          />
        </view>
        <view class="h-96 flex align-center border-bottom-f5f5f5">
          <view class="w-176 color-5a6f82"><text class="color-ff0000">*</text>产品名称</view>
          <input
            v-model="postData.productName"
            class="flex-1"
            maxlength="30"
            placeholder-class="font-28 color-b8b8b8"
            placeholder="输入产品名称"
          />
        </view>
        <view class="h-96 flex align-center border-bottom-f5f5f5">
          <view class="w-176 color-5a6f82">产品类型</view>
          <picker
            class="flex h-100 align-center flex-1 relative"
            @change="productTypeChange"
            range-key="label"
            :range="productTypeList"
          >
            <view class="flex-1 h-100 flex align-center">
              <view class="flex-1 absolute w-100 h-100 flex align-center">{{
                getProductType(postData.productType)
              }}</view>
              <image src="/static/images/icon_input_arr.svg" class="icon-48 picker-icon" />
            </view>
          </picker>
        </view>
        <view class="h-96 flex align-center border-bottom-f5f5f5">
          <view class="w-176 color-5a6f82">单位</view>
          <input
            v-model="postData.unit"
            class="flex-1"
            maxlength="10"
            placeholder-class="font-28 color-b8b8b8"
            placeholder="输入单位"
          />
        </view>
        <view class="h-96 flex align-center border-bottom-f5f5f5">
          <view class="w-176 color-5a6f82">产品规格</view>
          <input
            v-model="postData.standards"
            class="flex-1"
            maxlength="20"
            placeholder-class="font-28 color-b8b8b8"
            placeholder="输入产品规格"
          />
        </view>
        <view class="flex flex-col pt-40">
          <view class="color-5a6f82">产品描述</view>
          <h-textarea v-model="postData.productDesc" class="mt-16" :maxlength="300" placeholder="输入产品描述" />
        </view>
        <view class="flex flex-col mt-40">
          <view class="color-5a6f82">产品图片({{ uploadImageList.length }}/5)</view>
          <view class="flex flex-wrap">
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
      </view>
      <view class="py-32 box flex justify-center font-32">
        <h-button width="296" height="72" text="取消" type="bg-fff color-333" @tap.stop="cancel" />
        <h-button
          width="296"
          height="72"
          :text="postData.id ? '保存' : '确认并提交'"
          class="ml-32"
          @tap.stop="submit"
        />
      </view>
    </view>
    <edit-path v-else :productItem="postData" class="flex-1 mt-8 overflow-hidden mx-16 box" />
    <h-status-footer />
    <h-popup />
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
import { getCurrentInstance, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { _get, _post } from '@/utils/common-request'
import { getProductType, productTypeList } from '@/utils/formatData'
import HTextarea from '@/components/h-textarea.vue'
import HooksUploadImage from '@/hooks/upload-image'
import EditPath from '@/pages-my-center/product-manage/edit-path.vue'
import HPopup from '@/components/h-popup.vue'
import HTabs from '@/components/h-tabs.vue'

const refTabs = ref(null)

onLoad((e) => {
  if (e.productData) {
    postData.value = JSON.parse(e.productData)
    //原先产品是原材料类型的隐藏工艺路线tab
    if (postData.value.productType === 'YCL') {
      tabs.value = [
        {
          state: 0,
          disabled: false,
          name: '基础信息'
        }
      ]
    }
    if (postData.value.picture) {
      setImageList(postData.value.picture.split(','))
    }
  } else if (e.productSeq) {
    _get({ url: '/product/info/' + e.productSeq }).then((res) => {
      postData.value = res.data
      if (postData.value.picture) {
        setImageList(postData.value.picture.split(','))
      }
      refTabs.value?.tabClick(1)
    })
  } else if (e.productName) {
    postData.value.productName = e.productName
  } else {
    tabs.value.find((v) => v.state === 1).disabled = true
  }
})

const postData = ref<Partial<IPicture>>({
  productType: 'CP',
  pictureList: []
})

const tabs = ref([
  {
    state: 0,
    disabled: false,
    name: '基础信息'
  },
  {
    state: 1,
    disabled: false,
    name: '工艺路线'
  }
])
const activeIndex = ref(0)
const defaultConfig = ref({
  fontSize: 28,
  color: '#999999',
  activeBold: 'bold',
  activeColor: '#333333',
  underLineHeight: 8,
  underLineColor: '#0066ff'
})
function tabClick(event) {
  activeIndex.value = event
}

function productTypeChange(e) {
  let val = e.detail.value
  postData.value.productType = productTypeList[val].code
}
function submit() {
  if (!postData.value.productName) {
    uni.showToast({
      title: '请先填写产品名称',
      icon: 'none',
      duration: 1500
    })
    return
  }
  uni.showLoading({
    title: '加载中'
  })
  uploadImage().then((res) => {
    uni.hideLoading()
    let data = { ...postData.value }
    data.picture = res.toString()
    delete data.createdBy
    delete data.createdDate
    delete data.lastUpdBy
    delete data.lastUpdDate
    delete data.pictureList
    if (data.id) {
      _post({ url: '/product/edit', data: data }).then((res: IResponseType<{}>) => {
        if (res.code === 200) {
          uni.showToast({
            title: '提交成功',
            icon: 'none',
            duration: 1500
          })
          setTimeout(() => {
            uni.$emit('refreshProductList')
            uni.navigateBack()
          }, 1500)
        }
      })
    } else {
      _post({ url: '/product/add', data: { ...data, createdType: '1' } }).then((res: IResponseType<unknown>) => {
        if (res.code === 200) {
          uni.showToast({
            title: '提交成功',
            icon: 'none',
            duration: 1500
          })
          setTimeout(() => {
            uni.$emit('refreshProductList')
            uni.navigateBack()
          }, 1500)
        }
      })
    }
  })
}
function cancel() {
  uni.navigateBack({
    delta: 1
  })
}

// 上传图片模块
const { choseImage, uploadImageList, uploadImage, setImageList, previewImg, removeImage } = HooksUploadImage(5)
function handleSelectImages() {
  choseImage(getCurrentInstance(), { width: 800, height: 800, zip: true })
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
    border-radius: 0 px2vw(16) 0 px2vw(16);
    position: absolute;
    right: 0;
    top: 0;
  }
}

.picker-icon {
  position: absolute;
  top: px2vw(24);
  right: 0;
}
.bottom-btn {
  //position: absolute;
  //bottom: 0;
  height: px2vw(152);
  border-radius: 0;
}

.common-tips {
  width: px2vw(56);
  height: px2vw(32);
  line-height: px2vw(32);
  border: px2vw(2) solid #d3dfeb;
}

.mid {
  height: px2vw(169);
  border-radius: px2vw(16) px2vw(16) 0 0;
}
.compress_canvas {
  position: absolute;
  left: 10000px;
}
.tab_active {
  position: relative;
  &:after {
    position: absolute;
    bottom: px2vw(1);
    left: 0;
    content: ' ';
    width: 100%;
    height: px2vw(8);
    background-color: #0066ff;
    border-radius: px2vw(4);
  }
}
</style>
