<template>
  <!-- animation=false：H5 下 uni-popup 动画（slide-bottom）可能卡住导致弹窗不显示，关闭动画确保稳定弹出 -->
  <uni-popup ref="popup" type="bottom" @maskClick="handleCancel" :safe-area="false" :animation="false">
    <view class="open-filter bg-fff flex flex-col justify-start font-28">
      <view class="title w-100 flex align-center justify-center relative">
        <view class="py-12 px-16 rounded-8">
          <text class="font-28 color-5a6f82 bold">筛选</text>
        </view>
        <image @click="handleCancel" src="/static/images/icon_close_666.svg" class="icon-48 close" />
      </view>
      <view class="flex flex-col px-32 box">
        <view class="h-96 flex align-center border-bottom-f5f5f5">
          <view class="w-176 color-5a6f82">产品</view>
          <input
            :value="filterData.productNameOrCode"
            class="flex-1"
            placeholder-class="font-28 color-b8b8b8"
            placeholder="选择产品"
            @tap="handleProductTap"
          />
          <image
            v-if="filterData.productNameOrCode"
            src="/static/images/icon_del_b6c0c9.svg"
            class="icon-48"
            @tap="handleProductClear"
          />
          <image v-else src="/static/images/icon_input_arr.svg" class="icon-48" />
        </view>
        <template v-if="!onlyProduct">
          <view class="h-96 flex align-center border-bottom-f5f5f5">
            <view class="w-176 color-5a6f82">工序</view>
            <input
              :value="filterData.processNameOrCode"
              class="flex-1"
              placeholder-class="font-28 color-b8b8b8"
              placeholder="选择工序"
              @tap="handleProcessTap"
            />
            <image
              v-if="filterData.processNameOrCode"
              src="/static/images/icon_del_b6c0c9.svg"
              class="icon-48"
              @tap="handleProcessClear"
            />
            <image v-else src="/static/images/icon_input_arr.svg" class="icon-48" />
          </view>
          <view class="h-96 flex align-center border-bottom-f5f5f5">
            <view class="w-176 color-5a6f82">员工</view>
            <input
              :value="filterData.submitNickName"
              class="flex-1"
              placeholder-class="font-28 color-b8b8b8"
              placeholder="选择员工"
              @tap="handleNormalStaffTap"
            />
            <image
              v-if="filterData.submitNickName"
              src="/static/images/icon_del_b6c0c9.svg"
              class="icon-48"
              @tap="handleNormalStaffClear"
            />
            <image v-else src="/static/images/icon_input_arr.svg" class="icon-48" />
          </view>
          <view v-if="filterData.selectMonth" class="w-100 h-96 flex align-center border-bottom-f5f5f5">
            <view class="w-176 color-5a6f82">结算月份</view>
            <picker
              mode="date"
              fields="month"
              :value="filterData.selectMonth + '-01'"
              @change="monthChange"
              class="flex-1 bg-fff color-333 rounded-24 pr-16 py-10 box font-28"
            >
              {{ filterData.selectMonth }}
            </picker>
          </view>
        </template>
      </view>
      <view class="flex justify-center mt-32 mb-48">
        <h-button width="296" height="72" text="重置" type="bg-f3f3f5 color-333" @tap.stop="handleReset" />
        <h-button width="296" height="72" text="确定" class="ml-32" @tap.stop="handleConfirm" />
      </view>
    </view>
    <h-popup-staff ref="popupNormalStaff" @staffSelected="changeStaff" title="选择员工" />
    <h-popup-process ref="popupProcess" @processSelected="changeProcess" title="工序" />
    <h-popup-product ref="popupProduct" @productSelected="changeProduct" />
  </uni-popup>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import InputProcess from '@/pages/audit/input-process.vue'
import InputStaff from '@/pages/audit/input-staff.vue'
import HPopupStaff from '@/components/h-popup-staff.vue'
import HPopupProcess from '@/components/h-popup-process.vue'
import HPopupProduct from '@/components/h-popup-product.vue'
const props = defineProps({
  onlyProduct: {
    type: Boolean,
    default: false
  }
})
const filterData = ref({})

const popup = ref(null)

const emits = defineEmits(['confirm'])

function open(data) {
  filterData.value = data
  popup.value.open('bottom')
}
function handleCancel() {
  popup.value?.close('bottom')
}
function handleReset() {
  filterData.value = {}
}

function handleConfirm() {
  emits('confirm', filterData.value)
  popup.value?.close('bottom')
}
function clearStaff() {
  filterData.value.staffInfo = {}
  filterData.value.submitNickName = ''
}
defineExpose({
  open,
  clearStaff
})

function changeProduct(val) {
  filterData.value.productInfo = val
  filterData.value.productNameOrCode = val.itemName
}
function changeProcess(val) {
  filterData.value.processInfo = val
  filterData.value.processNameOrCode = val.itemName
}
function changeStaff(val) {
  filterData.value.staffInfo = val
  filterData.value.submitNickName = val.nickName
}
function monthChange(event) {
  filterData.value.selectMonth = event.detail.value
}

const popupProduct = ref(null)
const popupProcess = ref(null)
const popupNormalStaff = ref(null)
function handleProductTap() {
  popupProduct.value?.open()
}
function handleProcessTap() {
  popupProcess.value?.open()
}
function handleNormalStaffTap() {
  popupNormalStaff.value?.open()
}
function handleProductClear() {
  changeProduct({ itemName: '' })
}
function handleProcessClear() {
  changeProcess({ itemName: '' })
}
function handleNormalStaffClear() {
  changeStaff({ submitNickName: '' })
}
</script>

<style lang="scss" scoped>
.close {
  position: absolute;
  right: px2vw(24);
  top: px2vw(24);
}
.uni-popup {
  z-index: 999;
}
.open-filter {
  border-radius: px2vw(16) px2vw(16) 0 0;
  .title {
    height: px2vw(96);
    border-bottom: px2vw(1) solid #f0f0f0;
  }
}
</style>
