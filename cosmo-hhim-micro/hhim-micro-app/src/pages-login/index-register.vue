<template>
  <view class="flex flex-col h-full bg-0066ff pt-128 box">
    <view
      class="flex-1 relative bg-f2f7ff bigBg flex flex-col align-center px-24 box"
      :style="{ 'background-image': 'url(' + formatImage('bg_xinxi_sy') + ')' }"
    >
      <view class="font-56 color-fff mt-48">免费试用KU易记</view>
      <view class="font-26 color-fff mt-40">*仅限企业用户填写</view>
      <!-- 主要内容 -->
      <view class="bg-fff rounded-16 mt-64 w-100">
        <view class="h-72 bg-fff3e0 font-24 color-eb7f00 flex-center rounded-16-top">所有信息全部加密，请放心填写</view>
        <view class="px-40 pt-48 pb-64 box">
          <view class="h-88 flex-ac font-26 border-2 border-cad7eb rounded-8 px-32 box">
            <view class="color-5a6f82 w-155 box"> <text class="color-ff0000">*</text>企业名称 </view>
            <input
              v-model="inviteItem.companyName"
              :disabled="isTrialExist"
              placeholder="请输入"
              class="flex-1 text-right"
              placeholder-class="color-b8b8b8"
            />
          </view>
          <view class="h-88 flex-ac font-26 border-2 border-cad7eb rounded-8 px-32 box mt-24">
            <view class="color-5a6f82 w-155 box"> <text class="color-ff0000">*</text>行业 </view>
            <picker
              @change="bindPickerChange($event, '1')"
              :range="industryList"
              :disabled="isTrialExistBase"
              mode="selector"
              class="flex-1 h-88 flex-ac relative"
            >
              <view
                v-if="inviteItem.industry"
                class="font-26 color-333 absolute absoluteTR w-100 h-100 flex-ac justify-end"
                >{{ inviteItem.industry }}</view
              >
              <view v-else class="font-26 color-b8b8b8 absolute absoluteTR w-100 h-100 flex-ac justify-end"
                >请选择</view
              >
            </picker>
          </view>
          <view class="h-88 flex-ac font-26 border-2 border-cad7eb rounded-8 px-32 box mt-24">
            <view class="color-5a6f82 w-155 box"> <text class="color-ff0000">*</text>企业人数 </view>
            <picker
              @change="bindPickerChange($event, '2')"
              :range="companyPersonList"
              :disabled="isTrialExistBase"
              mode="selector"
              class="flex-1 h-88 flex-ac relative"
            >
              <view
                v-if="inviteItem.enterpriseScale"
                class="font-26 color-333 absolute absoluteTR w-100 h-100 flex-ac justify-end"
                >{{ inviteItem.enterpriseScale }}</view
              >
              <view v-else class="font-26 color-b8b8b8 absolute absoluteTR w-100 h-100 flex-ac justify-end"
                >请选择</view
              >
            </picker>
          </view>
          <view class="h-88 flex-ac font-26 border-2 border-cad7eb rounded-8 px-32 box mt-24">
            <view class="color-5a6f82 w-155 box"> <text class="color-ff0000">*</text>角色 </view>
            <picker
              @change="bindPickerChange($event, '3')"
              :range="roleList"
              :disabled="isTrialExistBase"
              mode="selector"
              class="flex-1 h-88 flex-ac relative"
            >
              <view
                v-if="inviteItem.userRole"
                class="font-26 color-333 absolute absoluteTR w-100 h-100 flex-ac justify-end"
                >{{ inviteItem.userRole }}</view
              >
              <view v-else class="font-26 color-b8b8b8 absolute absoluteTR w-100 h-100 flex-ac justify-end"
                >请选择</view
              >
            </picker>
          </view>
          <view class="h-88 flex-ac font-26 border-2 border-cad7eb rounded-8 px-32 box mt-24">
            <view class="color-5a6f82 w-155 box"><text class="color-ff0000">*</text>姓名</view>
            <input
              v-model="inviteItem.userName"
              :disabled="isTrialExist"
              placeholder="请输入"
              class="flex-1 text-right"
              placeholder-class="color-b8b8b8"
            />
          </view>
        </view>
      </view>
      <h-button width="536" height="72" text="进入小程序" class="mt-80" @tap.stop="handleRegister" />
    </view>
    <h-status-footer />
  </view>
</template>

