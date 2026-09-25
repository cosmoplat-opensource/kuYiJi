<template>
  <view class="w-full h-full bg-f3f3f5 relative flex flex-col">
    <uni-nav-bar />
    <h-status-header title="记工审核" />
    <view class="flex-1 flex flex-col box rounded-16 overflow-hidden mt-24 mb-24">
      <view class="flex align-center rounded-16-top px-32 pt-32 pb-24 bg-fff box font-32 mx-16 bold">
        {{ dataItem.productName }}<text class="color-999">({{ dataItem.productCode }})</text>
      </view>
      <view class="box flex align-center bg-f7f8e7 px-16 h-64 mx-16 mt-8" v-if="!checkIsStandard">
        <image src="/static/images/icon_tips.svg" class="icon-32" />
        <view class="flex-1 ml-16">
          <text class="font-24 color-5a6f82">该产品还未设置标准工艺路线</text>
        </view>
        <h-button
          type="border-1 border-0066ff color-0066ff bg-fff"
          font="font-24"
          height="48"
          width="104"
          text="去设置"
          @tap="jumpProductDetail"
        />
      </view>
      <view
        v-if="numData.unStandardTechSubmitRecordIds?.length > 0"
        class="error-tips mx-16 flex align-center px-16 box font-24 color-5a6f82"
      >
        <image src="/static/images/icon_tips.svg" class="icon-32 mr-16" />
        还有
        <text class="color-ff0000">{{ numData.unStandardTechSubmitRecordIds?.length }}</text>
        <view class="flex-1">条异常数据待处理</view>
        <h-button
          type="border-1 border-0066ff color-0066ff bg-fff"
          font="font-24"
          height="48"
          width="104"
          text="去处理"
          @tap="jumpHandleAudit"
        />
      </view>
      <view class="flex-1 overflow-hidden">
        <!-- 产品 表格样式 -->
        <scroll-view scroll-y class="overflow-auto" style="max-height: 100%">
          <view
            v-for="(item, index) in formatStockList"
            :key="index"
            class="border-bottom-f5f5f5 mx-16 mt-8 px-32 box relative"
            :class="item.submitNum ? 'bg-fff' : 'bg-fff-30'"
            @tap.stop="handleJumpDetail(item)"
          >
            <view class="py-32 pb-24 flex align-center justify-between mid border-bottom-f5f5f5">
              <view class="absolute-l-t flex align-center">
                <h-exception-label
                  v-if="item.overSubmitWarnFlag === '0' && (item.submitNum || item.processFlowNum)"
                  class="mr-8"
                  text="超报风险"
                  bg="#FE9F00"
                />
              </view>
              <view class="color-333 font-28 bold flex align-center">
                <h-text-display
                  :text="item.processName"
                  :width="280"
                  :class="[item.submitNum ? 'color-333' : 'color-999']"
                />
                <word-icon v-if="item.isFirstProcess === '0'" class="ml-8" text="首" />
                <word-icon v-if="item.isLastProcess === '0'" class="ml-8" text="尾" />
              </view>
              <view class="flex align-center">
                <view class="color-5a6f82 font-24 flex align-center">
                  记工共
                  <text class="color-ff0000 mx-8 bold">{{ item.submitNum }}</text>
                  <text>{{ formatStr(item.productUnit, 5) }}</text>
                </view>
                <image
                  v-if="item.overSubmitWarnFlag === '0' && (item.submitNum || item.processFlowNum)"
                  :src="formatImage('icon_tips_tanhao_red', 'svg')"
                  class="icon-32 ml-16"
                  @tap.stop="openDialog('1', item)"
                />
              </view>
            </view>
            <view class="h-76 flex align-center border-bottom-f5f5f5" v-if="item.checkNum && item.checkNum < 0">
              <view class="color-5a6f82 font-28 flex align-center">
                库存
                <text class="bold ml-8 color-333">{{ parseInt(item.stockNum) }}</text>
                <view class="mx-8">+</view>
                记工
                <text class="bold ml-8 color-333">{{ item.submitNum }}</text>
                <view class="mx-8">-</view>
                <view class="color-0066ff" @tap.stop="openNextProcess(item)">
                  转下序
                  <text class="bold ml-8">{{ item.processFlowNum }}</text>
                </view>
              </view>
              <view class="flex-1" />
              <view class="font-28">
                审产后
                <text class="color-ff0000 bold ml-8">{{ item.checkNum }}</text>
              </view>
              <word-icon
                @tap.stop="openDialog('2', item)"
                v-if="item.commonWarnFlag === '0' && (item.submitNum || item.processFlowNum)"
                class="ml-16"
                text="警"
              />
            </view>
            <view
              v-for="(userItem, index) in item.userDetailSubmitRecordInfos"
              :key="index"
              :class="{ 'border-top-f5f5f5': index > 0 }"
              class="h-80 flex justify-between align-center font-28"
              @tap.stop="handleJumpDetail(item, userItem)"
            >
              <image src="/pages-audit/static/images/icon_people.svg" class="icon-32 mr-16" />
              <h-text-display :text="userItem.nickName" :width="200" />
              <word-icon v-if="userItem.warnFlag === '0'" class="ml-16" text="警" />
              <view v-if="userItem.waitQcFlag === 0" class="mark-24a8ff ml-16">待检</view>
              <view class="flex-1" />
              <area-date-info
                :dataItem="{ pass: userItem.passNum, ng: userItem.ngNum, productUnit: userItem.productUnit }"
                :showNg="!!userItem.ngNum"
                :showPass="!!userItem.passNum"
              />
              <image src="/static/images/icon_detail.svg" class="icon-32 ml-16" />
            </view>
          </view>
          <view class="font-24 color-b6c0c9 mt-48 mb-32 text-center">已到底部</view>
        </scroll-view>
      </view>
    </view>
    <view class="bottom-btn w-100 my-32 flex justify-center align-center font-32">
      <h-button width="296" height="72" text="返回" type="bg-fff color-333" @tap.stop="cancel" />
      <h-button
        v-if="formatStockList.length > 0"
        width="296"
        height="72"
        text="确认审核"
        class="ml-32"
        @tap.stop="submit"
      />
    </view>
    <h-status-footer />
    <h-popup />
    <h-popup-dialog ref="refPopupDialog" :showFooter="false" :showHeader="false">
      <image @click="closePopupDialog" src="/static/images/icon_close_666.svg" class="icon-48 absolute r-8 t-8" />
      <view class="m-h-800 flex flex-col bg-fff">
        <view class="flex-1 overflow-auto">
          <view class="fit-content p-4 font-24 color-fff bg-FE9F00 rounded-4">异常提示</view>
          <!-- <view v-if="errorValue.type === '1'" class="mt-24 color-5a6f82 line-height-48">
            工序 <text class="color-333">{{ errorValue.processName }}</text> 累计报工
            <text class="color-ff0000">大于</text> 工序
            <text class="color-333">{{ errorValue.preProcessSeq }}</text> 累计报工！
          </view> -->
          <view v-if="errorValue.type === '1'" class="mt-24 color-5a6f82 line-height-48">
            <!-- {{
            errorValue.overSubmitWarnMessage
          }} -->
            本工序报工数量 <text class="color-ff0000">超出</text> 前一道工序的累计报工数量！
          </view>
          <!-- <view v-if="errorValue.type === '2'" class="mt-24 color-5a6f82 line-height-48">
            工序 <text class="color-333">{{ '后工序' }}</text> 累计报工 <text class="color-ff0000">大于</text> 工序
            本工序累计报工！
          </view> -->
          <view v-if="errorValue.type === '2'" class="mt-24 color-5a6f82 line-height-48">
            {{ errorValue.commonWarnMessage.split('大于')[0] }}
            <text class="color-ff0000 mx-8">大于</text>
            {{ errorValue.commonWarnMessage.split('大于')[1] }}
          </view>
          <view v-if="errorValue.type !== '1'" class="mt-32 fit-content p-4 font-24 color-fff bg-19AA8D rounded-4">
            建议
          </view>
          <view v-if="errorValue.type === '1'" class="mt-8 color-5a6f82 line-height-48">
            <view v-for="(l, idx) in overObject.overList" :key="idx" class="mt-24">
              <view class="color-418CBD font-28 flex align-center">
                <view class="mark-ebf0f5-rounded">前</view>
                <view class="ml-8 color-333">{{ l.processName }}</view>
              </view>
              <view class="flex align-center color-333 mt-12">
                <view class="mr-12 mark-ebf0f5">库存</view>
                <view :class="[l.stockNum < 0 ? 'color-ff0000' : '', 'bold font-28']">{{ l.stockNum }}</view>
                <view class="ml-24 mr-12 mark-ebf0f5">未审核</view>
                <view class="bold font-28">{{ l.submitNum }}</view>
              </view>
            </view>
          </view>
          <view v-if="errorValue.type === '2'" class="mt-24 color-5a6f82 line-height-48">
            请先确认后工序报工是否正确，如正确，可修正本工序库存。
          </view>
        </view>
        <view class="flex-center mt-40">
          <h-button
            height="64"
            width="204"
            text="去核对记工"
            @tap="handleJumpDetail(overObject)"
            v-if="errorValue.type === '1'"
          />
          <template v-if="errorValue.type === '2'">
            <h-button height="64" width="204" text="我知道了" type="bg-f3f3f5 color-333" @tap="closePopupDialog" />
            <h-button height="64" width="204" class="ml-32" text="去修正库存" @tap="toHistory" />
          </template>
        </view>
      </view>
    </h-popup-dialog>
    <h-popup-dialog ref="refNextProcess" :showFooter="false" :showHeader="false">
      <image @click="closeNextProcess" src="/static/images/icon_close_666.svg" class="icon-48 absolute r-8 t-8" />
      <view class="flex">
        <view class="nextProcess color-5a6f82 bg-EBF0F5 rounded-4 pl-4 py-4 font-24 flex align-center mb-16">
          转下序
          <text class="color-333 mx-8 bold">{{ processFlowNum }}</text>
        </view>
      </view>
      <view
        v-for="(flowItem, flowIdx) in processFlowList"
        :key="flowIdx"
        class="pt-16 flex align-center"
        :class="[flowIdx === processFlowList.length - 1 ? '' : 'border-bottom-f5f5f5 pb-16']"
      >
        <word-icon class="mr-8" text="转" />
        <view class="flex-1 font-28">{{ flowItem.processName }}</view>
        <view class="color-333 ml-8 bold font-28">{{ flowItem.flowNum }}</view>
      </view>
    </h-popup-dialog>
  </view>
