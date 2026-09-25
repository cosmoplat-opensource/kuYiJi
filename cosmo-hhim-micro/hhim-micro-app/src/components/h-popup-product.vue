<template>
  <uni-popup ref="popupStaff" type="bottom" @maskClick="handleCancel" :safe-area="false">
    <view class="bg-fff flex flex-col justify-start font-28 rounded-16-top">
      <view class="p-24 box flex align-center b-b-1 border-f0f0f0">
        <view class="w-48" />
        <view class="flex-1 text-center font-28 color-5a6f82 bold">{{ title }}</view>
        <image @click="handleCancel" src="/static/images/icon_close_666.svg" class="icon-48" />
      </view>
      <view class="min-h-500">
        <view class="px-32 mt-32">
          <h-search
            :initValue="searchInput"
            @searchInput="handleInput"
            placeholder="输入产品编码名称"
            bgClass="bg-f3f3f5"
          />
        </view>
        <view class="flex flex-col mt-16 h-500 overflow-auto" v-if="dataList.length || !searchInput">
          <view
            v-for="item in dataList"
            :key="item.itemSeq"
            class="mx-32 py-24 box font-28 flex align-center b-b-1 border-f5f5f5"
            @tap="handleSelectProduct(item)"
          >
            <view class="flex flex-1 bold align-center">
              <view
                class="w-m-192 single"
                :class="itemSelect.itemName && itemSelect.itemSeq === item.itemSeq ? 'color-0066ff' : 'color-333'"
              >
                {{ item.itemName }}
              </view>
              <view
                class="w-m-192 single"
                :class="itemSelect.itemName && itemSelect.itemSeq === item.itemSeq ? 'color-0066ff' : 'color-999'"
              >
                ({{ item.itemCode || '-' }})
              </view>
            </view>
            <image
              v-if="itemSelect.itemName && itemSelect.itemSeq === item.itemSeq"
              :src="formatImage('icon_checked', 'svg')"
              class="icon-48"
            />
            <view v-else class="icon-48" />
          </view>
        </view>
        <view v-else-if="showAdd" class="py-128 flex flex-col align-center">
          <h-empty tipsWord="未查到相关产品，是否新增？" />
          <view class="mt-64 btn-empty-add" @tap="handleAddProduct">添加此产品</view>
        </view>
        <view v-else class="py-128 flex flex-col align-center">
          <h-empty tipsWord="未查询到相关产品，请重新搜索" />
        </view>
      </view>
      <view class="bg-fff py-32 flex-center">
        <h-button width="296" height="72" text="取消" type="bg-f3f3f5 color-333" @tap.stop="handleCancel" />
        <h-button width="296" height="72" text="确认选择" class="ml-32" @tap.stop="confirmPopup" />
      </view>
    </view>
    <h-status-footer backgroundColor="#ffffff" />
  </uni-popup>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { _get, _post } from '@/utils/common-request'
import { formatImage } from '@/utils/common'
import HEmpty from '@/components/h-empty.vue'
import HStatusFooter from '@/components/h-status-footer.vue'
import WordIcon from '@/components/word-icon.vue'

const popupStaff = ref(null)
const searchInput = ref('')
const dataList = ref([])
const itemSelect = ref({})

const props = defineProps({
  title: {
    type: String,
    default: '选择产品'
  },
  showAdd: {
    type: Boolean,
    default: true
  }
})

const emits = defineEmits(['productSelected'])

function open(item = { itemName: '' }) {
  itemSelect.value = item
  searchInput.value = item.itemName || ''
  initData()
  popupStaff.value.open('bottom')
}
function handleInput(str) {
  searchInput.value = str
  initData()
}
function handleInputClear() {
  searchInput.value = ''
  initData()
}
function handleCancel() {
  popupStaff.value?.close('bottom')
}

function handleSelectProduct(val) {
  itemSelect.value = val
  //可取消选中
  // if (JSON.stringify(itemSelect.value) == JSON.stringify(val)) {
  //   itemSelect.value = {}
  // } else {
  //   itemSelect.value = val
  // }
}
function confirmPopup() {
  if (!itemSelect.value.itemName && !props.showAdd) {
    uni.showToast({
      title: '请先选中一款产品',
      icon: 'none'
    })
    return
  }
  const obj = { ...itemSelect.value, t: new Date().getTime() }
  if (!obj.itemName) {
    obj.itemName = searchInput.value
  }
  emits('productSelected', obj)
  handleCancel()
}

function initData() {
  _get({ url: '/product/select', data: { key: searchInput.value, mixed: true } }).then((res) => {
    dataList.value = res.data || []
  })
}

function handleAddProduct() {
  _post({ url: '/product/add', data: { productName: searchInput.value, productType: 'CP' } }).then(
    (res: IResponseType<{}>) => {
      const { productName, productCode, productSeq } = res.data
      const obj = {
        itemName: productName,
        itemCode: productCode,
        itemSeq: productSeq
      }
      itemSelect.value = obj
      dataList.value.unshift(obj)
    }
  )
}

defineExpose({ open })
</script>
