<template>
  <uni-popup ref="popupStaff" type="bottom" @maskClick="handleCancel" :safe-area="false">
    <view class="bg-fff flex flex-col justify-start font-28 rounded-16-top">
      <view class="p-24 box flex align-center b-b-1 border-f0f0f0">
        <view class="w-48" />
        <view class="flex-1 text-center font-28 color-5a6f82 bold">{{ title }}</view>
        <image @click="handleCancel" src="/static/images/icon_close_666.svg" class="icon-48" />
      </view>
      <view class="min-h-500">
        <view class="flex-center px-32 mt-32">
          <h-search class="flex-1" @searchInput="handleInput" bgClass="bg-f3f3f5" placeholder="输入产品编码/名称" />
        </view>
        <view class="flex py-16 font-24 mt-16 pl-32 box b-b-1 border-f5f5f5">
          <view class="color-333">已选</view>
          <view class="color-ff0000 bold ml-8">{{ checkedCount }}</view>
        </view>
        <view class="flex flex-col h-500 overflow-auto" v-if="dataList.length">
          <view
            v-for="(item, index) in dataList"
            :key="index"
            class="mx-32 py-32 box font-28 bold flex align-center b-b-1 border-f5f5f5"
            @tap="handleSelectProduct(item)"
          >
            <view class="flex flex-1 align-center">
              <view class="w-m-192 single" :class="item.checked ? 'color-0066ff' : 'color-333'">
                {{ item.itemName }}
              </view>
              <view class="w-m-192 single" :class="item.checked ? 'color-0066ff' : 'color-999'">
                ({{ item.itemCode || '-' }})
              </view>
            </view>
            <h-checkbox :checked="item.checked" @checkedChange="handleSelectProduct(item)" />
          </view>
        </view>
        <view v-else-if="!dataList.length && searchInput" class="py-128 flex flex-col align-center">
          <h-empty tipsWord="未查到相关产品，是否新增？" />
          <view class="mt-64 btn-empty-add" @tap="handleAddProduct">添加此产品</view>
        </view>
        <view v-else class="h-500" />
      </view>
      <view class="bg-fff py-32 box flex justify-center align-center font-32">
        <h-button width="296" height="72" text="取消" type="bg-f3f3f5 color-333" @tap.stop="handleCancel" />
        <h-button width="296" height="72" text="确认选择" class="ml-32" @tap.stop="confirmPopup" />
      </view>
    </view>
    <h-status-footer backgroundColor="#ffffff" />
  </uni-popup>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { _get, _post } from '@/utils/common-request'
import HEmpty from '@/components/h-empty.vue'
import HStatusFooter from '@/components/h-status-footer.vue'
import WordIcon from '@/components/word-icon.vue'
import { onShow } from '@dcloudio/uni-app'

const popupStaff = ref(null)
const searchInput = ref('')
const dataList = ref([])

const props = defineProps({
  title: {
    type: String,
    default: '选择记工产品'
  }
})

const emits = defineEmits(['productSelected'])
const baseList = ref([])

function open() {
  searchInput.value = ''
  initData()
  popupStaff.value.open('bottom')
}
function handleInput(str) {
  searchInput.value = str
  if (searchInput.value) {
    const reg = new RegExp(searchInput.value, 'gi')
    dataList.value = baseList.value.filter((v) => v.itemName.match(reg) || v.itemCode.match(reg))
  } else {
    handleInputClear()
  }
}
function handleInputClear() {
  searchInput.value = ''
  dataList.value = [...baseList.value.filter((v) => v.checked), ...baseList.value.filter((v) => !v.checked)]
}
function handleCancel() {
  popupStaff.value?.close('bottom')
}

function handleSelectProduct(val) {
  val.checked = !val.checked
  baseList.value.find((v) => v.itemSeq === val.itemSeq).checked = val.checked
}

const checkedCount = computed(() => {
  return baseList.value.filter((item) => item.checked).length
})

function confirmPopup() {
  const arr = baseList.value.filter((v) => v.checked)
  if (!arr.length) {
    uni.showToast({
      title: '请选择产品',
      icon: 'none'
    })
    return
  }
  emits('productSelected', arr)
  handleCancel()
}

function initData() {
  _get({ url: '/product/select', data: { key: searchInput.value, mixed: true } }).then((res) => {
    baseList.value = res.data
    dataList.value = res.data
  })
}
function handleAddProduct() {
  _post({ url: '/product/add', data: { productName: searchInput.value, productType: 'CP' } }).then(
    (res: IResponseType<{}>) => {
      const { productName, productCode, productSeq } = res.data
      const obj = {
        itemName: productName,
        itemCode: productCode,
        itemSeq: productSeq,
        standard: false,
        checked: true
      }
      baseList.value.unshift(obj)
      dataList.value.unshift(obj)
    }
  )
}

defineExpose({ open })
</script>
