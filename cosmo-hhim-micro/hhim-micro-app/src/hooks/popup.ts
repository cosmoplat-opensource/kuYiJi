/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author cosmo-hhim-open Team
 */
import { $state, $store } from '@/utils/common'

const HooksPopup = () => {
  function popupOpen(str) {
    $store.commit('popup/updatePopupContent', str)
    str && $state.popup.popupRefList[getCurrentPages()[getCurrentPages().length - 1]?.route]?.open()
  }
  function popupOpenImage(path) {
    $store.commit('popup/updatePopupImagePath', path)
    path && $state.popup.popupRefList[getCurrentPages()[getCurrentPages().length - 1]?.route]?.open()
  }

  return {
    popupOpen,
    popupOpenImage
  }
}

export default HooksPopup
