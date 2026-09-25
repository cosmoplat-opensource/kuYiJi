const state = {
  dataItem: {},
  params: {}
}

const mutations = {
  setDataItem(state, payload) {
    state.dataItem = payload
  },
  setParams(state, payload) {
    state.params = payload
  }
}

export default {
  namespaced: true,
  state,
  mutations
}
