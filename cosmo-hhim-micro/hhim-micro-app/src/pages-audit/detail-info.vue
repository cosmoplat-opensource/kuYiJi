<template>
  <view class="h-full bg-f3f3f5 box flex flex-col">
    <uni-nav-bar />
    <h-status-header :title="formatTitle" />
    <!--从审核来的显示记工人信息-->
    <view class="flex flex-wrap align-center mx-16 box">
      <view class="flex align-center mb-16 mr-40">
        <view class="mark-ffffff">记工</view>
        <h-text-display :width="120" :text="submitUser.submitNickName" class="color-5a6f82 font-28 ml-16" />
        <template v-if="submitUser.expiredRecordFlag === 0">
          <view class="color-5a6f82 font-28 ml-16">{{ formatDate(submitUser.submitDay, 'MM-DD') }}</view>
        </template>
        <template v-else>
          <view class="color-5a6f82 font-28 ml-16">
            <text>{{ formatDate(submitUser.createdDate) }}</text>
          </view>
        </template>
        <view class="mark-24a8ff ml-16" v-if="submitUser.checkStatus === 1">待检</view>
        <view class="mark-00bfa5 border ml-16" v-if="submitUser.checkStatus === 2">已检</view>
      </view>
      <template v-if="submitUser.expiredRecordFlag === 0">
        <view class="flex align-center mb-16 mr-40">
          <view class="w-56 h-32 flex-center rounded-4 bg-FE9F00">
            <text class="font-24 color-fff">补录</text>
          </view>
          <view class="color-5a6f82 font-28 ml-16">{{ formatDate(submitUser.createdDate, 'YYYY-MM-DD HH:mm') }}</view>
        </view>
      </template>
      <view class="flex align-center mb-16">
        <view class="icon-32 bg-68a2f8 rounded-full flex align-center justify-center" v-if="submitUser.checkNickName">
          <text class="font-24 color-fff">审</text>
        </view>
        <view class="color-5a6f82 font-28 ml-16">{{ formatStr(submitUser.checkNickName, 12) }}</view>
        <view class="color-5a6f82 font-28 ml-16 mr-40">{{ formatDate(submitUser.checkDate) }}</view>
      </view>
    </view>
    <view
      class="flex align-center pl-16 pr-32"
      v-if="
        optionsData.negativeStockFlag === '0' ||
        optionsData.lowPassRateFlag === '0' ||
        optionsData.overProductiveCapacityFlag === '0'
      "
    >
      <view class="mark-fe9f00 mr-8 mb-24" v-if="optionsData.negativeStockFlag === '0'">超报风险</view>
      <view class="mark-f26a60 border mr-8 mb-24" v-if="optionsData.lowPassRateFlag === '0'">良品率偏低</view>
      <view class="mark-9badbc border mr-8 mb-24" v-if="optionsData.overProductiveCapacityFlag === '0'">
        记工数超产能
      </view>
      <view class="flex-1" />
      <view class="btn-view-detail mb-18" @tap="openDetailPopup">查看</view>
    </view>
    <view class="box flex align-center bg-f7f8e7 px-16 h-64 mb-8 mx-16" v-if="!checkIsStandard">
      <image src="/static/images/icon_tips.svg" class="icon-32" />
      <view class="flex-1 ml-16">
        <text class="font-24 color-5a6f82">该产品还未设置标准工艺路线</text>
      </view>
      <view class="btn-view-detail" @tap="jumpProductDetail">去设置</view>
    </view>
    <view class="flex-1 mx-16 box flex flex-col overflow-auto">
      <!--产品-->
      <report-input-select
        @itemSelected="handleProductSelect"
        :processMark="REPORT_WORK.PRODUCT"
        :readOnly="checkReadOnly || sendCheck"
        :inputData="productItem"
      >
        <template #mark>
          <view class="process-mark">
            <text>产品</text>
          </view>
        </template>
      </report-input-select>
      <!--报工工序-->
      <report-input-select
        :processMark="REPORT_WORK.NOW"
        class="mt-16"
        :productSeq="productItem.itemSeq"
        :productCode="productItem.itemCode"
        :productName="productItem.itemName"
        @itemSelected="handleProcessSelectNow"
        :readOnly="checkReadOnly || sendCheck"
        isProcess
        :inputData="processItemNow"
        :standard="checkIsStandard"
      >
        <template #mark>
          <view class="process-mark purple">
            <text>报工工序</text>
          </view>
        </template>
        <template #lastProcess>
          <view class="flex align-center mt-32 pb-16" v-if="!checkReadOnly && !sendCheck">
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
        class="mt-16"
        isProcess
        :productSeq="productItem.itemSeq"
        :productCode="productItem.itemCode"
        :productName="productItem.itemName"
        v-if="processItemNow.isFirstProcess !== '0' && !standardPreProcess.length"
        @itemSelected="handleProcessSelectPre"
        :readOnly="checkReadOnly || sendCheck"
        :inputData="processItemPre"
        :standard="checkIsStandard"
      >
        <template #mark>
          <view class="process-mark blue">
            <text>前工序</text>
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
          <view class="py-24 pr-32 flex align-center justify-between box bold color-333 font-28 overflow-hidden">
            <h-text-display :text="item.processName" :width="200" />
            <h-process-stock :dataItem="stockMaps[item.processSeq]" v-if="stockMaps[item.processSeq]" />
          </view>
        </view>
      </view>
      <!--数量模块-->
      <view class="mt-16 flex">
        <!--良品数量-->
        <view class="flex-1 pl-32 pt-24 pr-24 bg-fff">
          <view class="flex align-end pb-32">
            <view class="flex-1 flex flex-col">
              <view class="font-28 bold" v-if="checkReadOnly">{{ countItem.checkPassNum }}</view>
              <input
                v-else
                v-model="countItem.checkPassNum"
                placeholder="请录入..."
                type="digit"
                class="font-28 bold"
                placeholder-style="color:#b8b8b8"
              />
              <text class="color-5a6f82 font-24 mt-24 line-height-24">良品数量</text>
            </view>
            <image :src="formatImage('icon_liangpin_96', 'svg')" class="icon-96" />
          </view>
          <view class="py-16 font-24 color-5a6f82 b-t-1 border-f5f5f5">记工数：{{ countItem.passNum }}</view>
          <view class="h-24 pb-16" v-if="countItem.repairNum || countItem.abandonedNum" />
        </view>
        <!--不良品数量-->
        <view class="flex-1 pl-32 pt-24 pr-24 bg-fff ml-4">
          <view class="flex align-end pb-32">
            <view class="flex-1 flex flex-col">
              <view class="font-28 bold" v-if="checkReadOnly">{{ countItem.checkNgNum }}</view>
              <input
                v-else
                v-model="countItem.checkNgNum"
                @input="handleCheckNgNumInput"
                placeholder="请录入..."
                type="digit"
                class="font-28 bold"
                placeholder-style="color:#b8b8b8"
              />
              <text class="color-5a6f82 font-24 mt-24 line-height-24">不良品数量</text>
            </view>
            <image :src="formatImage('icon_buliang_96', 'svg')" class="icon-96" />
          </view>
          <view class="py-16 font-24 color-5a6f82 b-t-1 border-f5f5f5">记工数：{{ countItem.ngNum }}</view>
          <view
            class="h-24 pb-16 font-24 line-height-24 flex align-center"
            v-if="countItem.repairNum || countItem.abandonedNum"
          >
            <view class="color-5a6f82 flex align-center">
              返修：<view class="color-ff0000">{{ countItem.repairNum }}</view>
            </view>
            <view class="color-5a6f82 ml-16 flex align-center">
              报废：<view class="color-ff0000">{{ countItem.abandonedNum }}</view>
            </view>
          </view>
        </view>
      </view>
      <!--备注-->
      <view class="flex flex-col mt-16 bg-fff pt-32 px-32 pb-24">
        <text class="font-24 line-height-24 color-5a6f82">备注</text>
        <view v-if="checkReadOnly" class="mt-24 font-28 bold">{{ countItem.remark || '-' }}</view>
        <h-textarea
          v-else
          v-model="countItem.remark"
          :border="false"
          :padding="false"
          class="mt-24 font-28 bold"
          placeholder="输入备注"
          :maxlength="100"
        />
      </view>
      <view class="flex flex-col mt-16 bg-fff p-32">
        <view class="color-5a6f82 font-24">拍照({{ uploadImageList.length }}{{ checkReadOnly ? '' : '/5' }})</view>
        <view class="flex flex-wrap">
          <view
            class="addImg flex align-center justify-center mt-24 mr-24 relative"
            v-for="(item, index) in uploadImageList"
            :key="index"
            @tap="previewImg(uploadImageList, index)"
          >
            <image :src="item" class="icon-128 rounded-16" mode="aspectFill" />
            <view @tap.stop="deleteImage(index)" class="icon-72 tap-view" v-if="!checkReadOnly">
              <view class="del flex align-center justify-center">
                <image src="/static/images/icon_delete_24.svg" class="icon-24" />
              </view>
            </view>
          </view>
          <view
            v-if="uploadImageList.length <= 4 && !checkReadOnly"
            class="addImg border-2-dashed flex align-center justify-center mt-24"
            @tap="handleSelectImages"
          >
            <image src="/static/images/icon_camera.svg" class="icon-72" />
          </view>
        </view>
      </view>
      <operation-log ref="refOperationLog" :submitNo="submitNo" class="mt-16" v-if="submitId" />
    </view>
    <view class="flex align-center py-32 justify-between px-48" v-if="!checkIsChecked">
      <template v-if="checkReadOnly">
        <h-button
          v-if="showSendBtn"
          height="72"
          width="100%"
          class="flex-1"
          text="编辑"
          @tap="handleEditAble"
          type="border-1 border-0066ff color-0066ff bg-fff"
        />
        <h-button height="72" width="296" text="审核" @tap="handleReport" class="ml-32" />
      </template>
      <template v-else>
        <h-button
          v-if="showSendBtn"
          height="72"
          width="100%"
          text="送检"
          @tap="handleSendCheck"
          class="mr-32 flex-1"
          type="border-1 border-0066ff color-0066ff bg-fff"
        />
        <h-button
          height="72"
          width="100%"
          text="驳回"
          class="flex-1"
          @tap="handleReject"
          type="bg-fff color-ff0000"
          active="red"
        />
        <h-button height="72" :width="showSendBtn ? 170 : 296" text="保存并通过" @tap="handleReport" class="ml-32" />
      </template>
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
import { $store, REPORT_WORK, formatDate, formatImage, Role_Experience, collectClick } from '@/utils/common'
import { computed, getCurrentInstance, reactive, ref } from 'vue'
import { _get, _post } from '@/utils/common-request'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { formatStr, setGio } from '@/utils/common'
import { checkSubmitRule } from '@/pages/report-work/util'

