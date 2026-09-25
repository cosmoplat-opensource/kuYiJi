<template>
  <view class="m-h-300 w-100 box bg-fff overflow-scroll absolute product-item-wrapper" v-if="getDataList.length">
    <view
      v-for="(item, index) in getDataList"
      @tap="handleProductSelected(item)"
      :key="index"
      class="product-item flex align-center"
    >
      <image src="/static/images/icon_s_b8.svg" class="icon-24" v-if="item.itemCode" />
      <view class="icon-24" v-else />
      <view class="flex-1 flex align-center">
        <rich-text :nodes="item.nodeName" v-if="item.itemCode" />
        <view v-else class="color-5a6f82">{{ item.itemName }}</view>
        <rich-text v-if="item.itemCode && !isProcess" :nodes="item.nodeCode" />
      </view>
      <view class="p-4 bg-EBF0F5 color-5a6f82 font-24 ml-8" v-if="item.unapprovedNum">
        <text>未审核 {{ item.unapprovedNum }}</text>
      </view>
      <view class="p-4 bg-EBF0F5 color-5a6f82 font-24 ml-8" v-if="item.totalStockNum">
        <text>库存 {{ item.totalStockNum }}</text>
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
  return props.productList.map((item: any) => {
    const tempName = escapeHtml(formatStr(item.itemName, 10)).replace(
      new RegExp(props.searchValue, 'g'),
      `<span style="color: #0066ff;">${escapeHtml(props.searchValue)}</span>`
    )
    const nodeName = `<div class="ml-16 color-333 word-break">${tempName}</div>`

    const tempCode = escapeHtml(formatStr(item.itemCode || '', 10)).replace(
      new RegExp(props.searchValue, 'g'),
      `<span style="color: #0066ff;">${escapeHtml(props.searchValue)}</span>`
    )
    const nodeCode = `<div class="color-999">(${tempCode})</div>`
    return {
      ...item,
      nodeName,
      nodeCode
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
