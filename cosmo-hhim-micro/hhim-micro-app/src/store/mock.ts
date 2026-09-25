const state = {
  mockOpen: false
}

const mutations = {
  setMockOpen(state, payload) {
    state.mockOpen = payload
  }
}

const actions = {
  updateName(context, payload) {
    context.commit('updateName', payload)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
