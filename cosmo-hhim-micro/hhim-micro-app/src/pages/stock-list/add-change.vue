<template>
  <view class="change-write bg-f3f3f5 h-full box flex flex-col overflow-hidden pl-16 pr-16">
    <uni-nav-bar />
    <h-status-header :title="title" />
    <scroll-view scroll-y class="flex-1 overflow-hidden">
      <tab-product
        ref="refProduct"
        class="flex flex-col"
        @productSelected="productSelected"
        :dataItem="{ itemName: dataModel.productName, itemCode: dataModel.productCode, itemSeq: dataModel.productSeq }"
      />
      <tab-process
        ref="refProcess"
        class="flex flex-col mt-16"
        :canUse="!!dataModel.productName"
        :standard="dataModel.standard"
        :productSeq="dataModel.productSeq"
        @processSelected="processSelected"
        :dataItem="{ itemName: dataModel.processName, itemCode: dataModel.processCode, itemSeq: dataModel.processSeq }"
      />
      <!-- <view class="num-module w-100 mt-16 mb-16 font-24 flex">
        <view class="h-100 flex-1 bg-fff mr-4 flex flex-col">
          <view class="w-100 flex-1 flex pl-32 pt-24 pr-24 pb-32 box">
            <view class="flex-1 flex flex-col justify-around">
              <view class="flex align-center">
                <view
                  @click="passSwitch('add')"
                  :class="[
                    'add-minus flex align-center justify-center',
                    dataModel.passChange === 'add' ? 'bg-0066ff  color-fff border-0' : 'bg-EBF0F5 color-9badbc'
                  ]"
                  >增加</view
                >
                <view
                  @click="passSwitch('minus')"
                  :class="[
                    'add-minus ml-16 box minus-border flex align-center justify-center',
                    dataModel.passChange === 'minus' ? 'bg-0066ff  color-fff border-0' : 'bg-EBF0F5 color-9badbc'
                  ]"
                  >减少</view
                >
              </view>
              <text class="color-5a6f82 mr-12 mt-24">良品</text>
              <view class="font-32 mt-12"
                ><input
                  v-model="dataModel.passNum"
                  @input="inputNum($event, 'pass')"
                  class="uni-input"
                  type="digit"
                  placeholder="0"
              /></view>
            </view>
            <image :src="formatImage('icon_liangpin_96', 'svg')" class="icon-96 mt-32" />
          </view>
          <view v-if="dataModel.productCode && dataModel.processCode" class="stock-line w-100 color-5a6f82 pl-32 box"
            >现有库存：{{ dataModel.passFromNum }}</view
          >
        </view>
        <view class="h-100 flex-1 bg-fff flex flex-col">
          <view class="w-100 flex-1 flex pl-32 pt-24 pr-24 pb-32 box">
            <view class="flex-1 flex flex-col justify-around">
              <view class="flex">
                <view
                  @click="ngSwitch('add')"
                  :class="[
                    'add-minus flex align-center justify-center',
                    dataModel.ngChange === 'add' ? 'bg-0066ff  color-fff border-0' : 'bg-EBF0F5 color-9badbc'
                  ]"
                  >增加</view
                >
                <view
                  @click="ngSwitch('minus')"
                  :class="[
                    'add-minus ml-16 box minus-border flex align-center justify-center',
                    dataModel.ngChange === 'minus' ? 'bg-0066ff  color-fff border-0' : 'bg-EBF0F5 color-9badbc'
                  ]"
                  >减少</view
                >
              </view>
              <text class="color-5a6f82 mr-12 mt-24">不良品</text>
              <view class="font-32 mt-12"
                ><input
                  :value="dataModel.ngNum"
                  @input="inputNum($event, 'ng')"
                  class="uni-input"
                  type="digit"
                  placeholder="0"
              /></view>
            </view>
            <image :src="formatImage('icon_buliang_96', 'svg')" class="icon-96 mt-32" />
          </view>
          <view v-if="dataModel.productCode && dataModel.processCode" class="stock-line w-100 color-5a6f82 pl-32"
            >现有库存：{{ dataModel.ngFromNum }}</view
          >
        </view>
      </view> -->
      <view class="num-module w-100 pt-32 box mt-16 font-24 relative bg-fff">
        <view class="change-type color-5a6f82 font-24 bg-EBF0F5 flex-center">变动类型</view>
        <view class="h-96 font-28 flex align-center pl-32 box">
          <view class="flex align-center" @tap="passNgSwitch('add')">
            <h-radio-box class="mr-24" :checked="dataModel.changeType === 'add'" />
            <text :class="[dataModel.changeType === 'add' ? 'color-333 bold' : 'color-5a6f82']">增加</text>
          </view>
          <view class="flex align-center ml-48" @tap="passNgSwitch('minus')">
            <h-radio-box class="mr-24" :checked="dataModel.changeType === 'minus'" />
            <text :class="[dataModel.changeType === 'minus' ? 'color-333 bold' : 'color-5a6f82']">扣减</text>
          </view>
        </view>
      </view>
      <view class="flex mt-4 mb-16">
        <view class="h-100 flex-1 bg-fff mr-4 flex flex-col">
          <view class="w-100 flex-1 flex pl-32 pt-16 pr-24 pb-32 box align-start">
            <view class="flex-1 flex flex-col justify-around">
              <input
                v-model="dataModel.passNum"
                @input="inputNum($event, 'pass')"
                class="bold font-32 mt-12"
                type="digit"
                placeholder="0"
              />
              <text class="color-5a6f82 font-24 mr-12 mt-24">良品数量</text>
            </view>
            <image :src="formatImage('icon_liangpin_96', 'svg')" class="icon-96" />
          </view>
          <view
            v-if="dataModel.productCode && dataModel.processCode"
            class="stock-line font-24 w-100 color-5a6f82 pl-32 box py-16 border-top-f5f5f5"
            >现有库存：{{ dataModel.passFromNum }}</view
          >
        </view>
        <view class="h-100 flex-1 bg-fff flex flex-col">
          <view class="w-100 flex-1 flex pl-32 pt-16 pr-24 pb-32 box align-start">
            <view class="flex-1 flex flex-col justify-around">
              <input
                :value="dataModel.ngNum"
                @input="inputNum($event, 'ng')"
                class="bold font-32 mt-12"
                type="digit"
                placeholder="0"
              />
              <text class="color-5a6f82 font-24 mr-12 mt-24">不良品数量</text>
            </view>
            <image :src="formatImage('icon_buliang_96', 'svg')" class="icon-96" />
          </view>
          <view
            v-if="dataModel.productCode && dataModel.processCode"
            class="stock-line font-24 w-100 color-5a6f82 pl-32 py-16 border-top-f5f5f5"
            >现有库存：{{ dataModel.ngFromNum }}</view
          >
        </view>
      </view>

      <view class="w-100 font-28 bg-fff p-32 relative box mt-16">
        <view class="color-5a6f82 font-24 pb-24">变动原因</view>
        <view class="w-100">
          <h-textarea
            class="mt-16 bold"
            v-model="dataModel.remark"
            @input="reasonInput"
            :maxlength="50"
            :border="false"
            :padding="false"
            :showClear="true"
            placeholder="输入变动原因"
            :minH="28"
            :showLength="false"
          />
          <!-- <input v-model="dataModel.remark" @input="tagsSelect()" class="uni-input font-28 bold" maxlength="50" placeholder-class="color-b8b8b8" placeholder="输入变动原因" /> -->
        </view>
      </view>
      <view v-if="recommendList.length > 0" class="w-100 bg-fff font-28 pb-32 box mt-8 tagshadow">
        <view class="color-e7a11a bg-fff5e3 font-24 flex-center tips">原因推荐</view>
        <view class="w-100 px-32 box flex flex-wrap">
          <view
            @tap="selectRecommend(item.itemName)"
            v-for="(item, idx) in recommendList"
            :key="idx"
            class="mark-ebf0f5 mr-16 mt-16 word-tap"
            >{{ formatStr(item.itemName, 11) }}</view
          >
        </view>
      </view>
    </scroll-view>
    <view class="bottom-btn w-100 mt-32 flex justify-center align-center font-32">
      <h-button width="296" height="72" text="取消" type="bg-fff color-333" @tap.stop="cancel" />
      <h-button width="296" height="72" :text="btnTitle" class="ml-32" @tap.stop="submit" />
    </view>
    <h-status-footer />
  </view>
