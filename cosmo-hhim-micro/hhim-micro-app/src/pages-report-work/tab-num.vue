<template>
  <view class="mt-16 flex relative">
    <template v-if="!dataItem.checkPassNum && !dataItem.checkNgNum">
      <!--良品数量-->
      <view class="flex-1 pl-32 pt-24 pr-24 bg-fff" @tap="handleInputFocus('passNum')">
        <view class="flex align-end pb-32">
          <view class="flex-1 flex flex-col">
            <view class="font-28 bold" v-if="readOnly">{{ dataItem.passNum }}</view>
            <input
              v-else
              v-model="dataItem.passNum"
              :focus="refInputPassNumFocus"
              @blur="handleInputBlur('passNum')"
              placeholder="请录入..."
              type="digit"
              class="font-28 bold"
              placeholder-style="color:#b8b8b8"
            />
            <text class="color-5a6f82 font-24 mt-24 line-height-24">良品数量</text>
          </view>
          <image :src="formatImage('icon_liangpin_96', 'svg')" class="icon-96" />
        </view>
      </view>
      <!--不良品数量-->
      <view class="flex-1 pl-32 pt-24 pr-24 bg-fff ml-4" @tap="handleInputFocus('ngNum')">
        <view class="flex align-end pb-32">
          <view class="flex-1 flex flex-col">
            <view class="font-28 bold" v-if="readOnly">{{ dataItem.ngNum }}</view>
            <input
              v-else
              v-model="dataItem.ngNum"
              :focus="refInputNgNumFocus"
              @blur="handleInputBlur('ngNum')"
              placeholder="请录入..."
              type="digit"
              class="font-28 bold"
              placeholder-style="color:#b8b8b8"
            />
            <text class="color-5a6f82 font-24 mt-24 line-height-24">不良品数量</text>
          </view>
          <image :src="formatImage('icon_buliang_96', 'svg')" class="icon-96" />
        </view>
      </view>
    </template>
    <template v-else>
      <!--良品数量-->
      <view class="flex-1 pl-32 pt-24 pr-24 bg-fff">
        <view class="flex align-end pb-32">
          <view class="flex-1 flex flex-col">
            <view class="font-28 bold">{{ dataItem.checkPassNum }}</view>
            <text class="color-5a6f82 font-24 mt-24 line-height-24">良品数量</text>
          </view>
          <image :src="formatImage('icon_liangpin_96', 'svg')" class="icon-96" />
        </view>
        <view
          class="py-16 font-24 b-t-1 border-f5f5f5"
          :class="dataItem.checkPassNum === dataItem.passNum ? 'color-5a6f82' : 'color-ff0000'"
          >记工数：{{ dataItem.passNum }}</view
        >
        <view class="h-24 pb-16" v-if="dataItem.repairNum || dataItem.abandonedNum" />
      </view>
      <!--不良品数量-->
      <view class="flex-1 pl-32 pt-24 pr-24 bg-fff ml-4">
        <view class="flex align-end pb-32">
          <view class="flex-1 flex flex-col">
            <view class="font-28 bold">{{ dataItem.checkNgNum }}</view>
            <text class="color-5a6f82 font-24 mt-24 line-height-24">不良品数量</text>
          </view>
          <image :src="formatImage('icon_buliang_96', 'svg')" class="icon-96" />
        </view>
        <view
          class="py-16 font-24 color-5a6f82 b-t-1 border-f5f5f5"
          :class="dataItem.checkNgNum === dataItem.ngNum ? 'color-5a6f82' : 'color-ff0000'"
          >记工数：{{ dataItem.ngNum }}</view
        >
        <view
          class="h-24 pb-16 font-24 line-height-24 flex align-center"
          v-if="dataItem.repairNum || dataItem.abandonedNum"
        >
          <view class="color-5a6f82 flex align-center">
            返修：<view class="color-ff0000 bold">{{ dataItem.repairNum }}</view>
          </view>
          <view class="color-5a6f82 ml-16 flex align-center">
            报废：<view class="color-ff0000 bold">{{ dataItem.abandonedNum }}</view>
          </view>
        </view>
      </view>
    </template>
  </view>
  <!--备注-->
  <view class="flex flex-col mt-16 bg-fff pt-32 px-32 pb-24">
    <text class="font-24 line-height-24 color-5a6f82">备注</text>
    <view v-if="readOnly" class="mt-24 font-28 bold">{{ dataItem.remark || '-' }}</view>
    <h-textarea
      v-else
      :border="false"
      :padding="false"
      class="mt-24 font-28 bold"
      v-model="dataItem.remark"
      :maxlength="100"
      placeholder="输入备注"
    />
  </view>
  <view class="flex flex-col mt-16 bg-fff p-32">
    <view class="color-5a6f82 font-24">拍照({{ uploadImageList.length }}{{ readOnly ? '' : '/5' }})</view>
    <view class="flex flex-wrap">
      <view
        class="addImg flex align-center justify-center mt-24 mr-24 relative"
        v-for="(item, index) in uploadImageList"
        :key="index"
        @tap="previewImg(uploadImageList, index)"
      >
        <image :src="item" class="icon-128 rounded-16" mode="aspectFill" />
        <view @tap.stop="deleteImage(index)" class="icon-72 tap-view" v-if="!readOnly">
          <view class="del flex align-center justify-center">
            <image src="/static/images/icon_delete_24.svg" class="icon-24" />
          </view>
        </view>
      </view>
      <view
        v-if="uploadImageList.length <= 4 && !readOnly"
        class="addImg border-2-dashed flex align-center justify-center mt-24"
        @tap="handleSelectImages"
      >
        <image src="/static/images/icon_camera.svg" class="icon-72" />
      </view>
    </view>
  </view>
  <slot>
    <view class="flex-1 gap-32 flex align-end justify-center py-32 relative" v-if="btnShow && !readOnly">
      <h-button
        width="100%"
        height="72"
        text="上一步"
        type="bg-fff color-333"
        class="flex-1"
        @tap.stop="handleBtnPre"
        v-if="btnPreShow"
      />
      <h-button
        v-if="btnCheckShow && checkSubmitInspect"
        width="100%"
        height="72"
        text="送检"
        :type="btnRecordShow ? 'border-1 border-0066ff color-0066ff bg-fff' : 'bg-0066ff color-fff'"
        class="flex-1"
        @tap.stop="handleSend"
      />
      <h-button v-if="btnRecordShow" class="flex-1" width="100%" height="72" :text="btnText" @tap.stop="handleReport" />
      <view v-if="!btnRecordShow || (!btnCheckShow && checkSubmitInspect)" class="btn-float" @tap="handleBtnDisplayAll">
        <text class="font-24 color-0066ff">{{ !btnRecordShow ? '记工' : '送检' }}</text>
      </view>
    </view>
  </slot>
