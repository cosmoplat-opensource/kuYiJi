import store from '@/store'
import { _get, _post } from '@/utils/common-request'
import dayjs from 'dayjs'
import { computed } from 'vue'
import { sha256Short } from '@/utils/sha256'
import NavigateToMiniProgramOptions = UniNamespace.NavigateToMiniProgramOptions

export const $store = store
export const $state = store.state
export const $commit = (str) => store.commit(str)

export enum REPORT_WORK {
  BEFORE = '前工序',
  NOW = '报工工序',
  CURRENT = '工序',
  PRODUCT = '产品'
}

export const uuc = false
// 直接指向 hhim-micro-be（bootstrap.yml server.port=9010），不再经过 hhim 平台网关 / 灰度路由
// H5 专用：可通过环境变量 VITE_API_BASE 覆盖（.env.development / .env.production），小程序/App 编译时走 #ifndef 分支不受影响
// #ifdef H5
export const server = import.meta.env.VITE_API_BASE || 'http://localhost:9010'
// #endif
// #ifndef H5
// 小程序/App：构建时可通过 VITE_MP_API_BASE 注入后端地址（真机/体验版必须指向实际服务器，如 https://api.example.com）。
// 注意：不能复用 H5 的 VITE_API_BASE——H5 的 .env 里 VITE_API_BASE=/api 是相对路径，
//      微信 wx.request 不支持相对路径（会报 invalid url），因此小程序端用独立变量名。
// 默认 http://localhost:9010 仅用于微信开发者工具本地调试。
export const server = import.meta.env.VITE_MP_API_BASE || 'http://localhost:9010'
// #endif
// 后端未配置 server.servlet.context-path，接口根路径留空
export const api_pre = ''

// 体验登录固定账号（开源部署可通过 VITE_EXPERIENCE_USERNAME / VITE_EXPERIENCE_PASSWORD 覆盖）
export const EXPERIENCE_USERNAME = import.meta.env.VITE_EXPERIENCE_USERNAME || '15888888888'
export const EXPERIENCE_PASSWORD = import.meta.env.VITE_EXPERIENCE_PASSWORD || 'cosmoplat'

// decouple-from-ops-platform：移除 DEFAULT_TENANT_* 兜底
// 登录前不再有默认租户；wxMiniAppLogin 返回 multiTenant=true 时跳 picker
// 登录成功后由 App.vue 的 initUserInfo 把真实 tenantCode 写入 storage 'micro_tenantCode'
export function clearTenantCode() {
  uni.removeStorageSync('micro_tenantCode')
}

export const formatStr = (str: string, size: number = 10) => {
  if (size < 0 || !str) {
    return str
  }
  if (str.length > size) {
    return str.substring(0, size - 1) + '...'
  } else {
    return str
  }
}

