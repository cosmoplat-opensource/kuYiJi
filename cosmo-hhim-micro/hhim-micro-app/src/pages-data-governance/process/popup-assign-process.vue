<template>
  <uni-popup ref="popup" type="bottom" @change="handlePopupOpen" :safe-area="false">
    <view class="rounded-16-top bg-fff">
      <view class="flex-center relative h-96">
        <text class="color-5a6f82 font-28 bold">{{ warnNotice.title }}</text>
        <image @click="closePopup" src="/static/images/icon_close_666.svg" class="icon-48 icon-close" />
      </view>
      <data-notice-area :text="warnNotice.notice" />
      <view class="h-80 flex align-center pl-32 box b-b-1 border-f5f5f5">
        <h-text-display :text="dataItem.warnProductName" :width="300" class="ml-16 color-5a6f82 font-28 bold" />
        <text class="color-999 font-28 bold">({{ dataItem.warnProductCode }})</text>
      </view>
      <view class="mt-16 pl-48 flex">
        <h-exception-label :text="warnNotice.subTitle" border="detail" bg="#FE9F00" className="p-4" />
      </view>
      <scroll-view scroll-y enable-flex class="flex flex-col m-h-500 overflow-hidden">
        <view
          v-for="(item, index) in processList"
          :key="index"
          class="flex align-center h-96 px-16 mx-32 box"
          :class="index > 0 ? 'b-t-1 border-f5f5f5' : ''"
          @tap="handleProcessSelect(item)"
        >
          <h-radiobox :checked="item.checked" />
          <h-text-display :text="item.processName" class="ml-32 font-28 color-333 bold" :width="300" />
          <text class="font-32 color-999 bold">({{ item.processCode }})</text>
        </view>
      </scroll-view>
      <view class="p-32 flex-center">
        <h-button width="296" height="72" text="取消" type="bg-f3f3f5 color-333" @tap.stop="closePopup" />
        <h-button width="296" height="72" text="确认并指定" class="ml-32" @tap.stop="handleSubmit" />
      </view>
    </view>
    <h-status-footer backgroundColor="#ffffff" />
  </uni-popup>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

import HStatusFooter from '@/components/h-status-footer.vue'
import DataNoticeArea from '@/pages-data-governance/data-notice-area.vue'
import HTextDisplay from '@/components/h-text-display.vue'
import HExceptionLabel from '@/components/h-exception-label.vue'
import { _get } from '@/utils/common-request'
import HRadiobox from '@/components/h-radiobox.vue'

const emits = defineEmits(['handlePopupOpen', 'refreshList'])
const popup = ref(null)
const dataItem = ref({})
const warnType = ref(1)
function openEdit(item, type) {
  dataItem.value = item
  warnType.value = type
  popup.value.open('bottom')
  initProcessList()
}
// 监听弹窗开关
function handlePopupOpen(e) {
  emits('handlePopupOpen', e)
}
function closePopup() {
  popup.value?.close('bottom')
}
defineExpose({ openEdit })

const warnNotice = computed(() => {
  switch (warnType.value) {
    case 1:
      return {
        title: '指定首序',
        subTitle: '首序指定为',
        notice: '记工时，首工序无需记录前工序'
      }
    case 2:
    case 3:
    default:
      return {
        title: '指定尾序',
        subTitle: '尾序指定为',
        notice: '指定尾序后，方便统计产品的产出'
      }
  }
})

const processList = ref([])
function initProcessList() {
  let requestData = {
    productSeq: dataItem.value.warnProductSeq
  }
  if (warnType.value === 3) {
    requestData.isLastProcess = '0'
  }
  _get({ url: '/submit/allProcess', data: requestData }).then((res) => {
    processList.value = res.data.map((v) => {
      v.checked = false
      return v
    })
  })
}
const processSelect = ref({})
function handleProcessSelect(item) {
  processList.value.forEach((v) => (v.checked = false))
  Object.assign(item, { checked: true })
  processSelect.value = item
}
function handleSubmit() {
  const params = {
    productSeq: dataItem.value.warnProductSeq,
    processSeq: processSelect.value.processSeq,
    processWarnType: warnType.value
  }
  _get({ url: '/warn/product/assignProcess', data: params }).then((res) => {
    if (res.data) {
      uni.showToast({ title: '指定成功', icon: 'none' })
      setTimeout(() => {
        closePopup()
      }, 1500)
      uni.$emit('auditListRefresh')
      emits('refreshList')
    } else {
      uni.showModal({
        title: '提示',
        content: `该产品暂无 ${processSelect.value.processName} 工序的待审核记工信息,请稍后指定`,
        showCancel: false,
        confirmText: '确定',
        confirmColor: '#0066ff'
      })
    }
  })
}
</script>

<style lang="scss" scoped>
.product-mark {
  width: px2vw(56);
  height: px2vw(32);
  text-align: center;
  line-height: px2vw(32);
  background: #def2ff;
  border-radius: px2vw(4);
  border: px2vw(2) solid #64b5ea;
}
.icon-close {
  position: absolute;
  right: px2vw(24);
  top: px2vw(24);
}
</style>