</template>

<script setup lang="ts">
import { reactive, ref, getCurrentInstance, onMounted } from 'vue'
import HTextarea from '@/components/h-textarea.vue'
import HooksUploadImage from '@/hooks/upload-image'
import { $store, collectClick, formatImage, Role_Experience } from '@/utils/common'
import { _post } from '@/utils/common-request'
import HooksSettingConfig from '@/hooks/setting-config'
import { onShow } from '@dcloudio/uni-app'

const props = defineProps({
  btnShow: {
    type: Boolean,
    default: true
  },
  countItem: {
    type: Object,
    default: () => {}
  },
  btnText: {
    type: String,
    default: '记工'
  },
  readOnly: {
    type: Boolean,
    default: false
  },
  btnPreShow: {
    type: Boolean,
    default: false
  },
  checkStatus: {
    type: Boolean,
    default: false
  },
  recommendContent: {
    type: Array,
    default: () => []
  }
})

const emits = defineEmits(['reportWork', 'tabActiveChange', 'showNoticeChange'])

const dataItem = reactive(props.countItem)

function handleBtnPre() {
  emits('tabActiveChange', 2)
}
function handleReport(type = '') {
  emits('showNoticeChange', 1)
  uni.setStorageSync('record_image_list', '')
  uploadImage().then((res: string[]) => {
    emits('reportWork', Object.assign(dataItem, { submitPictures: res ? res.join(';') : null, type }))
  })
}

// 校验是否已开启送检按钮权限
const { checkSubmitInspect } = HooksSettingConfig()

// 获取按钮显示权限
const btnCheckShow = ref(true)
const btnRecordShow = ref(true)
function initRecommendButton() {
  const obj = { productSeq: props.recommendContent[0], operateProcessSeq: props.recommendContent[1] }
  _post({ url: '/user/recommendButton', data: [obj] }).then((res: IResponseType<[]>) => {
    if (res.data?.length) {
      btnCheckShow.value = res.data.find((v) => v === '送检')
      btnRecordShow.value = res.data.find((v) => v === '记工')
    } else {
      handleBtnDisplayAll()
    }
  })
}
if ($store.state.user.userInfo.personalRecommend === 1) {
  initRecommendButton()
}
function handleBtnDisplayAll() {
  btnCheckShow.value = true
  btnRecordShow.value = true
}

// 送检
function handleSend() {
  handleReport('send')
}

const refInputPassNumFocus = ref(false)
const refInputNgNumFocus = ref(false)
function handleInputFocus(type) {
  if (type === 'passNum') {
    refInputPassNumFocus.value = true
  } else {
    refInputNgNumFocus.value = true
  }
}
function handleInputBlur(type) {
  if (type === 'passNum') {
    refInputPassNumFocus.value = false
  } else {
    refInputNgNumFocus.value = false
  }
}

onMounted(() => {
  const record_image_list = uni.getStorageSync('record_image_list')
  if (record_image_list) {
    setImageList(JSON.parse(record_image_list))
  }
})

// 上传图片模块
const { choseImage, uploadImageList, uploadImage, setImageList, previewImg, removeImage } = HooksUploadImage(5)
function handleSelectImages() {
  choseImage(getCurrentInstance(), { width: 800, height: 800, zip: true }, ['camera']).then((res) => {
    uni.setStorageSync('record_image_list', JSON.stringify(uploadImageList.value))
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

defineExpose({ handleReport, setImageList })
</script>

<style lang="scss" scoped>
.addImg {
  width: px2vw(128);
  height: px2vw(128);
  background: #ffffff;
  border-radius: px2vw(16);
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
.btn-float {
  position: absolute;
  right: 0;
  bottom: px2vw(152);
  width: px2vw(96);
  height: px2vw(64);
  background: #dbeaff;
  border-radius: px2vw(32) 0 0 px2vw(32);
  text-align: center;
  line-height: px2vw(64);
}
</style>
