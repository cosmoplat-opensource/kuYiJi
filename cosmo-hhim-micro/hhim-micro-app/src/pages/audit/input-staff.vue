<template>
  <view class="w-100 box h-96 flex align-center border-bottom-f5f5f5">
    <view class="w-176 color-5a6f82">员工</view>
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
  <staff-list
    v-show="showSelect && needSelect && currentFilter === 'staff'"
    :searchValue="searchValue"
    :staffList="staffList"
    :showInput="showInput"
    @itemSelected="handleProductSelect"
    @itemclose="itemclose"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import StaffList from '@/components/staff-search/staff-list.vue'
import { _get } from '@/utils/common-request'
import { $store } from '@/utils/common'
const props = defineProps({
  submitNickName: {
    type: String,
    default: ''
  },
  auditPopup: {
    type: Boolean,
    default: false
  },
  hideSelf: {
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
    default: '输入员工名称'
  }
})
const currentFilter = computed(() => {
  return $store.getters.currentFilter
})
const searchValue = ref('')
watch(
  () => props.submitNickName,
  (val) => {
    searchValue.value = val
  },
  { immediate: true, deep: true }
)

const emits = defineEmits(['confirm'])
function setCurrent() {
  $store.dispatch('audit/updateCurrentFilter', 'staff')
  getStaffList('')
}
function handleInputClear() {
  searchValue.value = ''
  itemclose()
  emits('confirm', '')
}
const showSelect = ref(false)
const needSelect = ref(true)
const needExact = ref(false)
const staffList = ref([])
function itemclose() {
  showSelect.value = false
}
function handleProductSelect(item) {
  staffList.value = []
  searchValue.value = item.itemName
  showSelect.value = false
  emits('confirm', item)
}
function iptChange(e) {
  const str = e.detail.value
  staffList.value = []
  //需要展示下拉框
  if (needSelect.value) {
    if (str) {
      if (!needExact.value && !props.hideSelf) {
        staffList.value = [{ nickName: str, userName: '' }]
      }
      props.auditPopup && emits('confirm', { itemName: str })
      getStaffList(str)
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
function getStaffList(key) {
  if (!key) {
    staffList.value = []
  }
  _get({ url: '/user/list', data: { key } }).then((res) => {
    showSelect.value = true
    if (!needExact.value) {
      staffList.value.push(...res.rows)
    } else {
      staffList.value = res.rows
    }
  })
}
</script>

<style lang="scss" scoped></style>
