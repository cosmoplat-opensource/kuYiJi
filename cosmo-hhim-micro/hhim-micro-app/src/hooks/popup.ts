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
