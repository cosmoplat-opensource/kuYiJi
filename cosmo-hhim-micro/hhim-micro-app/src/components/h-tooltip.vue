<template>
  <view class="w-100 relative">
    <slot></slot>
    <view v-if="content || $slots.content" class="toast-custom rounded-24 flex flex-col px-24 py-26 box font-28 color-333" :style="{display:show?'block':'none'}">
      <slot name="content">
        {{content}}
      </slot>
    </view>
  </view>
</template>
<script setup lang="ts">
import { ref } from 'vue'
//  @property {String} content   弹出层显示的内容
const props = defineProps({
  content: {
    type: String,
    default: ''
  }
})
const show = ref(false)
function showTooltip() {
  show.value = true
  setTimeout(()=>{
      show.value = false
  },2000)
}
function closeTooltip() {  
  show.value = false
}
defineExpose({ showTooltip,closeTooltip })
</script>
<style lang="scss" scoped>

.toast-custom {
  position: absolute;
  z-index: 999;
  line-height: px2vw(64);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 px2vw(8) px2vw(16) px2vw(1) rgba(216, 221, 229, 1);
  border: px2vw(1) solid #e5e5e5;
}
</style>