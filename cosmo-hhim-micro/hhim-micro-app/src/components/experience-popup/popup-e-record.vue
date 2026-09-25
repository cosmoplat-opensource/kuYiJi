<template>
  <uni-popup ref="popup" background-color="#00000000" :is-mask-click="false" class="rounded-16">
    <view class="popup-content relative">
      <image v-if="checkStep1" :src="formatImage('img_pop_jg')" class="popup-e-record__jg" />
      <image v-if="checkStep2 || checkStep3" :src="formatImage('img_pop_sh')" class="popup-e-record__sh" />
      <image v-if="checkStep4" :src="formatImage('img_pop_invite')" class="popup-e-record__invite" />
      <template v-if="checkStep4">
        <view class="notice-area flex flex-col">
          <view class="font-32 bold color-333">恭喜，你已完成了体验旅程！</view>
          <view class="mt-4 font-32 bold color-333">快邀请伙伴一起来体验吧~</view>
          <view class="flex align-center justify-center py-32">
            <h-button
              width="174"
              height="72"
              text="稍后邀请"
              type="bg-f3f3f5 color-333"
              @tap.stop="handleInviteCancel"
            />
            <h-button width="174" height="72" text="邀请伙伴" class="ml-32" @tap.stop="handleInviteConfirm" />
          </view>
        </view>
      </template>
      <template v-else>
        <view class="notice-title">{{ popupData.title }}</view>
        <view class="notice-time">
          <text>用时</text>
          <text class="color-19aa8d">{{ popupData.time }}</text>
          <text>分钟</text>
        </view>
        <view class="btn-back" @tap="handleJumpExperience" />
      </template>
      <h-button v-if="checkStep1" width="224" height="72" :text="`体验审产 ${timer}`" @tap.stop="handleJumpAudit" />
      <h-button v-if="checkStep2" width="296" height="72" :text="`体验库存调整 ${timer}`" @tap.stop="handleJumpAudit" />
      <h-button v-if="checkStep3" width="296" height="72" :text="`体验分析报表 ${timer}`" @tap.stop="handleJumpAudit" />
    </view>
  </uni-popup>
</template>

<script setup lang="ts">
// 弹窗
import { computed, ref, watch } from 'vue'
import { $state, $store } from '@/utils/common'
import { formatImage } from '@/utils/common'

import { _put } from '@/utils/common-request'

const popup = ref(null)

const popupData = computed(() => {
  return $state.popup.popupData
})

const timer = ref(5)
const tt = ref(null)
watch(
  () => popupData.value,
  (val) => {
    if (val?.title) {
      timer.value = checkStep1?.value ? 6 : 5
      popup?.value?.open()
      tt.value = setInterval(() => {
        if (!timer.value) {
          clearInterval(tt.value)
          handleJumpAudit()
        } else {
          timer.value--
        }
      }, 1000)
    } else {
      tt?.value && clearInterval(tt.value)
      popup?.value?.close()
    }
  },
  { deep: true }
)

const checkStep1 = computed(() => {
  return popupData.value?.title?.includes('记工')
})

const checkStep2 = computed(() => {
  return popupData.value?.title?.includes('审核')
})

const checkStep3 = computed(() => {
  return popupData.value?.title?.includes('库存变动')
})

const checkStep4 = computed(() => {
  return popupData.value?.title?.includes('体验旅程')
})

function handleJumpExperience() {
  $store.commit('popup/setPopupData', { title: '' })
  uni.navigateTo({ url: '/pages-experience/path/index' })
}

function handleJumpAudit() {
  initItem()
  if (checkStep1.value) {
    $store.commit('tabBar/setTabActiveByName', '审产')
    // 具体逻辑处理
    $store.commit('experience/updateStepIndex', 2)
    const item = $store.getters['experience/getCurrentStep']
    item.startDate = new Date().getTime()
    $store.commit('experience/updateStepItem', item)
    // 隐藏tabBar
    $store.commit('tabBar/setTabShow', false)
  } else if (checkStep2.value) {
    $store.commit('tabBar/setTabActiveByName', '车间库存')
    // 具体逻辑处理
    $store.commit('experience/updateStepIndex', 3)
    const item = $store.getters['experience/getCurrentStep']
    item.startDate = new Date().getTime()
    $store.commit('experience/updateStepItem', item)
    // 隐藏tabBar
    $store.commit('tabBar/setTabShow', false)
  } else if (checkStep3.value) {
    $store.commit('tabBar/setTabActiveByName', '分析')
    $store.commit('experience/updateStepIndex', 4)
    // 隐藏tabBar
    $store.commit('tabBar/setTabShow', false)
  }
}

function initItem() {
  tt?.value && clearInterval(tt.value)
  const item = $store.getters['experience/getCurrentStep']
  item.startDate = new Date().getTime()
  $store.commit('experience/updateStepItem', item)
}

// 邀请
function handleInviteCancel() {
  $store.commit('experience/setIsExperience', false)
  const item = $store.getters['experience/getCurrentStep']
  _put({
    url: '/experience/guide/updateStatus',
    data: { nodeCode: item.nodeCode, nodeStatus: 1, elapsedTime: 1 }
  })
  popup.value?.close()
  $store.commit('tabBar/setTabShow', true)
}
function handleInviteConfirm() {
  handleInviteCancel()
  uni.navigateTo({ url: '/pages-invite/index' })
}

function open() {
  popup?.value?.open()
}
defineExpose({
  open
})
</script>

<style lang="scss" scoped>
.popup-e-record__jg {
  width: px2vw(528);
  height: px2vw(379);
}
.popup-e-record__sh {
  width: px2vw(528);
  height: px2vw(379);
}
.popup-e-record__invite {
  width: px2vw(528);
  height: px2vw(490);
}
.notice-title {
  position: absolute;
  top: px2vw(146);
  left: px2vw(32);
  color: #333;
  font-weight: bold;
  font-size: px2vw(32);
}
.notice-time {
  position: absolute;
  top: px2vw(186);
  left: px2vw(32);
  color: #333;
  font-weight: bold;
  font-size: px2vw(32);
}
.btn-back {
  position: absolute;
  width: px2vw(70);
  height: px2vw(46);
  right: px2vw(16);
  top: px2vw(60);
}
.btn-next {
  position: absolute;
  bottom: px2vw(32);
  left: 50%;
  transform: translateX(-50%);
}
.notice-area {
  position: absolute;
  width: px2vw(464);
  top: px2vw(257);
  left: 50%;
  transform: translateX(-50%);
}
</style>
