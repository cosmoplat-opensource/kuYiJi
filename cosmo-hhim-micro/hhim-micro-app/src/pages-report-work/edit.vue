<template>
  <view class="h-full bg-f3f3f5 box flex flex-col">
    <uni-nav-bar />
    <h-status-header :title="getTitle" />
    <view class="h-64 flex align-center px-32 bg-fff rounded-16 mx-16 box" v-if="!readOnly">
      <view class="w-104 h-32 flex align-center justify-center rounded-4 bg-f3f3f5 mr-16">
        <text class="color-5a6f82 font-24">记工日期</text>
      </view>
      <text class="font-28 color-5a6f82">{{ formatDate(optionsData.date, 'MM-DD') }}</text>
      <view class="mark-24a8ff border ml-16" v-if="optionsData.checkStatus === 1">待检</view>
      <view class="mark-00bfa5 border ml-16" v-if="optionsData.checkStatus === 2">已检</view>
      <template v-if="optionsData.expiredRecordFlag === 0">
        <view class="w-104 h-32 flex align-center bg-FE9F00 justify-center rounded-4 mr-16 ml-40">
          <text class="color-fff font-24">补录日期</text>
        </view>
        <text class="font-28 color-5a6f82">{{ formatDate(optionsData.createdDate) }}</text>
      </template>
    </view>
    <view class="flex flex-wrap align-center mx-16 box" v-if="readOnly" tabindex="1">
      <view class="flex align-center mb-16 mr-40">
        <view class="w-56 text-center py-2 font-24 color-5a6f82 rounded-4 bg-fff">记工</view>
        <template v-if="userItem.expiredRecordFlag === 0">
          <view class="color-5a6f82 font-28 ml-16">{{ formatDate(userItem.submitDay, 'MM-DD') }}</view>
        </template>
        <template v-else>
          <view class="color-5a6f82 font-28 ml-16">
            <text>{{ formatDate(userItem.createdDate) }}</text>
          </view>
        </template>
        <view class="mark-24a8ff border ml-16" v-if="optionsData.checkStatus === 1">待检</view>
        <view class="mark-00bfa5 border ml-16" v-if="optionsData.checkStatus === 2">已检</view>
      </view>
      <template v-if="userItem.expiredRecordFlag === 0">
        <view class="flex align-center mb-16 mr-40">
          <view class="w-56 h-32 flex-center rounded-4 bg-FE9F00">
            <text class="font-24 color-fff">补录</text>
          </view>
          <view class="color-5a6f82 font-28 ml-16">{{ formatDate(userItem.createdDate, 'YYYY-MM-DD HH:mm') }}</view>
        </view>
      </template>
      <view class="flex align-center mb-16">
        <view class="icon-32 bg-68a2f8 rounded-full flex align-center justify-center" v-if="userItem.checkNickName">
          <text class="font-24 color-fff">审</text>
        </view>
        <view class="color-5a6f82 font-28 ml-16">{{ formatStr(userItem.checkNickName, 12) }}</view>
        <view class="color-5a6f82 font-28 ml-16 mr-40">{{ formatDate(userItem.checkDate) }}</view>
      </view>
    </view>
    <!--从审核来的显示记工人信息-->
    <view v-if="optionsData.from === 'audit'" class="flex align-center mx-16 box mb-16 mt-40">
      <view class="w-56 h-32 text-center line-height-32 font-24 color-5a6f82 bg-fff">记工</view>
      <view class="color-5a6f82 font-28 ml-16">{{ formatStr(optionsData.submitUser, 12) }}</view>
      <view class="color-5a6f82 font-28 ml-16">{{ formatDate(optionsData.createdDate) }}</view>
    </view>
    <view class="flex-1 mt-16 px-16 box flex flex-col overflow-auto">
      <!--产品-->
      <report-input-select
        @itemSelected="handleProductSelect"
        :processMark="REPORT_WORK.PRODUCT"
        :read-only="readOnly"
        :inputData="productItem"
        :showAdd="!checkWorkerBaseDataConfine"
      >
        <template #mark>
          <view class="process-mark blue-deep">
            <text>产品</text>
          </view>
        </template>
      </report-input-select>
      <!--报工工序-->
      <report-input-select
        :processMark="REPORT_WORK.NOW"
        class="mt-16"
        :product-item="productItem"
        @itemSelected="handleProcessSelectNow"
        :productSeq="productItem.itemSeq"
        :productCode="productItem.itemCode"
        :productName="productItem.itemName"
        isProcess
        :inputData="processItemNow"
        :read-only="readOnly"
        :showAdd="!checkWorkerBaseDataConfine"
      >
        <template #mark>
          <view class="process-mark purple">
            <text>报工工序</text>
          </view>
        </template>
        <template #lastProcess>
          <view class="flex align-center mt-32 pb-16" v-if="!readOnly">
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
        :processMark="REPORT_WORK.BEFORE"
        @itemSelected="handleProcessSelectPre"
        :productSeq="productItem.itemSeq"
        :productCode="productItem.itemCode"
        :productName="productItem.itemName"
        class="mt-16"
        v-if="processItemNow.isFirstProcess !== '0' && !standardPreProcess.length"
        :product-item="productItem"
        isProcess
        :readOnly="readOnly"
        :inputData="processItemPre"
      >
        <template #mark>
          <view class="process-mark blue">
            <text>前工序</text>
          </view>
        </template>
      </report-input-select>
      <!--标准工序前工序列表-->
      <view
        v-if="processItemNow.isLastProcess !== '0' && standardPreProcess.length"
        class="relative mt-16 pt-48 pb-16 px-32 bg-fff box flex flex-col"
      >
        <view class="process-mark blue">
          <text>前工序</text>
        </view>
        <view
          v-for="(item, index) in standardPreProcess"
          :key="item.processSeq"
          :class="index > 0 ? 'b-t-1 border-f5f5f5' : ''"
        >
          <view class="py-24 flex align-center justify-between box bold color-333 font-32 overflow-hidden">
            <h-text-display :text="item.processName" />
            <h-process-stock :dataItem="stockMaps[item.processSeq]" v-if="stockMaps[item.processSeq]" />
          </view>
        </view>
      </view>
      <!--数量模块-->
      <tab-num
        class="flex flex-col"
        ref="refTabNum"
        :read-only="readOnly"
        :checkStatus="checkStatus"
        :btnShow="false"
        :countItem="countItem"
        @reportWork="handleReport"
      />
      <operation-log
        ref="refOperationLog"
        class="mt-16"
        v-if="submitId"
        :submitNo="submitNo"
        v-model="scrollIntoView"
      />
    </view>
    <view class="flex justify-center py-32" v-if="!readOnly">
      <h-button
        width="296"
        height="72"
        text="送检"
        type="border-1 border-0066ff color-0066ff bg-fff"
        class="mr-32"
        @tap.stop="handleSendCheck"
        v-if="checkSubmitInspect"
      />
      <h-button
        :width="checkSubmitInspect ? 296 : 624"
        height="80"
        :text="submitId ? (submitStatus === 2 ? '提交审核' : '保存') : '记工'"
        @tap.stop="handleTabNumReport"
      />
    </view>
    <h-status-footer />
    <h-popup />
    <canvas
      v-for="idx in 5"
      :key="idx"
      :canvas-id="'compress_canvas' + idx"
      :id="'compress_canvas' + idx"
      class="compress_canvas"
      :style="{ width: '800px', height: '800px' }"
    />
    <h-popup-dialog ref="refPopupDialog" title="提示" isMaskClick>
      <template #default>
        <view class="color-333 font-28 py-32">
          <rich-text nodes="报工记录不符合产品的工艺路线, 请编辑报工记录" />
        </view>
      </template>
      <template #footer>
        <view class="flex align-center justify-center">
          <h-button width="174" height="64" text="去修正" class="ml-32" @tap.stop="closeDialog" />
        </view>
      </template>
    </h-popup-dialog>
  </view>
