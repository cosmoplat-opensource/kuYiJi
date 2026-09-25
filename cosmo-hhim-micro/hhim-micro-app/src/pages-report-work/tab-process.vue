<template>
  <view class="mx-16 flex-1 flex flex-col overflow-auto">
    <!--报工工序-->
    <report-input-select
      :processMark="REPORT_WORK.NOW"
      :productSeq="productItem.itemSeq"
      :productCode="productItem.itemCode"
      :productName="productItem.itemName"
      :standard="checkIsStandard"
      @itemSelected="handleProcessSelectNow"
      class="mt-16"
      isProcess
      :inputData="processItemNow"
      :showAdd="!checkWorkerBaseDataConfine"
    >
      <template #mark>
        <view class="process-mark purple">
          <text>报工工序</text>
        </view>
      </template>
      <template #lastProcess>
        <view class="flex align-center mt-32 pb-16">
          <view class="flex align-center" @tap="handleIsLastProcess">
            <h-checkbox-icon :checked="processItemNow.isLastProcess === '0'" />
            <view class="ml-16" :class="processItemNow.isLastProcess === '0' ? 'color-333' : 'color-5a6f82'">
              <text class="font-24">我是最后一道工序</text>
            </view>
          </view>
          <view class="flex align-center ml-48" @tap="handleIsFirstProcess">
            <h-checkbox-icon :checked="processItemNow.isFirstProcess === '0'" />
            <view class="ml-16" :class="processItemNow.isFirstProcess === '0' ? 'color-333' : 'color-5a6f82'">
              <text class="font-24">我是首序</text>
            </view>
          </view>
        </view>
      </template>
    </report-input-select>
    <!--前工序-->
    <report-input-select
      v-if="processItemNow.isFirstProcess !== '0' && !standardPreProcess.length"
      :processMark="REPORT_WORK.BEFORE"
      @itemSelected="handleProcessSelectPre"
      :productSeq="productItem.itemSeq"
      :productCode="productItem.itemCode"
      :productName="productItem.itemName"
      :standard="checkIsStandard"
      class="mt-16"
      isProcess
      isPreProcess
      :processItem="processItemNow"
      :inputData="processItemPre"
    >
      <template #mark>
        <view class="process-mark blue">
          <text>前工序</text>
        </view>
      </template>
      <template #lastProcess>
        <view class="pt-16 box">
          <text class="color-5a6f82 font-24">请准确选择前工序，保障工序库存的准确性</text>
        </view>
      </template>
    </report-input-select>
    <!--标准工序前工序列表-->
    <view v-if="standardPreProcess.length" class="relative mt-16 pt-48 pb-16 px-32 bg-fff box flex flex-col">
      <view class="process-mark blue">
        <text>前工序</text>
      </view>
      <view
        v-for="(item, index) in standardPreProcess"
        :key="item.processSeq"
        :class="index > 0 ? 'b-t-1 border-f5f5f5' : ''"
      >
        <view class="py-24 flex align-center justify-between box color-333 font-32 overflow-hidden">
          <h-text-display :text="item.processName" :width="200" class="bold" />
          <h-process-stock :dataItem="stockMaps[item.processSeq]" v-if="stockMaps[item.processSeq]" />
        </view>
      </view>
    </view>
    <view class="flex-1 flex align-end justify-center pb-32">
      <h-button width="296" height="72" text="上一步" type="bg-fff color-333" @tap.stop="handleBtnPre" />
      <h-button width="296" height="72" text="下一步" class="ml-32" @tap.stop="handleBtnNext" />
    </view>
  </view>
</template>

<script setup lang="ts">
import ReportInputSelect from '@/components/report-input-select.vue'
import HCheckboxIcon from '@/components/h-checkbox-icon.vue'
import { computed, reactive, watch } from 'vue'
import { $store, REPORT_WORK } from '@/utils/common'
import { _get } from '@/utils/common-request'
import HooksProductStandard from '@/hooks/product-standard'
import HProcessStock from '@/components/h-process-stock.vue'
import HooksSettingConfig from '@/hooks/setting-config'
const { checkWorkerBaseDataConfine } = HooksSettingConfig()
const stockMaps = computed(() => $store.state.process.stockMaps)
const props = defineProps({
  productItem: {
    type: Object,
    default: () => {}
  },
  preItem: {
    type: Object,
    default: () => {}
  },
  nowItem: {
    type: Object,
    default: () => {}
  },
  standardPreProcess: {
    type: Array,
    default: () => []
  }
})

const { checkIsStandard } = HooksProductStandard()

