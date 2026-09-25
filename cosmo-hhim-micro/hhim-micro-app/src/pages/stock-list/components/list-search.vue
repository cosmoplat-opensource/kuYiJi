<template>
  <view class="w-100 relative">
    <view class="flex align-center justify-between bg-ebecee pl-32 pr-24 ml-32 box input-class mr-24">
      <view class="flex align-center flex-1">
        <image src="/static/images/icon_s_b8.svg" class="icon-24" />
        <input
          v-model="searchValue"
          @input="iptChange"
          placeholder-style="color:#B8B8B8"
          :focus="focusAble"
          class="main-input pl-16 font-28"
          placeholder="输入产品编码/名称/工序"
        />
      </view>
      <image v-show="searchValue" @tap="clearValue" src="/static/images/icon_del.svg" class="icon-48" />
    </view>
    <product-process-list
      v-if="showSelect"
      :resultList="resultList"
      :searchValue="searchValue"
      class="select"
      @itemSelected="handleProductSelect"
      @itemclose="itemclose"
    />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ProductProcessList from '@/pages/stock-list/components/product-process-list.vue'
import { _get } from '@/utils/common-request'
const props = defineProps({
  focusAble: {
    type: Boolean,
    default: true
  }
})
const searchValue = ref('')
const resultList = ref({
  product: [],
  process: []
})
const showSelect = ref(false)
const emit = defineEmits(['search'])
function iptChange() {
  if (searchValue.value) {
    getProductList(searchValue.value)
  } else {
    showSelect.value = false
  }
}
// 获取产品
function getProductList(key) {
  _get({ url: '/storage/select', data: { key } }).then((res) => {
    if (res.data) {
      showSelect.value = true
      resultList.value = res.data
    }
  })
}
function handleProductSelect(item) {
  resultList.value = {}
  searchValue.value = item.itemName
  showSelect.value = false
  emit('search', item)
}
function clearValue() {
  searchValue.value = ''
  showSelect.value = false
  emit('search', {})
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
}
</style>
