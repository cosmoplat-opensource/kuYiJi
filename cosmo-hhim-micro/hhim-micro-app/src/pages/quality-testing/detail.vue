<template>
  <view class="h-full bg-f3f3f5 flex flex-col overflow-hidden">
    <uni-nav-bar />
    <h-status-header :title="pageTitle" />
    <view class="flex-1 flex flex-col overflow-auto">
      <!--基本信息-->
      <view class="relative bg-fff mt-16 pt-32 pb-24 mx-16 box rounded-16-top">
        <!--报产人/日期-->
        <view class="flex align-center px-32">
          <view class="mark-ebf0f5">记工</view>
          <h-text-display :text="dataItem.submitNickName || ''" :width="120" class="ml-16 font-28 color-5a6f82" />
          <view class="color-5a6f82 font-28 ml-16">{{ formatDate(dataItem.submitDate) }}</view>
        </view>
        <!--产品名称/数量-->
        <view class="mt-24 flex align-center px-32 box font-32 bold">
          <h-text-display :text="dataItem.productName || ''" :width="200" class="color-333" />
          <h-text-display :text="`(${dataItem.productCode || ''})`" :width="200" class="color-999" />
        </view>
        <!--工序信息-->
        <view class="flex align-center flex-wrap mt-8 px-32 box">
          <view class="flex align-center mt-16 mr-24">
            <view class="mark-ebf0f5-rounded">报</view>
            <view class="ml-8">
              <h-text-display :text="dataItem.processName" :width="512" class="color-5a6f82 font-28" />
            </view>
          </view>
          <view v-if="dataItem.preProcessName" class="flex align-center mt-16">
            <view class="mark-ebf0f5-rounded">前</view>
            <view class="ml-8">
              <h-text-display :text="dataItem.preProcessName" :width="512" class="color-5a6f82 font-28" />
            </view>
          </view>
        </view>
        <view class="mt-24 pt-24 box b-t-1 border-f5f5f5 mx-32">
          <area-date-info :dataItem="{ pass: dataItem.passNum, ng: dataItem.ngNum }" :unit="dataItem.productUnit" />
        </view>
        <view
          class="pt-24 mt-24 box b-t-1 border-f5f5f5 flex align-center font-28 color-5a6f82 mx-32"
          v-if="recordRemark"
        >
          <view>备注：</view>
          <h-text-display :text="recordRemark" :width="512" />
        </view>
      </view>
      <!--详情只读-->
      <template v-if="readonly">
        <view class="bg-fff mt-8 pt-32 mx-16 box">
          <view class="flex align-center relative px-32 box">
            <view class="mark-24a8ff">质检</view>
            <h-text-display :text="dataItem.qcNickName" :width="120" class="ml-16 font-28 color-5a6f82" />
            <view class="font-28 color-5a6f82 ml-16">{{ formatDate(dataItem.qcDate) }}</view>
          </view>
          <view class="mt-24 mx-32 box">
            <area-date-info
              :dataItem="{ pass: dataItem.checkPassNum, ng: dataItem.checkNgNum }"
              :unit="dataItem.productUnit"
            />
          </view>
          <rejects-type-displpy
            :dataList="dataItem.ngProductDetailInfoList"
            :unit="dataItem.productUnit || '件'"
            className="b-t-1 border-f5f5f5 pb-32 mt-24"
          />
          <view v-if="!ngTypeLength" class="h-32 w-8" />
          <view class="mx-32 box py-32 flex align-center remark-border" v-if="dataItem.remark">
            <view class="mark-f3f3f5 border">备注</view>
            <view class="ml-16 font-28 flex-1">{{ dataItem.remark }}</view>
          </view>
        </view>
      </template>
      <!--编辑-->
      <template v-else>
        <!--良品/不良品数量-->
        <view class="mx-16 box mt-16 bg-fff py-16 px-32">
          <!--良品-->
          <view class="flex align-center py-26 box b-b-1 border-f5f5f5 relative">
            <view class="detail-block" />
            <view class="color-5a6f82 font-28 text-require pl-16 w-176 box">良品数</view>
            <input
              v-model="currentItem.passNum"
              placeholder="请录入..."
              type="digit"
              class="font-28 bold flex-1"
              placeholder-style="color:#b8b8b8 font-32"
            />
            <view class="font-28 color-5a6f82">{{ dataItem.productUnit }}</view>
          </view>
          <!--不良品-->
          <view class="flex align-center py-26 box b-b-1 border-f5f5f5 mt-16 relative">
            <view class="detail-block grey" />
            <view class="color-5a6f82 font-28 text-require pl-16 w-176 box">不良品数</view>
            <input
              v-model="currentItem.ngNum"
              @input="handleCheckNgNumInput"
              placeholder="请录入..."
              type="digit"
              class="font-28 bold flex-1"
              placeholder-style="color:#b8b8b8"
            />
            <view class="font-28 color-5a6f82">{{ dataItem.productUnit }}</view>
          </view>
          <view class="box pt-16 flex align-center" v-if="dataItem.repairNum || dataItem.abandonedNum">
            <view class="w-176" />
            <view class="font-24 color-5a6f82 flex align-center">
              返修：
              <view class="color-ff0000">{{ dataItem.repairNum }}</view>
            </view>
            <view class="ml-24 font-24 color-5a6f82 flex align-center">
              报废：
              <view class="color-ff0000">{{ dataItem.abandonedNum }}</view>
            </view>
          </view>
        </view>
        <!--不良类型-->
        <view class="mt-16 p-32 box bg-fff mx-16 box" v-if="currentItem.ngNum">
          <view class="font-24 color-5a6f82">不良类型</view>
          <view class="flex align-center flex-wrap mt-8">
            <view
              v-for="(item, index) in typeList"
              :key="index"
              class="flex align-center mr-16 mt-16 px-8 box h-48 bg-F3F3F5 rounded-8"
            >
              <view class="font-28 color-5a6f82">{{ item.ngType }}</view>
              <view class="font-28 color-333 ml-8 bold">{{ item.ngNum }}{{ dataItem.productUnit }}</view>
              <image
                :src="formatImage('icon_del_32_fce0de', 'svg')"
                class="icon-32 ml-8"
                @tap="handleTypeDelete(index)"
              />
            </view>
            <image
              v-if="typeCount"
              :src="formatImage('icon_add_0066ff', 'svg')"
              @tap="handleTypeAdd"
              class="icon-48 mt-16"
            />
          </view>
        </view>
        <!--备注-->
        <view class="flex flex-col mt-16 bg-fff pt-32 px-32 pb-24 mx-16 box">
          <view class="font-24 color-5a6f82">备注</view>
          <view v-if="readonly" class="mt-24 font-28 bold">{{ dataItem.remark || '-' }}</view>
          <h-textarea
            v-else
            :border="false"
            :padding="false"
            class="mt-24 font-28 bold"
            v-model="dataItem.remark"
            :maxlength="50"
            placeholder="输入备注"
          />
        </view>
      </template>
    </view>
    <view class="py-32 box flex justify-center align-center font-32" v-if="!readonly">
      <h-button width="296" height="72" text="取消" type="bg-fff color-333" @tap.stop="handleCancel" />
      <h-button width="296" height="72" text="确认并提交" class="ml-32" @tap.stop="handleSubmit" />
    </view>
    <h-status-footer />
    <h-popup />
    <detail-type-add ref="refDetailTypeAdd" :count="typeCount" @submit="callbackTypeAdd" :unit="dataItem.productUnit" />
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import AreaDateInfo from '@/pages/report-production/components/area-date-info.vue'
import HTextDisplay from '@/components/h-text-display.vue'
import { onLoad } from '@dcloudio/uni-app'
import { formatDate, formatImage } from '@/utils/common'
import HTextarea from '@/components/h-textarea.vue'

