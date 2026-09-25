const state: IStockState = {
  storageList: []
}

const mutations = {
  setStorageList(state, payload) {
    state.storageList = payload
  }
}

export default {
  namespaced: true,
  state,
  mutations
}
