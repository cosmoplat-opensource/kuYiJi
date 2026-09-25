<template>
  <report-input-select :processMark="REPORT_WORK.PRODUCT" @itemSelected="handleProductSelect" :inputData="productItem">
    <template #mark>
      <view class="process-mark font-24 text-center">
        <text>产品</text>
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
  }
})
onMounted(() => {
  Object.assign(productItem, props.dataItem)
})

// 搜索的产品集合
const searchValue = ref('')
const emit = defineEmits(['productSelected'])

// 产品选中
const productItem = reactive({ itemName: '', itemCode: '', itemSeq: '', standard: false })
function handleProductSelect(item) {
  Object.assign(productItem, item)
  emit('productSelected', item)
}
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
