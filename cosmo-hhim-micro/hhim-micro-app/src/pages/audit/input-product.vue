<template>
  <view class="h-96 flex align-center border-bottom-f5f5f5">
    <view class="w-176 color-5a6f82">产品</view>
    <input
      v-model="searchValue"
      class="flex-1"
      maxlength="10"
      placeholder-class="font-28 color-b8b8b8"
      :placeholder="placeholder"
      @input="iptChange"
      :adjust-position="true"
      :cursor-spacing="250"
      @focus="setCurrent"
    />
    <image v-if="searchValue" src="/static/images/icon_del_b6c0c9.svg" class="icon-48" @tap="handleInputClear()" />
    <image v-if="dropDownIcon" src="/static/images/icon_input_arr.svg" class="icon-48" />
  </view>
  <product-list
    v-show="showSelect && needSelect && currentFilter === 'product'"
    :searchValue="searchValue"
    :productList="productList"
    :showInput="showInput"
    @itemSelected="handleProductSelect"
    @itemclose="itemclose"
    class="select"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch, getCurrentInstance } from 'vue'
import ProductList from '@/components/product-search/product-list.vue'
import { _get } from '@/utils/common-request'
import { $store } from '@/utils/common'
const props = defineProps({
  productNameOrCode: {
    type: String,
    default: ''
  },
  auditPopup: {
    type: Boolean,
    default: false
  },
  showInput: {
    //展示无查询结果时的输入值
    type: Boolean,
    default: true
  },
  dropDownIcon: {
    //下拉图标展示
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: '输入产品编码/名称'
  }
})
const currentFilter = computed(() => {
  return $store.getters.currentFilter
})
const searchValue = ref('')
watch(
  () => props.productNameOrCode,
  (val) => {
    searchValue.value = val
  },
  { immediate: true, deep: true }
)

const emits = defineEmits(['confirm'])
function setCurrent() {
  $store.dispatch('audit/updateCurrentFilter', 'product')
}
function handleInputClear() {
  searchValue.value = ''
  itemclose()
  emits('confirm', '')
}
const showSelect = ref(false)
const needSelect = ref(true)
const productList = ref([])
function itemclose() {
  showSelect.value = false
}
function handleProductSelect(item) {
  productList.value = []
  searchValue.value = item.itemName
  showSelect.value = false
  emits('confirm', item)
}
function iptChange(e) {
  const str = e.detail.value
  productList.value = []
  //需要展示下拉框
  if (needSelect.value) {
    if (str) {
      props.auditPopup && emits('confirm', { itemName: str })
      getProductList(str)
    } else {
      showSelect.value = false
      emits('confirm', '')
    }
  } else {
    //不需要下拉
    emits('confirm', str)
  }
}
// 获取产品
function getProductList(key) {
  _get({ url: '/product/select', data: { key } }).then((res) => {
    showSelect.value = true
    let resData = res.data.splice(0, 40)
    productList.value = [{ itemName: key, itemCode: '', itemSeq: '' }]
    productList.value.push(...resData)
  })
}
</script>

<style lang="scss" scoped></style>
