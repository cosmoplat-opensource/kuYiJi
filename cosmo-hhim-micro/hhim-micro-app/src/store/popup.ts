/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author cosmo-hhim-open Team
 */
const state: IPopupModules = {
  popupContent: '',
  popupImagePath: '',
  popupData: {},
  popupRefList: {}
}

const mutations = {
  updatePopupContent(state, payload) {
    state.popupContent = payload
  },
  updatePopupImagePath(state, payload) {
    state.popupImagePath = payload
  },
  setPopupData(state, payload) {
    Object.assign(state.popupData, payload)
  },
  addPopupRef(state, payload) {
    if (!state.popupRefList[payload.name]) {
      state.popupRefList[payload.name] = payload.ref
    }
  },
  closeAllPopup(state) {
    Object.keys(state.popupRefList).forEach((key) => {
      state.popupRefList[key]?.close()
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations
}