</template>

<script setup lang="ts">
import TabProduct from '@/pages/stock-list/components/tab-product.vue'
import TabProcess from '@/pages/stock-list/components/tab-process.vue'
import { onMounted, reactive, ref, computed, onUnmounted } from 'vue'
import { _get, _post } from '@/utils/common-request'
import BigNumber from 'bignumber.js'
import { formatStr, setGio, $state, $store, formatImage } from '@/utils/common'
import HTextarea from '@/components/h-textarea.vue'
import HRadioBox from '@/components/h-radiobox.vue'
import { onLoad } from '@dcloudio/uni-app'
// 获取登录人报工次数
const dataModel = ref({
  passFromNum: 0,
  passToNum: 0,

  ngFromNum: 0,
  ngToNum: 0,
  changeType: 'add'
})
//passNum ngNum
const title = ref('新增变动')
const btnTitle = ref('确认新增')

onLoad((e) => {
  if (e.dataItem) {
    dataModel.value = JSON.parse(e.dataItem)
    title.value = '编辑变动'
    btnTitle.value = '保存'
  }
  uni.$on('reportInputSelectClear', productInputClear)
})
function productInputClear(obj) {
  if (obj.type === 'product') {
    // 清空了产品
    productSelected({ itemName: '', itemCode: '', itemSeq: '', standard: false })
    processSelected({ itemName: '', itemCode: '', itemSeq: '' })
    refProcess.value.refreshDate({ itemName: '', itemCode: '', itemSeq: '' })
  }
}

