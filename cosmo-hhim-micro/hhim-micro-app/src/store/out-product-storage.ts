const state = {
  outList: [],
  outBoundReason: '' //出库备注
}

const mutations = {
  updateOutList(state, payload) {
    state.outList = payload
  },
  clearOutList(state, payload) {
    state.outList = []
  },
  updateOutBoundReason(state, payload) {
    state.outBoundReason = payload
  },
  clearOutBoundReason(state, payload) {
    state.outBoundReason = ''
  }
}

const actions = {}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
