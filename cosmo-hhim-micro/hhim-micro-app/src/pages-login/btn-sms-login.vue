<template>
  <view class="btn-sms-login">
    <!-- 手机号输入 -->
    <view class="sms-input-wrapper">
      <view class="sms-input-row">
        <text class="sms-input-label">手机号</text>
        <input
          v-model="phoneNumber"
          type="number"
          maxlength="11"
          placeholder="请输入手机号"
          class="sms-input"
          placeholder-class="color-b8b8b8"
        />
        <image
          v-if="phoneNumber"
          src="/static/images/icon_del_b6c0c9.svg"
          class="icon-48"
          @tap="handleClearPhone"
        />
      </view>
    </view>

    <!-- 验证码输入 -->
    <view class="sms-input-wrapper mt-24">
      <view class="sms-input-row">
        <text class="sms-input-label">验证码</text>
        <input
          v-model="smsCode"
          type="number"
          maxlength="6"
          placeholder="请输入验证码"
          class="sms-input sms-code-input"
          placeholder-class="color-b8b8b8"
        />
        <view
          :class="['sms-send-btn', countdown > 0 ? 'sms-send-btn--disabled' : '']"
          @tap="handleSendCode"
        >
          <text class="font-24">{{ sendBtnText }}</text>
        </view>
      </view>
    </view>

    <!-- 登录按钮 -->
    <view class="sms-login-btn mt-48" @tap="handleLogin">
      <text class="font-28 color-fff">{{ props.text }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { _get, _post } from '@/utils/common-request'
import { ref, computed } from 'vue'

const props = defineProps({
  text: {
    type: String,
    default: '登录'
  },
  textChecked: {
    type: Boolean,
    default: true
  }
})

const emits = defineEmits(['login'])

const phoneNumber = ref('')
const smsCode = ref('')
const countdown = ref(0)
let countdownTimer: number | null = null

const sendBtnText = computed(() => {
  if (countdown.value > 0) {
    return `${countdown.value}s后重发`
  }
  return '发送验证码'
})

/** 校验手机号 */
function isValidPhone(phone: string): boolean {
  return /^1[3-9]\d{9}$/.test(phone)
}

/** 清除手机号 */
function handleClearPhone() {
  phoneNumber.value = ''
}

/** 发送验证码 */
function handleSendCode() {
  if (countdown.value > 0) return

  if (!isValidPhone(phoneNumber.value)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }

  _post({ url: '/login/sms/sendCode', data: { phoneNumber: phoneNumber.value } })
    .then((res: IResponseType<unknown>) => {
      uni.showToast({ title: '验证码已发送', icon: 'success' })
      startCountdown()
    })
    .catch(() => {
      uni.showToast({ title: '发送失败，请稍后重试', icon: 'none' })
    })
}

/** 倒计时 */
function startCountdown() {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--
    } else {
      if (countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
    }
  }, 1000) as unknown as number
}

/** 登录 */
function handleLogin() {
  if (!props.textChecked) {
    uni.showToast({ title: '请先阅读并同意用户隐私协议', icon: 'none' })
    return
  }

  if (!isValidPhone(phoneNumber.value)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }

  if (!smsCode.value || smsCode.value.length < 4) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }

  emits('login', {
    phoneNumber: phoneNumber.value,
    smsCode: smsCode.value
  })
}

/** 组件卸载时清除定时器 */
import { onBeforeUnmount } from 'vue'
onBeforeUnmount(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})
</script>

<style lang="scss" scoped>
.btn-sms-login {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.sms-input-wrapper {
  width: px2vw(528);
}

.sms-input-row {
  display: flex;
  align-items: center;
  height: px2vw(80);
  border-bottom: px2vw(2) solid #e8ecf0;
  font-size: px2vw(28);
}

.sms-input-label {
  color: #333;
  width: px2vw(140);
  flex-shrink: 0;
}

.sms-input {
  flex: 1;
  height: 100%;
  font-size: px2vw(28);
  color: #333;

  /* #ifdef H5 */
  /* H5：uni-input 内部 input 撑满行高，观感与小程序一致 */
  :deep(.uni-input-input) {
    height: 100%;
    padding: 0;
    border: none;
    outline: none;
    background: transparent;
    font-size: px2vw(28);
    color: #333;
  }
  /* #endif */
}

.sms-code-input {
  /* 输入框宽度放宽到 300px，避免"请输入验证码"placeholder/6 位数字被发送验证码按钮遮挡（H5 + 小程序均生效） */
  max-width: px2vw(300);
  min-width: 0;

  /* #ifdef H5 */
  /* H5：HTML input 文字溢出不裁剪，让其撑满 300px */
  :deep(.uni-input-input) {
    width: 100%;
    min-width: 0;
    box-sizing: border-box;
  }
  /* #endif */
}

.sms-send-btn {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  height: px2vw(52);
  padding: 0 px2vw(20);
  border-radius: px2vw(26);
  background-color: #0066ff;
  color: #fff;
  font-size: px2vw(24);

  &:active {
    background-color: #0044e7;
  }

  &--disabled {
    background-color: #b8b8b8;
    &:active {
      background-color: #b8b8b8;
    }
  }
}

.sms-login-btn {
  width: px2vw(528);
  height: px2vw(88);
  background-color: #0066ff;
  border-radius: px2vw(44);
  display: flex;
  align-items: center;
  justify-content: center;

  &:active {
    background-color: #0044e7;
  }
}
</style>
