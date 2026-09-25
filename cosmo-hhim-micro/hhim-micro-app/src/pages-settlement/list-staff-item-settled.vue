<template>
  <view v-for="(item, index) in dataList" :key="index" class="flex flex-col pr-32 pt-32 box bg-fff mb-8">
    <view class="flex align-center overflow-hidden" :class="[item.listShow ? '' : 'pb-32']">
      <view class="flex-1 flex align-center">
        <view class="block" />
        <view class="color-333 font-28 bold ml-16">{{ item.employeeName }}</view>
        <view class="flex-1" />
        <view class="bg-EBF0F5 flex-center h-32 px-16 rounded-16">
          <text class="color-5a6f82 font-24">结算</text>
          <text class="color-ff0000 ml-4 bold font-24">
            {{ String(item.totalSettledNum).replace(/^(.*\..{4}).*$/, '$1') }}
          </text>
        </view>
      </view>
      <image
        src="/static/images/icon_unfold.svg"
        class="icon-32 ml-32 shrink-0"
        :class="{ 'icon-rotate-y': item.listShow }"
        @tap="handleListShow(item)"
      />
    </view>
    <template v-if="item.listShow">
      <view class="pl-32">
        <view
          v-for="(itemDetail, detailIdx) in item.detailList.slice(0, item.showNum)"
          :key="detailIdx"
          :class="detailIdx + 1 === item.detailList.length ? '' : 'border-bottom-dashed'"
          class="pb-24 pt-32 box"
        >
          <view class="flex align-center font-28">
            <h-text-display class="bold color-333" :text="itemDetail.productName" :width="200" />
            <view class="color-999 bold">({{ itemDetail.productCode }})</view>
          </view>
          <view class="flex align-center justify-between mt-24 box">
            <h-text-display :text="itemDetail.operateProcessName" :width="256" class="color-5a6f82 font-28" />
            <view class="flex align-center justify-end">
              <word-icon text="共" class="ml-32" />
              <view class="color-333 font-28 bold mx-8">{{ itemDetail.settledNum }}</view>
              <view class="color-333 font-28 bold">{{ itemDetail.productUnit }}</view>
            </view>
          </view>
        </view>
      </view>
      <view
        v-if="item.detailList?.length > 5 && item.showNum < item.detailList.length"
        @tap="itemShowMore(item)"
        class="more h-80 font-28 color-0066ff bg-f3f3f5 rounded-16 flex justify-center align-center my-32 ml-32"
      >
        查看更多
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { $store } from '@/utils/common'

const dataList = computed(() => {
  const arr = $store.state.settlement.dataList
  arr.forEach((v) => {
    v.listShow = true
    if (v.detailList?.length > 5) {
      v.showNum = 5
    } else {
      v.showNum = v.detailList.length
    }
    return v
  })
  return arr
})
//列表展开
function handleListShow(item) {
  item.listShow = !item.listShow
  if (item.listShow) {
    if (item.detailList?.length > 5) {
      item.showNum = 5
    } else {
      item.showNum = item.detailList.length
    }
  }
}
function itemShowMore(item) {
  item.showNum += 10
}
</script>

<style lang="scss" scoped>
.number-mark {
  background: #ebf0f5;
  border-radius: 0 px2vw(4) px2vw(4) 0;
  height: px2vw(32);
  line-height: px2vw(32);
  position: absolute;
  z-index: 2;
  left: px2vw(-32);
  top: px2vw(4);
}
.more {
  &:active {
    background-color: #dbeaff;
  }
}
</style>
