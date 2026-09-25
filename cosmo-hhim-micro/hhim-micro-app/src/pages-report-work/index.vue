<template>
  <view class="report-work bg-f3f3f5 h-full box flex flex-col overflow-hidden">
    <uni-nav-bar />
    <h-status-header title="记工" />
    <view class="flex align-center px-32 bg-fff rounded-16 mx-16 box">
      <view class="w-104 h-32 flex align-center justify-center bg-f3f3f5 mr-16">
        <text class="color-5a6f82 font-24">记工日期</text>
      </view>
      <text class="font-28 color-5a6f82 flex-1">{{ dateSelected }}</text>
      <h-page-video :videoArr="[1]" page-name="单次记工" />
    </view>
    <!--选项卡-->
    <view class="tab-wrapper relative py-16 px-32 mt-16 mx-16 box">
      <view class="line absolute area-line" />
      <view
        class="line-selected absolute area-line"
        :style="{
          width: `${formatPx2Vw(232 * (tabActive - 1))}vw`
        }"
      />
      <view class="absolute icon-pose-wrapper" :class="tabActive === 1 ? 'p1' : tabActive === 2 ? 'p2' : 'p3'">
        <image :src="formatImage('img_pose@2x')" class="icon-pose" v-if="!showNotice" />
        <view class="bottom-line" />
      </view>
      <!--产品-->
      <view class="tab-wrapper__item absolute item_product">
        <template v-if="tabActive === 1">
          <h-button-tab @tap="tabActiveChange(1)">
            <image :src="formatImage('icon_product_fff', 'svg')" class="icon-32" />
          </h-button-tab>
          <view class="font-24 mt-24 color-0066ff">产品</view>
        </template>
        <template v-else>
          <h-button-tab type="white" @tap="tabActiveChange(1)">
            <image :src="formatImage('icon_product_ed', 'svg')" class="icon-32" />
          </h-button-tab>
          <view class="font-24 mt-24">{{ formatStr(tabProductItem.itemName, 7) || '产品' }}</view>
          <view class="font-20 color-999 mt-4">{{ tabProductItem.itemCode }}</view>
        </template>
      </view>
      <!--工序-->
      <view class="tab-wrapper__item absolute item_process">
        <template v-if="tabActive === 1">
          <h-button-tab type="white" @tap="tabActiveChange(2)">
            <image :src="formatImage('icon_gongxu', 'svg')" class="icon-32" />
          </h-button-tab>
          <text class="font-24 mt-24 color-999">工序</text>
        </template>
        <template v-else-if="tabActive === 2">
          <h-button-tab @tap="tabActiveChange(2)">
            <image :src="formatImage('icon_gongxu_fff', 'svg')" class="icon-32" />
          </h-button-tab>
          <view class="font-24 mt-24 color-0066ff">工序</view>
        </template>
        <template v-else>
          <h-button-tab type="white" @tap="tabActiveChange(2)">
            <image :src="formatImage('icon_gongxu_ed', 'svg')" class="icon-32" />
          </h-button-tab>
          <view class="font-24 mt-24 w-100">
            <h-text-display :text="`现:${tabProcessNowItem.itemName || '-'}`" parentClass="justify-center" />
          </view>
          <view class="font-20 color-999 w-100 mt-4">
            <h-text-display
              :text="`前:${tabProcessPreItem.itemName || standardPreNames || '-'}`"
              parentClass="justify-center"
            />
          </view>
        </template>
      </view>
      <!--数量-->
      <view class="tab-wrapper__item absolute item_num">
        <template v-if="tabActive === 3">
          <h-button-tab @tap="tabActiveChange(3)">
            <image :src="formatImage('icon_num_input_fff', 'svg')" class="icon-32" />
          </h-button-tab>
          <text class="font-24 mt-24 color-0066ff">数量</text>
        </template>
        <template v-else>
          <h-button-tab type="white" @tap="tabActiveChange(3)">
            <image :src="formatImage('icon_num_input', 'svg')" class="icon-32" />
          </h-button-tab>
          <text class="font-24 mt-24 color-999">数量</text>
        </template>
      </view>
    </view>
    <!--产品模块-->
    <tab-product
      ref="refProduct"
      class="flex-1 overflow-hidden flex flex-col"
      v-if="tabActive === 1"
      :dataItem="tabProductItem"
      @productSelected="productSelected"
      @tabActiveChange="tabActiveChange"
    />
    <!--工序模块-->
    <tab-process
      ref="refProcess"
      class="flex-1 overflow-hidden flex flex-col"
      v-else-if="tabActive === 2"
      @processPreSelected="processPreSelected"
      @processNowSelected="processNowSelected"
      :product-item="tabProductItem"
      :preItem="tabProcessPreItem"
      :nowItem="tabProcessNowItem"
      @tabActiveChange="tabActiveChange"
      :standardPreProcess="standardPreProcess"
    />
    <!--数量模块-->
    <tab-num
      class="flex flex-col mx-16"
      :class="showNotice ? '' : 'flex-1'"
      :countItem="numItem"
      :recommendContent="[tabProductItem.itemSeq, tabProcessNowItem.itemSeq]"
      v-else
      btnPreShow
      @reportWork="handleReportWork"
      @tabActiveChange="tabActiveChange"
      @showNoticeChange="handleNoticeChange"
      :btnShow="!showNotice"
    />
    <!--submitNotice判断是否有系统提示消息-->
    <view
      class="flex flex-col align-center notice-success"
      :class="showNotice === 1 || submitNotice ? 'justify-center' : 'justify-end pb-340'"
      v-if="showNotice"
    >
      <template v-if="showNotice === 200 && submitNotice && !isExperience">
        <view class="relative">
          <image :src="formatImage('img_notice_tips_3')" class="icon-notice-bg" />
          <view class="icon-notice-text">{{ submitNotice }}</view>
          <view class="icon-notice-btn">
            <h-button width="174" height="64" text="我知道了" @tap.stop="handleSubmitNoticeDone" />
          </view>
        </view>
      </template>
      <template v-if="showNotice === 200 && !submitNotice">
        <image :src="formatImage('img_pose_well')" class="icon-notice" />
        <view class="mt-40 flex align-center px-48">
          <image src="./images/icon_record_well.svg" class="icon-32" />
          <view class="font-32 color-333 ml-8 flex-1">记工完成</view>
        </view>
      </template>
      <template v-if="showNotice === 500">
        <image :src="formatImage('img_pose_fail')" class="icon-notice" />
        <view class="mt-40">
          <text class="font-32 color-333">{{ showMsg }}</text>
        </view>
      </template>
      <template v-if="showNotice === 1">
        <image src="/static/images/icon_loading.svg" class="icon-loading" />
      </template>
    </view>
    <h-status-footer />
    <canvas
      v-for="idx in 5"
      :key="idx"
      :canvas-id="'compress_canvas' + idx"
      :id="'compress_canvas' + idx"
      class="compress_canvas"
      :style="{ width: '800px', height: '800px' }"
    />
    <h-popup />
  </view>
