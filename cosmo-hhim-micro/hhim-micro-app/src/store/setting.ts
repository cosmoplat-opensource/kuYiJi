/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author cosmo-hhim-open Team
 */
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
