<template>
  <view class="register-tenant flex flex-col">
    <uni-nav-bar title="自助注册" />
    <view class="register-tenant__wrapper">
      <view class="header">
        <text class="title">{{ mode === 'create' ? '开通新租户' : '加入租户' }}</text>
        <text class="subtitle">已通过微信授权验证，填写信息即可</text>
      </view>

      <!-- 模式切换（segment） -->
      <view class="segment">
        <view
          :class="['segment-item', mode === 'create' ? 'segment-item--active' : '']"
          @tap="switchMode('create')"
        >开通新租户</view>
        <view
          :class="['segment-item', mode === 'invite' ? 'segment-item--active' : '']"
          @tap="switchMode('invite')"
        >用邀请码加入</view>
      </view>

      <!-- 模式 1: 开通新租户 -->
      <uni-forms v-if="mode === 'create'" ref="formCreateRef" :modelValue="formCreate" :rules="rulesCreate" label-width="0">
        <uni-forms-item name="tenantName" required>
          <view class="input-row">
            <text class="label">企业名称</text>
            <input
              v-model="formCreate.tenantName"
              maxlength="50"
              placeholder="您的企业 / 团队名称"
              placeholder-class="placeholder"
            />
          </view>
        </uni-forms-item>

        <uni-forms-item name="customerName" required>
          <view class="input-row">
            <text class="label"><text class="text-require">*</text>客户名称</text>
            <input
              v-model="formCreate.customerName"
              maxlength="50"
              placeholder="对外显示名（必填）"
              placeholder-class="placeholder"
            />
          </view>
        </uni-forms-item>
      </uni-forms>

      <!-- 模式 2: 邀请码加入 -->
      <uni-forms v-else ref="formInviteRef" :modelValue="formInvite" :rules="rulesInvite" label-width="0">
        <uni-forms-item name="inviteCode" required>
          <view class="input-row">
            <text class="label">邀请码</text>
            <input
              v-model="formInvite.inviteCode"
              maxlength="8"
              placeholder="6 位企业邀请码"
              placeholder-class="placeholder"
            />
          </view>
        </uni-forms-item>

        <uni-forms-item name="nickName" required>
          <view class="input-row">
            <text class="label">姓名</text>
            <input
              v-model="formInvite.nickName"
              maxlength="20"
              placeholder="您在企业中的姓名"
              placeholder-class="placeholder"
            />
          </view>
        </uni-forms-item>
      </uni-forms>

      <button class="submit-btn" :disabled="submitting" @tap="handleSubmit">
        {{ submitting ? '提交中…' : submitButtonText }}
      </button>

      <view class="footer">
        <text class="footer-tip">提交即视为同意</text>
        <text class="link" @tap="xieyi">《用户协议》</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { _post } from '@/utils/common-request'
import { $store, formatTabDefaultJump, getUserInfo } from '@/utils/common'

/**
 * decouple-from-ops-platform-cleanup (C.17)
 *
 * 双入口注册页：
 * 1) mode=create  开通新租户  → POST /login/register-tenant
 * 2) mode=invite  邀请码加入  → POST /login/register-tenant/invite
 *
 * 两种模式都依赖 sessionId（来自 wxMiniAppLogin 0-租户场景写下的 Redis session）。
 * session 内已存 phone + openid，后端不收 body 中的 phone，避免伪造。
 */

type Mode = 'create' | 'invite'

const mode = ref<Mode>('create')

const formCreate = reactive({
  tenantName: '',
  customerName: ''
})

const formInvite = reactive({
  inviteCode: '',
  nickName: ''
})

const rulesCreate = {
  tenantName: { rules: [{ required: true, errorMessage: '请输入企业名称' }] },
  customerName: { rules: [{ required: true, errorMessage: '请输入客户名称' }] }
}

const rulesInvite = {
  inviteCode: { rules: [{ required: true, errorMessage: '请输入邀请码' }] },
  nickName: { rules: [{ required: true, errorMessage: '请输入姓名' }] }
}

