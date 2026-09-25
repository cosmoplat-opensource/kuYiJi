import { _get } from '@/utils/common-request'
import { ref } from 'vue'
import { openBuyMini } from '@/utils/common'

const HooksSystemNotice = (type) => {
  const systemNotice = ref('')
  const submitNotice = ref('')
  function getSystemTips() {
    _get({ url: '/tips/switchPage', data: { switchPageType: type } }).then((res: IResponseType<unknown>) => {
      if (res.tipWay === '10') {
        // 状态栏显示
        if (res.tip) {
          systemNotice.value = res.tip
        }
        // 5秒自动提示
        setTimeout(() => {
          systemNotice.value = ''
        }, 5000)
      }

      if (res.tipWay === '20') {
        uni.showModal({
          title: '提示',
          content: res.tip,
          showCancel: false,
          confirmText: '去购买',
          success: function (res) {
            if (res.confirm) {
              openBuyMini()
            }
          }
        })
      }
    })
  }
  async function getSubmitTips() {
    const res = (await _get({ url: '/tips/submit', data: { submitActionType: type } })) as IResponseType<unknown>
    submitNotice.value = res.tip
  }
  return {
    systemNotice,
    getSystemTips,
    submitNotice,
    getSubmitTips
  }
}

export default HooksSystemNotice
