/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author cosmo-hhim-open Team
 */
const state = {
  outList: [],
  outBoundReason: '' //出库备注
}

const mutations = {
  updateOutList(state, payload) {
    state.outList = payload
  },
  clearOutList(state, payload) {
    state.outList = []
  },
  updateOutBoundReason(state, payload) {
    state.outBoundReason = payload
  },
  clearOutBoundReason(state, payload) {
    state.outBoundReason = ''
  }
}

const actions = {}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
