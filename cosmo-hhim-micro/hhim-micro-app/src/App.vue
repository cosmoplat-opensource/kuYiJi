<script setup lang="ts">
import { onLaunch, onShow } from '@dcloudio/uni-app'
import { $store, formatTabDefaultJump, getUserInfo, uuc, EXPERIENCE_USERNAME } from '@/utils/common'
import { useStore } from 'vuex'
import { _get } from '@/utils/common-request'
import LaunchShowOption = App.LaunchShowOption
import HooksSubscribe from '@/hooks/subscribe'
const store = useStore()

const whiteList = [
  'pages-my-center/product-manage/add-edit',
  'pages-my-center/my-feedback/submit-feedback',
  'pages-my-center/edit-userinfo',
  'pages-report-work/index',
  'pages-report-work/edit',
  'pages-audit/detail-info'
]

interface TOptions extends LaunchShowOption {
  mode: string
  apiCategory: string
}

// 订阅页面进来的直接跳分析页面
const { subscribeType } = HooksSubscribe()

/**
 * 是否"入口页"启动（工作台 / 登录 / 欢迎页）。
 * 直接打开子页面（H5 深链、H5 整页重载后停在子页面、小程序分享卡片等）时为 false ——
 * 此时不做默认 tab 跳转、也不恢复 last_route，让深链优先。
 * 平台无关（不使用任何 H5-only API），小程序端同样使用。
 */
