<template>
  <view class="mt-8 bg-fff pb-32 tagshadow" v-if="recommendList.length">
    <view class="color-e7a11a bg-fff5e3 font-24 flex-center tips">推荐{{ keyWord }}</view>
    <view class="px-32 box flex flex-wrap mt-16 gap-16">
      <view
        class="recommend-item"
        @tap="handleProductSelected(item)"
        v-for="(item, index) in recommendList"
        :key="index"
      >
        <text>{{ item.itemName }}</text>
        <text v-if="keyWord !== '工序'">({{ item.itemCode }})</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { PropType } from 'vue'

type DataItem = {
  itemName: string
  itemCode: string
}

const props = defineProps({
  recommendList: {
    type: Array as PropType<DataItem[]>,
    default: () => []
  },
  keyWord: {
    type: String,
    default: '产品'
  }
})

const emits = defineEmits(['itemSelected'])
function handleProductSelected(item) {
  emits('itemSelected', item)
}
</script>

<style lang="scss" scoped>
.recommend-item {
  box-sizing: border-box;
  background: #ebf0f5;
  border-radius: px2vw(8);
  border: px2vw(2) solid #d3dfeb;
  padding: px2vw(4) px2vw(8);
  font-size: px2vw(28);
  color: #5a6f82;
  max-width: px2vw(304);
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;

  &:active {
    background: #0066ff;
    border: px2vw(2) solid #0066ff;
    color: #fff;
  }
}
.tips {
  width: px2vw(104);
  height: px2vw(32);
  background: #fff5e3;
  border-radius: 0 0 px2vw(8) 0;
}
.tagshadow {
  box-shadow: 0 px2vw(8) px2vw(16) px2vw(1) rgba(216, 221, 229, 1);
}
</style>
