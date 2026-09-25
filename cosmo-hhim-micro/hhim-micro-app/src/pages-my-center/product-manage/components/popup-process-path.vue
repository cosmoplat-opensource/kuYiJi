<template>
  <uni-popup ref="popupProcessSelect" type="bottom" :safe-area="false" :maskClick="false">
    <view class="flex flex-col bg-fff box rounded-16-top">
      <view class="p-24 box flex align-center justify-between b-b-1 border-f0f0f0">
        <view class="font-28 color-5a6f82 text-center flex-1 bold">选择工序</view>
        <image @tap="handlePopupClose" src="/static/images/icon_close_666.svg" class="icon-48 close" />
      </view>
      <view class="flex-center mt-32 pl-32 pr-24">
        <h-search class="flex-1" placeholder="输入工序编码/名称" @searchInput="handleInput" bgClass="bg-f3f3f5" />
        <image :src="formatImage('icon_add_0066ff', 'svg')" class="icon-48 ml-24" @tap="handleAddProcess" />
      </view>
      <view class="flex flex-col mt-16 h-500 overflow-auto" v-if="formatProcessList.length">
        <view
          v-for="(item, index) in formatProcessList"
          :key="index"
          :class="index > 0 ? 'b-t-1 border-f5f5f5' : ''"
          class="p-32 font-28 bold flex align-center"
          @tap="handleSelectProduct(item)"
        >
          <template v-if="itemSelect.processId === item.processId">
            <view class="color-0066ff">{{ item.processName }}</view>
            <view class="color-0066ff">({{ item.processCode }})</view>
            <word-icon text="首" v-if="isFirst" class="ml-16" />
            <view class="flex-1" />
            <image :src="formatImage('icon_checked', 'svg')" class="icon-48" />
          </template>
          <template v-else>
            <view class="color-333">{{ item.processName }}</view>
            <view class="color-999">({{ item.processCode }})</view>
            <view class="flex-1" />
            <view class="icon-48" />
          </template>
        </view>
      </view>
      <view v-else class="py-128">
        <h-empty :tipsWord="tipsText" />
      </view>
      <view
        class="py-24 px-32 mt-16 flex align-center justify-between first-area"
        :class="{ active: itemSelect.processName }"
      >
        <template v-if="!itemSelect.processName">
          <view class="font-24 color-5a6f82">请选择工序，选择后可设置首序</view>
          <view class="btn-first flex-center opacity-0">
            <text class="font-24">首序</text>
          </view>
        </template>
        <template v-if="itemSelect.processName && !isFirst">
          <view class="color-5a6f82 font-24">是否将所选工序设置为首序</view>
          <view class="btn-first flex-center" @tap="handleFirstChange">
            <text class="font-24">首序</text>
          </view>
        </template>
        <template v-if="itemSelect.processName && isFirst">
          <view class="color-5a6f82 font-24">所选工序已设置为首序，是否取消</view>
          <view class="btn-first flex-center" @tap="handleFirstChange">
            <text class="font-24">取消</text>
          </view>
        </template>
      </view>
      <view class="flex-center mt-48 pb-32">
        <h-button width="296" height="72" text="取消" type="bg-f3f3f5 color-333" @tap.stop="handlePopupClose" />
        <h-button width="296" height="72" text="确认添加" class="ml-32" @tap.stop="handlePopupSelect" />
      </view>
    </view>
  </uni-popup>
</template>

<script setup lang="ts">
import { computed, PropType, ref } from 'vue'
import { _get } from '@/utils/common-request'
import HEmpty from '@/components/h-empty.vue'
import { formatImage } from '@/utils/common'
import { onShow } from '@dcloudio/uni-app'
import WordIcon from '@/components/word-icon.vue'

const searchInput = ref('')
const inputFocus = ref(false)
const popupProcessSelect = ref(null)
const processList = ref<IProcessItem[]>([])
const isFirst = ref(false)
const itemSelect = ref({} as IProcessItem)
const tipsText = '暂无相关工序，请点击"+"号添加'

const props = defineProps({
  productData: {
    type: Object as PropType<Partial<TProductItem>>,
    default: () => ({})
  },
  dataArray: {
    type: Array as PropType<IProcessPath[]>,
    default: () => []
  },
  sourceItem: {
    type: Object as PropType<IProcessItem>,
    default: () => ({})
  }
})

onShow(() => {
  getProcessList()
})

const formatProcessList = computed(() => {
  const ids = []
  props.dataArray?.forEach((v) => {
    if (v.processId !== props.sourceItem.processId) {
      ids.push(v.processId)
    }
    v.targetArr?.forEach((t) => {
      if (t.processId !== props.sourceItem.processId) {
        ids.push(t.processId)
      }
    })
  })
  if (props.sourceItem.processId) {
    return [
      props.sourceItem,
      ...processList.value.filter((v) => !ids.includes(v.processId) && v.processId !== props.sourceItem.processId)
    ]
  } else {
    return processList.value.filter((v) => !ids.includes(v.processId))
  }
})

function getProcessList() {
  _get({ url: '/process/selectByChain', data: { key: searchInput.value } }).then(
    (res: IResponseType<TProcessItemBase[]>) => {
      processList.value = res.data.map((v: TProcessItemBase): IProcessItem => {
        return {
          processCode: v.itemCode,
          processId: v.itemId,
          processName: v.itemName,
          processSeq: v.itemSeq
        }
      })
    }
  )
}

function handleAddProcess() {
  uni.navigateTo({
    url: `/pages-my-center/manage-process/edit?processName=${searchInput.value}`
  })
}

const selectIds = ref([])

function handlePopupOpen() {
  inputFocus.value = true
  itemSelect.value = props.sourceItem
  isFirst.value = props.sourceItem.isFirstProcess === '0'
  getProcessList()
  popupProcessSelect.value.open('bottom')
}
function handlePopupClose() {
  inputFocus.value = false
  searchInput.value = ''
  isFirst.value = false
  itemSelect.value = {}
  popupProcessSelect.value.close('bottom')
}
function handlePopupSelect() {
  if (!itemSelect.value.processId) {
    uni.showToast({
      title: '请选择工序',
      icon: 'none'
    })
    return
  }
  emits('processSelected', { ...itemSelect.value, isFirstProcess: isFirst.value ? '0' : '1' })
  handlePopupClose()
}
function handleSelectProduct(item) {
  itemSelect.value = item
}

const emits = defineEmits(['processSelected'])
function handleInput(str) {
  searchInput.value = str
  getProcessList()
}
function handleInputClear() {
  searchInput.value = ''
  getProcessList()
}

function handleFirstChange() {
  isFirst.value = !isFirst.value
}

defineExpose({ handlePopupOpen })
</script>

<style lang="scss" scoped>
.btn-first {
  padding: px2vw(12) px2vw(24);
  background: #ffffff;
  border-radius: px2vw(8);
  border: px2vw(2) solid #0066ff;
  color: #0066ff;
  &:active {
    background-color: #dbeaff;
    color: #0066ff;
  }
}
.first-area {
  background-color: #f3f3f5;
  &.active {
    background-color: #f7f8e7;
  }
}

.btn-add {
  width: px2vw(96);
  height: px2vw(48);
  line-height: px2vw(48);
  text-align: center;
  background: rgba(61, 130, 234, 0);
  border-radius: px2vw(32);
  border: px2vw(2) solid #0066ff;
  color: #0066ff;
  &:active {
    background-color: #0066ff;
    color: #fff;
  }
}
</style>
