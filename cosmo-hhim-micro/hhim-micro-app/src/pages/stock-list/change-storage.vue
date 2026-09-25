<template>
  <view class="change-write bg-f3f3f5 h-full box flex flex-col overflow-hidden pl-16 pr-16">
    <uni-nav-bar />
    <h-status-header title="变动库存" />
    <view class="pl-32 pr-24 box w-100 h-80 rounded-16-top bg-fff mt-16 flex justify-between align-center">
      <view class="color-5a6f82 font-24">
        <text>新增变动</text>
        <text class="color-ff0000 bold">{{ storageList.length }}</text>
        <text>条</text>
      </view>
      <image @tap="addChange" :src="formatImage('icon_add_0066ff', 'svg')" class="icon-48" />
    </view>
    <scroll-view scroll-y class="flex-1 overflow-hidden pt-8 box">
      <view v-for="(dataItem, idx) in storageList" :key="idx" class="bg-fff box px-32 pt-32 pl-3 box mb-8">
        <!--产品名称/数量-->
        <view class="flex align-center">
          <view class="flex-1">
            <h-text-display :text="dataItem.productName" :width="212" class="bold font-32 color-333" />
          </view>
          <view
            class="rounded-8 ml-24 font-24 color-fff flex-center w-56 h-32"
            :class="[dataItem.changeType === 'add' ? 'bg-F26A60' : 'bg-FE9F00']"
            >{{ dataItem.changeType === 'add' ? '增加' : '扣减' }}</view
          >
          <view class="ml-12 color-5a6f82 font-28 bold">
            {{ new BigNumber(dataItem.passNum).plus(dataItem.ngNum).toNumber() }}
          </view>
          <image @tap="editChange(dataItem)" src="/static/images/icon_edit.svg" class="icon-32 ml-16" />
        </view>
        <!--产品编码-->
        <view class="mt-12 font-24 color-999">{{ dataItem.productCode }}</view>
        <!--工序信息-->
        <view class="flex align-center mt-24">
          <view class="color-5a6f82 font-28">{{ dataItem.processName }}</view>
          <view class="flex-1" />
          <area-date-info :dataItem="{ pass: Number(dataItem.passNum), ng: Number(dataItem.ngNum) }" />
        </view>
        <!--备注信息-->
        <view class="flex mt-24 b-t-1 border-f5f5f5 py-26 box">
          <view class="font-28 color-5a6f82">变动原因：</view>
          <view class="font-28 color-5a6f82 flex-1">{{ dataItem.remark ? dataItem.remark : '-' }}</view>
          <image @tap="deleteChange(idx)" src="/static/images/icon_clear.svg" class="icon-32 ml-32" />
        </view>
      </view>
    </scroll-view>
    <view class="h-152 w-100 flex justify-center align-center font-32">
      <h-button width="296" height="72" text="取消" type="bg-fff color-333" @tap.stop="cancel" />
      <h-button width="296" height="72" text="确认并提交" class="ml-32" @tap.stop="submit" />
    </view>
    <h-status-footer />
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { _post } from '@/utils/common-request'
import { setGio, $store, formatImage } from '@/utils/common'
import HTextDisplay from '@/components/h-text-display.vue'
import AreaDateInfo from '@/pages/report-production/components/area-date-info.vue'
import BigNumber from 'bignumber.js'
import { onUnload } from '@dcloudio/uni-app'

onMounted(() => {})
const storageList = computed(() => $store.state.stock.storageList)
onUnload(() => {
  $store.commit('stock/setStorageList', [])
})

function addChange() {
  uni.navigateTo({
    url: '/pages/stock-list/add-change'
  })
}

function editChange(dataItem) {
  uni.navigateTo({
    url: '/pages/stock-list/add-change?dataItem=' + JSON.stringify(dataItem)
  })
}
function deleteChange(id) {
  uni.showModal({
    title: '提示',
    content: '确定删除本条吗？',
    success: function (res) {
      if (res.confirm) {
        storageList.value.splice(id, 1)
        storageList.value.forEach((item, idx) => {
          item.id = idx
        })
        $store.commit('stock/setStorageList', storageList.value)
      } else if (res.cancel) {
      }
    }
  })
}
const submitLoading = ref(false)
// 提交、取消
function submit() {
  if (submitLoading.value) {
    return
  }
  setGio('jr_kcbd_sucess')
  let submitData = JSON.parse(JSON.stringify(storageList.value))
  submitData.forEach((item) => {
    delete item.passNum
    delete item.changeType
    delete item.ngNum
    delete item.id
  })
  submitLoading.value = true
  _post({ url: '/storage/edit', data: submitData })
    .then((res) => {
      uni.$emit('stockListRefresh')

      uni.showToast({
        title: '提交成功',
        icon: 'none',
        duration: 1500
      })
      setTimeout(() => {
        submitLoading.value = false
        uni.navigateBack({
          delta: 1
        })
      }, 1500)
    })
    .catch(() => {
      submitLoading.value = false
    })
}
function cancel() {
  uni.navigateBack({
    delta: 1
  })
}
</script>
