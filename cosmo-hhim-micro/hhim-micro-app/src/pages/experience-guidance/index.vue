<!--体验引导-->
<template>
  <view class="experience-guidance w-full h-full absolute flex flex-col justify-end" v-if="stepIndex">
    <step1 v-if="stepIndex === 1" @stepChange="handleStepChange" />
    <step2 v-else-if="stepIndex === 2" :sourceVideo="sourceVideo" @stepChange="handleStepChange" />
  </view>
</template>

<script setup lang="ts">
import Step1 from '@/pages/experience-guidance/step1.vue'
import Step2 from '@/pages/experience-guidance/step2.vue'
import { nextTick, ref } from 'vue'
import { _post } from '@/utils/common-request'
import { $store } from '@/utils/common'

const emits = defineEmits(['guidanceFinish'])
const stepIndex = ref(1)
const sourceVideo = ref([])
function handleStepChange(index, payload) {
  if (index === 2) {
    sourceVideo.value = payload.arr
    stepIndex.value = 2
  } else if (index === 3) {
    emits('guidanceFinish')
  }
}
function open() {
  stepIndex.value = 1
}
defineExpose({ open })
</script>

<style scoped lang="scss">
.experience-guidance {
  left: 0;
  top: 0;
  background-color: rgba(0, 0, 0, 0.7);
}
</style>