<script setup lang="ts">
import { _get, _post } from '@/utils/common-request'
import { reactive, ref } from 'vue'
import { $store, formatTabDefaultJump, getUserInfo, openBuyMini } from '@/utils/common'
import { onLoad, onShow, onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'
import { formatImage, setGio, EXPERIENCE_USERNAME, EXPERIENCE_PASSWORD } from '@/utils/common'
import HooksShare from '@/hooks/share'
import { log } from 'echarts/types/src/util/log'
const { shareTimeline, shareAppMessage } = HooksShare()
onShareAppMessage(() => shareAppMessage)
onShareTimeline(() => shareTimeline)

onLoad((options) => {
  inviteItem.phoneNumber = options.phoneNum
  handleLoginTypeChange()
})

// 登录成功逻辑处理
function loginSuccess(res) {
  const { token, userName } = res.data
  uni.setStorageSync('micro_user', userName)
  uni.setStorageSync('micro_token', token)
  $store.commit('user/setUserInfo', { token })
  btnLoading.value = true
  const params = EXPERIENCE_USERNAME
  getUserInfo(params).then((res: IUserInfo) => {
    $store.commit('user/setUserInfo', { ...res })
    formatTabDefaultJump(res)
  })
}

// 体验
const inviteItem = reactive({
  companyName: '',
  userName: '',
  userRole: '',
  phoneNumber: '',
  industry: '',
  enterpriseScale: ''
})

const btnLoading = ref(false)
function handleRegister() {
  if (btnLoading.value) return
  const { companyName, userName, enterpriseScale, industry, userRole } = inviteItem
  if (!companyName) {
    uni.showToast({ title: '请输入企业名称', icon: 'none' })
    return
  }
  if (!userName) {
    uni.showToast({ title: '请输入姓名', icon: 'none' })
    return
  }
  if (!enterpriseScale) {
    uni.showToast({ title: '请选择企业人数', icon: 'none' })
    return
  }
  if (!industry) {
    uni.showToast({ title: '请选择行业', icon: 'none' })
    return
  }
  if (!userRole) {
    uni.showToast({ title: '请选择角色', icon: 'none' })
    return
  }
  btnLoading.value = true
  // 体验
  if (isTrialExist.value) {
    trialLogin()
  } else {
    _post({ url: '/trial', data: inviteItem })
      .then((res) => {
        trialLogin()
      })
      .finally(() => {
        btnLoading.value = false
      })
  }
}

function trialLogin() {
  // #ifdef H5
  // H5 无微信 code，体验登录仅限微信小程序
  uni.hideLoading()
  btnLoading.value = false
  uni.showToast({ title: '体验登录请在微信小程序中使用', icon: 'none', duration: 2500 })
  return
  // #endif
  uni.login({
    provider: 'weixin',
    success: function (loginRes) {
      btnLoading.value = true
      _post({
        url: '/login/wxminiapp/login',
        data: {
          code: loginRes.code,
          username: EXPERIENCE_USERNAME,
          password: EXPERIENCE_PASSWORD
        }
      })
        .then((res) => {
          if (res.code === 411) {
            //到期提示
            uni.showModal({
              title: '提示',
              content: '您的试用已到期，可付费购买体验完整功能，如果您有其他问题咨询，请联系400-135-7277',
              showCancel: false,
              confirmText: '去购买',
              success: function (res) {
                if (res.confirm) {
                  openBuyMini()
                }
              }
            })
            return
          }

          loginSuccess(res)
        })
        .finally(() => {
          uni.hideLoading()
          btnLoading.value = false
        })
    }
  })
}

const isTrialExist = ref(false)
const isTrialExistBase = ref(false)
function handleLoginTypeChange() {
  inviteItem.companyName = ''
  inviteItem.userRole = ''
  _get({ url: `/trial/${inviteItem.phoneNumber}` }).then((res: IResponseType<IUserInfo>) => {
    if (res.data) {
      inviteItem.companyName = res.data.companyName
      inviteItem.userName = res.data.userName
      inviteItem.enterpriseScale = res.data.enterpriseScale
      inviteItem.industry = res.data.industry
      inviteItem.userRole = res.data.userRole
      if (inviteItem.companyName && inviteItem.userName) {
        isTrialExist.value = true
      }
      if (inviteItem.enterpriseScale && inviteItem.industry && inviteItem.userRole) {
        isTrialExistBase.value = true
      }
    }
  })
}
//行业
const industryList = ref(['金属加工', '机械设备制造', '生物制药', '食品加工', '橡胶和塑料制造', '其他'])
// 企业人数
const companyPersonList = ref(['50人以内', '50~100人', '100人以上'])
// 角色
const roleList = ref(['企业老板', '生产主管', '质量主管', '财务'])
function bindPickerChange(e, type) {
  switch (type) {
    case '1':
      inviteItem.industry = industryList.value[e.detail.value]
      break
    case '2':
      inviteItem.enterpriseScale = companyPersonList.value[e.detail.value]
      break
    case '3':
      inviteItem.userRole = roleList.value[e.detail.value]
      break
    default:
      break
  }
}
</script>

<style lang="scss" scoped>
.bigBg {
  background-size: 100%;
  background-repeat: no-repeat;
}
.absoluteTR {
  top: 0;
  right: 0;
}
</style>
