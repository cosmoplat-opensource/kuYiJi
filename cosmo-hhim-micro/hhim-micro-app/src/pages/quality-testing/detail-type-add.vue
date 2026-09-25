<template>
  <uni-popup ref="popup" type="bottom" :safe-area="false">
    <view class="bg-fff flex flex-col justify-start font-28 rounded-16-top">
      <view class="p-24 box flex align-center b-b-1 border-f0f0f0">
        <view class="w-48" />
        <view class="flex-1 text-center font-28 color-5a6f82 bold">添加不良类型</view>
        <image @click="handleCancel" src="/static/images/icon_close_666.svg" class="icon-48" />
      </view>
      <!--提示-->
      <view class="py-16 px-32 box bg-f7f8e7 flex align-center">
        <view class="font-24 color-5a6f82">可处理不良品数：</view>
        <view class="font-24 color-ff0000 bold">{{ count }}</view>
        <view class="font-24 color-ff0000 ml-8">{{ unit }}</view>
      </view>
      <view class="mx-32 box">
        <!-- #ifdef H5 -->
        <view class="mt-16 h-80 box flex align-center b-b-2 border-f5f5f5" @tap="handlePopupShow">
        <!-- #endif -->
        <!-- #ifndef H5 -->
        <view class="mt-16 h-80 box flex align-center b-b-2 border-f5f5f5">
        <!-- #endif -->
          <view class="w-176 font-28 color-5a6f82">不良类型</view>
          <input
            :value="typeItem.ngType"
            class="flex-1 ng-type-input"
            placeholder-class="font-28 color-b8b8b8"
            placeholder="选择不良类型"
            disabled
            @tap="handlePopupShow"
          />
          <image :src="formatImage('icon_input_arr', 'svg')" class="icon-48" />
        </view>
        <view class="mt-16 h-80 box flex align-center b-b-2 border-f5f5f5">
          <view class="w-176 font-28 color-5a6f82">数量</view>
          <input
            v-model="typeItem.ngNum"
            class="flex-1"
            placeholder-class="font-28 color-b8b8b8"
            type="digit"
            placeholder="输入不良数量"
          />
          <view class="ml-16 font-28 color-5a6f82">{{ unit }}</view>
        </view>
      </view>
      <!--底部操作区-->
      <view class="flex justify-center mt-66 mb-32">
        <h-button width="296" height="72" text="取消" type="bg-f3f3f5 color-333" @tap.stop="handleCancel" />
        <h-button width="296" height="72" text="添加" class="ml-32" @tap.stop="handleConfirm" />
      </view>
    </view>
    <detail-type-popup ref="popupType" @typeSelected="handleTypeSelected" :typeName="typeItem.ngType" />
  </uni-popup>
</template>

<script setup lang="ts">
import { reactive, readonly, ref } from 'vue'
import DetailTypePopup from '@/pages/quality-testing/detail-type-popup.vue'
import { formatImage } from '@/utils/common'

// --------------弹窗组件基本内容---------------------------
const props = defineProps({
  count: {
    type: Number,
    default: 0
  },
  unit: {
    type: String,
    default: '件'
  }
})
const popup = ref(null)
function handleCancel() {
  popup.value?.close('bottom')
}
function open() {
  typeItem.ngType = ''
  typeItem.ngNum = ''
  popup.value?.open('bottom')
}
defineExpose({
  open
})

//---------------姓名输入框监听,下拉选中已有的类型-----------------
const typeItem = reactive<ngInfoItem>({ ngType: '', ngNum: '' })

//--------------弹窗提交--------------------
const emits = defineEmits(['submit'])
function handleConfirm() {
  if (!typeItem.ngType) {
    uni.showToast({
      title: '请填写不良类型',
      icon: 'none'
    })
    return
  }
  if (!typeItem.ngNum) {
    uni.showToast({
      title: '请填写不良数量',
      icon: 'none'
    })
    return
  }
  if (typeItem.ngNum && !/^\d+(\.\d{1,4})?$/.test(typeItem.ngNum)) {
    uni.showToast({
      title: '不良数量应为非负数且最多保留4位小数',
      icon: 'none'
    })
    return false
  }
  if (Number(typeItem.ngNum) > props.count) {
    uni.showToast({ title: '输入数量超出可处理数', icon: 'none', duration: 2000 })
    return
  }
  handleCancel()
  emits('submit', typeItem)
}

const popupType = ref(null)
function handlePopupShow() {
  popupType.value?.open()
}
function handleTypeSelected(item) {
  typeItem.ngType = item.itemName
}
</script>

<style lang="scss" scoped>
/* #ifdef H5 */
/* H5：disabled input 不响应点击，pointer-events:none 让点击穿透到行级 @tap（选择不良类型） */
:deep(.ng-type-input) {
  pointer-events: none;
}
/* #endif */
</style>