const submitting = ref(false)
const sessionId = ref('')

const submitButtonText = computed(() => (mode.value === 'create' ? '开通并登录' : '加入并登录'))

onLoad((options) => {
  if (options && options.sessionId) {
    sessionId.value = options.sessionId
  } else {
    console.warn('[register-tenant] 缺少 sessionId，进入 onShow 后再尝试从场景补登')
  }

  // 支持从 query 预填：mode=invite 场景下邀请码直接带入（来自扫码或邀请页跳转）
  if (options && options.mode === 'invite') {
    mode.value = 'invite'
  }
  if (options && options.inviteCode) {
    formInvite.inviteCode = String(options.inviteCode).trim()
    if (mode.value !== 'invite') mode.value = 'invite'
  }
  if (options && options.nickName) {
    formInvite.nickName = String(options.nickName).trim()
  }
})

function switchMode(target: Mode) {
  if (submitting.value) return
  mode.value = target
}

function readAppContext() {
  let applicationSign = 'micro_process'
  let platformType = 'wechatMiniApp'
  try {
    const accountInfo = uni.getAccountInfoSync()
    if (accountInfo && accountInfo.miniProgram && accountInfo.miniProgram.appId) {
      applicationSign = accountInfo.miniProgram.appId
    }
  } catch (e) {
    console.warn('[register] getAccountInfoSync 失败', e)
  }
  try {
    const sysInfo = uni.getSystemInfoSync()
    if (sysInfo && sysInfo.platform) {
      if (sysInfo.platform.indexOf('mp-weixin') >= 0 || sysInfo.platform.indexOf('weixin') >= 0) {
        platformType = 'wechatMiniApp'
      }
    }
  } catch (e) {
    console.warn('[register] getSystemInfoSync 失败', e)
  }
  return { applicationSign, platformType }
}

function handleSubmit() {
  if (submitting.value) return
  if (!sessionId.value) {
    uni.showToast({ title: '会话无效，请重新登录', icon: 'none' })
    setTimeout(() => uni.reLaunch({ url: '/pages-login/index' }), 1500)
    return
  }
  const { applicationSign, platformType } = readAppContext()

  if (mode.value === 'create') {
    submitCreate({ applicationSign, platformType })
  } else {
    submitInvite({ applicationSign, platformType })
  }
}

function submitCreate(ctx: { applicationSign: string; platformType: string }) {
  if (!formCreate.tenantName || !formCreate.tenantName.trim()) {
    uni.showToast({ title: '请输入企业名称', icon: 'none' })
    return
  }
  if (!formCreate.customerName || !formCreate.customerName.trim()) {
    uni.showToast({ title: '请输入客户名称', icon: 'none' })
    return
  }
  submitting.value = true
  _post({
    url: '/login/register-tenant',
    data: {
      sessionId: sessionId.value,
      tenantName: formCreate.tenantName,
      customerName: formCreate.customerName,
      applicationSign: ctx.applicationSign,
      platformType: ctx.platformType
    }
  })
    .then((res: IResponseType<ILogin>) => {
      submitting.value = false
      onRegisterSuccess(res)
    })
    .catch((err) => {
      submitting.value = false
      onRegisterFail(err)
    })
}

function submitInvite(ctx: { applicationSign: string; platformType: string }) {
  if (!formInvite.inviteCode || !formInvite.inviteCode.trim()) {
    uni.showToast({ title: '请输入邀请码', icon: 'none' })
    return
  }
  if (!formInvite.nickName || !formInvite.nickName.trim()) {
    uni.showToast({ title: '请输入姓名', icon: 'none' })
    return
  }
  submitting.value = true
  _post({
    url: '/login/register-tenant/invite',
    data: {
      sessionId: sessionId.value,
      inviteCode: formInvite.inviteCode,
      nickName: formInvite.nickName,
      applicationSign: ctx.applicationSign,
      platformType: ctx.platformType
    }
  })
    .then((res: IResponseType<ILogin>) => {
      submitting.value = false
      onRegisterSuccess(res)
    })
    .catch((err) => {
      submitting.value = false
      onRegisterFail(err)
    })
}

