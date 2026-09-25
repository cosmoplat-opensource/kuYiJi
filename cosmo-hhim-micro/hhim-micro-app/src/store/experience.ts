import { _get } from '@/utils/common-request'

const state: IExperienceModules = {
  isExperience: false,
  step: {},
  stepIndex: 1,
  guideCode: '',
  stepList: [
    {
      index: 1,
      status: '',
      nodeCode: '',
      elapsedTime: '3.56',
      startDate: '',
      bg: 'img_step_1',
      bgDone: 'img_step_1_done',
      label: '记工'
    },
    {
      index: 2,
      status: '',
      nodeCode: '',
      startDate: '',
      elapsedTime: '4.22',
      bg: 'img_step_2',
      bgDone: 'img_step_2_done',
      label: '审核'
    },
    {
      index: 3,
      status: '',
      nodeCode: '',
      startDate: '',
      bg: 'img_step_3',
      bgDone: 'img_step_3_done',
      label: '库存调整'
    },
    {
      index: 4,
      status: '',
      nodeCode: '',
      startDate: '',
      bg: 'img_step_4',
      label: '分析报表'
    }
  ]
}

const mutations = {
  setIsExperience(state, payload) {
    state.isExperience = payload
  },
  updateStepItem(state, payload) {
    Object.assign(state.step, payload)
  },
  updateStepIndex(state, payload) {
    state.stepIndex = payload
  },
  setGuideCode(state, payload) {
    state.guideCode = payload
  }
}

const actions = {
  getExperienceStepList({ commit, state }) {
    _get({ url: '/experience/guide/guideList', data: { groupCode: '20000' } }).then((res: any) => {
      let doneCount = 0
      res.data.forEach((v, i) => {
        const item = state.stepList[i]
        item.nodeCode = v.nodeCode
        item.status = v.status
        item.elapsedTime = v.elapsedTime
        if (v.status === '1') {
          doneCount++
        }
        commit('updateStepItem', item)
      })
      commit('updateStepIndex', doneCount + 1)
    })
  },
  getOfficialUseFlag() {
    _get({ url: '/setting/getOfficialUseFlag' }).then((res: any) => {
    })
  }
}

const getters = {
  getCurrentStep(state) {
    return state.stepList[state.stepIndex - 1]
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
