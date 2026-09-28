/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author cosmo-hhim-open Team
 */
const state: IUserState = {
  userInfo: {
    id: null,
    userName: '',
    token: '',
    sex: '',
    remark: '',
    nickName: '',
    phonenumber: '',
    phoneNumber: '', //体验手机号 没用到存到存储里了  experiencePhoneNum
    userType: '',
    avatar: '/static/images/img_profile_default@2x.png',
    wxCode: '',
    phoneCode: '',
    roleCode: '',
    roleName: '',
    tenantCode: '',
    tenantName: '',
    personalRecommend: 3
  }
}

const mutations = {
  setUserInfo(state, payload) {
    // payload.roleCode = '25'
    Object.assign(state.userInfo, payload)
  }
}

const actions = {}

const getters = {}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