</template>

<script setup lang="ts">
import ReportInputSelect from '@/components/report-input-select.vue'
import HCheckboxIcon from '@/components/h-checkbox-icon.vue'
import TabNum from '@/pages-report-work/tab-num.vue'
import { REPORT_WORK, formatDate, $store } from '@/utils/common'
import { computed, h, reactive, ref, watch } from 'vue'
import { _get, _post } from '@/utils/common-request'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { formatStr, setGio } from '@/utils/common'
import { checkSubmitRule } from '@/pages/report-work/util'
import HPopup from '@/components/h-popup.vue'
import HooksPopup from '@/hooks/popup'
import OperationLog from '@/components/operation-log.vue'
import checkProcessExist from '@/hooks/checkProcessExist'
import HooksProductStandard from '@/hooks/product-standard'
import HProcessStock from '@/components/h-process-stock.vue'
import HooksSettingConfig from '@/hooks/setting-config'

import HPopupDialog from '@/components/h-popup-dialog.vue'
onShow(() => {
  setGio('jr_jg_edit')
})

const { checkIsStandard, checkProductHasStandard, standardPreProcess, getStandardPreProcess, assembleSubmitParams } =
  HooksProductStandard()

const { checkSubmitInspect, checkWorkerBaseDataConfine } = HooksSettingConfig()

