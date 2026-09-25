<!--
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 *
 * @author cosmo-hhim-open Team
 -->
<template>
  <view v-if="visible" class="privacy-consent">
    <view class="privacy-consent__mask" @tap.stop="preventClose" />
    <view class="privacy-consent__card">
      <view class="privacy-consent__header">
        <text class="privacy-consent__title">用户协议与隐私政策</text>
      </view>
      <view class="privacy-consent__body">
        <text class="privacy-consent__text">
          欢迎使用KU易记（以下简称"本产品"）。我们非常重视您的个人信息和隐私保护。为了更好地保障您的合法权益，在您使用本产品前，请您务必审慎阅读并充分理解
        </text>
        <text class="privacy-consent__link" @tap.stop="openContract('1')">《KU易记用户协议》</text>
        <text class="privacy-consent__text">和</text>
        <text class="privacy-consent__link" @tap.stop="openContract('2')">《KU易记隐私政策》</text>
        <text class="privacy-consent__text">
          的全部内容。如您同意上述协议内容，请点击"同意"按钮开始使用我们的服务。
        </text>
      </view>
      <view class="privacy-consent__footer">
        <view class="privacy-consent__btn privacy-consent__btn--disagree" @tap.stop="handleDisagree">
          <text>不同意</text>
        </view>
        <view class="privacy-consent__btn privacy-consent__btn--agree" @tap.stop="handleAgree">
          <text>同意</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const props = defineProps({
  requireToken: {
    type: Boolean,
    default: false
  }
})

const visible = ref(false)

onLoad(() => {
  // requireToken 为 true 时，需要 token 存在才弹窗（避免 main 页弹完又被跳转到登录页再弹一次）
  if (props.requireToken && !uni.getStorageSync('micro_token')) {
    return
  }
  const agreed = uni.getStorageSync('privacy_agreed')
  if (!agreed) {
    visible.value = true
  }
})

function preventClose() {
  // 不允许点击遮罩关闭
}

function handleAgree() {
  uni.setStorageSync('privacy_agreed', true)
  visible.value = false
}

function handleDisagree() {
  // #ifdef MP-WEIXIN
  uni.exitMiniProgram({
    success: () => {
      // 直接退出小程序
    }
  })
  // #endif
  // #ifndef MP-WEIXIN
  uni.showToast({ title: '您需要同意协议才能使用本产品', icon: 'none' })
  // #endif
}

function openContract(type: string) {
  uni.navigateTo({ url: '/pages-login/contractText?type=' + type })
}
</script>

<style lang="scss" scoped>
.privacy-consent {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;

  &__mask {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
  }

  &__card {
    position: relative;
    width: px2vw(600);
    max-height: px2vw(800);
    background-color: #fff;
    border-radius: px2vw(16);
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  &__header {
    padding: px2vw(40) px2vw(40) 0;
  }

  &__title {
    font-size: px2vw(32);
    font-weight: bold;
    color: #333;
    line-height: px2vw(44);
  }

  &__body {
    padding: px2vw(32) px2vw(40);
    flex: 1;
    overflow-y: auto;
    line-height: px2vw(44);
  }

  &__text {
    font-size: px2vw(28);
    color: #333;
    line-height: px2vw(44);
  }

  &__link {
    font-size: px2vw(28);
    color: #0066ff;
    line-height: px2vw(44);
  }

  &__footer {
    display: flex;
    justify-content: center;
    gap: px2vw(32);
    padding: px2vw(24) px2vw(40) px2vw(40);
  }

  &__btn {
    width: px2vw(240);
    height: px2vw(72);
    border-radius: px2vw(16);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: px2vw(28);

    &--disagree {
      background-color: #f3f3f5;
      color: #333;
    }

    &--agree {
      background-color: #004baa;
      color: #fff;
    }
  }
}
</style>

