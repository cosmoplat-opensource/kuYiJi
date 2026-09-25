<template>
  <uni-popup ref="popupProductResemblanceSelect" type="bottom" :safe-area="false" :maskClick="false">
    <view class="flex flex-col bg-fff pb-32 box rounded-16-top">
      <view class="p-24 box flex align-center justify-between b-b-1 border-f0f0f0">
        <view class="font-28 color-5a6f82 text-center flex-1">推荐设置</view>
      </view>
      <view class="flex align-center bg-f7f8e7 px-32 py-16">
        <image src="/static/images/icon_tips.svg" class="icon-32" />
        <view class="flex-1 ml-16">
          <text class="font-24 color-5a6f82">识别出以下产品在应用此工艺，是否一同调整</text>
        </view>
      </view>
      <view class="flex py-18 px-32 box align-center justify-between b-b-1 border-f5f5f5">
        <view class="font-24">
          <text class="color-333">已选</text>
          <text class="color-ff0000 ml-4 bold">{{ getCheckedCount }}</text>
        </view>
        <view
          class="font-24 ml-16 px-16 py-10 rounded-24"
          :class="checkAll ? 'color-fff bg-0066ff' : 'color-5a6f82 bg-f3f3f5'"
          @tap="handleCheckAll"
          >{{ checkAll ? '全不选' : '全选' }}
        </view>
      </view>
      <view class="flex flex-col h-500 px-32 box overflow-auto">
        <view
          v-for="(item, index) in productList"
          :key="item.id"
          @tap="handleChecked(index)"
          class="pr-32 py-24 font-32 bold flex align-center"
          :class="index > 0 ? 'b-t-1 border-f5f5f5' : ''"
        >
          <view class="flex flex-col align-start flex-1">
            <view class="color-333 font-32">{{ item.productName }}</view>
            <view class="flex-center mt-14">
              <view class="color-5a6f82 font-28">{{ item.productCode }}</view>
            </view>
          </view>
          <h-checkbox :checked="item.checked" class="mr-16" @checkedChange="handleChecked(index)" />
        </view>
      </view>
      <view class="flex-center mt-48 pb-32">
        <h-button
          width="296"
          height="72"
          text="仅保存当前产品"
          class="bg-fff border-1 border-0066ff color-0066ff"
          @tap.stop="handlePopupClose"
        />
        <h-button width="296" height="72" text="应用其他产品" class="ml-32" @tap.stop="handlePopupConfirm" />
      </view>
    </view>
  </uni-popup>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { formatImage } from '@/utils/common'
import { _post } from '@/utils/common-request'

const popupProductResemblanceSelect = ref(null)
const productList = ref([])
const emits = defineEmits(['popupBack'])
const props = defineProps({
  isSortOrder: {
    type: Boolean,
    default: false
  }
})

const checkAll = ref(false)
const getCheckedCount = computed(() => {
  return productList.value.filter((v) => v.checked).length
})
function handleCheckAll() {
  checkAll.value = !checkAll.value
  productList.value.forEach((v) => {
    v.checked = checkAll.value
  })
}
function handleChecked(index) {
  productList.value[index].checked = !productList.value[index].checked
  checkAll.value = productList.value.every((v) => v.checked)
}

const sourceType = ref('')
const chainList = ref([])
function handlePopupOpen(dataList, type, cList) {
  sourceType.value = type
  chainList.value = cList
  productList.value = dataList.map((v) => {
    return {
      ...v,
      checked: false
    }
  })
  popupProductResemblanceSelect.value.open('bottom')
}
function handlePopupClose() {
  popupProductResemblanceSelect.value.close('bottom')
  emits('popupBack')
}
function handlePopupConfirm() {
  const arr = productList.value.filter((v) => v.checked)
  if (arr.length) {
    _post({
      url: '/tech/coverOrSimilar',
      data: {
        [sourceType.value]: arr,
        chainList: chainList.value,
        standard: true,
        techPattern: props.isSortOrder ? 10 : 20
      }
    }).then((res) => {
      handlePopupClose()
    })
  } else {
    uni.showToast({
      title: '请选择产品',
      icon: 'none'
    })
  }
}

defineExpose({ handlePopupOpen })
</script>

<style lang="scss" scoped></style>
