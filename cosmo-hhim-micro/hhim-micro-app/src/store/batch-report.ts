/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author cosmo-hhim-open Team
 */
const state: IBatchReportState = {
  productList: [],
  productItem: {},
  productIndex: 0
}

const mutations = {
  setProductList(state, payload) {
    state.productList = payload
  },
  changeProductItem(state, payload) {
    state.productList[payload.index] = payload.item
  },
  deleteProductItem(state, index) {
    state.productList.splice(index, 1)
  },
  currentProductIndex(state, index) {
    state.productIndex = index
  },
  currentProductItem(state, payload) {
    if (isNaN(payload)) {
      state.productItem = state.productList[payload]
    } else {
      state.productItem = payload
    }
  }
}

const actions = {}

const getters = {
  getCurrentProductItem: (state) => {
    return state.productList[state.productIndex]
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
