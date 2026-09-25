<template>
  <view class="w-100 h-96 flex align-center border-bottom-f5f5f5">
    <view class="w-176 color-5a6f82">工序</view>
    <input
      v-model="searchValue"
      class="flex-1"
      maxlength="10"
      :cursor-spacing="250"
      placeholder-class="font-28 color-b8b8b8"
      :placeholder="placeholder"
      @input="iptChange"
      @focus="setCurrent"
    />
    <image v-if="searchValue" src="/static/images/icon_del_b6c0c9.svg" class="icon-48" @tap="handleInputClear()" />
    <image v-if="dropDownIcon" src="/static/images/icon_input_arr.svg" class="icon-48" />
  </view>
  <process-list
    v-show="showSelect && needSelect && currentFilter === 'process'"
    :searchValue="searchValue"
    :processList="processList"
    :showInput="showInput"
    @itemSelected="handleProductSelect"
    @itemclose="itemclose"
    class="select"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import ProcessList from '@/components/process-search/process-list.vue'
import { _get } from '@/utils/common-request'
import { $store } from '@/utils/common'
const props = defineProps({
  processNameOrCode: {
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
    default: '输入工序编码/名称'
  }
})
const currentFilter = computed(() => {
  return $store.getters.currentFilter
})
const searchValue = ref('')
watch(
  () => props.processNameOrCode,
  (val) => {
    searchValue.value = val
  },
  { immediate: true, deep: true }
)

const emits = defineEmits(['confirm'])
function setCurrent() {
  $store.dispatch('audit/updateCurrentFilter', 'process')
}
function handleInputClear() {
  searchValue.value = ''
  itemclose()
  emits('confirm', '')
}
const showSelect = ref(false)
const needSelect = ref(true)
const needExact = ref(false)
const processList = ref([])
function itemclose() {
  showSelect.value = false
}
function handleProductSelect(item) {
  processList.value = []
  searchValue.value = item.itemName
  showSelect.value = false
  emits('confirm', item)
}
function iptChange(e) {
  const str = e.detail.value
  processList.value = []
  //需要展示下拉框
  if (needSelect.value) {
    if (str) {
      if (!needExact.value) {
        processList.value = [{ itemName: str, itemCode: '', itemSeq: '' }]
      }
      props.auditPopup && emits('confirm', { itemName: str })
      getprocessList(str)
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
function getprocessList(key) {
  _get({ url: '/process/select', data: { key } }).then((res) => {
    showSelect.value = true
    if (!needExact.value) {
      processList.value.push(...res.data)
    } else {
      processList.value = res.data
    }
  })
}
</script>

<style lang="scss" scoped></style>
