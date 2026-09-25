const state: ISettlementState = {
  dataList: []
}

const mutations = {
  setSettlementList(state, payload) {
    state.dataList = payload
  },
  changeEditStatus(state, payload) {
    state.dataList[payload.index].editStatus = payload.status
  }
}

const actions = {
  // updateName(context, payload) {
  //   context.commit('updateName', payload)
  // }
}

const getters = {
  // getCopyName(state) {
  //   return state.name
  // }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