const optionsData = reactive<{ readOnly?: string; date?: string }>({})
onLoad((options) => {
  Object.assign(optionsData, options)
  readOnly.value = !!optionsData.readOnly
  if (options.from) {
    _get({ url: `/submit/${options.id}` }).then((res: IResponseType<unknown>) => {
      initDateItem(res.data)
    })
  }
})

const scrollIntoView = ref('')

const readOnly = ref(false)
const getTitle = computed(() => {
  return readOnly.value ? '记工详情' : submitId.value ? '编辑记工' : '记工'
})
const stockMaps = computed(() => $store.state.process.stockMaps)
const refOperationLog = ref(null)
const submitNo = ref('')
const checkStatus = ref(false)
async function initDateItem(dataItem) {
  submitId.value = dataItem.id
  submitNo.value = dataItem.submitNo
  submitDay.value = dataItem.submitDay
  submitStatus.value = dataItem.submitStatus
  checkStatus.value = dataItem.checkStatus === 2
  Object.assign(optionsData, dataItem)
  Object.assign(productItem, {
    itemName: dataItem.productName,
    itemCode: dataItem.productCode,
    itemSeq: dataItem.productSeq
  })
  // 判断产品是否有标准工艺
  checkProductHasStandard(dataItem.productSeq)
  Object.assign(processItemNow, {
    itemName: dataItem.operateProcessName,
    itemCode: dataItem.operateProcessCode,
    itemSeq: dataItem.operateProcessSeq,
    isLastProcess: dataItem.isLastProcess,
    isFirstProcess: dataItem.isFirstProcess
  })
  await $store.dispatch('process/getProcessStock', {
    productSeq: dataItem.productSeq,
    processList: [dataItem.operateProcessSeq]
  })
  // 如果是标准工艺,查看是否有前工序
  await getStandardPreProcess(dataItem.productSeq, dataItem.operateProcessSeq)
  if (!standardPreProcess.value.length) {
    Object.assign(processItemPre, {
      itemName: dataItem.preProcessName,
      itemCode: dataItem.preProcessCode,
      itemSeq: dataItem.preProcessSeq
    })
  }
  Object.assign(countItem, {
    passNum: dataItem.passNum,
    checkPassNum: dataItem.checkPassNum,
    checkNgNum: dataItem.checkNgNum,
    ngNum: dataItem.ngNum,
    remark: dataItem.remark,
    abandonedNum: dataItem.abandonedNum,
    repairNum: dataItem.repairNum
  })
  Object.assign(userItem, {
    submitNickName: dataItem.submitNickName,
    createdDate: dataItem.createdDate,
    checkNickName: dataItem.checkNickName,
    checkDate: dataItem.checkDate,
    submitDay: dataItem.submitDay,
    expiredRecordFlag: dataItem.expiredRecordFlag
  })
  if (submitId.value) {
    setTimeout(() => {
      dataItem.submitPictures && refTabNum.value?.setImageList(dataItem.submitPictures.split(';'))
    }, 100)
  }
}

const submitId = ref(0)
const submitDay = ref('')
const submitStatus = ref(0)
const userItem = reactive({})

// 产品模块
const productItem = reactive({ itemName: '', itemCode: '', itemSeq: '' })
const searchValue = ref('')
function handleProductSelect(item) {
  Object.assign(productItem, item)
  if (!item.itemSeq) return
  checkProductHasStandard(item.itemSeq)
}

// 前置工序
const processItemPre = reactive({ itemName: '', itemCode: '', itemSeq: '' })
function handleProcessSelectPre(item) {
  Object.assign(processItemPre, item)
}

