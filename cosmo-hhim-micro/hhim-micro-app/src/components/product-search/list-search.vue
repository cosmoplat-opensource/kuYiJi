<template>
  <view class="relative box" :class="[wrapperPaddingRight, wrapperPaddingLeft]">
    <view class="flex">
      <h-search @searchInput="iptChange" class="flex-1" bgClass="bg-fff-80" placeholder="输入产品编码/名称" />
    </view>
    <product-list
      v-show="showSelect && needSelect"
      :searchValue="searchValue"
      :productList="productList"
      @itemSelected="handleProductSelect"
      @itemclose="itemclose"
      class="select"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import ProductList from './product-list.vue'
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
  },
  wrapperPaddingRight: {
    type: String,
    default: 'pr-32'
  },
  wrapperPaddingLeft: {
    type: String,
    default: 'pl-24'
  },
  initialValue: {
    // 外部初始关键字（如问数跳转带入的产品名，仅展示）
    type: String,
    default: ''
  }
})
const searchValue = ref('')
const showSelect = ref(false)
const productList = ref([])
const emit = defineEmits(['search'])
// 外部传入关键字时回显到搜索框（不触发查询，由调用方自行初始化数据）
watch(
  () => props.initialValue,
  (v) => {
    if (v) {
      searchValue.value = v
    }
  },
  { immediate: true }
)
function iptChange(str) {
  //需要展示下拉框
  if (props.needSelect) {
    if (str) {
      if (!props.needExact) {
        productList.value = [{ itemName: str, itemCode: '', itemSeq: '' }]
      }
      getProductList(str)
    } else {
      showSelect.value = false
      emit('search', {})
    }
  } else {
    //不需要下拉
    emit('search', str)
  }
}
// 获取产品
function getProductList(key) {
  _get({ url: '/product/select', data: { key } }).then((res) => {
    showSelect.value = true
    // if (res?.data) {
    if (!props.needExact) {
      productList.value.push(...res.data)
    } else {
      productList.value = res.data
    }
    // }
  })
}
function handleProductSelect(item) {
  productList.value = []
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
  // top: px2vw(72);
  // left: -2.2vw;
  z-index: 99;
  // width: calc(100% - 4.4vw);
}
</style>