// HTML 转义：防止用户输入被 rich-text 当作 HTML 解析（存储型 XSS）
export function escapeHtml(str: any): string {
  return String(str ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

export function formatImage(name, suffix = 'png') {
  // 静态资源本地化：图片已随项目存放在 public/static/micro_app/（H5 构建后为 /static/micro_app/）
  // 如需使用自己的 CDN/对象存储，可通过环境变量 VITE_CDN_BASE 覆盖（例如 https://cdn.example.com）
  const cdnBase = import.meta.env.VITE_CDN_BASE || '/static/micro_app'
  // #ifndef H5
  // 小程序/App：图片统一由 H5 部署的静态目录提供（与 formatVideo 同源 VITE_MP_VIDEO_BASE），
  // 避免图片打包进小程序包导致主包超 2MB；未配置时回退包内路径（仅开发调试可用）
  const mpBase = import.meta.env.VITE_MP_VIDEO_BASE
  if (mpBase) {
    return `${mpBase}/${name}.${suffix}`.replace(/[^\w:/.%-]+/g, (m) => encodeURIComponent(m))
  }
  // #endif
  return `${cdnBase}/${name}.${suffix}`
}

/**
 * 视频地址（mp4）：H5 与小程序/App 对资源路径要求不同，这里做平台区分：
 * - H5：静态服务器/CDN 对中文文件名要求百分号编码（未编码会被拒绝 400）
 * - 小程序/App：优先使用配置的公网视频地址 VITE_MP_VIDEO_BASE（参考原 CDN 用法，网络 URL 同样需要编码）；
 *   未配置时回退包内路径（中文文件名按字面路径匹配，不做百分号编码）
 */
export function formatVideo(name) {
  const raw = formatImage(name, 'mp4')
  // #ifdef H5
  return raw.replace(/[^\w:/.%-]+/g, (m) => encodeURIComponent(m))
  // #endif
  // #ifndef H5
  const mpVideoBase = import.meta.env.VITE_MP_VIDEO_BASE || ''
  if (mpVideoBase) {
    return `${mpVideoBase}/${name}.mp4`.replace(/[^\w:/.%-]+/g, (m) => encodeURIComponent(m))
  }
  return raw
  // #endif
}

export function formatPx2Vw(px: number) {
  return (px / 720) * 100
}

export function formatDate(str, formatter = 'MM-DD HH:mm') {
  return str ? dayjs(str).format(formatter) : ''
}

export function setGio(str, type = 'track') {
  if (uuc) return
  // global.gio(type, str)
}

export function setGioUser(userInfo) {
  if (uuc) return
  // global.gio('setUser', userInfo)
  // global.gio('setVisitor', userInfo)
}

export function getUserInfo(username) {
  _get({ url: '/experience/guide/guideFlag' }).then((res: IResponseType<any>) => {
    $store.commit('experience/setGuideCode', res.data)
  })
  return new Promise((resolve) => {
    _get({ url: '/user/completeUserInfo', data: { username } }).then((res: IResponseType<IUserInfo>) => {
      const {
        phonenumber,
        nickName,
        sex,
        avatar,
        userName,
        remark,
        id,
        status,
        tenantCode,
        tenantName,
        microRoles,
        microUserAppRes,
        personalRecommend
      } = res.data
      setGioUser({ JR_EN: tenantName, JR_UN: userName, JR_PHONE: phonenumber })
      const roleName = microRoles[0]?.roleName ?? ''
      const roleCode = microRoles[0]?.roleCode ?? ''
      const validDate = microUserAppRes[0]?.validDate ?? ''
      const item = {
        phonenumber,
        nickName,
        sex,
        tenantCode,
        tenantName,
        validDate,
        avatar,
        userName,
        remark,
        id,
        roleCode,
        roleName,
        personalRecommend,
        status
      }
      resolve(item)
    })
  })
}

/**
 * 按角色/个性化推荐落地默认 tab
 *
 * @param obj     { roleCode, personalRecommend }
 * @param tabMode 'jump' = 落地默认 tab 并跳转（冷启动落在入口页；登录后；默认）
 *                'hold' = 只落地 tab 状态、不跳页（冷启动落在深链/子页面：让深链优先，
 *                         同时保证之后回到 pages/main 时 tabBar 正常）
 *                'none' = 什么都不做（H5 从后台/其他标签页切回来触发的 App.onShow：
 *                         uni-h5 把 visibilitychange 转发成 App.onShow，此时任何跳转都会
 *                         把停在多级页面（如"问一问"）的用户 reLaunch 回工作台、丢掉页面栈）
 */
export function formatTabDefaultJump(obj, tabMode: 'jump' | 'hold' | 'none' = 'jump') {
  const { roleCode, personalRecommend } = obj
  if (tabMode === 'none') {
    return
  }
  const pageIndex = personalRecommend === 1 ? uni.getStorageSync('recommendPage_index') : ''
  // #ifdef H5
  // H5：登录流程的默认 tab 不覆盖"上次所在 tab"（刷新/重新打开网页恢复用）。
  // 校验 tabList 含该 tab（角色切换后 last_tab 可能失效）才采用
  const lastTab = uni.getStorageSync('last_tab')
  const defaultTab = lastTab && $store.state.tabBar.tabList.some((v) => v.text === lastTab) ? lastTab : ''
  // #endif
  // #ifndef H5
  const defaultTab = ''
  // #endif
  const tabAction = tabMode === 'hold' ? 'tabBar/setTabActiveByNameNoJump' : 'tabBar/setTabActiveByName'
  switch (roleCode) {
    case '10':
      $store.commit('tabBar/setRole20')
      $store.commit('tabBar/setTabShow', true)
      $store.commit(tabAction, defaultTab || pageIndex || '工作台')
      break
    case '20':
      $store.commit('tabBar/setRole20')
      $store.commit('tabBar/setTabShow', true)
      $store.commit(tabAction, defaultTab || pageIndex || '审产')
      break
    case '40':
      $store.commit('tabBar/setRole20')
      $store.commit('tabBar/setTabShow', true)
      $store.commit(tabAction, defaultTab || pageIndex || '工作台')
      break
    case '25':
      $store.commit('tabBar/setRole25')
      $store.commit('tabBar/setTabShow', true)
      $store.commit(tabAction, defaultTab || pageIndex || '质检')
      break
    case '30':
    default:
      $store.commit('tabBar/setRole30')
      $store.commit('tabBar/setTabShow', true)
      $store.commit(tabAction, defaultTab || pageIndex || '记工')
      break
  }
}

//这里可以返回小程序环境哦~
// H5/App 下 getAccountInfoSync 不可用，兜底返回 'release'
export function getEnvVersion() {
  try {
    const accountInfo = uni.getAccountInfoSync()
    return accountInfo?.miniProgram?.envVersion || 'release'
  } catch (e) {
    return 'release'
  }
}

// 员工
export const Role_Staff = computed(() => {
  return $state.user.userInfo?.roleCode === '30' ?? false
})
// 管理员
export const Role_Admin = computed(() => {
  return $state.user.userInfo?.roleCode === '10' ?? false
})
// 审产员
export const Role_Review = computed(() => {
  return $state.user.userInfo?.roleCode === '20' ?? false
})
// 质检员
export const Role_Quality = computed(() => {
  return $state.user.userInfo?.roleCode === '25' ?? false
})
// 体验
export const Role_Experience = computed(() => {
  return $state.user.userInfo?.roleCode === '40' ?? false
})

export const Personal_Recommend = computed(() => {
  return $state.user.userInfo?.personalRecommend === 1 ?? false
})

export function collectClick(name, currentPage, prePage, content?) {
  const phonenumber = Role_Experience.value
    ? uni.getStorageSync('experiencePhoneNum')
    : $store.state.user.userInfo.phonenumber
  const params = {
    duration: 1,
    eventCode: sha256Short(name),
    weight: 1,
    eventContent: content || name,
    eventName: name,
    eventSource: 'PAGE',
    eventType: 'CLICK',
    phonenumber,
    currentPage: currentPage,
    referPage: prePage,
    userLocation: '',
    userType: Role_Experience.value ? 'EXPERIENCE' : 'OFFICIAL'
  }
  _post({ url: '/tracking/collect', data: params })
}

export function collectPV(name, currentPage, prePage, weight) {
  const phonenumber = Role_Experience.value
    ? uni.getStorageSync('experiencePhoneNum')
    : $store.state.user.userInfo.phonenumber
  const params = {
    duration: 1,
    weight,
    eventCode: sha256Short(name),
    eventContent: name,
    eventName: name,
    eventSource: 'PAGE',
    eventType: 'PV',
    phonenumber,
    currentPage: currentPage,
    referPage: prePage,
    userLocation: '',
    userType: Role_Experience.value ? 'EXPERIENCE' : 'OFFICIAL'
  }
  _post({ url: '/tracking/collect', data: params })
}

export function collectVideo(name, playing, duration, currentPage, prePage) {
  const phonenumber = Role_Experience.value
    ? uni.getStorageSync('experiencePhoneNum')
    : $store.state.user.userInfo.phonenumber
  const params = {
    duration: playing,
    weight: 1,
    eventCode: sha256Short(name),
    eventContent: duration,
    eventName: name,
    eventSource: 'VIDEO',
    eventType: 'TP',
    phonenumber,
    currentPage: currentPage,
    referPage: prePage,
    userLocation: '',
    userType: Role_Experience.value ? 'EXPERIENCE' : 'OFFICIAL'
  }
  _post({ url: '/tracking/collect', data: params })
}

export function openBuyMini() {
  // #ifdef H5
  // H5 无法跳转小程序，降级为提示
  uni.showToast({ title: '请在微信小程序中使用购买功能', icon: 'none' })
  return
  // #endif
  const id = '1595312907104034817'
  _get({ url: '/login/getUucUserTicket', data: { phoneNumber: uni.getStorageSync('experiencePhoneNum') } }).then(
    (res) => {
      const options: NavigateToMiniProgramOptions = {
        // AppID 占位:发布前替换为实际目标小程序 AppID
        // 本地开发:在 .env.development.local / .env.production.local 配置 VITE_MP_TARGET_APPID=真实AppID(文件已 gitignore,不会入库)
        appId: import.meta.env.VITE_MP_TARGET_APPID || 'wx0000000000000000',
        path: `/pages/product-detail/product-detail?id=${id}`,
        extraData: { ticket: res.msg },
        envVersion: 'release',
        success: () => {
          setTimeout(() => {
            _get({ url: '/experience/guide/guideFlag' }).then((res: IResponseType<any>) => {
              $store.commit('experience/setGuideCode', res.data)
            })
          }, 1500)
        }
      }
      wx.navigateToMiniProgram(options)
    }
  )
  // collectClick('去购买', '购买页', '我的')
  // uni.navigateTo({
  //   url: '/pages-my-center/purchase-guide/index'
  // })
}