import HCheckboxIcon from '@/components/h-checkbox-icon.vue'
import HTextarea from '@/components/h-textarea.vue'
import HPopup from '@/components/h-popup.vue'
import OperationLog from '@/components/operation-log.vue'
import HTextDisplay from '@/components/h-text-display.vue'
import bigNumber from 'bignumber.js'
import HProcessStock from '@/components/h-process-stock.vue'
import HPopupDialog from '@/components/h-popup-dialog.vue'
import HooksPopup from '@/hooks/popup'
import HooksUploadImage from '@/hooks/upload-image'
import checkProcessExist from '@/hooks/checkProcessExist'
import HooksProductStandard from '@/hooks/product-standard'
import HooksSettingConfig from '@/hooks/setting-config'
onShow(() => {
  setGio('jr_sc_edit')
  checkProductHasStandard(productItem.itemSeq)
})

const { checkIsStandard, checkProductHasStandard, standardPreProcess, getStandardPreProcess, assembleSubmitParams } =
  HooksProductStandard()

const { checkSubmitInspect } = HooksSettingConfig()

const showSendBtn = computed(() => {
  return checkSubmitInspect.value && submitUser.checkStatus !== 1
})

const optionsData = reactive<Partial<IQualityTestingItem>>({})
onLoad((options) => {
  Object.assign(optionsData, options)
  _get({ url: `/submit/${options.id}` }).then((res: IResponseType<unknown>) => {
    initDateItem(res.data, options.isLastProcess, options.isFirstProcess)
  })
})
const stockMaps = computed(() => $store.state.process.stockMaps)
const checkIsChecked = ref(false)
const sendCheck = ref(false)
const submitUser = reactive<Partial<IQualityTestingItem>>({})
const formatTitle = computed(() => {
  return checkReadOnly.value ? '记工详情' : '记工编辑'
})
const submitNo = ref('')
async function initDateItem(dataItem, isLastProcess, isFirstProcess) {
  submitId.value = dataItem.id
  submitNo.value = dataItem.submitNo
  checkIsChecked.value = dataItem.submitStatus === 0
  sendCheck.value = dataItem.checkStatus === 2
  Object.assign(submitUser, dataItem)
  Object.assign(productItem, {
    itemName: dataItem.productName,
    itemCode: dataItem.productCode,
    itemSeq: dataItem.productSeq
  })
  // 判断产品是否有标准工艺
  checkProductHasStandard(productItem.itemSeq)
  Object.assign(processItemNow, {
    itemName: dataItem.operateProcessName,
    itemCode: dataItem.operateProcessCode,
    itemSeq: dataItem.operateProcessSeq,
    isLastProcess: isLastProcess || dataItem.isLastProcess,
    isFirstProcess: isFirstProcess || dataItem.isFirstProcess
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
  await $store.dispatch('process/getProcessStock', {
    productSeq: dataItem.productSeq,
    processList: [dataItem.operateProcessSeq, dataItem.preProcessSeq]
  })
  Object.assign(countItem, {
    passNum: dataItem.passNum,
    expiredRecordFlag: dataItem.expiredRecordFlag,
    // 审产数量预填：库里为 null 或 0 都视为"没填"→ 预填记工数
    // （历史问题：只判 null，库里存的 0 会被原样带出来，审产员直接点"通过"就写了 0/0 的空审记录）
    checkPassNum: checkFilled(dataItem.checkPassNum) ? dataItem.checkPassNum : dataItem.passNum,
    ngNum: dataItem.ngNum,
    checkNgNum: checkFilled(dataItem.checkNgNum) ? dataItem.checkNgNum : dataItem.ngNum,
    remark: dataItem.remark,
    abandonedNum: dataItem.abandonedNum,
    repairNum: dataItem.repairNum
  })
  if (submitId.value) {
    setTimeout(() => {
      // refOperationLog.value?.getList(dataItem.submitNo)
      setImageList(dataItem.submitPictures?.split(';') ?? [])
    }, 500)
  }
}
function openDetailPopup() {
  const dataItem = JSON.stringify(submitUser)
  const flagData = JSON.stringify(optionsData)
  uni.navigateTo({ url: `/pages-audit/detail-exception?dataItem=${dataItem}&flagData=${flagData}` })
}
const checkReadOnly = computed(() => {
  return optionsData.readOnly === 'true'
})
const submitId = ref(0)
const refOperationLog = ref(null)

// 产品模块
const productItem = reactive({ itemName: '', itemCode: '', itemSeq: '' })
const searchValue = ref('')
function handleProductSelect(item) {
  Object.assign(productItem, item)
  if (!item.itemSeq) return
  checkProductHasStandard(item.itemSeq)
}

// 前置工序模块
const processItemPre = reactive({ itemName: '', itemCode: '', itemSeq: '', isFirstProcess: '1' })
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
    Object.assign(processItemPre, { itemName: '', itemCode: '', itemSeq: '', isFirstProcess: '0' })
  }
}
function handleProcessSelectPre(item) {
  Object.assign(processItemPre, item)
}