</template>
<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { _get, _post } from '@/utils/common-request'
import { onShow, onLoad } from '@dcloudio/uni-app'
import { $store, collectClick, formatImage, formatStr, setGio } from '@/utils/common'
import AreaDateInfo from '@/pages/report-production/components/area-date-info.vue'
import HExceptionLabel from '@/components/h-exception-label.vue'
import HPopupDialog from '@/components/h-popup-dialog.vue'
import HooksPopup from '@/hooks/popup'
import HooksProductStandard from '@/hooks/product-standard'
onShow(() => {
  setGio('jr_kcfx')
})
const dataItem = reactive({})
const { checkProductHasStandard, checkIsStandard } = HooksProductStandard()
onLoad((e) => {
  Object.assign(dataItem, JSON.parse(e.dataItem))
  checkProductHasStandard(dataItem.productSeq)
  uni.$on('auditDetailRefresh', (options) => {
    getDetailRecords()
  })
})
onShow(() => {
  getDetailRecords()
  checkProductHasStandard(dataItem.productSeq)
})

/** 异常原因弹窗模块 */
const refPopupDialog = ref(null)
const errorValue = ref({})
function closePopupDialog() {
  refPopupDialog.value.close()
}
const overObject = ref({ overList: [] })
function openDialog(type, item) {
  errorValue.value = { ...item, type: type }
  if (type === '1') {
    overObject.value = { ...item, overList: [] }
    //超报提示 根据 preProcessSeq 当前异常数据的前工序，去formatStockList里找到对应的前工序数据展示 stockNum submitNum
    let preProcessSeqList = item.preProcessSeq.split(',')
    formatStockList.value.forEach((itemProcess) => {
      for (let i in preProcessSeqList) {
        if (itemProcess.processSeq === preProcessSeqList[i]) {
          let data = {
            processName: itemProcess.processName,
            stockNum: itemProcess.stockNum,
            submitNum: itemProcess.submitNum
          }
          overObject.value.overList.push(data)
          break
        }
      }
    })
  }
  refPopupDialog.value.open()
}
function toHistory() {
  closePopupDialog()
  let productItem = {
    productName: dataItem.productName,
    productCode: dataItem.productCode,
    productSeq: errorValue.value.productSeq,
    processName: errorValue.value.processName,
    processCode: errorValue.value.processCode,
    processSeq: errorValue.value.processSeq,
    productUnit: errorValue.value.productUnit
  }
  let data = {
    processSeq: errorValue.value.processSeq,
    productSeq: errorValue.value.productSeq
  }
  _get({ url: '/storage/detail', data }).then((res) => {
    if (res.data) {
      productItem.passNum = res.data.passNum
      productItem.ngNum = res.data.ngNum
    } else {
      productItem.passNum = 0
      productItem.ngNum = 0
    }
    uni.navigateTo({
      url: '/pages/stock-list/change-history?item=' + JSON.stringify(productItem)
    })
  })
}
//转下序
const processFlowList = ref(null)
const refNextProcess = ref(null)
const processFlowNum = ref('')
function openNextProcess(item) {
  processFlowList.value = item.processFlowList
  processFlowNum.value = item.processFlowNum
  refNextProcess.value.open()
}
function closeNextProcess() {
  refNextProcess.value.close()
}
//明细跳转
function handleJumpDetail(item, userItem) {
  let ids = []
  if (userItem) {
    ids = userItem.ids
  } else {
    if (item.userDetailSubmitRecordInfos) {
      item.userDetailSubmitRecordInfos.forEach((e) => {
        ids = ids.concat(e.ids)
      })
    }
  }
  if (ids.length === 0) {
    return
  }
  closePopupDialog()
  uni.navigateTo({
    url: `/pages-audit/detail-list?ids=${ids}&product=${dataItem.productName}&process=${item.processName}&productSeq=${item.productSeq}`
  })
}

