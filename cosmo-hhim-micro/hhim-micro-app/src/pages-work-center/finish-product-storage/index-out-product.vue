<template>
  <view class="h-full flex flex-col bg-f3f3f5 overflow-hidden">
    <uni-nav-bar />
    <h-status-header title="成品出库" />
    <view class="bg-fff rounded-16-top px-32 py-34 box mx-16">
      <view class="flex align-center justify-between">
        <view class="color-5a6f82 font-28 bold">
          出库产品(<text class="color-ff0000">{{ checkCheckedNum }}种</text>)
        </view>
        <image @click="add" :src="formatImage('icon_add_0066ff', 'svg')" class="icon-48" />
      </view>
      <view class="mt-48 flex align-center">
        <view class="color-5a6f82 font-28 w-176">出库备注</view>
        <input
          class="flex-1 pl-16 font-28"
          v-model="outBoundReason"
          placeholder-style="color:#B8B8B8"
          placeholder="输入备注"
          maxlength="100"
          @input="reasonIpt"
        />
      </view>
    </view>
    <!--数据列表滚动区域-->
    <scroll-view scroll-y class="w-100 px-16 box flex-1 overflow-hidden">
      <!-- 产品列表 -->
      <h-empty v-if="!dataList" className="pt-320" tipsWord="暂无产品" />
      <template v-else>
        <view
          v-for="(dataItem, index) in dataList"
          :key="index"
          class="bg-fff flex flex-col pt-32 pb-24 pr-32 box mt-8"
        >
          <view class="w-100 flex-between">
            <view class="font-28 relative flex-1 bold flex align-center ml-32">
              <h-text-display :text="dataItem.productName" :width="200" />
              <text class="color-999">({{ dataItem.productSeq }})</text>
            </view>
            <view class="h-32 line-32 px-16 font-24 flex align-center bg-EBF0F5 rounded-16">
              <text class="color-5a6f82">库存</text>
              <text class="color-333 word-break ml-4 bold">{{ dataItem.num }}</text>
            </view>
          </view>
          <view class="h-80 bg-f3f3f5 flex-between mt-24 ml-32 pl-16 py-8 pr-8 box">
            <view class="color-5a6f82 font-24">出库数量</view>
            <input
              type="digit"
              class="w-240 h-64 bg-fff pr-24 font-28 text-right"
              v-model="dataItem.outBoundNum"
              placeholder-style="color:#B8B8B8"
              placeholder="0"
              maxlength="30"
              @blur="inputNum($event, dataItem)"
            />
          </view>
        </view>
      </template>
    </scroll-view>
    <view class="py-32 box flex justify-center align-center font-32">
      <h-button width="296" height="72" text="取消" type="bg-fff color-333" @tap.stop="cancel" />
      <h-button width="296" height="72" text="确认出库" class="ml-32" @tap.stop="confirm" />
    </view>

    <h-popup tabBar />
  </view>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { _put } from '@/utils/common-request'
import { $store } from '@/utils/common'
import { formatImage } from '@/utils/common'
const dataList = computed(() => {
  return $store.state.outProductStorage.outList
})
const outBoundReason = computed(() => {
  return $store.state.outProductStorage.outBoundReason
})
const checkCheckedNum = computed(() => {
  return dataList.value.filter((item: any) => item.checked).length
})
function inputNum(e, item) {
  let value = e.detail.value
  if (value > item.num) {
    uni.showToast({
      title: '出库数量不可大于库存数',
      duration: 2000,
      icon: 'none'
    })
    item.outBoundNum = 0
  } else if (value < 0) {
    uni.showToast({
      title: '请填写大于0的数据',
      duration: 2000,
      icon: 'none'
    })
    item.outBoundNum = 0
  } else {
    item.outBoundNum = value.replace(/^(.*\..{4}).*$/, '$1')
  }
}
function reasonIpt(e) {
  $store.commit('outProductStorage/updateOutBoundReason', e.detail.value)
}
function add() {
  $store.commit('outProductStorage/updateOutBoundReason', outBoundReason.value)
  $store.commit('outProductStorage/updateOutList', dataList.value)
  uni.navigateBack()
}
function confirm() {
  let outBoundParamList = []
  for (let i in dataList.value) {
    let item = dataList.value[i]
    if (item.outBoundNum && !/^\d+(\.\d{1,4})?$/.test(item.outBoundNum)) {
      uni.showToast({
        title: '产品' + item.productName + '出库数量应为非负数且最多保留4位小数',
        icon: 'none'
      })
      return
    }
    if (!Number(item.outBoundNum)) {
      uni.showToast({
        title: '请给产品' + item.productName + '填写大于0的数据',
        duration: 2000,
        icon: 'none'
      })
      return
    }
    let data = {
      productSeq: item.productSeq,
      changeNum: Number(item.outBoundNum)
    }
    outBoundParamList.push(data)
  }
  uni.showLoading({ title: '提交中', mask: true })
  _put({
    url: '/storage/finish/batch/inOrOutBound',
    data: {
      reason: outBoundReason.value,
      boundParamList: outBoundParamList,
      inOrOutFlag: false
    }
  })
    .then((res) => {
      uni.showToast({
        title: '出库成功',
        duration: 2000,
        icon: 'none'
      })
      setTimeout(() => {
        $store.commit('outProductStorage/clearOutBoundReason')
        $store.commit('outProductStorage/clearOutList')
        uni.$emit('cancelPopup')
        uni.navigateBack()
      }, 2000)
    })
    .finally(() => {
      uni.hideLoading()
    })
}
function cancel() {
  $store.commit('outProductStorage/updateOutBoundReason', outBoundReason.value)
  $store.commit('outProductStorage/updateOutList', dataList.value)
  uni.navigateBack()
}
</script>
<style lang="scss" scoped></style>
