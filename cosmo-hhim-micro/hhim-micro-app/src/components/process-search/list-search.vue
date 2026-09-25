<template>
  <view class="w-100 relative">
    <view class="flex px-32">
      <h-search class="flex-1" placeholder="输入工序编码/名称" @searchInput="iptChange" bgClass="bg-fff-80" />
    </view>
    <process-list
      v-show="showSelect && needSelect"
      :processList="processList"
      :searchValue="searchValue"
      @itemSelected="handleProductSelect"
      @itemclose="itemclose"
      class="select"
    />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ProcessList from './process-list.vue'
import { _get } from '@/utils/common-request'
const props = defineProps({
  needExact: {
    //需要下拉框精确查询
    type: Boolean,
    default: false
  },
  needSelect: {
    //需要下拉框展示
    type: Boolean,
    default: true
  }
})
const searchValue = ref('')
const showSelect = ref(false)
const processList = ref([])
const emit = defineEmits(['search'])
function iptChange(str) {
  //需要展示下拉框
  if (props.needSelect) {
    if (str) {
      if (!props.needExact) {
        processList.value = [{ itemName: str, itemCode: '', itemSeq: '' }]
      }
      getprocessList(str)
    } else {
      showSelect.value = false
      emit('search', {})
    }
  } else {
    //不需要下拉
    emit('search', str)
  }
}
// 获取工序
function getprocessList(key) {
  _get({ url: '/process/select', data: { key } }).then((res) => {
    showSelect.value = true
    if (!props.needExact) {
      processList.value.push(...res.data)
    } else {
      processList.value = res.data
    }
  })
}
function handleProductSelect(item) {
  processList.value = []
  searchValue.value = item.itemName
  showSelect.value = false
  emit('search', item)
}
function clearValue() {
  searchValue.value = ''
  showSelect.value = false
  emit('search', '')
}
function itemclose() {
  showSelect.value = false
}
</script>

<style lang="scss" scoped>
.select {
  top: px2vw(72);
  left: px2vw(0);
  z-index: 5;
  width: 100vw;
}
</style>
