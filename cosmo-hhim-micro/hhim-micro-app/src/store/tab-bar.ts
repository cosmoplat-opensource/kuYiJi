const state: ITabBar = {
  tabList: [
    {
      checked: false,
      pagePath: 'pages/report-production/list',
      iconPath: 'icon_work_submit',
      selectedIconPath: 'icon_work_submit_chosed',
      text: '记工'
    },
    {
      checked: false,
      pagePath: 'pages/audit/list',
      iconPath: 'icon_check',
      selectedIconPath: 'icon_check_chosed',
      text: '审产'
    },
    {
      checked: false,
      pagePath: 'pages/stock-list/list',
      iconPath: 'icon_stock',
      selectedIconPath: 'icon_stock_chosed',
      text: '车间库存'
    },
    {
      checked: false,
      pagePath: 'pages/analysis/index',
      iconPath: 'icon_workbench',
      selectedIconPath: 'icon_workbench_chosed',
      text: '工作台'
    }
  ],
  tabActive: {} as TabItem,
  tabShow: true
}

function jumpToMainPage() {
  const length = getCurrentPages().length
  if (getCurrentPages()[length - 1]?.route !== 'pages/main') {
    uni.reLaunch({ url: '/pages/main' })
  }
}

/** 应用当前 tab（写 state + 记 last_tab）；tabList 不含该 tab 时返回 false（角色切换后 last_tab 可能失效） */
function applyTabActive(state, name) {
  const index = state.tabList.findIndex((v) => v.text === name)
  if (index === -1) {
    return false
  }
  state.tabActive = state.tabList[index]
  state.tabList.forEach((v, i) => {
    v.checked = i === index
  })
  // H5：记录当前 tab，刷新/重新打开网页时恢复（见 main.vue onShow）
  uni.setStorageSync('last_tab', state.tabActive.text)
  return true
}

const mutations = {
  setTabList(state, payload) {
    state.tabList = payload
  },
  setTabShow(state, payload) {
    state.tabShow = payload
  },
  clearTabList(state) {
    state.tabList = []
  },
  setRole30(state) {
    state.tabList = [
      {
        checked: false,
        pagePath: 'pages/report-production/list',
        iconPath: 'icon_work_submit',
        selectedIconPath: 'icon_work_submit_chosed',
        text: '记工'
      },
      {
        checked: false,
        pagePath: 'pages/stock-list/list',
        iconPath: 'icon_stock',
        selectedIconPath: 'icon_stock_chosed',
        text: '车间库存'
      },
      {
        checked: false,
        pagePath: 'pages/analysis/index',
        iconPath: 'icon_workbench',
        selectedIconPath: 'icon_workbench_chosed',
        text: '工作台'
      }
    ]
  },
  setRole20(state) {
    state.tabList = [
      {
        checked: false,
        pagePath: 'pages/report-production/list',
        iconPath: 'icon_work_submit',
        selectedIconPath: 'icon_work_submit_chosed',
        text: '记工'
      },
      {
        checked: false,
        pagePath: 'pages/audit/list',
        iconPath: 'icon_check',
        selectedIconPath: 'icon_check_chosed',
        text: '审产'
      },
      {
        checked: false,
        pagePath: 'pages/stock-list/list',
        iconPath: 'icon_stock',
        selectedIconPath: 'icon_stock_chosed',
        text: '车间库存'
      },
      {
        checked: false,
        pagePath: 'pages/analysis/index',
        iconPath: 'icon_workbench',
        selectedIconPath: 'icon_workbench_chosed',
        text: '工作台'
      }
    ]
  },
  setRole25(state) {
    state.tabList = [
      {
        checked: false,
        pagePath: 'pages/quality/list',
        iconPath: 'icon_check',
        selectedIconPath: 'icon_check_chosed',
        text: '质检'
      },
      {
        checked: false,
        pagePath: 'pages/stock-list/list',
        iconPath: 'icon_stock',
        selectedIconPath: 'icon_stock_chosed',
        text: '车间库存'
      },
      {
        checked: false,
        pagePath: 'pages/analysis/index',
        iconPath: 'icon_workbench',
        selectedIconPath: 'icon_workbench_chosed',
        text: '工作台'
      }
    ]
  },
  setTabActive(state, index) {
    state.tabActive = state.tabList[index]
    state.tabList.forEach((v, i) => {
      v.checked = i === index
    })
    // H5：记录当前 tab，刷新/重新打开网页时恢复（见 main.vue onShow）
    uni.setStorageSync('last_tab', state.tabActive.text)
    jumpToMainPage()
  },
  setTabActiveByName(state, name) {
    if (applyTabActive(state, name)) {
      jumpToMainPage()
    }
  },
  /**
   * 只落地当前 tab 状态，不跳页（H5 冷启动落在深链/子页面时用）：
   * 既保证用户之后回到 main 时 tab 与 tabBar 正常渲染，又不把深链页面抢回工作台。
   */
  setTabActiveByNameNoJump(state, name) {
    applyTabActive(state, name)
  }
}

const actions = {}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
