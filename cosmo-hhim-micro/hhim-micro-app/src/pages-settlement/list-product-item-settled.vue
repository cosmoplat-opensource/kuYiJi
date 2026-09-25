<template>
  <view
    v-for="(item, index) in dataList"
    :key="index"
    class="card flex flex-col py-32 px-32 box bg-fff mb-8"
    @tap="jumpDetail(item)"
  >
    <view class="flex align-center font-28">
      <h-text-display class="bold color-333" :text="item.productName" :width="200" />
      <view class="color-999 bold">({{ item.productCode }})</view>
    </view>
    <view class="flex align-center mt-24 box font-24 color-5a6f82">
      <text>工序</text>
      <text class="color-333 mx-8 bold">{{ item.processNum || 0 }}</text>
      <text>道</text>
      <text class="mx-16">|</text>
      <text>员工</text>
      <text class="color-333 mx-8 bold">{{ item.employeeNum || 0 }}</text>
      <text>人</text>
      <text class="mx-16">|</text>
      <text>完工</text>
      <text class="color-333 mx-8 bold">{{ item.storageNum || 0 }}</text>
      <text>{{ item.productUnit }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { $store } from '@/utils/common'

const dataList = computed(() => $store.state.settlement.dataList)
const emits = defineEmits(['toLink'])
function jumpDetail(item) {
  emits('toLink', item)
}
</script>
<style scoped lang="scss">
.card {
  &:active {
    background-color: #f3f3f5;
  }
}
</style>
