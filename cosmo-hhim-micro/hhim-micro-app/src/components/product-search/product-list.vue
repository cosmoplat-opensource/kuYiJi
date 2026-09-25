<template>
  <scroll-view scroll-y class="m-h-500 box bg-fff overflow-auto absolute product-item-wrapper">
    <template v-if="getDataList.length > 0">
      <view
        v-for="(item, index) in getDataList"
        @tap="handleProductSelected(item)"
        :key="index"
        class="product-item flex align-center overflow-auto"
      >
        <image src="/static/images/icon_s_b8.svg" class="icon-24" />
        <view class="flex-1 flex align-center">
          <rich-text :nodes="item.nodeName" v-if="item.itemCode" />
          <view v-else class="color-5a6f82 ml-16">{{ item.itemName }}</view>
          <rich-text v-if="item.itemCode" :nodes="item.nodeCode" />
          <!-- <view class="ml-16 color-333 word-break">{{ formatStr(item.itemName, 10) }}</view>
          <view class="color-999" v-if="item.itemCode">({{ item.itemCode }})</view> -->
        </view>
        <view class="p-4 bg-EBF0F5 color-5a6f82 font-24 ml-8" v-if="item.unapprovedNum"
          >未审核 {{ item.unapprovedNum }}</view
        >
        <view class="p-4 bg-EBF0F5 color-5a6f82 font-24 ml-8" v-if="item.totalStockNum"
          >库存 {{ item.totalStockNum }}</view
        >
      </view>
    </template>
    <template v-else>
      <view class="product-item flex align-center">
        <image src="/static/images/icon_s_b8.svg" class="icon-24" />
        <text class="font-28 color-b8b8b8 ml-16 flex-1">未查到相关产品，请重新输入</text>
        <!-- <image @tap="close" src="/static/images/icon_del.svg" class="icon-48" /> -->
      </view>
    </template>
  </scroll-view>
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
  showInput: {
    //展示无查询结果时的输入值
    type: Boolean,
    default: true
  }
})
const getDataList = computed(() => {
  return props.productList
    .filter((v) => (!props.showInput && v.itemCode) || props.showInput)
    .map((item: any) => {
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
function handleProductSelected(item: IDataType) {
  emits('itemSelected', item)
}
function close() {
  emits('itemclose')
}
</script>

<style lang="scss" scoped>
.product-item-wrapper {
  background: #ffffff;
  box-shadow: 0 px2vw(8) px2vw(16) px2vw(1) #d8dde5;
  // top: px2vw(72);
  left: 2.2vw;
  z-index: 99;
  width: calc(100% - 4.4vw);
}
.product-item {
  padding: px2vw(34) px2vw(32);
  border-bottom: px2vw(1) solid #f5f5f5;

  &:active {
    background-color: #f3f3f5;
  }
}
</style>
