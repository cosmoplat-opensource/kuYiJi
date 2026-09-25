import { ref } from 'vue'
import { _get } from '@/utils/common-request'

const HooksExportEmail = () => {
  const exportEmail = ref(null)
  const exportLoading = ref(false)
  function exportToMail(startDate = '', endDate = '') {
    exportEmail.value.openExportToMail({ startDate, endDate })
  }
  const exportUrl = ref('')
  const resquestData = ref({})
  function setExportUrl(url, data = {}) {
    exportUrl.value = url
    resquestData.value = data
  }
  function confirmExportPopup(receivedBy = '') {
    uni.showLoading({
      title: '发送中'
    })
    if (receivedBy) {
      resquestData.value.receivedBy = receivedBy
    }
    exportLoading.value = true
    _get({ url: `${exportUrl.value}`, data: resquestData.value })
      .then((res) => {
        exportEmail.value.closePopup()
        uni.showToast({
          title: '导出成功',
          duration: 2000,
          icon: 'none'
        })
        exportLoading.value = false
      })
      .catch(() => {
        exportLoading.value = false
      })
      .finally(() => {
        uni.hideLoading()
        exportLoading.value = false
      })
  }

  return {
    exportEmail,
    exportToMail,
    exportLoading,
    setExportUrl,
    confirmExportPopup
  }
}

export default HooksExportEmail
