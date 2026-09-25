<template>
  <view class="login-content flex flex-col">
    <uni-nav-bar />
    <view class="flex-1 relative">
      <view class="p-16 back-area">
        <image
          :src="formatImage('icon_return', 'svg')"
          class="icon-48"
          @tap="handleBack"
          v-if="loginType === LOGIN_TYPE.CODE"
        />
      </view>
      <view class="login-content__wrapper">
        <image :src="formatImage('logo_hyzz', 'svg')" class="img-logo" />
        <!--常规登录-->
        <template v-if="loginType === LOGIN_TYPE.NORMAL">
          <view class="phone-title-area">
            <text class="font-40 color-0066ff bold">KU易记</text>
          </view>
          <!-- 手机号一键登录（微信授权，仅小程序/App 可用） -->
          <!-- #ifndef H5 -->
          <template v-if="loginMode === LOGIN_MODE.PHONE">
            <btn-phone
              class="login-content__btn-phone"
              :text="loginBtnText"
              :textChecked="textChecked"
              @getPhoneCode="getPhoneCode"
            />
          </template>
          <!-- #endif -->
          <!-- 验证码登录 -->
          <!-- #ifndef H5 -->
          <template v-else>
            <btn-sms-login
              class="login-content__btn-phone"
              text="登录"
              :textChecked="textChecked"
              @login="handleSmsLogin"
            />
          </template>
          <!-- #endif -->
          <!-- #ifdef H5 -->
          <btn-sms-login
            class="login-content__btn-phone"
            text="登录"
            :textChecked="textChecked"
            @login="handleSmsLogin"
          />
          <!-- #endif -->
          <!-- 登录方式切换（H5 仅验证码登录，隐藏切换入口） -->
          <!-- #ifndef H5 -->
          <view class="login-mode-switch" @tap="handleToggleLoginMode">
            <text class="color-0066ff font-26">
              {{ loginMode === LOGIN_MODE.PHONE ? '验证码登录' : '手机号一键登录' }}
            </text>
          </view>
          <!-- #endif -->
        </template>
        <template v-else>
          <view class="mt-48" />
          <view class="mt-48 w-528 flex align-center" v-if="inviteItem.roleCode">
            <view class="mark-24a8ff">角色</view>
            <view class="color-24a8ff font-28 px-8">{{ getRoleList(inviteItem.roleCode) }}</view>
          </view>
          <view class="w-528 h-80 flex align-center mt-48 font-28 b-b-1 border-fff">
            <view class="text-require color-333 w-155 pl-16 box">
              <text>{{ loginType === LOGIN_TYPE.EXPERIENCE ? '企业名称' : '邀请码' }}</text>
            </view>
            <input
              v-model="inviteItem.inviteCode"
              :disabled="isTrialExist"
              :placeholder="loginType === LOGIN_TYPE.EXPERIENCE ? '输入企业名称' : '输入企业邀请码'"
              class="flex-1"
              placeholder-class="color-b8b8b8"
            />
            <image
              v-if="inviteItem.inviteCode && !isTrialExist"
              src="/static/images/icon_del_b6c0c9.svg"
              class="icon-48"
              @tap="handleInputClear('inviteCode')"
            />
          </view>
          <view class="w-528 h-80 flex align-center mt-66 font-28 b-b-1 border-fff">
            <view class="text-require color-333 w-155 pl-16 box">姓名</view>
            <input
              v-model="inviteItem.nickName"
              :disabled="isTrialExist"
              placeholder="输入姓名"
              class="flex-1"
              placeholder-class="color-b8b8b8"
            />
            <image
              v-if="inviteItem.nickName && !isTrialExist"
              src="/static/images/icon_del_b6c0c9.svg"
              class="icon-48"
              @tap="handleInputClear('nickName')"
            />
          </view>
          <h-button width="536" height="72" text="进入小程序" class="mt-80" @tap.stop="handleRegister" />
        </template>
        <!-- 隐私协议 -->
        <view v-if="loginType === LOGIN_TYPE.NORMAL" class="flex align-top justify-center px-96 mt-80">
          <view
            :class="[
              'icon-24 rounded-4 box shrink-0 flex justify-center align-center mt-4',
              textChecked ? 'border-0 bg-0066ff' : 'border-1-9BADBC bg-f3f3f5'
            ]"
            @tap="handleChecked"
            ><image src="/static/images/icon_checked_20.svg" mode="scaleToFill" class="icon-14" v-if="textChecked"
          /></view>
          <view class="font-24 ml-16 line-height-36">
            我已阅读并同意
            <text class="color-0066ff" @tap="xieyi('1')">《COSMOPlat用户协议》</text>
            和
            <text class="color-0066ff" @tap="xieyi('2')">《COSMOPlat隐私权协议》</text>
          </view>
        </view>
        <view class="flex-1" />
        <view class="bottom-notice">
          <text>卡奥斯</text>
          <text class="font-bold">COSMO</text>
          <text>Plat</text>
        </view>
        <h-status-footer />
      </view>
      <image :src="formatImage('img_login')" class="login-bg" />
    </view>
  </view>
  <h-share />
