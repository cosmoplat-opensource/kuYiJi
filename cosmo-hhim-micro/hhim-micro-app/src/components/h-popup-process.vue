<template>
  <uni-popup ref="popupStaff" type="bottom" @maskClick="handleCancel" :safe-area="false">
    <view class="bg-fff flex flex-col justify-start font-28 rounded-16-top">
      <view class="p-24 box flex align-center b-b-1 border-f0f0f0">
        <view class="w-48" />
        <view class="flex-1 text-center font-28 color-5a6f82 bold">{{ formatPageTitle }}</view>
        <image @click="handleCancel" src="/static/images/icon_close_666.svg" class="icon-48" />
      </view>
      <view class="min-h-500">
        <view class="px-32 mt-32">
          <h-search
            :initValue="searchInput"
            @searchInput="handleInput"
            placeholder="输入工序编码/名称"
            bgClass="bg-f3f3f5"
          />
        </view>
        <view class="flex flex-col mt-16 h-500 overflow-auto" v-if="dataList.length || !searchInput">
          <view
            v-for="item in dataList"
            :key="item.itemSeq"
            class="mx-32 py-32 box font-28 flex align-center b-b-1 border-f5f5f5"
            @tap="handleSelectProduct(item)"
          >
            <view
              class="bold flex align-center flex-1"
              :class="itemSelect.itemName && itemSelect.itemSeq === item.itemSeq ? 'color-0066ff' : 'color-333'"
            >
              {{ item.itemName }}
              <word-icon text="新" class="ml-16" v-if="!item.itemCode" />
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
          <h-empty tipsWord="未查到相关工序，是否新增？" />
          <view class="mt-64 btn-empty-add" @tap="handleAddProcess">添加此工序</view>
        </view>
        <view v-else class="py-128 flex flex-col align-center">
          <h-empty tipsWord="未查询到相关工序，请重新搜索" />
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
import { computed, ref } from 'vue'
import { _get, _post } from '@/utils/common-request'
import { formatImage } from '@/utils/common'
import HEmpty from '@/components/h-empty.vue'
import HStatusFooter from '@/components/h-status-footer.vue'
import WordIcon from '@/components/word-icon.vue'
import HooksSettingConfig from '@/hooks/setting-config'
const { checkWorkerBaseDataConfine } = HooksSettingConfig()
const popupStaff = ref(null)
const searchInput = ref('')
const dataList = ref([])
const itemSelect = ref({})

const props = defineProps({
  title: {
    type: String,
    default: '选择产品'
  },
  productName: {
    type: String,
    default: ''
  },
  productSeq: {
    type: String,
    default: ''
  },
  productCode: {
    type: String,
    default: ''
  },
  standard: {
    type: Boolean,
    default: false
  },
  isPreProcess: {
    type: Boolean,
    default: false
  },
  processItem: {
    type: Object,
    default: () => {}
  },
  urlLink: {
    type: String,
    default: '/process/selectMixed'
  },
  showAdd: {
    type: Boolean,
    default: true
  }
})

const formatPageTitle = computed(() => {
  return `选择${props.title}`
})

const emits = defineEmits(['processSelected'])

function open(item) {
  itemSelect.value = item || {}
  searchInput.value = item?.itemName ?? ''
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
  if (!itemSelect.value.itemName && !props.showAdd) {
    uni.showToast({
      title: '请先选中一条工序',
      icon: 'none'
    })
    return
  }
  const obj = { ...itemSelect.value, t: new Date().getTime() }
  if (!obj.itemName) {
    obj.itemName = searchInput.value
  }
  emits('processSelected', obj)
  handleCancel()
}

function initData() {
  if (props.urlLink === '/process/selectMixed') {
    const params = {
      searchKey: searchInput.value,
      productCode: props.productCode,
      productName: props.productName,
      productSeq: props.productSeq,
      standard: props.standard
    }
    if (props.isPreProcess) {
      params.operateProcessCode = props.processItem.itemCode
      params.operateProcessSeq = props.processItem.itemSeq
    }
    _post({
      url: props.urlLink,
      data: params
    }).then((res) => {
      dataList.value = res.data.selectList || []
    })
  } else if (props.urlLink === '/process/select') {
    const params = {
      key: searchInput.value,
      productSeq: props.productSeq,
      standard: props.standard
    }
    _get({
      url: props.urlLink,
      data: params
    }).then((res) => {
      dataList.value = res.data || []
    })
  }
}

function handleAddProcess() {
  _post({ url: '/process/add', data: { processName: searchInput.value } }).then((res: IResponseType<{}>) => {
    const { processName, processCode, processSeq } = res.data
    const obj = {
      itemName: processName,
      itemCode: processCode,
      itemSeq: processSeq
    }
    itemSelect.value = obj
    dataList.value.unshift(obj)
  })
}

defineExpose({ open })
</script>
