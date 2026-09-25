import { _get } from '@/utils/common-request'
import { uuc } from '@/utils/common'
import { ref } from 'vue'
import dayjs from 'dayjs'

const HooksSubscribe = () => {
  const subscribeType = ref(false)
  const subscribeStatusShow = ref(false)
  // 缓存订阅模板 id：页面加载时预取，点击订阅时【同步】调用 wx.requestSubscribeMessage。
  // 微信要求 requestSubscribeMessage 在用户点击事件的同步上下文中调用，
  // 在异步回调（网络请求成功后）调用会被拒绝、不弹窗。
  const templateIds: string[] = []
  ;(function loadTemplateIds() {
    _get({ url: '/wechatMsgTemplate/getMsgTemplateInfos' }).then((res: IResponseType<[]>) => {
      templateIds.length = 0
      ;(res.data || []).forEach((v: { templateId: string }) => {
        templateIds.push(v.templateId)
      })
    })
  })()
  function handleSubscribe() {
    // #ifdef H5
    // H5 无微信订阅消息能力，降级为提示
    uni.showToast({ title: 'H5 暂不支持订阅消息', icon: 'none', duration: 2000 })
    return
    // #endif
    if (uuc) {
      uni.showToast({ title: 'app暂不支持订阅功能', icon: 'none', duration: 2000 })
      return
    }
    checkSubscribeStatus()
    if (subscribeStatusShow.value) return
    if (!templateIds.length) {
      uni.showToast({ title: '订阅配置加载中，请稍后重试', icon: 'none', duration: 2000 })
      return
    }
    wx.requestSubscribeMessage({
      tmplIds: templateIds,
      complete: (e) => {
        for (const eKey in e) {
          if (e[eKey] === 'accept') {
            uni.setStorageSync('subscribeStatus', dayjs().format('YYYY-MM-DD'))
            checkSubscribeStatus()
          }
        }
      }
    })
  }

  function checkSubscribeStatus() {
    const state = uni.getStorageSync('subscribeStatus')
    subscribeStatusShow.value = state && dayjs().format('YYYY-MM-DD') === state
  }

  function clearSubscribeStatus() {
    uni.removeStorageSync('subscribeStatus')
    checkSubscribeStatus()
  }

  return {
    subscribeType,
    subscribeStatusShow,
    handleSubscribe,
    checkSubscribeStatus,
    clearSubscribeStatus
  }
}

export default HooksSubscribe
