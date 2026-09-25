const state = {
  chatList: []
}

const mutations = {
  resetChatList(state, payload) {
    state.chatList = []
  },
  chatAdd(state, payload) {
    state.chatList.push(payload)
  }
}

const actions = {
  // updateName(context, payload) {
  //   context.commit('updateName', payload)
  // }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
