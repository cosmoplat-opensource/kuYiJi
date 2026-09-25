<template>
  <uni-popup ref="popupStaff" type="bottom" @maskClick="handleCancel" :safe-area="false">
    <view class="bg-fff flex flex-col justify-start font-28 rounded-16-top">
      <view class="p-24 box flex align-center b-b-1 border-f0f0f0">
        <view class="w-48" />
        <view class="flex-1 text-center font-28 color-5a6f82 bold">选择不良类型</view>
        <image @click="handleCancel" src="/static/images/icon_close_666.svg" class="icon-48" />
      </view>
      <view class="min-h-500">
        <view class="flex-center pl-32 pr-24 mt-32">
          <h-search class="flex-1" bgClass="bg-f3f3f5" placeholder="输入不良类型" @searchInput="handleInput" />
        </view>
        <view class="flex flex-col mt-16 h-500 overflow-auto" v-if="dataList.length || !searchInput">
          <view
            v-for="item in dataList"
            :key="item.itemSeq"
            class="mx-32 py-32 box font-28 flex align-center b-b-1 border-f5f5f5"
            @tap="handleSelectProduct(item)"
          >
            <view
              class="bold line-height-32 flex align-center flex-1"
              :class="itemSelect.itemName === item.itemName ? 'color-0066ff' : 'color-333'"
            >
              {{ item.itemName }}
            </view>
            <image
              v-if="itemSelect.itemName === item.itemName"
              :src="formatImage('icon_checked', 'svg')"
              class="icon-48"
            />
            <view v-else class="icon-48" />
          </view>
        </view>
        <view v-else class="py-128 flex flex-col align-center">
          <h-empty tipsWord="未查到相关不良类型，是否新增？" />
          <view class="mt-64 btn-empty-add" @tap="handleAddProcess">添加此不良类型</view>
        </view>
      </view>
      <view class="bg-fff py-32 box flex justify-center align-center font-32">
        <h-button width="296" height="72" text="取消" type="bg-f3f3f5 color-333" @tap.stop="handleCancel" />
        <h-button width="296" height="72" text="确认选择" class="ml-32" @tap.stop="confirmPopup" />
      </view>
    </view>
  </uni-popup>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { _get } from '@/utils/common-request'
import { formatImage } from '@/utils/common'
import HEmpty from '@/components/h-empty.vue'
import WordIcon from '@/components/word-icon.vue'

const popupStaff = ref(null)
const searchInput = ref('')
const dataList = ref([])
const itemSelect = ref({})

const props = defineProps({
  typeName: {
    type: String,
    default: ''
  }
})

const emits = defineEmits(['typeSelected'])

function open() {
  searchInput.value = props.typeName
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
}
function confirmPopup() {
  emits('typeSelected', { ...itemSelect.value, t: new Date().getTime() })
  handleCancel()
}

function initData() {
  _get({ url: '/ngProduct/manage/ngType/list', data: { key: searchInput.value } }).then(
    (res: IResponseType<string[]>) => {
      dataList.value = res.data.map((v) => {
        return { itemName: v }
      })
    }
  )
}

function handleAddProcess() {
  itemSelect.value = { itemName: searchInput.value }
  dataList.value.push(itemSelect.value)
}

defineExpose({ open })
</script>