const processItemPre = reactive({ itemName: '', itemCode: '', itemSeq: '' })
const processItemNow = reactive({ itemName: '', itemCode: '', itemSeq: '', isLastProcess: '1', isFirstProcess: '1' })
// 产品选中
const emit = defineEmits(['processPreSelected', 'processNowSelected', 'tabActiveChange'])
function handleProcessSelectPre(item) {
  Object.assign(processItemPre, item)
  emit('processPreSelected', item)
  if (!item.itemSeq) return
  // 获取所选工序的库存信息
  $store.dispatch('process/getProcessStock', { productSeq: props.productItem.itemSeq, processList: [item.itemSeq] })
}
function handleProcessSelectNow(item) {
  Object.assign(processItemNow, item)
  emit('processNowSelected', item)
  if (!item.itemSeq) return
  // 获取所选工序的库存信息
  $store.dispatch('process/getProcessStock', { productSeq: props.productItem.itemSeq, processList: [item.itemSeq] })
  // 根据已选工序获取首序和尾序的标识
  _get({
    url: '/submit/getFirstOrLastProcessFlag',
    data: { productSeq: props.productItem.itemSeq, processSeq: item.itemSeq, standard: checkIsStandard.value }
  }).then((res: IResponseType<any>) => {
    if (res.data) {
      const { isLastProcess, isFirstProcess } = res.data
      item.isLastProcess = isLastProcess
      item.isFirstProcess = isFirstProcess
      if (isFirstProcess === '0') {
        Object.assign(processItemPre, { itemName: '', itemCode: '', itemSeq: '' })
        emit('processPreSelected', processItemPre)
      }
    } else {
      item.isLastProcess = '1'
      item.isFirstProcess = '1'
    }
    Object.assign(processItemNow, item)
    emit('processNowSelected', item)
  })
}

function handleIsLastProcess() {
  if (checkIsStandard.value) {
    uni.showModal({
      title: '提示',
      content: '已规定标准工艺路线,不可更改勾选信息',
      showCancel: false
    })
    return
  }
  processItemNow.isLastProcess = processItemNow.isLastProcess === '1' ? '0' : '1'
  emit('processPreSelected', processItemPre)
  emit('processNowSelected', processItemNow)
}
function handleBtnPre() {
  emit('tabActiveChange', 1, true)
}

function handleBtnNext() {
  if (!processItemNow.itemName) {
    uni.showToast({ title: '请选择报工工序', icon: 'none' })
    return
  }
  if (processItemNow.itemName === processItemPre.itemName) {
    uni.showToast({ title: '前工序和报工工序不能为同一工序', icon: 'none' })
    return
  }
  if (!checkIsStandard.value && processItemNow.isFirstProcess === '1' && !processItemPre.itemName) {
    uni.showModal({
      title: '提示',
      content: '您还未填写前工序，请填写',
      cancelText: '这是首序',
      confirmText: '去完善',
      success: (res) => {
        if (!res.confirm) {
          handleIsFirstProcess()
        }
      }
    })
    return
  }
  if (checkIsStandard.value && !processItemNow.itemSeq) {
    uni.showModal({
      title: '提示',
      content: '已规定标准工艺路线,请从报工工序下拉列表中选择工序',
      showCancel: false
    })
    return
  }
  emit('tabActiveChange', 3, true)
}
function handleIsFirstProcess() {
  if (checkIsStandard.value) {
    uni.showModal({
      title: '提示',
      content: '已规定标准工艺路线,不可更改勾选信息',
      showCancel: false
    })
    return
  }
  processItemNow.isFirstProcess = processItemNow.isFirstProcess === '1' ? '0' : '1'
  if (processItemNow.isFirstProcess === '0') {
    Object.assign(processItemPre, { itemName: '', itemCode: '', itemSeq: '' })
  }
  emit('processPreSelected', processItemPre)
  emit('processNowSelected', processItemNow)
}

watch(
  props.preItem,
  (val) => {
    Object.assign(processItemPre, val)
  },
  { immediate: true, deep: true }
)

watch(
  props.nowItem,
  (val) => {
    Object.assign(processItemNow, val)
  },
  { immediate: true, deep: true }
)
defineExpose({ handleBtnNext })
</script>

<style lang="scss" scoped>
//角标
.process-mark {
  position: absolute;
  left: 0;
  top: 0;
  padding: px2vw(4) px2vw(8);
  font-size: px2vw(24);
  border-radius: 0 0 px2vw(8) 0;
  background-color: #ebf0f5;
  color: #5a6f82;
}
</style>
