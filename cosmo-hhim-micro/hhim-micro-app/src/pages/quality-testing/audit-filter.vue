<template>
  <uni-popup ref="popup" type="bottom" @maskClick="handleCancel" :safe-area="true">
    <view class="open-filter bg-fff px-32 box flex flex-col justify-start font-28">
      <view class="title w-100 flex align-center">
        <view
          class="py-12 px-16 bg-f3f3f5 rounded-8"
          :class="!checkFollow ? 'bg-0066ff' : 'bg-f3f3f5'"
          @tap="handleTabChange(TabType.NORMAL)"
        >
          <text class="font-28" :class="!checkFollow ? 'color-fff' : 'color-5a6f82'">筛选</text>
        </view>
        <view
          v-if="!onlyProduct"
          class="py-12 px-16 rounded-8 ml-16"
          :class="checkFollow ? 'bg-0066ff' : 'bg-f3f3f5'"
          @tap="handleTabChange(TabType.FOLLOW)"
        >
          <text class="font-28" :class="checkFollow ? 'color-fff' : 'color-5a6f82'"
            >我关注的({{ followList.length }})</text
          >
        </view>
        <view class="flex-1" />
        <image @click="handleCancel" src="/static/images/icon_close_666.svg" class="icon-48" />
      </view>
      <block v-if="tabActive === TabType.NORMAL">
        <view class="flex flex-col min-h-500">
          <input-product :productNameOrCode="filterData.productNameOrCode" @confirm="changeProduct" auditPopup />
          <template v-if="!onlyProduct">
            <input-process :processNameOrCode="filterData.processNameOrCode" @confirm="changeProcess" auditPopup />
            <input-staff :submitNickName="filterData.submitNickName" @confirm="changeStaff" auditPopup />
            <input-remark :remark="filterData.remark" @input="changeRemark" />
            <view class="w-100 h-96 flex align-center border-bottom-f5f5f5">
              <view class="w-176 color-5a6f82">开始时间</view>
              <picker
                mode="date"
                :value="filterData.startDate"
                :start="startDatePicker"
                :end="endDatePicker"
                @change="bindDateChangeStart"
                class="flex-1"
              >
                <text v-if="filterData.startDate">{{ filterData.startDate }}</text>
                <text v-else class="color-b8b8b8">选择开始时间</text>
              </picker>
              <image
                v-if="filterData.startDate"
                src="/static/images/icon_del_b6c0c9.svg"
                class="icon-48"
                @tap="handleInputClear('startDate')"
              />
            </view>
            <view class="w-100 h-96 flex align-center border-bottom-f5f5f5">
              <view class="w-176 color-5a6f82">结束时间</view>
              <picker
                mode="date"
                :value="filterData.endDate"
                :start="startDatePicker"
                :end="endDatePicker"
                @change="bindDateChangeEnd"
                class="flex-1"
              >
                <text v-if="filterData.endDate">{{ filterData.endDate }}</text>
                <text v-else class="color-b8b8b8">选择结束时间</text>
              </picker>
              <image
                v-if="filterData.endDate"
                src="/static/images/icon_del_b6c0c9.svg"
                class="icon-48"
                @tap="handleInputClear('endDate')"
              />
            </view>
          </template>
        </view>
        <view class="flex justify-center mt-40 mb-32">
          <h-button width="296" height="72" text="重置" type="bg-fff color-333" @tap.stop="handleReset" />
          <h-button width="296" height="72" text="确定" class="ml-32" @tap.stop="handleConfirm" />
        </view>
      </block>
      <block v-else>
        <view class="flex flex-wrap min-h-800 py-16 box">
          <view
            class="px-8 box bg-f3f3f5 h-48 flex align-center mr-16 mt-16"
            v-for="(item, index) in followList"
            :key="item.id"
          >
            <view class="mr-8 color-5a6f82 font-28">{{ item.nickName }}</view>
            <image :src="formatImage('icon_del_32_fce0de', 'svg')" class="icon-32" @tap="handleUserDelete(index)" />
          </view>
          <image :src="formatImage('icon_add_0066ff', 'svg')" class="icon-48 mt-16" @tap="handleChangeStaff" />
        </view>
        <view class="flex justify-center mt-40 mb-32">
          <h-button width="296" height="72" text="确定" class="ml-32" @tap.stop="handleConfirm" />
        </view>
      </block>
    </view>
    <popup-staff ref="popupStaff" @userSelect="handleUserSelect" />
  </uni-popup>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import InputProduct from './input-product.vue'