// 用来计算比例的数据，排行第一的数据和
const total = ref(0)
//获取排行数据
const StockList = ref([])
const numData = ref({})
function getDetailRecords() {
  let mockParams = {
    url: '/submit/checkIndex/product/detailRecords',
    data: {
      productSeq: dataItem.productSeq,
      submitStatus: '1'
    }
  }

  _get(mockParams)
    .then((res) => {
      if (res?.data) {
        StockList.value = res.data.detailCheckSubmitRecordByProductList || []
        numData.value = res.data
        if (StockList.value.length === 0 && !res.data.unStandardTechSubmitRecordIds?.length) {
          cancel()
        }
      }
    })
    .finally(() => {})
}

const formatStockList = computed(() => {
  return StockList.value.filter((v) => v.processFlowNum || v.submitNum)
})

function cancel() {
  uni.navigateBack({
    delta: 1
  })
}
function submit() {
  let ids = []
  StockList.value.forEach((item) => {
    if (item.userDetailSubmitRecordInfos) {
      item.userDetailSubmitRecordInfos.forEach((userItem) => {
        ids = ids.concat(userItem.ids)
      })
    }
  })
  let clerk = ''
  if (!numData.value.exceptionRecordNum) {
    clerk = '是否确认审核'
  } else {
    clerk = `审核包含${numData.value.needCheckRecordNum}条记工数据，有${numData.value.exceptionRecordNum}条数据可能存在异常，是否确认审核`
  }
  uni.showModal({
    title: '提示',
    content: clerk,
    success: function (res) {
      if (res.confirm) {
        checkRequest(ids)
      } else if (res.cancel) {
      }
    }
  })
}
const { popupOpen } = HooksPopup()
function checkRequest(ids) {
  if (!ids.length) {
    popupOpen('无记工数据,请前往员工维度审核')
    return
  }
  collectClick('批量审核', '记工审核', '审核列表-产品维度')
  _get({ url: `/submit/check/${ids.join(',')}` })
    .then((res: IResponseType<unknown>) => {
      if (res.msg.includes('已被审核')) {
        popupOpen(res.msg)
      } else {
        popupOpen('审核成功')
        uni.$emit('auditListRefresh')
        setTimeout(() => {
          cancel()
        }, 1500)
      }
    })
    .finally(() => {})
}

function jumpProductDetail() {
  uni.navigateTo({ url: '/pages-my-center/product-manage/add-edit?productSeq=' + dataItem.productSeq })
}
function jumpHandleAudit() {
  $store.commit('tabBar/setTabActive', 1)
  uni.setStorageSync('ku_follow_disable', '1')
  $store.commit('audit/setCrossPageData', {
    switchChange: false,
    ids: numData.value.unStandardTechSubmitRecordIds.toString()
  })
  uni.reLaunch({ url: '/pages/main' })
}
</script>
<style lang="scss" scoped>
.error-tips {
  height: px2vw(64);
  background: #f7f8e7;
}
</style>