</template>

<script setup lang="ts">
import HButtonTab from './components/h-button-tab.vue'
import TabProduct from './tab-product.vue'
import TabProcess from './tab-process.vue'
import TabNum from './tab-num.vue'
import { computed, reactive, ref } from 'vue'
import { _get, _post } from '@/utils/common-request'
import { $store, formatStr, formatImage, setGio, formatPx2Vw, collectClick } from '@/utils/common'
import { checkSubmitRule } from '@/pages/report-work/util'
import HTextDisplay from '@/components/h-text-display.vue'
import { onLoad } from '@dcloudio/uni-app'
import checkProcessExist from '@/hooks/checkProcessExist'
import HooksSystemNotice from '@/hooks/system-notice'

import HooksProductStandard from '@/hooks/product-standard'

const dateSelected = ref('')
onLoad((options) => {
  dateSelected.value = options.date
})
// 检查是否是体验
const isExperience = computed(() => {
  return $store.getters.isExperience
})

const { checkIsStandard, checkProductHasStandard, standardPreProcess, getStandardPreProcess, assembleSubmitParams } =
  HooksProductStandard()

// 功能栏切换模块
const refProduct = ref(null)
const refProcess = ref(null)
const tabActive = ref(1)
const tabProductItem = reactive({ itemName: '', itemCode: '', itemSeq: '' })
const tabProcessPreItem = reactive({ itemName: '', itemCode: '', itemSeq: '' })
const tabProcessNowItem = reactive({ itemName: '', itemCode: '', itemSeq: '', isLastProcess: '1', isFirstProcess: '1' })
const numItem = reactive({ passNum: '', checkPassNum: null, ngNum: '', checkNgNum: '', remark: '' })
function tabActiveChange(index, jumpAble?) {
  if (tabActive.value === 1 && index > 1 && !tabProductItem.itemName) {
    uni.showToast({ title: '请选择产品', icon: 'none', duration: 2000 })
    return
  }
  if (index === 1 && tabActive.value === 2) {
    // 如果不选工序切回产品不做判断
    tabActive.value = index
    return
  }
  // 1.产品 2.工序 3.数量
  if (tabActive.value === 1) {
    refProduct.value?.checkSameName(() => {
      tabActive.value = index
    })
  } else if (tabActive.value === 2 && !jumpAble) {
    refProcess.value?.handleBtnNext()
  } else {
    tabActive.value = index
  }
}

