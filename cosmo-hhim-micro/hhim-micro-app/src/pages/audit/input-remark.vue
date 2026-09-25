<template>
  <view class="w-100 box h-96 flex align-center border-bottom-f5f5f5">
    <view class="w-176 color-5a6f82">备注</view>
    <input
      v-model="searchValue"
      class="flex-1"
      maxlength="10"
      :cursor-spacing="250"
      placeholder-class="font-28 color-b8b8b8"
      placeholder="输入备注"
    />
    <image v-if="searchValue" src="/static/images/icon_del_b6c0c9.svg" class="icon-48" @tap="handleInputClear()" />
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import StaffList from '@/components/staff-search/staff-list.vue'
import { _get } from '@/utils/common-request'
import { $store } from '@/utils/common'
const props = defineProps({
  remark: {
    type: String,
    default: ''
  }
})
const searchValue = ref('')

watch(
  () => props.remark,
  (val) => {
    searchValue.value = val
  },
  { immediate: true, deep: true }
)

watch(
  () => searchValue.value,
  (val) => {
    emits('input', val)
  }
)

const emits = defineEmits(['input'])
function handleInputClear() {
  searchValue.value = ''
}
</script>

<style lang="scss" scoped></style>