onMounted(() => {
  recommendSelect()
})
onUnmounted(() => {
  uni.$off('reportInputSelectClear', productInputClear)
})

const tabProductItem = reactive({ itemName: '产品', itemCode: '' })
// 输入数量处理
function passNgSwitch(type) {
  dataModel.value.changeType = type
  if (dataModel.value.changeType === 'add') {
    dataModel.value.passToNum = new BigNumber(dataModel.value.passFromNum ? dataModel.value.passFromNum : 0)
      .plus(dataModel.value.passNum ? dataModel.value.passNum : 0)
      .toNumber()
    dataModel.value.ngToNum = new BigNumber(dataModel.value.ngFromNum)
      .plus(dataModel.value.ngNum ? dataModel.value.ngNum : 0)
      .toNumber()
  }
  if (dataModel.value.changeType === 'minus') {
    dataModel.value.passToNum = new BigNumber(dataModel.value.passFromNum ? dataModel.value.passFromNum : 0)
      .minus(dataModel.value.passNum ? dataModel.value.passNum : 0)
      .toNumber()
    dataModel.value.ngToNum = new BigNumber(dataModel.value.ngFromNum)
      .minus(dataModel.value.ngNum ? dataModel.value.ngNum : 0)
      .toNumber()
  }
}
function inputNum(e, type) {
  let value = e.detail.value
  if (type === 'pass') {
    dataModel.value.passNum = value //良品的差值
  } else {
    dataModel.value.ngNum = value
  }
  passNgSwitch(dataModel.value.changeType)
  // if (type === 'pass') {
  //   dataModel.value.passNum = value //良品的差值
  //   if (dataModel.value.changeType === 'add') {
  //     dataModel.value.passToNum = new BigNumber(dataModel.value.passFromNum ? dataModel.value.passFromNum : 0)
  //       .plus(dataModel.value.passNum ? dataModel.value.passNum : 0)
  //       .toNumber()
  //   }
  //   if (dataModel.value.changeType === 'minus') {
  //     dataModel.value.passToNum = new BigNumber(dataModel.value.passFromNum ? dataModel.value.passFromNum : 0)
  //       .minus(dataModel.value.passNum ? dataModel.value.passNum : 0)
  //       .toNumber()
  //   }
  // }
  // if (type === 'ng') {
  //   dataModel.value.ngNum = value
  //   if (dataModel.value.changeType === 'add') {
  //     dataModel.value.ngToNum = new BigNumber(dataModel.value.ngFromNum)
  //       .plus(dataModel.value.ngNum ? dataModel.value.ngNum : 0)
  //       .toNumber()
  //   }
  //   if (dataModel.value.changeType === 'minus') {
  //     dataModel.value.ngToNum = new BigNumber(dataModel.value.ngFromNum)
  //       .minus(dataModel.value.ngNum ? dataModel.value.ngNum : 0)
  //       .toNumber()
  //   }
  // }
}
function getNumber(value) {
  var num = value
  if (num.indexOf('.') == 0) {
    //‘首位小数点情况‘
    num = num.replace(/[^$#$]/g, '0.')
    num = num.replace(/\.{4,}/g, '.')
  } else if (!/^(\d?)+(\.\d{0,4})?$/.test(num)) {
    var dot = String(num).indexOf('.')
    num = String(num).substring(0, dot + 5)
  }
  return num
}
const refProcess = ref(null)
const refProduct = ref(null)
function selectRecommend(val) {
  dataModel.value.remark = val
  tagsSelect()
}
const recommendList = ref([])
function tagsSelect() {
  _get({ url: '/tags/select', data: { key: dataModel.value.remark || '', type: 'STORAGE_CHANGE' } }).then((res) => {
    recommendList.value = res.data || []
  })
}
function recommendSelect() {
  _get({
    url: '/storage/recommendChangeTag',
    data: { key: dataModel.value.remark || '', type: 'STORAGE_CHANGE' }
  }).then((res) => {
    recommendList.value = res.data.HOT_KEY_TAG_STORAGE_CHANGE || []
  })
}
const storageList = computed(() => $store.state.stock.storageList)
// 提交、取消
function submit() {
  if (!dataModel.value.productName) {
    uni.showToast({
      title: '请输入产品',
      icon: 'none',
      duration: 1500
    })
    return
  }
  if (!dataModel.value.processName) {
    uni.showToast({
      title: '请输入工序',
      icon: 'none',
      duration: 1500
    })
    return
  }
  if (dataModel.value.passNum && !/^\d+(\.\d{1,4})?$/.test(dataModel.value.passNum)) {
    uni.showToast({
      title: '良品数量应为非负数且最多保留4位小数',
      icon: 'none'
    })
    return
  }
  if (dataModel.value.ngNum && !/^\d+(\.\d{1,4})?$/.test(dataModel.value.ngNum)) {
    uni.showToast({
      title: '不良品数量应为非负数且最多保留4位小数',
      icon: 'none'
    })
    return
  }
  if (
    (dataModel.value.passNum == 0 || !dataModel.value.passNum) &&
    (dataModel.value.ngNum == 0 || !dataModel.value.ngNum)
  ) {
    uni.showToast({
      title: '请确认良品或不良品的数量',
      icon: 'none',
      duration: 2000
    })
    return
  }
  if (dataModel.value.changeType === 'minus') {
    if (dataModel.value.passToNum < 0) {
      uni.showToast({
        title: '良品当前库存不足',
        icon: 'none',
        duration: 2000
      })
      return
    }
    if (dataModel.value.ngToNum < 0) {
      uni.showToast({
        title: '不良品当前库存不足',
        icon: 'none',
        duration: 2000
      })
      return
    }
  }

  let data = { ...dataModel.value }
  if (!data.passNum) {
    data.passNum = 0
  }
  if (!data.ngNum) {
    data.ngNum = 0
  }

  if (!data.id && data.id !== 0) {
    //新增
    data.id = storageList.value.length
    storageList.value.push(data)
  } else {
    //编辑
    storageList.value[data.id] = data
  }

  $store.commit('stock/setStorageList', storageList.value)
  let pages = getCurrentPages()
  let prePage = pages[pages.length - 2]
  if (prePage.$page.fullPath === '/pages/stock-list/change-storage') {
    cancel()
  } else {
    uni.redirectTo({
      url: '/pages/stock-list/change-storage'
    })
  }
}
function cancel() {
  uni.navigateBack({
    delta: 1
  })
}
// 产品取值
function productSelected(item) {
  dataModel.value.productName = item.itemName
  dataModel.value.productCode = item.itemCode
  dataModel.value.productSeq = item.itemSeq
  dataModel.value.standard = item.standard
  getStockNum()
}

// 工序取值
function processSelected(item) {
  dataModel.value.processName = item.itemName
  dataModel.value.processCode = item.itemCode
  dataModel.value.processSeq = item.itemSeq
  getStockNum()
}
//获取库存
function getStockNum() {
  if (dataModel.value.processSeq && dataModel.value.productSeq) {
    let data = {
      processSeq: dataModel.value.processSeq,
      productSeq: dataModel.value.productSeq
    }
    _get({ url: '/storage/detail', data }).then((res) => {
      if (res.data) {
        dataModel.value.passFromNum = res.data.passNum
        dataModel.value.ngFromNum = res.data.ngNum
      } else {
        dataModel.value.passFromNum = 0
        dataModel.value.ngFromNum = 0
      }
    })
  } else {
    dataModel.value.passFromNum = 0
    dataModel.value.ngFromNum = 0
  }
}

function handleAudioInput() {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}
function reasonInput(val) {
  if (val) {
    tagsSelect()
  } else {
    recommendSelect()
  }
}
</script>
<style lang="scss" scoped>
.change-type {
  width: px2vw(112);
  height: px2vw(32);
  border-radius: 0 0 px2vw(8) 0;
  position: absolute;
  top: 0;
  left: 0;
}
.tagshadow {
  box-shadow: 0 px2vw(8) px2vw(16) px2vw(1) rgba(216, 221, 229, 1);
}
.word-tap {
  &:active {
    background: #0066ff;
    color: #ffffff;
  }
}
.minus-border {
  border: px2vw(2) solid #d3dfeb;
}
.bottom-btn {
  height: px2vw(152);
  border-radius: 0px 0px 0px 0px;
}
.change-write {
}
.num-module {
  // height: px2vw(200);
  .stock-line {
    height: px2vw(56);
    line-height: px2vw(56);
    border-top: px2vw(1) solid #f5f5f5;
  }
  .add-minus {
    width: px2vw(56);
    height: px2vw(32);
    // line-height: px2vw(32);
    border-radius: px2vw(4);
  }
}

.line {
  height: px2vw(1);
}
.tips {
  width: px2vw(104);
  height: px2vw(32);
  background: #fff5e3;
  border-radius: 0 px2vw(4) px2vw(4) px2vw(4);
}
</style>