</template>

<script setup lang="ts">
import BtnPhone from './btn-phone.vue'
import BtnSmsLogin from './btn-sms-login.vue'
import { _get, _post } from '@/utils/common-request'
import { reactive, ref } from 'vue'
import { $store, formatTabDefaultJump, getUserInfo } from '@/utils/common'
import { showTenantPicker } from '@/components/tenant-picker'

import { onLoad, onShow, onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'
import { getRoleList } from '@/utils/rule'
import { formatImage, setGio, EXPERIENCE_USERNAME, EXPERIENCE_PASSWORD } from '@/utils/common'
import HooksShare from '@/hooks/share'
const { shareTimeline, shareAppMessage } = HooksShare()
onShareAppMessage(() => shareAppMessage)
onShareTimeline(() => shareTimeline)

const enum LOGIN_TYPE {
  NORMAL = 'normal', //常规登录
  CODE = 'code', //邀请码
  EXPERIENCE = 'experience' //体验
}

const enum LOGIN_MODE {
  PHONE = 'phone', //手机号一键登录
  SMS = 'sms' //验证码登录
}

// 登录页面切换
const loginType = ref<String>(LOGIN_TYPE.NORMAL)

// 登录方式切换（一键登录 / 验证码登录）
// H5 无微信环境，默认且仅支持验证码登录
// #ifdef H5
const loginMode = ref<String>(LOGIN_MODE.SMS)
// #endif
// #ifndef H5
const loginMode = ref<String>(LOGIN_MODE.PHONE)
// #endif
function handleToggleLoginMode() {
  loginMode.value = loginMode.value === LOGIN_MODE.PHONE ? LOGIN_MODE.SMS : LOGIN_MODE.PHONE
}

function handleBack() {
  loginType.value = LOGIN_TYPE.NORMAL
}

const accountShow = ref(true)
const loginBtnText = ref('手机号一键登录')
onLoad((options) => {
  if (options.scene) {
    accountShow.value = false
    const scene = decodeURIComponent(options.scene)
    const arr = scene.split('&')
    const obj = { code: '', role: '' }
    arr.forEach((v) => {
      obj[v.split('=')[0]] = v.split('=')[1]
    })
    uni.setStorageSync('inviteCode', obj.code)
    uni.setStorageSync('roleCode', obj.role)
    $store.commit('user/setUserInfo', {
      roleCode: obj.role
    })
    if (obj.role === '40') {
      loginBtnText.value = '立即体验'
    }
  } else {
    uni.setStorageSync('inviteCode', '')
    uni.setStorageSync('roleCode', '')
  }
})

// 获取手机号code
const phoneLoginCode = ref('')
function getPhoneCode(phoneCode) {
  // #ifdef H5
  // H5 无微信授权能力，降级提示（按钮已隐藏，此为兜底）
  uni.showToast({ title: '该功能仅在微信小程序可用', icon: 'none' })
  return
  // #endif
  phoneLoginCode.value = phoneCode
  uni.showLoading({ title: '登录中...' })
  if (loginBtnText.value === '立即体验') {
    _get({ url: '/login/codeToPhone', data: { phoneCode } })
      .then((res: IResponseType<unknown>) => {
        inviteItem.phoneNum = res.msg || ''
        uni.setStorageSync('experiencePhoneNum', inviteItem.phoneNum)
        handleLoginTypeChange()
      })
      .finally(() => {
        uni.hideLoading()
      })
  } else {
    uni.login({
      provider: 'weixin',
      success: function (loginRes) {
        //清除体验手机号
        uni.removeStorageSync('experiencePhoneNum')
        _post({ url: '/login/wxminiapp/login', data: { code: loginRes.code, phoneCode } })
          .then((res: IResponseType<ILogin>) => {
            handleWxLoginResponse(res)
          })
          .catch((err) => {
            if (err && (err.msg === 'NO_TENANT' || err.code === 'NO_TENANT')) {
              const sessionId = err.sessionId || err.tempToken || ''
              uni.showModal({
                title: '提示',
                content: '您尚未关联任何租户，请先注册新租户',
                confirmText: '去注册',
                success: (modal) => {
                  if (modal.confirm) {
                    goRegisterTenant(sessionId)
                  }
                }
              })
            } else {
              uni.showToast({
                title: (err && err.msg) ? err.msg : '登录失败，请重试',
                icon: 'none'
              })
            }
          })
          .finally(() => {
            uni.hideLoading()
          })
      },
      fail: (err) => {
        console.error('[wxLogin] uni.login failed', err)
        uni.showToast({ title: '微信登录失败，请重试', icon: 'none' })
      },
      complete: () => {
        uni.hideLoading()
      }
    })
  }
}

// 验证码登录
// 小程序/App：uni.login 拿微信 code 传给后端；H5：无微信环境，code 传空字符串
function handleSmsLogin({ phoneNumber, smsCode }) {
  uni.showLoading({ title: '登录中...' })
  // #ifdef H5
  uni.removeStorageSync('experiencePhoneNum')
  _post({
    url: '/login/sms/login',
    data: { code: '', phoneNumber, smsCode, isH5: true }
  })
    .then((res: IResponseType<ILogin>) => processSmsLoginResult(res))
    .catch((err) => processSmsLoginError(err))
    .finally(() => {
      uni.hideLoading()
    })
  // #endif
  // #ifndef H5
  uni.login({
    provider: 'weixin',
    success: function (loginRes) {
      uni.removeStorageSync('experiencePhoneNum')
      _post({
        url: '/login/sms/login',
        data: { code: loginRes.code, phoneNumber, smsCode }
      })
        .then((res: IResponseType<ILogin>) => processSmsLoginResult(res))
        .catch((err) => processSmsLoginError(err))
        .finally(() => {
          uni.hideLoading()
        })
    },
    fail: () => {
      uni.hideLoading()
      uni.showToast({ title: '微信登录失败，请重试', icon: 'none' })
    }
  })
  // #endif
}

function processSmsLoginResult(res: IResponseType<ILogin>) {
  handleWxLoginResponse(res)
}

function processSmsLoginError(err) {
  if (err && (err.msg === 'NO_TENANT' || err.code === 'NO_TENANT')) {
    const sessionId = err.sessionId || err.tempToken || ''
    uni.showModal({
      title: '提示',
      content: '您尚未关联任何租户，请先注册新租户',
      confirmText: '去注册',
      success: (modal) => {
        if (modal.confirm) {
          goRegisterTenant(sessionId)
        }
      }
    })
  } else {
    uni.showToast({
      title: (err && err.msg) ? err.msg : '登录失败，请重试',
      icon: 'none'
    })
  }
}

// 统一处理微信登录响应（一键登录 & 验证码登录共用）
function handleWxLoginResponse(res: IResponseType<ILogin>) {
  // 请求本身失败（code2Session 502 / 验证码 401 等）→ 提示错误，不要误判为"需注册"跳注册页
  if (res.code !== 200 || !res.data) {
    const msg = (res as any).msg || ''
    // 后端技术性报错（code2Session 失败等）转成对用户友好的提示
    const friendly =
      msg.indexOf('WECHAT_') === 0 || msg.indexOf('code2Session') >= 0
        ? '微信登录失败，请稍后重试'
        : msg || '登录失败，请重试'
    uni.showToast({ title: friendly, icon: 'none' })
    return
  }
  const { token, openId, phoneNumber, multiTenant, tempToken, tenantList } = res.data
  // decouple C.16: 后端 0 租户场景返 200 + multiTenant:false + tempToken(<sessionId>)
  if (multiTenant === false && !token && tempToken) {
    goRegisterTenant(tempToken)
    return
  }
  // 兜底
  if ((res as any).code === 404 || (res as any).msg === 'USER_NOT_FOUND') {
    console.warn('[wxLogin] ← USER_NOT_FOUND')
    uni.showToast({ title: '账号信息异常，请联系管理员', icon: 'none' })
    return
  }
  // 多租户：无 token 但有 tempToken + tenantList → 走 loginSuccess 弹 picker
  if (multiTenant && tempToken) {
    loginSuccess(res)
    return
  }
  if (!token) {
    const sceneInviteCode = uni.getStorageSync('inviteCode')
    const sceneRoleCode = uni.getStorageSync('roleCode')
    if (sceneInviteCode) {
      inviteItem.openId = openId
      inviteItem.phoneNum = phoneNumber
      loginType.value = LOGIN_TYPE.CODE
      inviteItem.inviteCode = sceneInviteCode
      inviteItem.roleCode = sceneRoleCode
    } else {
      console.warn('[wxLogin] ← 异常：未返 token + 无 inviteCode + 无 sessionId')
      goRegisterTenant('')
    }
  } else {
    loginSuccess(res)
  }
}
onShow(() => {
  setGio('jr_logon')
})

// decouple-from-ops-platform-cleanup: 0-租户场景统一跳转到 register-tenant 双入口页面
function goRegisterTenant(sessionId: string) {
  // 收集待带入 register-tenant 的信息：
  // 1) 来自扫码 scene 的 inviteCode（onLoad 写入 storage）
  // 2) 来自 handleRegister 手动输入的 pendingInvite（含 inviteCode / nickName / roleCode）
  const sceneInviteCode = uni.getStorageSync('inviteCode')
  const pendingInvite = uni.getStorageSync('pendingInvite') as
    | { inviteCode: string; nickName: string; roleCode: string }
    | undefined
  const finalInviteCode = pendingInvite?.inviteCode || sceneInviteCode

  const params: string[] = []
  if (sessionId) params.push(`sessionId=${encodeURIComponent(sessionId)}`)
  if (finalInviteCode) {
    params.push('mode=invite')
    params.push(`inviteCode=${encodeURIComponent(finalInviteCode)}`)
  }
  if (pendingInvite?.nickName) {
    params.push(`nickName=${encodeURIComponent(pendingInvite.nickName)}`)
  }
  if (pendingInvite?.roleCode) {
    params.push(`roleCode=${encodeURIComponent(pendingInvite.roleCode)}`)
  }
  const url = `/pages-login/register-tenant/index${params.length ? '?' + params.join('&') : ''}`
  uni.navigateTo({ url })
}

// 登录成功逻辑处理
// decouple-from-ops-platform-cleanup (B.3): 处理新 multiTenant 响应
function loginSuccess(res) {
  const { token, userName, multiTenant, tempToken, tenantList, openId, phoneNumber } = res.data

  // 0 租户：服务端通常会返回 403，但兼容后端没拦截的情况
  if (multiTenant === false && (!tenantList || tenantList.length === 0)) {
    uni.showModal({
      title: '提示',
      content: '您尚未关联任何租户，请注册新租户',
      confirmText: '去注册',
      success: (modal) => {
        if (modal.confirm) {
          goRegisterTenant(tempToken || '')
        }
      }
    })
    return
  }

  // 多租户：弹 picker → 调 /login/select-tenant
  if (multiTenant && tempToken && tenantList && tenantList.length > 1) {
    showTenantPicker(tenantList).then((picked) => {
      if (!picked) {
        uni.showToast({ title: '请选择租户', icon: 'none' })
        return
      }
      _post({ url: '/login/select-tenant', data: { tempToken, tenantCode: picked.tenantCode } })
        .then((selectRes: IResponseType<ILogin>) => {
          finalizeLogin(selectRes.data, picked)
        })
        .catch((err) => {
          if (err && err.code === 'TEMP_TOKEN_INVALID') {
            uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
          } else if (err && err.code === 'TENANT_NOT_LINKED') {
            uni.showToast({ title: '租户已变更，请重新登录', icon: 'none' })
            // 重新跑 wxMiniAppLogin
            setTimeout(() => {
              uni.reLaunch({ url: '/pages-login/index' })
            }, 1500)
          } else {
            uni.showToast({ title: '选择租户失败', icon: 'none' })
          }
        })
    })
    return
  }

  // 单租户：直接进
  finalizeLogin({ token, userName, tenantList }, tenantList && tenantList[0])
}

function finalizeLogin(data, pickedTenant) {
  const { token, userName, tenantList: fullList } = data
  uni.setStorageSync('micro_user', userName)
  uni.setStorageSync('micro_token', token)
  const list = Array.isArray(fullList) && fullList.length > 0 ? fullList : (pickedTenant ? [pickedTenant] : [])
  if (list.length > 0) {
    uni.setStorageSync('micro_tenantList', list)
  }
  if (pickedTenant) {
    uni.setStorageSync('micro_tenantCode', pickedTenant.tenantCode)
    uni.setStorageSync('micro_tenantName', pickedTenant.tenantName)
    uni.setStorageSync('micro_customerName', pickedTenant.customerName || '')
  }
  $store.commit('user/setUserInfo', { token, userName })
  getUserInfo(userName).then((info: IUserInfo) => {
    $store.commit('user/setUserInfo', { ...info })
    formatTabDefaultJump(info)
  })
}

// 邀请
const inviteItem = reactive({
  inviteCode: '',
  nickName: '',
  roleCode: '',
  openId: '',
  phoneNum: '',
  username: ''
})
function handleInputClear(key) {
  inviteItem[key] = ''
}
const btnLoading = ref(false)
function handleRegister() {
  if (btnLoading.value) return
  const { inviteCode, nickName } = inviteItem
  if (!inviteCode) {
    uni.showToast({ title: loginType.value === LOGIN_TYPE.CODE ? '请输入邀请码' : '请输入企业名称', icon: 'none' })
    return
  }
  if (!nickName) {
    uni.showToast({ title: '请输入姓名', icon: 'none' })
    return
  }
  btnLoading.value = true
  if (loginType.value === LOGIN_TYPE.CODE) {
    setGio('jr_regiest')
    // 邀请码注册走新流程 register-tenant/invite（不依赖远程 Feign 9201 运营平台）
    // 老接口 /login/wxminiapp/register 会调 remoteCustomerService.addMicroUser，
    // 本地开发环境 hhim-implatform 未启动时该 Feign 会失败
    // 策略：把 inviteCode / nickName / roleCode 暂存 storage，跳到 login 页让用户走微信授权登录；
    //       微信登录触发 NO_TENANT 时自动跳转到 register-tenant 完成加入
    uni.setStorageSync('pendingInvite', {
      inviteCode: inviteItem.inviteCode,
      nickName: inviteItem.nickName,
      roleCode: inviteItem.roleCode
    })
    btnLoading.value = false
    // 切到常规登录模式，让用户点微信一键登录 / H5 走验证码登录
    loginType.value = LOGIN_TYPE.NORMAL
    // #ifdef H5
    uni.showToast({ title: '请使用验证码登录', icon: 'none' })
    // #endif
    // #ifndef H5
    uni.showToast({ title: '请使用微信一键登录', icon: 'none' })
    // #endif
    return
  } else {
    // 体验
    if (isTrialExist.value) {
      trialLogin()
    } else {
      const data = {
        companyName: inviteItem.inviteCode,
        userName: inviteItem.nickName,
        phoneNumber: inviteItem.phoneNum
      }
      _post({ url: '/trial', data })
        .then((res) => {
          trialLogin()
        })
        .finally(() => {
          btnLoading.value = false
        })
    }
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
      _post({
        url: '/login/wxminiapp/login',
        data: {
          code: loginRes.code,
          username: EXPERIENCE_USERNAME,
          password: EXPERIENCE_PASSWORD
        }
      })
        .then((res) => {
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

function handleLoginTypeChange() {
  inviteItem.inviteCode = ''
  inviteItem.roleCode = ''
  //跳转去注册页处理
  uni.navigateTo({ url: '/pages-login/index-register?phoneNum=' + inviteItem.phoneNum })
}
// 隐私协议
const textChecked = ref(false)
function handleChecked() {
  textChecked.value = !textChecked.value
}
function xieyi(type) {
  uni.navigateTo({ url: '/pages-login/contractText?type=' + type })
}
</script>

<style lang="scss">
.border-1-9BADBC {
  border: px2vw(1) solid #9badbc;
}
.login-content {
  /* #ifdef H5 */
  /* H5：页面根容器铺满视口（小程序由 page 天然撑满） */
  min-height: 100vh;
  /* H5：背景图 absolute 在内容区内动画移动，裁剪溢出防止横向滚动条 */
  .flex-1.relative {
    overflow: hidden;
  }
  /* #endif */

  &__wrapper {
    /* 非定位元素的 z-index 无效，补 position:relative 让 z-index:1 真正生效（背景图保持在其下） */
    position: relative;
    z-index: 1;
    background-color: rgba(243, 243, 245, 0.96);
    width: 100vw;
    height: 100vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    overflow: hidden;
    /* #ifdef H5 */
    /* H5：uni-nav-bar(44px) 在文档流中占位，内容区高度需扣除，避免页面整体超出视口出现滚动条，
       与小程序（自定义导航 + 100vh 内容区，首屏全显示）视觉一致 */
    min-height: calc(100vh - 44px);
    height: calc(100vh - 44px);
    box-sizing: border-box;
    /* #endif */
  }

  .img-logo {
    width: px2vw(406);
    height: px2vw(96);
    margin-top: px2vw(200);
  }
  &__btn-account {
    margin-top: px2vw(48);
  }
  .bottom-notice {
    color: #999;
    padding-bottom: px2vw(64);
    font-size: px2vw(24);
    .font-bold {
      font-weight: bold;
    }
  }
  .login-bg {
    width: 200%;
    height: 100%;
    position: fixed;
    top: 0;
    left: 0;
    z-index: -1;
    /* #ifdef H5 */
    /* H5：fixed 改为 absolute——跟随内容区(.flex-1.relative)滚动，滑动时与 wrapper 同步不错位；
       背景只覆盖导航栏下方内容区，导航栏区域保持页面白底（天然遮罩，不再需要固定尺寸遮罩层） */
    position: absolute;
    z-index: 0;
    /* #endif */
    animation-duration: 20s;
    animation-name: bg-move;
    animation-iteration-count: infinite;
    animation-direction: alternate;
    animation-timing-function: linear;
  }

  @keyframes bg-move {
    from {
      transform: translateX(0);
    }

    to {
      transform: translateX(-50%);
    }
  }
  .back-area {
    position: absolute;
    z-index: 2;
    left: 0;
    padding-left: px2vw(24);
    box-sizing: border-box;
    top: 0;
  }
  .btn-phone {
    margin-top: px2vw(192);
  }
  .login-mode-switch {
    margin-top: px2vw(32);
    padding: px2vw(16);
  }
  .phone-title-area {
    margin-top: px2vw(112);
    width: px2vw(199);
    height: px2vw(80);
    line-height: px2vw(80);
    text-align: center;
    background: linear-gradient(180deg, rgba(245, 247, 250, 0.16) 0%, #ffffff 100%);
    border-radius: px2vw(8);
  }
}
</style>