// 产品选择
function productSelected(item) {
  // 走接口判断是否是标准工艺
  checkProductHasStandard(item.itemSeq)
  Object.assign(tabProductItem, item)
}
function processPreSelected(item) {
  Object.assign(tabProcessPreItem, item)
}
function processNowSelected(item) {
  Object.assign(tabProcessNowItem, item)
  if (checkIsStandard.value) {
    getStandardPreProcess(tabProductItem.itemSeq, item.itemSeq)
  } else {
    standardPreProcess.value = []
  }
}

const standardPreNames = computed(() => {
  const arr = standardPreProcess.value.map((v) => v.processName)
  return arr.join(',')
})

// 记工提交模块
const showNotice = ref(0)
const showMsg = ref('记工失败，请检查网络')
async function handleReportWork(item) {
  if (
    !checkSubmitRule(
      tabProductItem,
      tabProcessPreItem,
      tabProcessNowItem,
      item,
      'passNum',
      'ngNum',
      checkIsStandard.value
    )
  ) {
    showNotice.value = 0
    return
  }
  Object.assign(numItem, item)
  if (checkIsStandard.value) {
    submitNext(item)
    return
  }
  await checkProcessExist(
    tabProductItem,
    tabProcessNowItem,
    () => submitNext(item),
    () => {
      tabProcessNowItem.isFirstProcess = '1'
      tabProcessNowItem.isLastProcess = '1'
      tabActive.value = 2
      showNotice.value = 0
    }
  )
}
const { submitNotice, getSubmitTips } = HooksSystemNotice(10)
function submitNext(item) {
  const params = {
    productName: tabProductItem.itemName,
    productCode: tabProductItem.itemCode,
    productSeq: tabProductItem.itemSeq,
    preProcessName: tabProcessPreItem.itemName,
    preProcessCode: tabProcessPreItem.itemCode,
    preProcessSeq: tabProcessPreItem.itemSeq,
    operateProcessName: tabProcessNowItem.itemName,
    operateProcessCode: tabProcessNowItem.itemCode,
    operateProcessSeq: tabProcessNowItem.itemSeq,
    isLastProcess: tabProcessNowItem.isLastProcess,
    isFirstProcess: tabProcessNowItem.isFirstProcess,
    submitDay: dateSelected.value,
    passNum: item.passNum || 0,
    ngNum: item.ngNum || 0,
    remark: item.remark,
    submitPictures: item.submitPictures
  }
  // 如果是标准工艺,前工序参数改为多个前工序数据拼装
  Object.assign(params, assembleSubmitParams(params))
  // 根据send判断是点击的保存还是送检按钮
  const url = item.type === 'send' ? '/ngProduct/manage/submitForInspection' : '/submit/add'
  _post({ url, data: params })
    .then(async (res: IResponseType<unknown>) => {
      await getSubmitTips()
      setGio('jr_jg_end')
      showNotice.value = res.code
      uni.$emit('listRefresh', dateSelected.value)
      if (isExperience.value) {
        uni.$emit('refreshExperience', 1)
      }
      if (submitNotice.value) return
      setTimeout(() => {
        uni.navigateBack()
      }, 2000)
    })
    .finally(() => {
      if (showNotice.value === 1) {
        showNotice.value = 0
      }
    })
  collectClick(
    item.type === 'send' ? '送检' : '记工',
    '单次记工页面',
    '记工列表',
    `${tabProductItem.itemSeq}&${tabProcessNowItem.itemSeq}`
  )
}

