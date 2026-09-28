/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author cosmo-hhim-open Team
 */
const state = {
  dataItem: {},
  params: {}
}

const mutations = {
  setDataItem(state, payload) {
    state.dataItem = payload
  },
  setParams(state, payload) {
    state.params = payload
  }
}

export default {
  namespaced: true,
  state,
  mutations
}
