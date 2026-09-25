<template>
  <view class="m-h-300 w-100 box bg-fff overflow-scroll absolute product-item-wrapper" v-if="getDataList.length">
    <view
      v-for="(item, index) in getDataList"
      @tap="handleProductSelected(item)"
      :key="index"
      class="product-item flex align-center"
    >
      <image src="/static/images/icon_s_b8.svg" class="icon-24" />
      <view class="flex-1 flex align-center">
        <rich-text :nodes="item.nodeName" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { escapeHtml, formatStr } from '@/utils/common'
import { computed } from 'vue'

const props = defineProps({
  productList: {
    type: Array,
    default: () => [] as IDataType[]
  },
  searchValue: {
    type: String,
    default: ''
  },
  isProcess: {
    type: Boolean,
    default: false
  }
})

const getDataList = computed(() => {
  return props.productList
    .filter((item) => item.itemName)
    .map((item: any) => {
      const tempName = escapeHtml(formatStr(item.itemName, 10)).replace(
        new RegExp(props.searchValue, 'g'),
        `<span style="color: #0066ff;">${escapeHtml(props.searchValue)}</span>`
      )
      const nodeName = `<div class="ml-16 color-333 word-break">${tempName}</div>`
      return {
        ...item,
        nodeName
      }
    })
})

const emits = defineEmits(['itemSelected'])
function handleProductSelected(item: IDataType) {
  emits('itemSelected', item)
}
</script>

<style lang="scss" scoped>
.product-item-wrapper {
  background: #ffffff;
  box-shadow: 0 px2vw(8) px2vw(16) px2vw(1) #d8dde5;
}
.product-item {
  padding: px2vw(34) px2vw(32);
  border-bottom: px2vw(1) solid #f5f5f5;

  &:active {
    background-color: #f3f3f5;
  }
}
</style>
