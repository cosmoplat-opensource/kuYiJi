const state = {
  item: {}
}

const mutations = {
  updateStaff(state, payload) {
    state.item = payload
  }
}

const actions = {}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
