<template>
  <uni-popup ref="popup" @maskClick="handleCancel" class="w-full">
    <view class="w-100 bg-fff rounded-16 p-32 box">
      <h-textarea v-model="textDesc" :maxlength="300" :placeholder="props.textPlaceholder" />
      <view class="flex justify-center mt-40 mb-32">
        <h-button width="296" height="72" text="取消" type="bg-f3f3f5 color-333" @tap.stop="handleCancel" />
        <h-button width="296" height="72" text="确定" class="ml-32" @tap.stop="handleConfirm" />
      </view>
    </view>
  </uni-popup>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import HTextarea from '@/components/h-textarea.vue'
const props = defineProps({
  textPlaceholder: {
    type: String,
    default: '请输入内容'
  }
})
const textDesc = ref('')

const popup = ref(null)
const emits = defineEmits(['confirm'])

function open(textValue) {
  textDesc.value = textValue
  popup.value.open('center')
}
function handleCancel() {
  textDesc.value = ''
  popup.value?.close('center')
}
function handleConfirm() {
  emits('confirm', textDesc.value)
  handleCancel()
}
defineExpose({
  open
})
</script>

<style lang="scss" scoped>
.uni-popup {
  z-index: 999;
}
.open-filter {
  border-radius: px2vw(16) px2vw(16) 0 0;
  .title {
    height: px2vw(96);
    border-bottom: px2vw(1) solid#F0F0F0;
  }
}
</style>