// 当前工序
const processItemNow = reactive({ itemName: '', itemCode: '', itemSeq: '', isLastProcess: '1', isFirstProcess: '1' })
function handleProcessSelectNow(item) {
  Object.assign(processItemNow, item)
  getStandardPreProcess(productItem.itemSeq, item.itemSeq)
  if (!item.itemSeq) return
  $store.dispatch('process/getProcessStock', { productSeq: productItem.itemSeq, processList: [item.itemSeq] })
  _get({
    url: '/submit/getFirstOrLastProcessFlag',
    data: { productSeq: productItem.itemSeq, processSeq: item.itemSeq, standard: checkIsStandard.value }
  }).then((res: IResponseType<any>) => {
    if (res.data) {
      const { isLastProcess, isFirstProcess } = res.data
      item.isLastProcess = isLastProcess
      item.isFirstProcess = isFirstProcess
      if (isFirstProcess === '0') {
        Object.assign(processItemPre, { itemName: '', itemCode: '', itemSeq: '' })
      }
    } else {
      item.isLastProcess = '1'
      item.isFirstProcess = '1'
    }
    Object.assign(processItemNow, item)
  })
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
}

const countItem = reactive({ passNum: '', checkPassNum: '', checkNgNum: '', ngNum: '', remark: '' })

async function handleReport(item) {
  if (!checkSubmitRule(productItem, processItemPre, processItemNow, item, 'passNum', 'ngNum', checkIsStandard.value))
    return
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
  if (checkIsStandard.value) {
    await submitNext(item)
    return
  }
  await checkProcessExist(
    productItem,
    processItemNow,
    () => {
      submitNext(item)
    },
    () => {
      processItemNow.isFirstProcess = '1'
      processItemNow.isLastProcess = '1'
    }
  )
}
const { popupOpen } = HooksPopup()
async function submitNext(item) {
  uni.showLoading({ title: '记工中' })
  const params = {
    id: submitId.value,
    productName: productItem.itemName,
    productCode: productItem.itemCode,
    productSeq: productItem.itemSeq,
    preProcessName: processItemPre.itemName,
    preProcessCode: processItemPre.itemCode,
    preProcessSeq: processItemPre.itemSeq,
    operateProcessName: processItemNow.itemName,
    operateProcessCode: processItemNow.itemCode,
    operateProcessSeq: processItemNow.itemSeq,
    isLastProcess: processItemNow.isLastProcess,
    isFirstProcess: processItemNow.isFirstProcess,
    submitDay: optionsData.date,
    passNum: item.passNum || 0,
    ngNum: item.ngNum || 0,
    remark: item.remark,
    submitPictures: item.submitPictures
  }
  // 如果是标准工艺,前工序参数改为多个前工序数据拼装
  Object.assign(params, assembleSubmitParams(params))
  let res = null
  if (submitId.value) {
    res = await _post({ url: '/submit/edit', data: params })
  } else {
    res = await _post({ url: '/submit/add', data: params })
  }
  uni.hideLoading()
  if (res.msg.includes('已被审核')) {
    popupOpen(res.msg)
  } else {
    if (sendCheck.value) {
      await _post({ url: '/ngProduct/manage/submitForInspection', data: { ids: submitId.value } })
      sendCheck.value = false
      uni.showToast({ title: '记工送检成功', icon: 'none' })
    } else {
      uni.showToast({
        title: params.id ? (submitStatus.value === 2 ? '提交审核完成' : '记工编辑完成') : '记工完成',
        icon: 'none'
      })
    }
    uni.$emit('listRefresh', submitDay.value)
    setTimeout(() => {
      uni.navigateBack()
    }, 1000)
  }
}

const sendCheck = ref(false)
const refPopupDialog = ref(null)
function closeDialog() {
  refPopupDialog.value?.close()
}
async function handleSendCheck() {
  let resData = await _post({
    url: '/tech/validSubmitRecordTechInfo',
    data: {
      productSeq: productItem.itemSeq,
      processSeq: processItemNow.itemSeq,
      preProcessSeq: processItemPre.itemSeq
    }
  })
  if (!resData.data) {
    refPopupDialog.value.open()
    return
  }
  sendCheck.value = true
  handleTabNumReport()
}

const refTabNum = ref(null)
function handleTabNumReport() {
  setGio('jr_jg_sucess')
  refTabNum.value?.handleReport()
}
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
.compress_canvas {
  position: absolute;
  left: 10000px;
}
</style>
