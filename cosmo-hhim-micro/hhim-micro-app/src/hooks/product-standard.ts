import { $store } from '@/utils/common'
import { computed, ref } from 'vue'
import { _get } from '@/utils/common-request'

type ProcessType = {
  processName: string
  processSeq: string
  processCode: string
}

const HooksProductStandard = () => {
  /** 标准工艺下的前工序列表 */
  const standardPreProcess = ref([])
  function getStandardPreProcess(productSeq, operateProcessSeq) {
    _get({ url: '/process/defaultRecommend', data: { productSeq, operateProcessSeq } }).then(
      async (res: IResponseType<ProcessType[]>) => {
        standardPreProcess.value = res.data
        const seq = res.data?.map((v) => v.processSeq)
        await $store.dispatch('process/getProcessStock', {
          productSeq: productSeq,
          processList: seq
        })
      }
    )
  }

  /** 检查产品是否有标准工艺 */
  function checkProductHasStandard(productSeq) {
    // 如果有产品seq,调接口判断是否包含标准工艺,如果没有,直接设置为false
    standardPreProcess.value = []
    if (productSeq) {
      _get({ url: '/tech/isOrNotHaveStandardTech', data: { productSeq } }).then((res: IResponseType<boolean>) => {
        $store.commit('report/setStandard', !!res.data)
      })
    } else {
      $store.commit('report/setStandard', false)
    }
  }
  const checkIsStandard = computed(() => {
    return $store.state.report.standard
  })

  // 组装提交的参数
  function assembleSubmitParams(params) {
    if (standardPreProcess.value.length) {
      const names = []
      const codes = []
      const seqs = []
      standardPreProcess.value.forEach((item) => {
        names.push(item.processName)
        codes.push(item.processCode)
        seqs.push(item.processSeq)
      })
      params.preProcessName = names.join(',')
      params.preProcessCode = codes.join(',')
      params.preProcessSeq = seqs.join(',')
    }
    return params
  }
  return {
    standardPreProcess,
    checkIsStandard,
    getStandardPreProcess,
    checkProductHasStandard,
    assembleSubmitParams
  }
}

export default HooksProductStandard
