const state = {
  item: {}
}

const mutations = {
  updateProduct(state, payload) {
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