import HooksPopup from '@/hooks/popup'
import HPopup from '@/components/h-popup.vue'
import DetailTypeAdd from '@/pages/quality-testing/detail-type-add.vue'
import { _get, _post } from '@/utils/common-request'
import bigNumber from 'bignumber.js'
import RejectsTypeDisplpy from '@/components/rejects-type-displpy.vue'

// ---------------页面标题-----------------------------------
const pageTitle = computed(() => {
  return readonly.value ? '质检明细' : '质检'
})

// ---------------数据初始化---------------------
const { popupOpen } = HooksPopup()
const dataItem = reactive<Partial<IQualityTestingItem>>({})
const readonly = ref(false)
const submitId = ref('')
const recordRemark = ref('')
const currentItem = reactive({ passNum: 0, ngNum: 0 })
onLoad((options) => {
  readonly.value = options.readonly === 'true'
  submitId.value = options.submitId
  recordRemark.value = options.recordRemark
  _get({ url: `/ngProduct/manage/info/${options.submitId}` }).then((res: IResponseType<IQualityTestingItem>) => {
    Object.assign(dataItem, res.data)
    currentItem.passNum = dataItem.passNum
    currentItem.ngNum = dataItem.ngNum
    typeList.value = []
  })
})

// ---------------良品/不良品输入框焦点监听------------------------------------
const refInputPassNumFocus = ref(false)
const refInputNgNumFocus = ref(false)
function handleInputFocus(type) {
  if (type === 'passNum') {
    refInputPassNumFocus.value = true
  } else {
    refInputNgNumFocus.value = true
  }
}
function handleInputBlur(type) {
  if (currentItem[type] && !/^\d{1,8}(\.\d{1,4})?$/.test(String(currentItem[type]))) {
    popupOpen('最多支持8位整数,4位小数')
    currentItem[type] = dataItem[type]
    return
  }
  if (type === 'passNum') {
    refInputPassNumFocus.value = false
  } else {
    refInputNgNumFocus.value = false
  }
}

