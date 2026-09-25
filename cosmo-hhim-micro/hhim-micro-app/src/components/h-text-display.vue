<template>
  <view class="relative flex" :class="parentClass">
    <text :id="id1" class="single z-index-2" :style="formatWidth" @tap.stop="handleOpenDialog">{{ text }}</text>
    <text :id="idTemp" :style="{ opacity: 0 }" class="absolute single">{{ text }}</text>
  </view>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, nextTick, ref, watch } from 'vue'
import HooksPopup from '@/hooks/popup'

const props = defineProps({
  text: {
    type: String,
    default: ''
  },
  width: {
    type: Number,
    default: 0
  },
  parentClass: {
    type: String,
    default: ''
  }
})

const id1 = computed(() => `a-${new Date().getTime()}`)
const idTemp = computed(() => `a2-${new Date().getTime()}`)
const w1 = ref(0)
const wTemp = ref(0)
const checkDialogShow = computed(() => w1.value && w1.value !== wTemp.value)
const formatWidth = computed(() => {
  if (props.width) {
    return { maxWidth: (props.width / 720) * 100 + 'vw' }
  } else {
    return { width: 'auto' }
  }
})
const instance = getCurrentInstance()
const query = uni.createSelectorQuery().in(instance)
watch(
  () => props.text,
  (val) => {
    if (val) {
      nextTick(() => {
        query.select(`#${id1.value}`).boundingClientRect((rect) => {
          w1.value = rect?.width
        })
        query.select(`#${idTemp.value}`).boundingClientRect((rect) => {
          wTemp.value = rect?.width
        })
        query.exec()
      })
    }
  },
  { immediate: true }
)

const { popupOpen } = HooksPopup()
function handleOpenDialog(e) {
  if (checkDialogShow.value) {
    popupOpen(props.text)
  } else {
    e.preventDefault()
  }
}
</script>
