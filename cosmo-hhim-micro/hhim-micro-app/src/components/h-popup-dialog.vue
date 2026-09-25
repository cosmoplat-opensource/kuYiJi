<template>
  <uni-popup ref="popup" :is-mask-click="isMaskClick" :animation="animation">
    <view class="rounded-16 h-popup-dialog relative" :style="{ width: getWidth }">
      <view class="popup-title flex flex-ac justify-between" v-if="showHeader">
        <view class="color-5a6f82">{{ title }}</view>
        <image src="/static/images/icon_close_666.svg" class="icon-48" @click="close" />
      </view>
      <view class="popup-content">
        <slot></slot>
      </view>
      <view class="popup-footer flex justify-center" v-if="showFooter">
        <slot name="footer">
          <h-button width="296" height="72" :text="cancelText" type="bg-f3f3f5 color-333" @tap.stop="handleCancel" />
          <h-button width="296" height="72" :text="confirmText" class="ml-32" @tap.stop="handleConfirm" />
        </slot>
      </view>
    </view>
  </uni-popup>
</template>

<script setup lang="ts">
// 弹窗
import { computed, ref, watch } from 'vue'
import { $state, $store, formatPx2Vw } from '@/utils/common'
import { number } from 'echarts'

const popup = ref(null)
const emit = defineEmits(['confirm', 'cancel'])

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  confirmText: {
    type: String,
    default: '确定'
  },
  cancelText: {
    type: String,
    default: '取消'
  },
  showHeader: {
    type: Boolean,
    default: true
  },
  showFooter: {
    type: Boolean,
    default: true
  },
  isMaskClick: {
    type: Boolean,
    default: false
  },
  // 是否开启动画（H5 下 uni-transition 动画在 slot 内容变化时可能卡在初始态 opacity:0 导致弹窗内容不可见，
  // 需要稳定的弹窗（如视频播放器）可传 :animation="false" 关闭动画）
  animation: {
    type: Boolean,
    default: true
  },
  width: {
    type: [String, Number],
    default: 528
  }
})

const getWidth = computed(() => {
  return `${formatPx2Vw(Number(props.width))}vw`
})

const open = () => {
  popup.value?.open()
}

const close = () => {
  popup.value?.close()
}

const handleConfirm = () => {
  emit('confirm')
}

const handleCancel = () => {
  close()
  emit('cancel')
}

defineExpose({
  open,
  close
})
</script>

<style lang="scss">
.h-popup-dialog {
  padding: px2vw(32);
  box-sizing: border-box;
  background-color: #fff;
  .popup-footer {
    padding: px2vw(24) 0;
  }
}
</style>
