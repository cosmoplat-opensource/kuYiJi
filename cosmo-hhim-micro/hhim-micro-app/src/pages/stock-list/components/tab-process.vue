<template>
  <report-input-select
    isProcess
    :canUse="canUse"
    urlLink="/process/select"
    :processMark="REPORT_WORK.CURRENT"
    @itemSelected="handleProcessSelect"
    :productSeq="productSeq"
    :standard="standard"
    :inputData="processItem"
  >
    <template #mark>
      <view class="process-mark purple font-24 text-center">
        <text>工序</text>
      </view>
    </template>
  </report-input-select>
</template>

<script setup lang="ts">
import ReportInputSelect from '@/components/report-input-select.vue'
import { REPORT_WORK } from '@/utils/common'
import { onMounted, ref, reactive } from 'vue'
const props = defineProps({
  dataItem: {
    type: Object,
    default: () => {}
  },
  canUse: {
    type: Boolean,
    default: true
  },
  standard: {
    type: Boolean,
    default: false
  },
  productSeq: {
    type: String,
    default: ''
  }
})
const emit = defineEmits(['processSelected'])
onMounted(() => {
  Object.assign(processItem, props.dataItem)
})
const searchValue = ref('')
const processItem = reactive({})
function handleProcessSelect(item) {
  Object.assign(processItem, item)
  emit('processSelected', { ...item, t: new Date().getTime() })
}
function refreshDate(data) {
  Object.assign(processItem, data)
}

defineExpose({ refreshDate })
</script>

<style lang="scss" scoped>
.process-mark {
  width: px2vw(64);
  height: px2vw(32);
  line-height: px2vw(32);
  position: absolute;
  left: 0;
  top: 0;
  border-radius: 0 0 px2vw(8) 0;
  background-color: #ebf0f5;
  color: #5a6f82;
}
</style>
