const state: IAuditState = {
  currentFilter: '',
  filterData: {
    productNameOrCode: '',
    processNameOrCode: '',
    submitNickName: '',
    submitUserIds: '',
    startDate: '',
    remark: '',
    endDate: ''
  },
  followList: [],
  followChecked: false,
  crossPageData: {} //其他页跳转到审产首页时解决多次加载页面，数据不正常的问题
}

const baseObj = {
  productNameOrCode: '',
  processNameOrCode: '',
  submitNickName: '',
  startDate: '',
  remark: '',
  endDate: ''
}

const mutations = {
  updateCurrentFilter(state, payload) {
    state.currentFilter = payload
  },
  setFilterData(state, payload) {
    Object.assign(state.filterData, payload)
  },
  resetFilterData(state) {
    Object.assign(state.filterData, { ...baseObj })
  },
  setFollowList(state, payload) {
    state.followList = payload
  },
  setFollowChecked(state, payload) {
    state.followChecked = payload
  },
  setCrossPageData(state, payload) {
    state.crossPageData = payload
  }
}

const actions = {
  updateCurrentFilter(context, payload) {
    context.commit('updateCurrentFilter', payload)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
