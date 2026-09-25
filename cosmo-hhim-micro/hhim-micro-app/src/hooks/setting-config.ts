import { _get, _put } from '@/utils/common-request'
import { $store } from '@/utils/common'
import { requireAuth } from '@/utils/auth-guard'
import { computed, reactive } from 'vue'

type SetItem = {
  id: number
  submitInspectSwitch: string
  batchSubmitSwitch: string
  workerBaseDataConfine: string
}

const HooksSettingConfig = () => {
  const configItem = reactive<SetItem>({
    submitInspectSwitch: '1',
    id: 0,
    batchSubmitSwitch: '1',
    workerBaseDataConfine: '1'
  })
  // 防重入：上一次保存未完成时忽略新的切换点击（连点/快速切多个开关只发一个请求）
  let saving = false
  async function handleConfigChange(type) {
    if (saving) return
    saving = true
    try {
      switch (type) {
        case 'submitInspectSwitch':
          $store.commit('setting/setSubmitInspectSwitch', checkSubmitInspect.value ? '1' : '0')
          break
        case 'batchSubmitSwitch':
          $store.commit('setting/setBatchSubmitSwitch', checkBatchSubmit.value ? '1' : '0')
          break
        case 'workerBaseDataConfine':
          $store.commit('setting/setWorkerBaseDataConfine', checkWorkerBaseDataConfine.value ? '1' : '0')
          break
      }
      const params = {
        submitInspectSwitch: $store.state.setting.submitInspectSwitch,
        batchSubmitSwitch: $store.state.setting.batchSubmitSwitch,
        workerBaseDataConfine: $store.state.setting.workerBaseDataConfine,
        id: $store.state.setting.id
      }
      await _put({ url: '/setting/updateIndividuationConfig', data: params })
      getIndividuationConfig()
    } finally {
      saving = false
    }
  }
  function getIndividuationConfig() {
    // decouple-from-ops-platform-cleanup (C.9): 严格契约守卫
    // 该接口需要 ThreadContext 的 schema/customer（由 CertificateInterceptor 从 Redis userInfo 解析）
    if (!requireAuth({ onFail: 'return' })) return
    _get({ url: '/setting/getIndividuationConfig' }).then((res: IResponseType<SetItem>) => {
      // 契约守卫：后端可能在租户无配置记录时返回 data: null，此处不能直接解构
      if (!res?.data) return
      $store.commit('setting/setSubmitInspectSwitch', res.data.submitInspectSwitch)
      $store.commit('setting/setBatchSubmitSwitch', res.data.batchSubmitSwitch)
      $store.commit('setting/setWorkerBaseDataConfine', res.data.workerBaseDataConfine)
      $store.commit('setting/setId', res.data.id)
    })
  }

  // 送检
  const checkSubmitInspect = computed(() => $store.state.setting.submitInspectSwitch === '0')

  // 批量记工
  const checkBatchSubmit = computed(() => $store.state.setting.batchSubmitSwitch === '0')
  // 限制员工新增工序或产品开关（0:开启，1:关闭）
  const checkWorkerBaseDataConfine = computed(() => $store.state.setting.workerBaseDataConfine === '0')

  getIndividuationConfig()

  return {
    configItem,
    handleConfigChange,
    checkSubmitInspect,
    checkBatchSubmit,
    checkWorkerBaseDataConfine,
    getIndividuationConfig
  }
}

export default HooksSettingConfig
