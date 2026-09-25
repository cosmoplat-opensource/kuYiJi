const state = {
  name: '121231'
}

const mutations = {
  updateName(state, payload) {
    state.name = payload
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
