<template>
  <uni-popup ref="popupProcessSelect" type="bottom" :safe-area="false" :maskClick="false">
    <view class="flex flex-col bg-fff box rounded-16-top">
      <view class="p-24 box flex align-center justify-between b-b-1 border-f0f0f0">
        <view class="font-28 color-5a6f82 text-center flex-1 bold">添加工序</view>
        <image @tap="handlePopupClose" src="/static/images/icon_close_666.svg" class="icon-48 close" />
      </view>
      <view class="flex-center mt-32 pl-32 pr-24">
        <h-search class="flex-1" @searchInput="handleInput" bgClass="bg-f3f3f5" />
        <image :src="formatImage('icon_add_0066ff', 'svg')" class="icon-48 ml-24" @tap="handleAddProcess" />
      </view>
      <view class="box py-18 flex align-center px-32 b-b-1 border-f5f5f5">
        <view class="bold font-24 color-333">已选</view>
        <view class="bold font-24 color-ff0000 ml-8">{{ selectList.length }}</view>
        <view class="flex-1" />
        <view class="btn-check px-32" :class="{ checked: checkAll }" @tap="handleCheckAll">
          {{ checkAll ? '全不选' : '全选' }}
        </view>
      </view>
      <view class="flex flex-col mt-16 h-500 overflow-auto" v-if="processFormatList.length">
        <view
          v-for="(item, index) in processFormatList"
          :key="item.id"
          class="p-32 font-28 bold flex align-center b-b-1 border-f5f5f5"
          @tap="handleSelectProduct(item, index)"
        >
          <template v-if="item.checked">
            <view class="color-0066ff">{{ item.processName }}</view>
            <view class="color-0066ff flex-1">({{ item.processCode }})</view>
          </template>
          <template v-else>
            <view class="color-333">{{ item.processName }}</view>
            <view class="color-999 flex-1">({{ item.processCode }})</view>
          </template>
          <h-checkbox class="ml-24" :checked="item.checked" @checkedChange="handleSelectProduct(item, index)" />
        </view>
      </view>
      <view v-else class="py-128">
        <h-empty tipsWord="暂无相关工序，请重新搜索" />
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
import { onShow } from '@dcloudio/uni-app'
import { formatImage } from '@/utils/common'

const searchInput = ref('')
const inputFocus = ref(false)
const popupProcessSelect = ref(null)
const processList = ref<IProcessItem[]>([])
const selectList = ref<IProcessItem[]>([])
const checkAll = ref(false)

const props = defineProps({
  productData: {
    type: Object as PropType<Partial<TProductItem>>,
    default: () => ({})
  },
  dataArray: {
    type: Array as PropType<IProcessItem[]>,
    default: () => []
  }
})

onShow(() => {
  getProcessList()
})

function getProcessList() {
  _get({ url: '/process/selectByChain', data: { key: searchInput.value } }).then(
    (res: IResponseType<TProcessItemBase[]>) => {
      processList.value = res.data.map((v: TProcessItemBase): IProcessItem => {
        return {
          processCode: v.itemCode,
          processId: v.itemId,
          processName: v.itemName,
          processSeq: v.itemSeq,
          isFirstProcess: '1',
          isLastProcess: '1'
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

const processFormatList = computed(() => {
  const arr = selectList.value?.reduce((pre, v) => {
    pre.push(v.processId)
    return pre
  }, [])
  const temp = processList.value.map((v) => {
    v.checked = arr.includes(v.processId)
    return v
  })
  if (!searchInput.value) {
    return [...temp.filter((v) => v.checked), ...temp.filter((v) => !v.checked)]
  }
  return temp
})

function handlePopupOpen() {
  selectList.value = props.dataArray.map((v) => {
    v.checked = true
    return v
  })
  inputFocus.value = true
  getProcessList()
  popupProcessSelect.value.open('bottom')
}
function handlePopupClose() {
  inputFocus.value = false
  searchInput.value = ''
  selectList.value = []
  popupProcessSelect.value.close('bottom')
}
function handlePopupSelect() {
  emits('processSelected', selectList.value)
  handlePopupClose()
}
function handleSelectProduct(item, index) {
  item.checked = !item.checked
  if (item.checked) {
    selectList.value.push(item)
  } else {
    selectList.value.splice(index, 1)
  }
  if (selectList.value.length === 0) {
    checkAll.value = false
  } else if (selectList.value.length === processList.value.length) {
    checkAll.value = true
  }
}

const emits = defineEmits(['processSelected'])
function handleInput(e) {
  searchInput.value = e.detail.value
  getProcessList()
}
function handleInputClear() {
  searchInput.value = ''
  getProcessList()
}
defineExpose({ handlePopupOpen })

function handleCheckAll() {
  checkAll.value = !checkAll.value
  processList.value.forEach((v) => (v.checked = checkAll.value))
  if (checkAll.value) {
    processList.value.forEach((v) => {
      if (!selectList.value.find((s) => s.processId === v.processId)) {
        selectList.value.push(v)
      }
    })
  } else {
    const temp = []
    selectList.value.forEach((v) => {
      if (!processList.value.find((s) => s.processId === v.processId)) {
        temp.push(v)
      }
    })
    selectList.value = temp
  }
}
</script>

<style lang="scss" scoped>
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
.btn-check {
  padding: px2vw(10) px2vw(16);
  box-sizing: border-box;
  background-color: #f3f3f5;
  border-radius: px2vw(24);
  color: #5a6f82;
  font-size: px2vw(24);
  &.checked {
    background-color: #0066ff;
    color: #ffffff;
  }
}
</style>