// 当前工序模块
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

// 上传图片模块
const { choseImage, uploadImageList, uploadImage, setImageList, previewImg, removeImage } = HooksUploadImage(5)
function handleSelectImages() {
  choseImage(getCurrentInstance(), { width: 800, height: 800, zip: true }, ['camera']).then((res) => {
  })
}
function deleteImage(index) {
  uni.showModal({
    title: '提示',
    content: '确认要删除这张图片吗',
    success: function (res) {
      if (res.confirm) {
        removeImage(index)
      } else if (res.cancel) {
      }
    }
  })
}

// 数量模块
const countItem = reactive({ passNum: '', ngNum: '', remark: '', checkPassNum: '', checkNgNum: '' })

// 提交操作模块
function handleReject() {
  uni.showModal({
    title: '提示',
    content: '确认要驳回该条记工吗',
    success: function (res) {
      if (res.confirm) {
        _get({ url: '/submit/rejectRecord', data: { ids: submitId.value } }).then((res: IResponseType<{}>) => {
          if (res.msg.includes('已被驳回')) {
            popupOpen(res.msg)
          } else {
            if (optionsData.from === 'audioDetail') {
              uni.$emit('auditDetailListRefresh')
            }
            uni.$emit('auditListRefresh')
            uni.$emit('auditDetailRefresh')
            uni.showToast({ title: '驳回成功', icon: 'none' })
            setTimeout(() => {
              uni.navigateBack()
            }, 1500)
          }
        })
      }
    }
  })
}
function handleEditAble() {
  optionsData.readOnly = 'false'
}
async function handleReport() {
  setGio('jr_sc_sucess')
  if (
    !checkSubmitRule(
      productItem,
      processItemPre,
      processItemNow,
      countItem,
      'checkPassNum',
      'checkNgNum',
      checkIsStandard.value
    )
  ) {
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
  if (checkIsStandard.value) {
    submitNext()
    return
  }
  await checkProcessExist(
    productItem,
    processItemNow,
    () => {
      submitNext()
    },
    () => {
      processItemNow.isFirstProcess = '1'
      processItemNow.isLastProcess = '1'
    }
  )
}
const { popupOpen } = HooksPopup()
/** 审产数量是否"填了"：null / undefined / '' / 0 都算没填（后端会按记工数回填） */
function checkFilled(v) {
  return v !== null && v !== undefined && v !== '' && new bigNumber(v).gt(0)
}
function submitNext() {
  if (bigNumber(countItem.repairNum).plus(countItem.abandonedNum).gt(countItem.checkNgNum)) {
    popupOpen('不良数量不能小于报废数+返修数')
    return
  }
  if (bigNumber(countItem.ngNum).plus(countItem.passNum).lt(countItem.checkNgNum)) {
    popupOpen('不良数量不能大于总记工数')
    return
  }
  // 两个数量都没填：不阻断（后端会按记工数审产），但明确告知，避免又产生"0 审产"的空记录
  if (!checkFilled(countItem.checkPassNum) && !checkFilled(countItem.checkNgNum)) {
    const total = bigNumber(countItem.passNum).plus(countItem.ngNum).toNumber()
    if (total <= 0) {
      popupOpen('该记工的记工数为 0，请填写良品/不良数量，或先驳回该记工')
      return
    }
    uni.showModal({
      title: '提示',
      content: `未填写审产数量，将按记工数审产（良品 ${countItem.passNum} / 不良 ${countItem.ngNum}），是否继续？`,
      success: (res) => {
        if (res.confirm) {
          submitNextConfirmed()
        }
      }
    })
    return
  }
  submitNextConfirmed()
}

function submitNextConfirmed() {
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
    isFirstProcess: processItemNow.isFirstProcess, //首序 勾选 0
    isLastProcess: processItemNow.isLastProcess,
    passNum: countItem.passNum,
    ngNum: countItem.ngNum,
    checkPassNum: countItem.checkPassNum || 0,
    checkNgNum: countItem.checkNgNum || 0,
    remark: countItem.remark || null,
    submitDay: submitUser.submitDay,
    submitUser: submitUser.submitUser,
    submitPictures: uploadImageList.value?.join(';') ?? ''
  }
  // 如果是标准工艺,前工序参数改为多个前工序数据拼装
  Object.assign(params, assembleSubmitParams(params))
  _post({ url: '/submit/tipsForEditAndCheck', data: params }).then((res: IResponseType<IQualityTestingItem>) => {
    if (res.data) {
      let check = res.data
      if (check.negativeStockFlag === '0') {
        //负库存 优先级1
        uni.showModal({
          title: '提示',
          content: '审核后可能会造成' + check.preProcessName + '工序负库存，是否确认审核?',
          success: (res) => {
            if (res.confirm) {
              requestData(params)
            }
          }
        })
      } else if (check.lowPassRateFlag === '0') {
        // 良品率偏低 优先级2 avgPassRateByDay
        uni.showModal({
          title: '提示',
          content:
            '记工数量低于工序平均良品率(' +
            new bigNumber(check.avgPassRateByDay).multipliedBy(100).toNumber() +
            '%)，是否确认审核?',
          success: (res) => {
            if (res.confirm) {
              requestData(params)
            }
          }
        })
      } else if (check.overProductiveCapacityFlag === '0') {
        // 记工数量超产能 优先级3 avgProductionCapacityByDay
        uni.showModal({
          title: '提示',
          content: ' 记工数量已超过工序日均产能(' + check.avgProductionCapacityByDay + ')，是否确认审核?',
          success: (res) => {
            if (res.confirm) {
              requestData(params)
            }
          }
        })
      } else {
        requestData(params)
      }
    }
  })
  return
}
async function requestData(params) {
  await uploadImage()
  _post({ url: '/submit/editAndCheck', data: params }).then((res: IResponseType<{}>) => {
    if (res.msg.includes('已被审核')) {
      popupOpen(res.msg)
    } else {
      if (optionsData.from === 'audioDetail') {
        uni.$emit('auditDetailListRefresh')
      }
      uni.$emit('auditListRefresh')
      uni.showToast({ title: '审核成功', icon: 'none' })
      uni.$emit('auditDetailRefresh')
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  })
  collectClick('单次审核', '审核详情页', '审核列表-员工维度')
}

const refPopupDialog = ref(null)
function closeDialog() {
  refPopupDialog.value?.close()
}
async function handleSendCheck() {
  if (bigNumber(countItem.repairNum).plus(countItem.abandonedNum).gt(countItem.checkNgNum)) {
    popupOpen('不良数量不能小于报废数+返修数')
    return
  }
  if (bigNumber(countItem.ngNum).plus(countItem.passNum).lt(countItem.checkNgNum)) {
    popupOpen('不良数量不能大于总记工数')
    return
  }
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
  _post({ url: '/ngProduct/manage/submitForInspection', data: { ids: submitId.value } }).then((res) => {
    if (optionsData.from === 'audioDetail') {
      uni.$emit('auditDetailListRefresh')
    }
    uni.$emit('auditListRefresh')
    uni.showToast({ title: '送检成功', icon: 'none' })
    uni.$emit('auditDetailRefresh')
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  })
}

function jumpProductDetail() {
  uni.navigateTo({ url: '/pages-my-center/product-manage/add-edit?productSeq=' + productItem.itemSeq })
}

function handleCheckNgNumInput(e) {
  const val = e.detail.value
  if (bigNumber(countItem.ngNum).plus(countItem.passNum).lt(val)) {
    uni.showToast({ title: '不良数量不能大于总记工数', icon: 'none' })
    return
  } else {
    countItem.checkPassNum = bigNumber(countItem.ngNum)
      .plus(countItem.passNum)
      .minus(val || '0')
      .toString()
  }
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
.addImg {
  width: px2vw(128);
  height: px2vw(128);
  background: #ffffff;
  border-radius: px2vw(16);
  opacity: 1;
  .tap-view {
    position: absolute;
    right: 0;
    top: 0;
  }
  .del {
    width: px2vw(40);
    height: px2vw(40);
    background: rgba(0, 0, 0, 0.3);
    border-radius: 0px px2vw(16) 0px px2vw(16);
    position: absolute;
    right: 0;
    top: 0;
  }
}
.compress_canvas {
  position: absolute;
  left: 10000px;
}
.btn-view-detail {
  padding: px2vw(10) px2vw(16);
  line-height: px2vw(24);
  background: rgba(255, 255, 255, 0);
  border-radius: px2vw(24);
  border: px2vw(2) solid #0066ff;
  font-size: px2vw(24);
  color: #0066ff;
  &:active {
    color: #ffffff;
    background-color: #0066ff;
  }
}
</style>