function onRegisterSuccess(res: IResponseType<ILogin>) {
  const { token, userName, tenantList } = res.data || {}
  if (!token) {
    uni.showToast({ title: '操作失败：未返回 token', icon: 'none' })
    return
  }
  uni.setStorageSync('micro_user', userName)
  uni.setStorageSync('micro_token', token)
  if (tenantList && tenantList.length > 0) {
    uni.setStorageSync('micro_tenantList', tenantList)
    const t = tenantList[0]
    uni.setStorageSync('micro_tenantCode', t.tenantCode)
    uni.setStorageSync('micro_tenantName', t.tenantName)
    uni.setStorageSync('micro_customerName', t.customerName || '')
  }
  $store.commit('user/setUserInfo', { token, userName, tenantList })
  getUserInfo(userName).then((info: IUserInfo) => {
    $store.commit('user/setUserInfo', { ...info })
    formatTabDefaultJump(info)
  })
  // 邀请码消费成功，清理临时存储
  uni.removeStorageSync('pendingInvite')
  uni.removeStorageSync('inviteCode')
  uni.removeStorageSync('roleCode')
  uni.showToast({ title: mode.value === 'create' ? '开通成功' : '加入成功', icon: 'success' })
}

function onRegisterFail(err: any) {
  const code = err && err.code
  const msg = err && err.msg
  if (code === 'SESSION_INVALID_OR_EXPIRED' || code === 401) {
    uni.showToast({ title: '会话已过期，请重新登录', icon: 'none' })
    setTimeout(() => uni.reLaunch({ url: '/pages-login/index' }), 1500)
  } else if (code === 'INVITE_CODE_INVALID' || code === 400) {
    uni.showToast({ title: '邀请码无效或已过期', icon: 'none' })
  } else if (code === 'TENANT_ALREADY_LINKED' || code === 409) {
    uni.showToast({ title: '您已属于该租户，请直接登录', icon: 'none' })
  } else if (msg) {
    uni.showToast({ title: String(msg), icon: 'none' })
  } else {
    uni.showToast({ title: '操作失败，请稍后再试', icon: 'none' })
  }
}

function xieyi() {
  uni.navigateTo({ url: '/pages-login/contractText?type=1' })
}
</script>

<style lang="scss" scoped>
.register-tenant {
  &__wrapper {
    padding: 32rpx 48rpx;
  }
  .header {
    margin: 24rpx 0 32rpx;
    .title {
      display: block;
      font-size: 48rpx;
      font-weight: 600;
      color: #1a1a1a;
    }
    .subtitle {
      display: block;
      font-size: 26rpx;
      color: #999;
      margin-top: 12rpx;
    }
  }
  .segment {
    display: flex;
    background: #f3f3f5;
    border-radius: 8rpx;
    padding: 6rpx;
    margin-bottom: 32rpx;
    .segment-item {
      flex: 1;
      text-align: center;
      padding: 18rpx 0;
      font-size: 28rpx;
      color: #666;
      border-radius: 6rpx;
      &--active {
        background: #fff;
        color: #0066ff;
        font-weight: 500;
        box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.06);
      }
    }
  }
  .input-row {
    display: flex;
    align-items: center;
    border-bottom: 1rpx solid #eee;
    padding: 24rpx 0;
    .label {
      width: 140rpx;
      font-size: 28rpx;
      color: #333;
    }
    input {
      flex: 1;
      font-size: 28rpx;
    }
    .placeholder {
      color: #b8b8b8;
    }
  }
  .submit-btn {
    margin-top: 64rpx;
    background: #0066ff;
    color: #fff;
    border-radius: 8rpx;
    font-size: 30rpx;
  }
  .footer {
    margin-top: 32rpx;
    text-align: center;
    font-size: 24rpx;
    color: #999;
    .link {
      color: #0066ff;
      margin-left: 8rpx;
    }
  }
}
</style>
