import { _get } from '@/utils/common-request'
import { $store } from '@/utils/common'

const state: IGuideModules = {
  step: {},
  stepIndex: 1,
  shareTimeLine: false
}

const mutations = {
  updateStepItem(state, payload) {
    Object.assign(state.step, payload)
  },
  updateStepIndex(state, payload) {
    state.stepIndex = payload
  },
  setShareTimeLine(state, payload) {
    state.shareTimeLine = payload
  }
}

const actions = {
  async checkGuideStep(context, payload) {
    const guideRes = (await _get({ url: '/experience/guide/guideFlag' })) as IResponseType<{}>
    $store.commit('experience/setGuideCode', guideRes.data)
    switch (guideRes.data) {
      case 'QUESTION_GUIDE_UNDONE':
        uni.navigateTo({ url: '/pages-guide/index' })
        break
      // case 'NORMAL_GUIDE_UNDONE':
      //   uni.navigateTo({ url: '/pages-experience/path/index' })
      //   break
      default:
        $store.commit('tabBar/setTabShow', true)
        $store.commit('tabBar/setTabActiveByName', '工作台')
        break
    }
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
