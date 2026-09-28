/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author cosmo-hhim-open Team
 */
const state: IReportType = {
  reportItem: {},
  standard: false
}

const mutations = {
  setReportItem(state, payload) {
    Object.assign(state.reportItem, payload)
  },
  setStandard(state, payload) {
    state.standard = payload
  }
}

const actions = {}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