function handleSubmitNoticeDone() {
  uni.navigateBack()
}

function handleNoticeChange(val) {
  showNotice.value = val
}
</script>
<style lang="scss" scoped>
.report-work {
  .tab-wrapper {
    background: #ffffff;
    border-radius: px2vw(16) px2vw(16) 0 0;
    display: flex;
    height: px2vw(184);
    .item_product {
      left: px2vw(32);
    }
    .item_process {
      left: px2vw(250);
    }
    .item_num {
      left: px2vw(470);
    }
    .area-line {
      left: px2vw(125);
      top: px2vw(62);
    }
    .icon-pose-wrapper {
      top: px2vw(-36);
      transition: left 0.3s linear;
      &.p1 {
        left: px2vw(72);
      }
      &.p2 {
        left: px2vw(290);
      }
      &.p3 {
        left: px2vw(510);
      }
      .icon-pose {
        width: px2vw(58);
        height: px2vw(122);
      }
      .bottom-line {
        margin-top: px2vw(82);
        width: px2vw(32);
        height: px2vw(8);
        margin-left: px2vw(38);
        background: #0066ff;
      }
    }
    &__item {
      box-sizing: border-box;
      width: px2vw(186);
      height: px2vw(152);
      padding-top: px2vw(16);
      padding-bottom: px2vw(24);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: space-between;
    }
    .line {
      width: px2vw(465);
      height: px2vw(4);
      background: #f3f3f5;
    }
    .line-selected {
      width: px2vw(465);
      height: px2vw(4);
      background: linear-gradient(90deg, #24a8ff 0%, #7ccbff 100%);
      transition: width 0.3s linear;
    }
  }
  .icon-notice {
    width: px2vw(188);
    height: px2vw(320);
  }
}
.compress_canvas {
  position: absolute;
  left: 10000px;
}
.notice-success {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.1);
  z-index: 999;
  box-sizing: border-box;
  .icon-loading {
    width: px2vw(48);
    height: px2vw(48);
    animation: rotate 2s linear infinite;
  }
  @keyframes rotate {
    0% {
      transform: rotate(0deg);
    }
    100% {
      transform: rotate(360deg);
    }
  }
}
.icon-notice-bg {
  width: px2vw(528);
  height: px2vw(362);
  z-index: 1;
}
.icon-notice-text {
  position: absolute;
  width: px2vw(464);
  left: px2vw(32);
  top: px2vw(146);
  font-size: px2vw(32);
  color: #333333;
  line-height: px2vw(48);
}
.icon-notice-btn {
  position: absolute;
  left: px2vw(176);
  top: px2vw(266);
}
</style>
