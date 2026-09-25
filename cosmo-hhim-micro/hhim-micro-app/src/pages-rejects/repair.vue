<template>
  <view class="h-full bg-f3f3f5 flex flex-col">
    <uni-nav-bar />
    <h-status-header :title="`${readonly ? '返修详情' : '返修复核'}`" />
    <view class="flex-1 px-16 box">
      <view class="bg-fff rounded-16-top box px-32 pt-32 pb-24">
        <view class="flex align-center">
          <view class="mark-ebf0f5">记工</view>
          <view class="ml-16 color-333 font-28">{{ dataItem.submitNickName }}</view>
        </view>
        <view class="mt-24 flex align-center">
          <view class="font-32 color-333 bold">{{ dataItem.productName }}</view>
          <view class="font-32 color-999 bold">({{ dataItem.productCode }})</view>
        </view>
        <view class="mt-24 flex align-center">
          <h-text-display :text="dataItem.processName" :width="512" class="font-28 color-5a6f82" />
        </view>
        <view class="mt-24 b-t-1 border-f5f5f5 pt-24 flex align-center">
          <view class="mark-ebf0f5">总计</view>
          <view class="ml-12 color-333 font-28 bold">{{ readonly ? totalCount : dataItem.exceptionNum }}</view>
          <view class="ml-8 color-333 font-28 flex-1 bold">台</view>
          <template v-if="recordCount">
            <view class="color-5a6f82 font-24">质检记录：</view>
            <view class="color-333 border font-28 bold">{{ recordCount }}</view>
            <image src="/static/images/icon_detail.svg" @tap="handleOpenRecord" class="icon-32 ml-16" />
          </template>
        </view>
      </view>
      <view class="mt-8 bg-fff box px-32 pt-16 pb-16">
        <!--返修人-->
        <view class="flex align-center h-80 b-b-1 border-f5f5f5">
          <view class="w-176 font-28 color-5a6f82 box" :class="{ 'text-require pl-16': !readonly }">返修人</view>
          <input
            disabled
            :value="submitData.repairNickName"
            @tap="handleChangeStaff"
            placeholder="返修人"
            placeholder-class="font-28 color-b8b8b8"
            class="flex-1 font-28"
          />
          <image v-if="!readonly" :src="formatImage('icon_input_arr', 'svg')" class="icon-48" />
        </view>
        <!--返修完成-->
        <view class="flex align-center h-80 mt-16 b-b-1 border-f5f5f5">
          <view class="w-176 font-28 color-5a6f82">返修完成</view>
          <input
            v-model="submitData.repairNum"
            type="digit"
            placeholder="0"
            :disabled="readonly"
            placeholder-class="font-28 color-b8b8b8"
            class="flex-1 font-28"
          />
          <view class="font-28 color-5a6f82 w-48 text-center">台</view>
        </view>
        <!--让步接收-->
        <view class="flex align-center h-80 mt-16 b-b-1 border-f5f5f5">
          <view class="w-176 font-28 color-5a6f82">让步接收</view>
          <input
            v-model="submitData.concessionNum"
            placeholder="0"
            type="digit"
            :disabled="readonly"
            placeholder-class="font-28 color-b8b8b8"
            class="flex-1 font-28"
          />
          <view class="font-28 color-5a6f82 w-48 text-center">台</view>
        </view>
        <!--报废-->
        <view class="flex align-center h-80 mt-16 b-b-1 border-f5f5f5">
          <view class="w-176 font-28 color-5a6f82">报废</view>
          <input
            v-model="submitData.abandonedNum"
            type="digit"
            placeholder="0"
            :disabled="readonly"
            placeholder-class="font-28 color-b8b8b8"
            class="flex-1 font-28"
          />
          <view class="font-28 color-5a6f82 w-48 text-center">台</view>
        </view>
        <!--报废类型-->
        <view class="flex align-center h-80 mt-16 b-b-1 border-f5f5f5" v-if="submitData.abandonedNum">
          <view class="w-176 font-28 color-5a6f82 box" :class="{ 'text-require pl-16': !readonly }">报废类型</view>
          <template v-if="readonly">
            <view>{{ submitData.abandonedType }}</view>
          </template>
          <template v-else>
            <view class="flex align-center" @tap="handleChangeScrapType('工废')">
              <h-radio-box class="mr-24" :checked="submitData.abandonedType === '工废'" />
              <text>工废</text>
            </view>
            <view class="flex align-center ml-48" @tap="handleChangeScrapType('料废')">
              <h-radio-box class="mr-24" :checked="submitData.abandonedType === '料废'" />
              <text>料废</text>
            </view>
          </template>
        </view>
        <view class="flex align-center h-80 mt-16">
          <view class="w-176 font-28 color-5a6f82">备注</view>
          <input
            v-model.trim="submitData.remark"
            :disabled="readonly"
            placeholder="备注"
            maxlength="50"
            placeholder-class="font-28 color-b8b8b8"
            class="flex-1 font-28"
          />
        </view>
      </view>
    </view>
    <view class="flex p-32 justify-center" v-if="!readonly">
      <h-button width="296" height="72" text="取消" type="bg-fff color-333" @tap.stop="handleCancel" />
      <h-button width="296" height="72" text="确认并提交" class="ml-32" @tap.stop="handleSubmit" />
    </view>
    <h-popup-staff ref="popupStaff" @staffSelected="handleUserSelect" title="选择返修人" />
    <popup-record ref="popupRecord" :dataList="dataItem.qualityControlInfoWhenRepairList" />
    <h-popup />
    <h-status-footer />
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { $store, formatImage } from '@/utils/common'
import HRadioBox from '@/components/h-radiobox.vue'
import HTextDisplay from '@/components/h-text-display.vue'
import { _get, _post } from '@/utils/common-request'
import PopupRecord from '@/pages-rejects/popup-record.vue'
import { onLoad } from '@dcloudio/uni-app'
import HooksPopup from '@/hooks/popup'
import HPopup from '@/components/h-popup.vue'
import bigNumber from 'bignumber.js'
import HPopupStaff from '@/components/h-popup-staff.vue'

