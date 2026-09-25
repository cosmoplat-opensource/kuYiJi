<template>
  <uni-popup ref="popupProcessSelect" type="bottom" :safe-area="false" :maskClick="false">
    <view class="flex flex-col bg-fff box rounded-16-top">
      <view class="p-24 box flex align-center justify-between b-b-1 border-f0f0f0">
        <view class="font-28 color-5a6f82 text-center flex-1 bold">设置首尾序</view>
        <image @tap="handlePopupClose" src="/static/images/icon_close_666.svg" class="icon-48 close" />
      </view>
      <view class="flex align-center font-28 box pt-32 px-32">
        <view class="color-5a6f82">将工序</view>
        <view class="color-333 mx-8 bold">{{ processName }}</view>
        <view class="color-5a6f82">设置为</view>
      </view>
      <view class="flex flex-col mt-16">
        <view
          v-for="(item, index) in dataList"
          :key="index"
          class="p-32 font-28 bold flex align-center"
          @tap="handleSelect(item)"
        >
          <template v-if="dataSelect === item">
            <view class="color-0066ff flex-1">{{ item }}</view>
            <image :src="formatImage('icon_checked', 'svg')" class="icon-48" />
          </template>
          <template v-else>
            <view class="color-333 flex-1">{{ item }}</view>
            <view class="icon-48" />
          </template>
        </view>
      </view>
      <view class="flex-center mt-48 pb-32">
        <h-button width="296" height="72" text="取消" type="bg-f3f3f5 color-333" @tap.stop="handlePopupClose" />
        <h-button width="296" height="72" text="确认" class="ml-32" @tap.stop="handlePopupSelect" />
      </view>
    </view>
  </uni-popup>
</template>

<script setup lang="ts">
import { PropType, ref } from 'vue'
import { formatImage } from '@/utils/common'

const props = defineProps({
  productData: {
    type: Object as PropType<Partial<TProductItem>>,
    default: () => ({})
  },
  processName: {
    type: String as PropType<string>,
    default: ''
  }
})

const dataList = ['首序', '尾序']
const dataSelect = ref('')
function handleSelect(item: string) {
  if (dataSelect.value === item) {
    dataSelect.value = ''
  } else {
    dataSelect.value = item
  }
}

const popupProcessSelect = ref(null)
function handlePopupOpen(order) {
  dataSelect.value = order || ''
  popupProcessSelect.value.open('bottom')
}

function handlePopupClose() {
  popupProcessSelect.value.close()
}

const emits = defineEmits(['orderSelect'])
function handlePopupSelect() {
  let obj = {} as IProcessItem
  if (dataSelect.value === '首序') {
    obj.isFirstProcess = '0'
    obj.isLastProcess = '1'
  } else if (dataSelect.value === '尾序') {
    obj.isFirstProcess = '1'
    obj.isLastProcess = '0'
  } else {
    obj.isFirstProcess = '1'
    obj.isLastProcess = '1'
  }
  emits('orderSelect', obj)
  handlePopupClose()
}

defineExpose({ handlePopupOpen })
</script>

<style lang="scss" scoped></style>
