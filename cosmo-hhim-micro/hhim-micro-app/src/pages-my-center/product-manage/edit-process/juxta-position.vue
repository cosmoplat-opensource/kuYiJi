<template>
  <view class="mx-16 flex flex-col overflow-auto rounded-16 bg-fff pb-32 box m-h-100">
    <view class="flex flex-wrap px-32 box pt-16" v-if="dataArr.length">
      <view v-for="(item, index) in dataArr" :class="{ 'mr-48': index % 2 === 0 }" :key="index">
        <view class="edit-card relative mt-32">
          <view class="parent-last-icon flex align-center">
            <word-icon text="尾" v-if="item.isLastProcess === '0'" :class="{ 'mr-32': item.isFirstProcess === '0' }" />
            <word-icon text="首" v-if="item.isFirstProcess === '0'" />
          </view>
          <view :class="item.processName ? 'color-333' : 'color-b6c0c9'" @tap="handleCardSelect(index)">
            <text>{{ item.processName || '选择工序 ' }}</text>
          </view>
        </view>
        <view class="flex-center mt-16">
          <image :src="formatImage('icon_del_circle', 'svg')" class="icon-48" @tap.stop="handleProcessDelete(index)" />
        </view>
      </view>
    </view>
    <view v-else class="pt-128 pb-64 box">
      <h-empty :tips-word="tipsText" />
    </view>
    <view class="mt-32 mx-32 box rounded-16 bg-f3f3f5 py-24 flex-center btn-hover" @tap="handlePopupOpen">
      <image :src="formatImage('icon_add_0066ff', 'svg')" class="icon-48" />
    </view>
    <popup-process-multiple
      ref="refPopupProcessMultiple"
      :dataArray="dataArr"
      @processSelected="handleProcessSelected"
    />
    <popup-process-order ref="refPopupProcessOrder" @orderSelect="handleOrderSelect" :processName="tempName" />
  </view>
</template>

<script setup lang="ts">
import { formatImage } from '@/utils/common'
import WordIcon from '@/components/word-icon.vue'
import { computed, ref } from 'vue'
import PopupProcessMultiple from '@/pages-my-center/product-manage/edit-process/popup-process-multiple.vue'
import PopupProcessOrder from '@/pages-my-center/product-manage/edit-process/popup-process-order.vue'
import HEmpty from '@/components/h-empty.vue'

const props = defineProps({
  sortType: {
    type: String,
    default: '顺序'
  },
  modelValue: {
    type: Array,
    default: () => []
  }
})
const tipsText = '暂无工序，请点击"+"号添加'

const emits = defineEmits(['update:modelValue', 'cardSelect', 'processDelete'])
const dataArr = computed({
  get: function () {
    const arr = props.modelValue as IProcessItem[]
    return arr.filter((v) => v.processName)
  },
  set: function (value) {
    emits('update:modelValue', value)
  }
})

const tempIndex = ref(0)
const tempName = ref('')
const refPopupProcessOrder = ref(null)
function handleCardSelect(index: number) {
  tempIndex.value = index
  const item = dataArr.value[index]
  tempName.value = item.processName
  let order = ''
  if (item.isFirstProcess === '0') {
    order = '首序'
  } else if (item.isLastProcess === '0') {
    order = '尾序'
  }
  refPopupProcessOrder.value?.handlePopupOpen(order)
}
function handleOrderSelect(val) {
  Object.assign(dataArr.value[tempIndex.value], val)
}
function handleProcessDelete(index: number) {
  dataArr.value.splice(index, 1)
  emits('update:modelValue', dataArr.value)
}

function handleProcessSelected(arr) {
  dataArr.value = arr
}

const refPopupProcessMultiple = ref(null)
function handlePopupOpen() {
  tempIndex.value = -1
  tempName.value = ''
  refPopupProcessMultiple.value.handlePopupOpen()
}
</script>
<style lang="scss">
.btn-hover:active {
  background-color: #ebf0f5;
}
/* 工艺节点卡片样式(原在父页面 edit-process-path.vue,父页面被 scoped 化后 H5 下子组件节点命中不了,改为组件内样式) */
.edit-card {
  width: px2vw(288);
  max-width: 288px;
  box-sizing: border-box;
  height: px2vw(80);
  line-height: px2vw(80);
  text-align: center;
  background: #f7f8e7;
  border-radius: px2vw(16);
  border: px2vw(2) dashed #9badbc;
  &.disable {
    background-color: #f5f5f5;
    border: px2vw(2) solid #f5f5f5;
  }
  &.empty {
    color: #b6c0c9;
    background-color: white;
  }
}
.parent-last-icon {
  position: absolute;
  top: px2vw(-20);
  left: 50%;
  transform: translateX(-50%);
}
</style>