const dataItem = ref({})
const readonly = ref(false)

onLoad((options) => {
  const { repairNo } = options
  if (repairNo) {
    readonly.value = true
    _get({
      url: '/ngProduct/manage/detailRepairInfo',
      data: { repairNo }
    }).then((res) => {
      dataItem.value = res.data
      submitData.ids = dataItem.value.ids
      Object.assign(submitData, dataItem.value)
    })
  } else {
    const { productSeq, processSeq, submitUser } = $store.state.qualityTesting.repairItem
    _get({
      url: '/ngProduct/manage/infoWhenRepair',
      data: { productSeq, operateProcessSeq: processSeq, submitUser }
    }).then((res) => {
      dataItem.value = res.data
      submitData.ids = dataItem.value.ids
    })
    const { id, nickName } = $store.state.user.userInfo
    handleUserSelect({ id, nickName })
  }
})

function handleChangeScrapType(type) {
  submitData.abandonedType = type
}

const totalCount = computed(() => {
  return (submitData.repairNum || 0) + (submitData.concessionNum || 0) + (submitData.abandonedNum || 0)
})

function handleCancel() {
  uni.navigateBack()
}
const { popupOpen } = HooksPopup()
function handleSubmit() {
  if (submitData.repairNum && !/^\d+(\.\d{1,4})?$/.test(String(submitData.repairNum))) {
    popupOpen('返修完成应为非负数且最多保留4位小数')
    return false
  }
  if (submitData.concessionNum && !/^\d+(\.\d{1,4})?$/.test(String(submitData.concessionNum))) {
    popupOpen('让步接收应为非负数且最多保留4位小数')
    return false
  }
  if (submitData.abandonedNum && !/^\d+(\.\d{1,4})?$/.test(String(submitData.abandonedNum))) {
    popupOpen('报废数量应为非负数且最多保留4位小数')
    return false
  }
  if (
    bigNumber(submitData.repairNum || 0)
      .plus(submitData.concessionNum || 0)
      .plus(submitData.abandonedNum || 0)
      .gt(dataItem.value.exceptionNum || 0)
  ) {
    popupOpen('返修完成、让步接收、报废数量之和不能大于异常数量')
    return false
  }
  uni.showLoading({ title: '提交中', mask: true })
  submitData.repairNum = submitData.repairNum || 0
  submitData.concessionNum = submitData.concessionNum || 0
  submitData.abandonedNum = submitData.abandonedNum || 0
  _post({ url: '/ngProduct/manage/repairAndThenQualityControl', data: submitData })
    .then((res) => {
      handleCancel()
    })
    .finally(() => {
      uni.hideLoading()
    })
}

// ----------------人员弹窗------------

const popupStaff = ref(null)
const userSelected = reactive({ nickName: '' })
function handleChangeStaff() {
  if (readonly.value) return
  popupStaff?.value?.open()
}
function handleUserSelect(item) {
  submitData.repairUser = item.id
  submitData.repairNickName = item.nickName
  userSelected.nickName = item.nickName
}

// -----------------------质检记录
const popupRecord = ref(null)
function handleOpenRecord() {
  popupRecord?.value?.open()
}
const recordCount = computed(() => {
  return dataItem.value.qualityControlInfoWhenRepairList?.length ?? 0
})

const submitData = reactive({
  ids: '',
  repairUser: 0,
  repairNum: '',
  concessionNum: '',
  abandonedNum: '',
  abandonedType: '工废',
  remark: ''
})
</script>

<style lang="scss" scoped></style>
