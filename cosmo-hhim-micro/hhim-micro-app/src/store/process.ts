import { _post } from '@/utils/common-request'

const state: IProcessState = {
  item: {},
  // 根据选择的工序获取库存数量和待审核数量
  stockMaps: {}
}

const mutations = {
  updateProcess(state, payload) {
    state.item = payload
  },
  setStockMaps(state, payload) {
    payload?.forEach((v) => {
      state.stockMaps[v.itemSeq] = { unapprovedNum: v.unapprovedNum, totalStockNum: v.totalStockNum }
    })
  }
}

const actions = {
  getProcessStock({ commit, state }, { productSeq, processList }) {
    if (!processList.length) return
    _post({ url: '/process/selectStock', data: { productSeq, processList } }).then(
      (res: IResponseType<selectStock[]>) => {
        commit('setStockMaps', res.data)
      }
    )
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
