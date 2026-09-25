<template>
  <view class="bg-F3F3F5 h-full">
    <uni-nav-bar />
    <h-status-header title="设置" />
    <view class="rounded-16 bg-fff font-28 ml-16 mr-16 mt-16 pl-32 pr-24 box">
      <view class="flex flex-col align-start border-bottom-f5f5f5 pt-32 pb-24 box" v-if="!getOFFICIAL">
        <view class="w-100 flex-1 flex justify-between align-center">
          <image :src="formatImage('icon_vip', 'svg')" class="icon-48 mr-16" />
          <text>开启正式使用</text>
          <view class="flex-1" />
          <h-button
            type="border-1 border-0066ff color-0066ff bg-fff"
            font="font-24"
            height="48"
            width="80"
            text="开启"
            @tap="open"
          />
        </view>
        <text class="color-e7a11a font-24 mt-14 ml-64">开启时，可选择保留或清除体验数据</text>
      </view>
      <view class="flex flex-col align-start border-bottom-f5f5f5 pt-32 pb-24 box">
        <view class="w-100 flex-1 flex justify-between align-center">
          <image :src="formatImage('icon_setting_jgsj', 'svg')" class="icon-48 mr-16" />
          <text>记工送检</text>
          <view class="mark-ebf0f5 ml-32">{{ checkSubmitInspect ? '启用中' : '已停用' }}</view>
          <view class="flex-1" />
          <h-button
            v-if="checkSubmitInspect"
            type="border-1 border-b6c0c9 color-5a6f82 bg-fff"
            font="font-24"
            active="grey"
            width="80"
            height="48"
            text="停用"
            @tap="handleConfigChange('submitInspectSwitch')"
          />
          <h-button
            v-else
            type="border-1 border-0066ff color-0066ff bg-fff"
            font="font-24"
            width="80"
            height="48"
            text="启用"
            @tap="handleConfigChange('submitInspectSwitch')"
          />
        </view>
        <view class="line-height-36 mt-14 pl-64">
          <text class="color-e7a11a font-24">工人记工后可送检，质检人员检测并判定是否不良，并针对不良品进行管理</text>
        </view>
      </view>
      <view class="flex flex-col align-start border-bottom-f5f5f5 pt-32 pb-24 box">
        <view class="w-100 flex-1 flex justify-between align-center">
          <image :src="formatImage('icon_pljg', 'svg')" class="icon-48 mr-16" />
          <text>批量记工</text>
          <view class="mark-ebf0f5 ml-32">{{ checkBatchSubmit ? '启用中' : '已停用' }}</view>
          <view class="flex-1" />
          <h-button
            v-if="checkBatchSubmit"
            type="border-1 border-b6c0c9 color-5a6f82 bg-fff"
            font="font-24"
            active="grey"
            width="80"
            height="48"
            text="停用"
            @tap="handleConfigChange('batchSubmitSwitch')"
          />
          <h-button
            v-else
            type="border-1 border-0066ff color-0066ff bg-fff"
            font="font-24"
            width="80"
            height="48"
            text="启用"
            @tap="handleConfigChange('batchSubmitSwitch')"
          />
        </view>
        <view class="line-height-36 mt-14 pl-64">
          <text class="color-e7a11a font-24"> 启用批量记工后，工人可一次记工多个产品的多道工序的产出 </text>
        </view>
      </view>
      <view class="flex flex-col align-start border-bottom-f5f5f5 pt-32 pb-24 box">
        <view class="w-100 flex-1 flex justify-between align-center">
          <image :src="formatImage('icon_add_forbid', 'svg')" class="icon-48 mr-16" />
          <view>产品、工序新增限制</view>
          <view class="mark-ebf0f5 ml-32">{{ checkWorkerBaseDataConfine ? '启用中' : '已停用' }}</view>
          <view class="flex-1" />
          <h-button
            type="border-1 border-0066ff color-0066ff bg-fff"
            font="font-24"
            height="48"
            width="80"
            :text="checkWorkerBaseDataConfine ? '停用' : '启用'"
            @tap="handleConfigChange('workerBaseDataConfine')"
          />
        </view>
        <view class="line-height-36 mt-14 pl-64">
          <text class="color-e7a11a font-24">启用此限制后，工人记工时不可新增产品、工序数据</text>
        </view>
      </view>
      <view class="flex align-center border-bottom-f5f5f5 h-112">
        <image src="/pages-my-center/static/images/icon_research.svg" class="icon-48 mr-16" />
        <text class="flex-1">企业调研</text>
        <h-button
          type="border-1 border-0066ff color-0066ff bg-fff"
          font="font-24"
          height="48"
          width="80"
          text="进入"
          @tap="handleEnter"
        />
      </view>
      <!-- 解散企业（仅管理员可见） -->
      <view v-if="Role_Admin" class="flex flex-col align-start pt-32 pb-24 box">
        <view class="w-100 flex-1 flex justify-between align-center">
          <image src="/pages-my-center/static/images/icon_safe.svg" class="icon-48 mr-16" />
          <text class="color-ff0000">解散企业</text>
          <view class="flex-1" />
          <h-button
            type="border-1 border-ff0000 color-ff0000 bg-fff"
            font="font-24"
            height="48"
            width="80"
            text="解散"
            @tap="handleDissolveOpen"
          />
        </view>
        <view class="line-height-36 mt-14 pl-64">
          <text class="color-ff0000 font-24">解散后所有数据无法恢复，请谨慎操作</text>
        </view>
      </view>
    </view>
    <uni-popup ref="popup" type="bottom">
      <view class="w-full h-full flex align-center justify-center">
        <view class="open-service bg-fff rounded-16 px-24 py-32 box flex flex-col justify-start">
          <view class="flex align-center">
            <image src="/pages-my-center/static/images/icon_safe.svg" class="icon-32 mr-8" />
            <text class="font-24 color-e7a11a">开启更好的服务</text>
            <view class="flex-1"></view>
            <image @tap="handleCancel" src="/static/images/icon_close_666.svg" class="icon-48 mr-8" />
          </view>
          <text class="font-28 mt-12 ml-40">开启时，是否保留体验数据？</text>
          <view class="font-28 flex align-center justify-start ml-40 mt-40">
            <view class="flex align-center" @tap="handleChange(false)">
              <h-radio-box class="mr-24" :checked="!isClear" />
              <text :class="!isClear ? 'color-19aa8d' : ''">保留</text>
            </view>
            <view class="flex align-center ml-48" @tap="handleChange(true)">
              <h-radio-box class="mr-24" :checked="isClear" />
              <text :class="isClear ? 'color-ff0000' : ''">清除</text>
            </view>
          </view>
          <view class="flex justify-center mt-40">
            <h-button width="120" height="72" text="取消" type="bg-f3f3f5 color-333" @tap.stop="handleCancel" />
            <h-button width="204" height="72" text="确认并开启" class="ml-32" @tap.stop="handleOpen" />
          </view>
        </view>
      </view>
    </uni-popup>
    <!-- 解散企业确认弹窗 -->
    <uni-popup ref="dissolvePopup" type="center">
      <view class="w-full h-full flex align-center justify-center">
        <view class="dissolve-dialog bg-fff rounded-16 px-24 py-32 box flex flex-col">
          <view class="flex align-center">
            <text class="font-32 bold">解散企业确认</text>
            <view class="flex-1" />
            <image @tap="handleDissolveCancel" src="/static/images/icon_close_666.svg" class="icon-48" />
          </view>
          <text class="font-26 color-ff0000 mt-16">此操作不可逆！解散后企业所有用户将无法登录。</text>
          <text class="font-26 mt-8">请输入企业名称确认：</text>
          <input
            v-model="dissolveInputName"
            placeholder="请输入企业名称"
            class="dissolve-input mt-16"
            placeholder-class="color-b8b8b8"
          />
          <view class="flex justify-center mt-32">
            <h-button width="120" height="72" text="取消" type="bg-f3f3f5 color-333" @tap.stop="handleDissolveCancel" />
            <h-button
              width="204"
              height="72"
              text="确认解散"
              class="ml-32"
              type="bg-ff0000 color-fff"
              :disabled="dissolveLoading"
              @tap.stop="handleDissolveConfirm"
            />
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { $state, $store, formatImage, setGio, Role_Admin } from '@/utils/common'