import InputProcess from './input-process.vue'
import InputStaff from './input-staff.vue'
import { $store, formatImage } from '@/utils/common'
import InputRemark from '@/pages/audit/input-remark.vue'
import PopupStaff from '@/pages/audit/popup-staff.vue'
import { _get, _post } from '@/utils/common-request'
const props = defineProps({
  onlyProduct: {
    type: Boolean,
    default: false
  }
})
const filterData = computed(() => $store.state.audit.filterData)

const popup = ref(null)
const popupStaff = ref(null)
const emits = defineEmits(['clearFilter', 'refresh'])

function open(type) {
  if (type === 'follow') {
    tabActive.value = TabType.FOLLOW
  } else {
    tabActive.value = TabType.NORMAL
  }
  getFollowList()
  popup.value.open('bottom')
}

function getFollowList() {
  _get({ url: '/follow/list', data: { followType: 'EMPLOYEE' } }).then((res: IResponseType<[]>) => {
    const arr =
      res.data?.map((v: { itemId; itemName }) => {
        return {
          id: v.itemId,
          nickName: v.itemName
        }
      }) ?? []
    followList.value = arr
    $store.commit('audit/setFollowList', arr)
  })
}

function handleCancel() {
  $store.dispatch('audit/updateCurrentFilter', '')
  popup.value?.close('bottom')
}
function handleReset() {
  $store.commit('audit/resetFilterData')
}

function handleConfirm() {
  if (tabActive.value === TabType.FOLLOW) {
    if (followList.value.length) {
      $store.commit('audit/setFollowChecked', true)
    }
    const followIds = followList.value.map((v) => v.id)
    $store.commit('audit/setFilterData', { submitUserIds: followIds.join(',') })
    _post({ url: '/follow/do', data: { followType: 'EMPLOYEE', followIds } })
  }
  emits('refresh')
  popup.value?.close('bottom')
}
function clearFilter() {
  emits('clearFilter')
}
function handleInputClear(key) {
  $store.commit('audit/setFilterData', { [key]: '' })
}
//时间处理
const startDatePicker = computed(() => {
  return getDate('start')
})
const endDatePicker = computed(() => {
  return getDate('end')
})
function getDate(type) {
  const date = new Date()
  let year = date.getFullYear()
  let month = date.getMonth() + 1
  let day = date.getDate()

  if (type === 'start') {
    year = year - 60
  } else if (type === 'end') {
    year = year + 2
  }
  return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
}

defineExpose({
  open
})

function bindDateChangeStart(e) {
  $store.commit('audit/setFilterData', { startDate: e.detail.value })
}
function bindDateChangeEnd(e) {
  $store.commit('audit/setFilterData', { endDate: e.detail.value })
}
function changeProduct(val) {
  $store.commit('audit/setFilterData', { productNameOrCode: val?.itemName ?? '' })
}
function changeProcess(val) {
  $store.commit('audit/setFilterData', { processNameOrCode: val?.itemName ?? '' })
}
function changeStaff(val) {
  $store.commit('audit/setFilterData', { submitNickName: val?.nickName ?? '' })
}
function changeRemark(val) {
  $store.commit('audit/setFilterData', { remark: val || '' })
}

enum TabType {
  NORMAL = 1, // 筛选
  FOLLOW = 2 // 我关注的
}
const tabActive = ref(TabType.NORMAL)
const followList = ref([])
const checkFollow = computed(() => {
  return tabActive.value === TabType.FOLLOW
})
function handleTabChange(val) {
  tabActive.value = val
}

function handleChangeStaff() {
  popupStaff?.value?.open()
}

function handleUserSelect(user) {
  if (!followList.value.find((v) => v.id === user.id)) {
    followList.value.push(user)
  }
  $store.commit('audit/setFollowList', followList.value)
}

function handleUserDelete(index) {
  followList.value.splice(index, 1)
}
</script>

<style lang="scss" scoped>
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
