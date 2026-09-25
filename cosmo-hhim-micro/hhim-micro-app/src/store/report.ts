const state: IReportType = {
  reportItem: {},
  standard: false
}

const mutations = {
  setReportItem(state, payload) {
    Object.assign(state.reportItem, payload)
  },
  setStandard(state, payload) {
    state.standard = payload
  }
}

const actions = {}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