import HRadioBox from '@/components/h-radiobox.vue'
import { _post } from '@/utils/common-request'
import HooksSettingConfig from '@/hooks/setting-config'
const popup = ref(null)
const isClear = ref(false) //是否清除数据

// 解散企业
const dissolvePopup = ref(null)
const dissolveInputName = ref('')
const dissolveLoading = ref(false)
function handleDissolveOpen() {
  dissolveInputName.value = ''
  dissolvePopup.value?.open('center')
}
function handleDissolveCancel() {
  dissolvePopup.value?.close()
}
function handleDissolveConfirm() {
  const tenantName = uni.getStorageSync('micro_tenantName') || ''
  if (!dissolveInputName.value) {
    uni.showToast({ title: '请输入企业名称', icon: 'none' })
    return
  }
  if (dissolveInputName.value !== tenantName) {
    uni.showToast({ title: '企业名称不匹配', icon: 'none' })
    return
  }
  dissolveLoading.value = true
  _post({ url: '/login/tenant/dissolve' })
    .then(() => {
      uni.showToast({ title: '企业已解散', icon: 'none', duration: 2000 })
      // 清除本地缓存并跳转登录页
      setTimeout(() => {
        uni.removeStorageSync('micro_token')
        uni.removeStorageSync('micro_user')
        uni.removeStorageSync('micro_tenantCode')
        uni.removeStorageSync('micro_tenantName')
        uni.removeStorageSync('micro_customerName')
        uni.reLaunch({ url: '/pages-login/index' })
      }, 2000)
    })
    .catch(() => {
      uni.showToast({ title: '解散失败，请稍后重试', icon: 'none' })
    })
    .finally(() => {
      dissolveLoading.value = false
      dissolvePopup.value?.close()
    })
}
// 是否开启正式使用
const getOFFICIAL = computed(() => $state.experience.guideCode === 'OFFICIAL')
function open() {
  popup.value.open('bottom')
}
function handleCancel() {
  popup.value?.close('bottom')
}
function handleOpen() {
  _post({ url: '/setting/officialUse', data: { clearData: isClear.value } })
    .then((res) => {
      $store.commit('experience/setGuideCode', 'OFFICIAL')
      uni.showToast({ title: '开启成功', icon: 'none' })
    })
    .finally(() => {
      popup.value?.close('bottom')
    })
}
function handleEnter() {
  uni.navigateTo({ url: '/pages-guide/index' })
}
function handleChange(res) {
  if (!res) {
    setGio('jr_use_bl')
  } else {
    setGio('jr_use_sc')
  }
  isClear.value = res
}

// -----------------------企业个人信息设置-------------------------
const { handleConfigChange, checkSubmitInspect, checkBatchSubmit, checkWorkerBaseDataConfine } = HooksSettingConfig()
</script>

<style lang="scss" scoped>
.open-service {
  width: px2vw(528);
}
.btn {
  height: px2vw(48);
  line-height: px2vw(48);
  padding-left: px2vw(16);
  padding-right: px2vw(16);
  box-sizing: border-box;
  background: rgba(61, 130, 234, 0);
  border-radius: px2vw(32);
  opacity: 1;
  border: px2vw(2) solid #0066ff;
  &:active {
    background-color: #0066ff;
    color: #ffffff;
  }
}
.dissolve-input {
  width: 100%;
  height: px2vw(72);
  border: px2vw(2) solid #e8ecf0;
  border-radius: px2vw(8);
  padding: 0 px2vw(16);
  font-size: px2vw(28);
  box-sizing: border-box;
}
.dissolve-dialog {
  width: px2vw(560);
}
</style>
