/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author cosmo-hhim-open Team
 */
const state = {
  mockOpen: false
}

const mutations = {
  setMockOpen(state, payload) {
    state.mockOpen = payload
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
