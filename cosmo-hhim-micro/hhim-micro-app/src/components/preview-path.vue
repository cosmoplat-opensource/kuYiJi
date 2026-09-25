<template>
  <view v-if="Object.keys(headMaps).length" class="flex mb-48 relative align-end">
    <view v-if="Object.keys(headMaps).length > 1" class="group-line-area">
      <view class="group-line" v-for="item in groupLength" :key="item" />
    </view>
    <preview-path
      :techData="techData"
      v-for="(item, index) in Object.keys(headMaps)"
      :halfLine="index > 0"
      :key="item"
      :parentProcessId="item"
    />
  </view>
  <preview-path-card :item="lastList[0]" :halfLine="halfLine" />
</template>

<script setup lang="ts">
import { computed, onMounted, PropType, reactive, ref } from 'vue'
import PreviewPathCard from '@/components/preview-path-card.vue'

const props = defineProps({
  techData: {
    type: Array as PropType<IProcessPath[]>,
    default: () => []
  },
  parentProcessId: {
    type: String,
    default: ''
  },
  halfLine: {
    type: Boolean,
    default: false
  }
})

const lastList = ref([] as IProcessPath[])
const headMaps = reactive({})
const groupLength = computed(() => Object.keys(headMaps).length - 1)

onMounted(() => {
  if (!props.parentProcessId) {
    lastList.value = props.techData.filter((item) => item.isLastProcess === '0')
  } else {
    lastList.value = props.techData.filter((item) => props.parentProcessId === String(item.processId))
  }
  lastList.value.forEach((l) => {
    const pList = props.techData.filter((item) => item.processId === l.parentProcessId)
    pList.forEach((p) => {
      headMaps[p.processId] = 1
    })
  })
})
</script>

<style lang="scss" scoped>
.group-line-area {
  position: absolute;
  display: flex;
  align-items: center;
  padding-left: px2vw(160);
  box-sizing: border-box;
  bottom: px2vw(-24);
}
.group-line {
  width: px2vw(320);
  height: px2vw(1);
  border-bottom: px2vw(2) dashed #cccccc;
}
</style>