function isEntryPath(path?: string) {
  const p = (path || '').replace(/^\//, '')
  return !p || p === 'pages/main' || p.indexOf('pages-login/') === 0 || p.indexOf('pages/welcome/') === 0
}

// #ifdef H5
// H5：记录并恢复上次访问的页面级路由（避免每次打开/刷新网页都回工作台）。
// 页面跳转（navigateTo/redirectTo/reLaunch）会改变 hash，hashchange 记录；
// 打开时读到 last_route 则跳回。登录页不记录（避免回登录页）。
// 注意：只有"入口页启动"才恢复，否则会把分享/深链打开的页面抢走。
onLaunch((options: TOptions) => {
  const lastRoute = uni.getStorageSync('last_route')
  if (isEntryPath(options?.path) && lastRoute && !lastRoute.includes('/pages-login/')) {
    // 等路由初始化完成后跳转（onLaunch 时页面栈可能未就绪）
    setTimeout(() => {
      uni.reLaunch({ url: lastRoute })
    }, 0)
  }
  window.addEventListener('hashchange', () => {
    const hash = (window.location.hash || '').replace(/^#/, '')
    if (hash && !hash.includes('/pages-login/')) {
      uni.setStorageSync('last_route', hash)
    }
  })
})
// #endif

/**
 * H5 注意：uni-h5 监听 document.visibilitychange → ON_APP_ENTER_FOREGROUND → invokeHook(getApp(), ON_SHOW)，
 * 也就是说"切到别的标签页/切后台再切回来"会**再次触发 App.onShow**。
 * 因此启动初始化（含"按角色默认 tab 跳转"）只能在冷启动后的首次 onShow 执行：
 * 否则用户停在多级页面（如"问一问"）时切走再回来，会被 reLaunch 到 /pages/main（工作台）——
 * 页面栈与当前对话全部丢失。前台回归只刷新用户信息，不再切 tab、不再跳页。
 */
let shownOnce = false

onShow((options: TOptions) => {
  const firstShow = !shownOnce
  shownOnce = true
  if (!whiteList.includes(options.path)) {
    // tabMode 三态：
    //  - 前台回归（非首次 onShow）→ 'none'：完全不碰 tab、不跳页，保住多级页面与当前对话；
    //  - 冷启动落在入口页 → 'jump'：按角色/推荐跳到默认 tab（原行为）；
    //  - 冷启动落在深链/子页面 → 'hold'：只落地 tab 状态不跳页，让深链优先。
    const tabMode = !firstShow ? 'none' : isEntryPath(options.path) ? 'jump' : 'hold'
    initData(options.mode === 'default', tabMode)
  }
  subscribeType.value = options.query?.subscribeType === '10' ?? false
  $store.commit('guide/setShareTimeLine', options.apiCategory === 'browseOnly')
})

/**
 * 启动/进入前台初始化
 *
 * @param type    H5 无用；小程序端表示"默认启动"（走版本更新检查）
 * @param tabMode 默认 tab 落地方式（'jump' | 'hold' | 'none'），见 onShow 注释
 */
async function initData(type, tabMode: 'jump' | 'hold' | 'none' = 'jump') {
  if (uuc) {
    // uuc的登录
    const info = JSON.parse(plus.runtime.arguments)
    _get({ url: '/login/mobileapp/token', data: { uucToken: info.accessToken } }).then(
      (res: IResponseType<IUserInfo>) => {
        const { userName, token } = res.data
        uni.setStorageSync('micro_user', userName)
        uni.setStorageSync('micro_token', token)
        store.commit('user/setUserInfo', { token, userName })
        initUserInfo(userName, tabMode)
      }
    )
  } else {
    // #ifndef H5
    type && mpUpdate()
    // #endif
    const token = uni.getStorageSync('micro_token')
    if (token) {
      // 塞缓存中的token和username
      let userName = uni.getStorageSync('micro_user')
      if (uni.getStorageSync('roleCode') === '40') {
        userName = EXPERIENCE_USERNAME
      }
      store.commit('user/setUserInfo', { token, userName })
      initUserInfo(userName, tabMode)
      initUserPersonal()
    }
  }
}
function initUserInfo(userName, tabMode: 'jump' | 'hold' | 'none' = 'jump') {
  getUserInfo(userName).then((res: IUserInfo) => {
    let { id, nickName, avatar, roleCode, tenantName, tenantCode, roleName, phonenumber, personalRecommend } = res
    // decouple-from-ops-platform-cleanup (B.4): 持久化新 token 结构字段
    if (tenantCode) {
      uni.setStorageSync('micro_tenantCode', tenantCode)
    }
    if (tenantName) {
      uni.setStorageSync('micro_tenantName', tenantName)
    }
    // customerName 由 picker 阶段写入，App 启动时若缺失则置空
    if (!uni.getStorageSync('micro_customerName')) {
      uni.setStorageSync('micro_customerName', '')
    }
    store.commit('user/setUserInfo', {
      id,
      nickName,
      avatar,
      phonenumber,
      roleCode,
      roleName,
      tenantCode,
      tenantName,
      personalRecommend
    })
    if (subscribeType.value) {
      $store.commit('tabBar/setRole20')
      $store.commit('tabBar/setTabShow', true)
      // 前台回归（tabMode='none'）不切 tab，避免把多级页面的用户拉回工作台
      if (tabMode !== 'none') {
        $store.commit('tabBar/setTabActiveByName', '工作台')
      }
      return
    }
    formatTabDefaultJump({ roleCode, personalRecommend }, tabMode)
  })
}

// 获取个性化设置
function initUserPersonal() {
  _get({ url: '/user/recommendPage', data: { pageType: 'CHECK_DIMENSIONS' } }).then((res: IResponseType<String>) => {
    // 后端在无任何使用记录时返回 data: ""，此处兜底为 '员工' 避免后续 .includes 抛错或语义异常
    if (!res?.data) {
      uni.setStorageSync('recommendPage_check_dimensions', '员工')
      return
    }
    res.data = res.data.includes('产品') ? '产品' : '员工'
    uni.setStorageSync('recommendPage_check_dimensions', res.data)
  })
  _get({ url: '/user/recommendPage', data: { pageType: 'INDEX' } }).then((res: IResponseType<String>) => {
    // 记工列表: '记工',
    //   审核列表: '审产',
    //   车间库存: '车间库存',
    //   工作台: '工作台',
    //   质检列表: '质检'
    if (!res?.data) {
      uni.setStorageSync('recommendPage_index', '')
      return
    }
    if (res.data === '审核列表') {
      res.data = '审产'
    }
    uni.setStorageSync('recommendPage_index', res.data.replace('列表', ''))
  })
}

// #ifndef H5
function mpUpdate() {
  const updateManager = uni.getUpdateManager()

  updateManager.onCheckForUpdate(function () {
    // 请求完新版本信息的回调
    // console.log(res.hasUpdate)
  })

  updateManager.onUpdateReady(function () {
    uni.showModal({
      title: '更新提示',
      content: '新版本已经准备好，是否重启应用？',
      showCancel: false,
      success(res) {
        if (res.confirm) {
          // 新的版本已经下载好，调用 applyUpdate 应用新版本并重启
          updateManager.applyUpdate()
        }
      }
    })
  })

  updateManager.onUpdateFailed(function () {
    // 新的版本下载失败
    uni.showModal({
      title: '提示',
      content: '新版小程序下载失败\n请自行退出程序，手动卸载本程序，再运行',
      confirmText: '知道了'
    })
  })
}
// #endif
</script>
<style lang="scss">
@import '@/static/styles/common.scss';
@import '@/static/styles/color.scss';
@import '@/static/styles/layout.scss';
@import '@/static/styles/size.scss';
@import '@/static/styles/other.scss';

/* #ifdef H5 */
/* ===== H5 全局基础适配（仅 H5 生效，小程序不受影响） ===== */
/* 1. 重置浏览器默认 body 8px margin，保证页面铺满视口 */
html,
body {
  margin: 0;
  padding: 0;
  min-height: 100vh;
  background-color: #f8f8f8;
}

/* 2. 页面容器铺满视口（对应小程序 page 的行为） */
uni-page,
uni-page-body,
page {
  min-height: 100vh;
}

/* 3. 输入框观感与小程序保持一致：去掉浏览器默认内边距/边框/焦点轮廓 */
uni-input {
  .uni-input-input {
    padding: 0;
    border: none;
    outline: none;
    background: transparent;
    box-shadow: none;
  }
}
/* #endif */
</style>
