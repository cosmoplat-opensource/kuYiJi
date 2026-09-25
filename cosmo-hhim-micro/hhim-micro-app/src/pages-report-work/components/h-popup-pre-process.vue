<template>
  <uni-popup ref="popupStaff" type="bottom" @maskClick="handleCancel" :safe-area="false">
    <view class="bg-fff flex flex-col justify-start font-28 rounded-16-top">
      <view class="p-24 box flex align-center b-b-1 border-f0f0f0">
        <view class="w-48" />
        <view class="flex-1 text-center font-28 color-5a6f82 bold">{{ formatPageTitle }}</view>
        <image @click="handleCancel" src="/static/images/icon_close_666.svg" class="icon-48" />
      </view>
      <view class="min-h-500">
        <view class="flex-center px-32 mt-24">
          <h-search class="flex-1" @searchInput="handleInput" bgClass="bg-f3f3f5" placeholder="输入工序编码/名称" />
        </view>
        <view class="flex flex-col mt-16 h-500 overflow-auto" v-if="dataList.length">
          <view
            v-for="item in dataList"
            :key="item.itemSeq"
            class="mx-32 py-32 box font-28 flex align-center b-b-1 border-f5f5f5"
            @tap="handleSelectProduct(item)"
          >
            <view
              class="bold line-height-32 flex align-center flex-1"
              :class="item.checked ? 'color-0066ff' : 'color-333'"
            >
              {{ item.itemName }}
              <word-icon text="新" class="ml-16" v-if="!item.itemCode" />
            </view>
            <image v-if="item.checked" :src="formatImage('icon_checked', 'svg')" class="icon-48" />
            <view class="icon-48" v-else />
          </view>
        </view>
        <view v-else class="py-128 flex flex-col align-center">
          <h-empty tipsWord="未查到相关工序，是否新增？" />
          <view class="mt-64 btn-empty-add" @tap="handleAddProcess">添加此工序</view>
        </view>
      </view>
      <view class="bg-fff py-32 box flex justify-center align-center">
        <h-button width="296" height="72" text="取消" type="bg-f3f3f5 color-333" @tap.stop="handleCancel" />
        <h-button width="296" height="72" text="确认选择" class="ml-32" @tap.stop="confirmPopup" />
      </view>
    </view>
    <h-status-footer backgroundColor="#ffffff" />
  </uni-popup>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { _post } from '@/utils/common-request'
import { formatImage } from '@/utils/common'
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
    default: '前工序'
  }
})

const formatPageTitle = computed(() => {
  return `选择${props.title}`
})

const emits = defineEmits(['processSelected'])
const productItem = reactive({})
const processItem = reactive({})
const tempProcessList = ref([])
function open(process, product, processSource) {
  Object.assign(productItem, product)
  Object.assign(processItem, processSource)
  tempProcessList.value = [
    {
      itemName: process.preProcessName || '',
      itemCode: process.preProcessCode || '',
      itemSeq: process.preProcessSeq || ''
    }
  ]
  searchInput.value = process.preProcessName || ''
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
  if (val.checked) {
    dataList.value.forEach((v) => {
      v.checked = v.itemSeq === val.itemSeq
    })
  }
}
function confirmPopup() {
  const arr = baseList.value.filter((v) => v.checked)
  if (!arr.length) {
    uni.showToast({ title: '请选择前工序', icon: 'none' })
    return
  }
  emits('processSelected', arr[0])
  handleCancel()
}
const baseList = ref(null)
function initData() {
  _post({
    url: '/process/selectMixed',
    data: {
      searchKey: searchInput.value,
      productName: productItem.itemName,
      productCode: productItem.itemCode,
      productSeq: productItem.itemSeq,
      operateProcessCode: processItem.itemCode,
      operateProcessSeq: processItem.itemSeq,
      standard: productItem.standard
    }
  }).then((res) => {
    const arr = (res.data.selectList || []).map((v) => {
      v.checked = tempProcessList.value.some((item) => item.itemSeq === v.itemSeq)
      return v
    })
    baseList.value = arr
    dataList.value = arr
  })
}
function handleAddProcess() {
  _post({ url: '/process/add', data: { processName: searchInput.value } }).then((res: IResponseType<{}>) => {
    const { processName, processCode, processSeq } = res.data
    const obj = {
      itemName: processName,
      itemCode: processCode,
      itemSeq: processSeq,
      checked: true
    }
    baseList.value.forEach((v) => (v.checked = false))
    dataList.value.forEach((v) => (v.checked = false))
    baseList.value.unshift(obj)
    dataList.value.unshift(obj)
  })
}
defineExpose({ open })
</script>