//----------------不良类型相关操作--------------------------
const typeList = ref<ngInfoItem[]>([])
const refDetailTypeAdd = ref(null)
const typeCount = computed(() => {
  const total = typeList.value.reduce((pre, acc) => {
    pre = bigNumber(pre).plus(acc.ngNum).toNumber()
    return pre
  }, 0)
  return Math.max(0, bigNumber(currentItem.ngNum).minus(total).toNumber())
})
function handleTypeAdd() {
  refDetailTypeAdd.value?.open()
}
function handleTypeDelete(index) {
  typeList.value.splice(index, 1)
}
function callbackTypeAdd(item) {
  typeList.value.push({ ...item })
}

const ngTypeLength = computed(() => {
  return dataItem.ngProductDetailInfoList?.filter((v) => v.ngType)?.length
})

// ---------------页面提交方法---------------------------------
function handleCancel() {
  uni.navigateBack()
}

const submitLoading = ref(false)
function handleSubmit() {
  if (submitLoading.value) return
  submitLoading.value = true
  if (
    bigNumber(dataItem.repairNum || 0)
      .plus(dataItem.abandonedNum || 0)
      .gt(currentItem.ngNum)
  ) {
    popupOpen('不良数量不能小于报废数+返修数')
    submitLoading.value = false
    return
  }
  if (bigNumber(dataItem.passNum).plus(dataItem.ngNum).lt(currentItem.ngNum)) {
    popupOpen('不良数量不能大于总记工数')
    return
  }
  const targetSum = dataItem.passNum + dataItem.ngNum
  const currentSum = Number(currentItem.passNum) + Number(currentItem.ngNum)
  if (targetSum !== currentSum) {
    popupOpen(`质检数量不等于记工数量，请确认，质检数量总数为${currentSum}，记工总数${targetSum}`)
    submitLoading.value = false
    return
  }
  const params = {
    submitId: submitId.value,
    checkPassNum: currentItem.passNum || 0,
    checkNgNum: currentItem.ngNum || 0,
    remark: dataItem.remark,
    ngProductDetailInfos: typeList.value
  }
  _post({ url: '/ngProduct/manage/qc', data: params })
    .then((res) => {
      uni.$emit('qualityTestingListRefresh')
      uni.showToast({ title: '提交成功', icon: 'none', duration: 1500 })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    })
    .finally(() => {
      submitLoading.value = false
    })
}

function handleCheckNgNumInput(e) {
  const val = e.detail.value
  if (bigNumber(dataItem.passNum).plus(dataItem.ngNum).lt(val)) {
    uni.showToast({ title: '不良数量不能大于总记工数', icon: 'none' })
    return
  } else {
    currentItem.passNum = bigNumber(dataItem.passNum)
      .plus(dataItem.ngNum)
      .minus(val || '0')
      .toNumber()
  }
}
</script>

<style lang="scss" scoped>
.remark-border {
  border-top: px2vw(2) dashed #e5e5e5;
}
.detail-block {
  position: absolute;
  top: px2vw(32);
  left: px2vw(-32);
  width: px2vw(8);
  height: px2vw(24);
  background: #00bfa5;
  border-radius: 0 px2vw(2) px2vw(2) 0;
  &.grey {
    background: #9badbc;
  }
}
</style>
