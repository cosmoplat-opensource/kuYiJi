<template>
  <view class="m-h-500 w-full box bg-fff overflow-auto absolute" style="z-index: 99">
    <template v-if="resultList && (getDataProduct.length > 0 || getDataProcess.length > 0)">
      <template v-if="getDataProduct">
        <view
          v-for="(item, index) in getDataProduct"
          @tap="handleProductSelected(item)"
          :key="index"
          class="product-item flex align-center justify-between"
        >
          <view class="flex align-center">
            <image src="/static/images/icon_s_b8.svg" class="icon-24" />
            <rich-text :nodes="item.nodeName" v-if="item.itemCode" />
            <rich-text v-if="item.itemCode" :nodes="item.nodeCode" />
            <!-- <text class="ml-16 color-333">{{ formatStr(item.itemName, 10) }}</text>
            <text class="color-999">({{ formatStr(item.itemCode, 8) }})</text> -->
          </view>
          <view class="font-24 color-5a6f82 bg-EBF0F5 rounded-4 pl-4 pr-4">产品</view>
        </view>
      </template>
      <template v-if="getDataProcess">
        <view
          v-for="(item, index) in getDataProcess"
          @tap="handleProductSelected(item)"
          :key="index"
          class="product-item flex align-center justify-between"
        >
          <view class="flex align-center">
            <image src="/static/images/icon_s_b8.svg" class="icon-24" />
            <rich-text :nodes="item.nodeName" v-if="item.itemCode" />
            <rich-text v-if="item.itemCode" :nodes="item.nodeCode" />
            <!-- <text class="ml-16 color-333">{{ formatStr(item.itemName, 10) }}</text>
            <text class="color-999">({{ formatStr(item.itemCode, 8) }})</text> -->
          </view>
          <view class="font-24 color-fff bg-64B5EA rounded-4 pl-4 pr-4">工序</view>
        </view>
      </template>
    </template>
    <template v-else>
      <view class="product-item flex align-center">
        <image src="/static/images/icon_s_b8.svg" class="icon-24" />
        <text class="font-28 color-b8b8b8 ml-16 flex-1">未查到相关产品或工序，请重新输入</text>
        <image @tap="close" src="/static/images/icon_del.svg" class="icon-48" />
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { escapeHtml, formatStr } from '@/utils/common'
import { computed } from 'vue'
const props = defineProps({
  resultList: {
    type: Array,
    default: () => []
  },
  searchValue: {
    type: String,
    default: ''
  }
})
const getDataProduct = computed(() => {
  if (!props.resultList.product) return []
  return props.resultList.product.map((item: any) => {
    const tempName = escapeHtml(formatStr(item.itemName, 10)).replace(
      new RegExp(props.searchValue, 'g'),
      `<span style="color: #0066ff;">${escapeHtml(props.searchValue)}</span>`
    )
    const nodeName = `<div class="ml-16 color-333 word-break">${tempName}</div>`

    const tempCode = escapeHtml(formatStr(item.itemCode, 10)).replace(
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
const getDataProcess = computed(() => {
  if (!props.resultList.process) return []
  return props.resultList.process.map((item: any) => {
    const tempName = escapeHtml(formatStr(item.itemName, 10)).replace(
      new RegExp(props.searchValue, 'g'),
      `<span style="color: #0066ff;">${escapeHtml(props.searchValue)}</span>`
    )
    const nodeName = `<div class="ml-16 color-333 word-break">${tempName}</div>`

    const tempCode = escapeHtml(formatStr(item.itemCode, 10)).replace(
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

const emits = defineEmits(['itemSelected', 'itemclose'])
function handleProductSelected(item) {
  emits('itemSelected', item)
}
function close() {
  emits('itemclose')
}
</script>

<style lang="scss" scoped>
.product-item {
  padding: px2vw(34) px2vw(32);
  border-bottom: px2vw(1) solid #f5f5f5;

  &:active {
    background-color: #f3f3f5;
  }
}
</style>
