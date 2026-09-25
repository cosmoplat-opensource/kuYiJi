<template>
  <uni-popup ref="popupProductSelect" type="bottom" :safe-area="false" :maskClick="false">
    <view class="flex flex-col bg-fff pb-32 box rounded-16-top">
      <view class="p-24 box flex align-center justify-between b-b-1 border-f0f0f0">
        <view class="font-28 color-5a6f82 text-center flex-1 bold">复制类似产品工艺</view>
        <image @tap="handlePopupClose" src="/static/images/icon_close_666.svg" class="icon-48 close" />
      </view>
      <view class="mt-32 mx-32">
        <h-search class="w-100" bgClass="bg-f3f3f5" @searchInput="handleInput" placeholder="输入产品编码/名称" />
      </view>
      <view class="flex flex-col mt-16 h-500 px-32 box overflow-y-auto" v-if="productList.length">
        <view
          v-for="(item, index) in productList"
          :key="item.id"
          class="py-24 font-28 flex align-center b-b-1 border-f5f5f5"
          @tap="handleSelectProduct(item)"
        >
          <view class="flex-1 flex flex-col">
            <view class="font-28 bold flex">
              <view :class="itemSelect.id === item.id ? 'color-0066ff' : item.standard ? 'color-333' : 'color-999'">
                <h-text-display :text="item.itemName" :width="212" />
              </view>
              <view :class="itemSelect.id === item.id ? 'color-0066ff' : 'color-999'">
                <h-text-display :width="212" :text="`(${item.itemCode})`" />
              </view>
            </view>
            <view class="flex">
              <view class="mark-00bfa5 mt-16" v-if="item.standard">标准工艺</view>
              <view class="mark-ebf0f5 mt-16" v-else>无标准工艺</view>
            </view>
          </view>
          <image v-if="item.id === itemSelect.id" :src="formatImage('icon_checked', 'svg')" class="icon-48" />
        </view>
        <view class="font-24 color-b6c0c9 py-48 text-center">仅展示有标准工艺的产品</view>
      </view>
      <view v-else class="px-32 py-128">
        <h-empty tipsWord="暂无相关产品，请重新搜索" />
      </view>
      <view class="flex-center mt-48 pb-32">
        <h-button width="296" height="72" text="取消" type="bg-f3f3f5 color-333" @tap.stop="handlePopupClose" />
        <h-button width="296" height="72" text="确定并复制" class="ml-32" @tap.stop="handlePopupSelect" />
      </view>
    </view>
  </uni-popup>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { _get } from '@/utils/common-request'
import HEmpty from '@/components/h-empty.vue'
import { formatImage } from '@/utils/common'
import { onShow } from '@dcloudio/uni-app'

const searchInput = ref('')
const itemSelect = ref({})
const popupProductSelect = ref(null)
const productList = ref([])

const props = defineProps({
  product: {
    type: Object,
    default: () => {}
  }
})

function getProductList() {
  _get({ url: '/tech/select/standardPro', data: { key: searchInput.value } }).then((res: IResponseType<[]>) => {
    productList.value = res.data.filter((v) => v.itemSeq !== props.product.productSeq && v.standard)
    itemSelect.value = res.data?.find((v) => v.itemName === searchInput.value) ?? {}
  })
}

function handlePopupOpen() {
  searchInput.value = ''
  itemSelect.value = {}
  getProductList()
  popupProductSelect.value.open('bottom')
}
function handlePopupClose() {
  popupProductSelect.value.close('bottom')
}
function handlePopupSelect() {
  if (!itemSelect.value.itemSeq) {
    uni.showToast({
      title: '请选择产品',
      icon: 'none'
    })
    return
  }
  emits('productSelected', itemSelect.value)
  handlePopupClose()
}
function handleSelectProduct(item) {
  if (!item.standard) {
    uni.showToast({
      title: `${item.itemName}无标准工艺,请重新选择`,
      icon: 'none'
    })
    return
  }
  itemSelect.value = item
}

const emits = defineEmits(['productSelected'])
function handleInput(str) {
  searchInput.value = str
  getProductList()
}
function handleInputClear() {
  searchInput.value = ''
  getProductList()
}

defineExpose({ handlePopupOpen })
</script>
