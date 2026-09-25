import { server, api_pre, uuc, $state, Role_Experience } from '@/utils/common'

export const _get = ({ url, data = {} }) => {
  return requestApi(formatUrl(url), data, 'GET')
}
export const _post = ({ url, data = {} }) => {
  return requestApi(formatUrl(url), data, 'POST')
}
export const _delete = ({ url, data = {} }) => {
  return requestApi(formatUrl(url), data, 'DELETE')
}
export const _put = ({ url, data = {}, header = {} }) => {
  return requestApi(formatUrl(url), data, 'PUT', header)
}

function formatUrl(url) {
  if (url.includes('http')) {
    return url
  }
  return `${server}${api_pre}${url}`
}

let loginOpen = false

interface RequestSuccessCallbackResult {
  /**
   * 开发者服务器返回的数据
   */
  data: IResponseType<unknown>
  /**
   * 开发者服务器返回的 HTTP 状态码
   */
  statusCode: number
  /**
   * 开发者服务器返回的 HTTP Response Header
   */
  header: any
  /**
   * 开发者服务器返回的 cookies，格式为字符串数组
   */
  cookies: string[]
}

function requestApi(url, data, method, header = {}) {
  // 直接调用 hhim-micro-be（decouple-from-ops-platform 后单库，无需 target_ds / target_schema 头）
  // target_customer 仍保留用于后端 ThreadContext 兼容；新登录态由拦截器从 Redis userInfo 解析
  const tenantCode = uni.getStorageSync('micro_tenantCode') || ''
  const headers = {
    type: 'wechatMiniApp',
    application_sign: 'micro_process',
    Authorization: 'Bearer ' + uni.getStorageSync('micro_token'),
    username: uni.getStorageSync('micro_user'),
    target_customer: tenantCode,
    ...header
  }
  return new Promise((resolve, reject) => {
    uni.request({
      url,
      data,
      header: headers,
      method,
      success: (res: RequestSuccessCallbackResult) => {
        if (res.data.code === 408) {
          if (!loginOpen && !uuc) {
            loginOpen = true
            setTimeout(() => (loginOpen = false), 2000)
            uni.reLaunch({ url: '/pages-login/index' })
          } else if (uuc) {
            uni.reLaunch({ url: '/pages/welcome/index' })
            uni.showToast({ title: res.data.msg, icon: 'none', duration: 3000 })
          }
        } else if ([500, 911].includes(res.data.code)) {
          uni.showToast({ title: res.data.msg, icon: 'none', duration: 3000 })
          reject(res.data)
        } else if (res.data.code === 902) {
          reject(res.data)
        } else if (res.data.code === 411) {
          //login 体验到期提示
          resolve(res.data)
        } else if (res.data.code === 403) {
          // decouple-from-ops-platform-cleanup: 业务级 403（如 NO_TENANT）也走 reject，
          // 让调用方能 catch 后做特定动作（弹注册提示等）
          reject(res.data)
        } else {
          resolve(res.data)
        }
      },
      fail: (res) => {
        console.log('[request] fail', url, res)
        reject(res)
      }
    })
  })
}

export const checkGuide = () => {
  uni.navigateTo({ url: '/pages-guide/index' })
}
