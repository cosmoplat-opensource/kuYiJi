const state: ISettingState = {
  submitInspectSwitch: '1',
  batchSubmitSwitch: '1',
  workerBaseDataConfine: '1',
  id: 0
}

const mutations = {
  setSubmitInspectSwitch(state, payload) {
    state.submitInspectSwitch = payload
  },
  setBatchSubmitSwitch(state, payload) {
    state.batchSubmitSwitch = payload
  },
  setWorkerBaseDataConfine(state, payload) {
    state.workerBaseDataConfine = payload
  },
  setId(state, payload) {
    state.id = payload
  }
}

export default {
  namespaced: true,
  state,
  mutations
}
