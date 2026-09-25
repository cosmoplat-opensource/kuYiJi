<template>
  <view class="list-item rounded-16 pl-32 pr-32 box" @tap="handleTapItem">
    <view class="flex align-start border-bottom-f5f5f5 pt-30 pb-24">
      <view class="flex flex-col flex-1 pt-2 bold">
        <h-text-display :text="dataItem.productName" :width="512" class="color-333 font-28" />
        <h-text-display :text="dataItem.productCode" :width="512" class="color-999 font-24 mt-16" />
      </view>
      <image v-if="!showDelete" src="/static/images/icon_edit.svg" class="icon-32" />
      <h-checkbox v-if="showDelete" :checked="dataItem.checked" class="ml-16" @checkedChange="handleCheckedAble" />
    </view>
    <view class="flex align-center h-200 pt-24 pb-32 box">
      <view class="flex-1 flex flex-col justify-between h-100">
        <view class="flex align-center">
          <view class="mark-ebf0f5">类型</view>
          <view class="font-28 ml-16 color-5a6f82">
            {{ dataItem.productType ? getProductType(dataItem.productType) : '-' }}
          </view>
        </view>
        <view class="flex">
          <view class="flex align-center">
            <view class="mark-ebf0f5">单位</view>
            <view class="font-28 ml-16 color-5a6f82">{{ dataItem.unit ? formatStr(dataItem.unit, 5) : '-' }}</view>
          </view>
          <view class="flex align-center ml-36">
            <view class="mark-ebf0f5">规格</view>
            <view class="font-28 ml-16 color-5a6f82">
              {{ dataItem.standards ? formatStr(dataItem.standards, 5) : '-' }}
            </view>
          </view>
        </view>
        <view class="color-999 font-24">
          产品描述：{{ dataItem.productDesc ? formatStr(dataItem.productDesc, 10) : '-' }}
        </view>
      </view>
      <image
        :src="dataItem.picture ? dataItem.picture.split(',')[0] : '/static/images/img_pro_default.svg'"
        class="icon-144 rounded-8"
        mode="aspectFill"
      />
    </view>
  </view>
</template>

<script setup lang="ts">
import { formatStr } from '@/utils/common'
import { getProductType } from '@/utils/formatData'
const props = defineProps({
  dataItem: {
    type: Object,
    default: () => {}
  },
  showDelete: {
    //展示选择框
    type: Boolean,
    default: false
  }
})

const emits = defineEmits(['itemCheck'])
function handleCheckedAble() {
  emits('itemCheck', props.dataItem)
}
function handleTapItem() {
  if (props.showDelete) {
    handleCheckedAble()
  } else {
    uni.navigateTo({
      url: '/pages-my-center/product-manage/add-edit?productData=' + JSON.stringify(props.dataItem)
    })
  }
}
</script>

<style lang="scss" scoped>
.common-tips {
  width: px2vw(56);
  height: px2vw(32);
  line-height: px2vw(32);
  border: px2vw(2) solid #d3dfeb;
}

.list-item {
  background-color: #fff;
  //   padding: px2vw(24) px2vw(32) !important;

  .top__status--shen {
    background-color: #ff4655;
    border-radius: 50%;
    color: #fff;
    font-size: px2vw(24);
    line-height: px2vw(24);
    padding: px2vw(4);
  }
}
</style>
