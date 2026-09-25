const state: IQualityTestingState = {
  dataItem: {},
  repairItem: {}
}

const mutations = {
  setDataItem(state, payload) {
    state.dataItem = payload
  },
  setRepairItem(state, payload) {
    state.repairItem = payload
  }
}

export default {
  namespaced: true,
  state,
  mutations
}
