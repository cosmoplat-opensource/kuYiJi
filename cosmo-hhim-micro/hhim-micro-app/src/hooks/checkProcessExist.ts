import { _get } from '@/utils/common-request'

const HooksCheckProcessExist = async (productItem, processItem, cb_success, cb_fail) => {
  let checkLast = null
  if (processItem.isLastProcess === '0' || processItem.isFirstProcess === '0') {
    const processType = processItem.isLastProcess === '0' ? (processItem.isFirstProcess === '0' ? 2 : 1) : 0
    try {
      checkLast = await _get({
        url: '/submit/isOrNotHaveFirstOrLastProcess',
        data: { productSeq: productItem.itemSeq || null, processSeq: processItem.itemSeq || null, processType }
      })
    } catch (e) {
      cb_fail()
    }
    let content = ''
    switch (checkLast.data.isOrNotMutex) {
      case '0':
        if (processItem.isFirstProcess === '0') {
          content = '该工序已被设置为最后一道工序,确认记工吗?'
        } else {
          content = '该工序已被设置为首序,确认记工吗?'
        }
        uni.showModal({ title: '提示', content, success: (res) => (res.confirm ? cb_success() : cb_fail()) })
        break
      case '1':
        if (processItem.isFirstProcess === '0') {
          content = `该产品已设置\"${checkLast.data.processName}\"为首序,确认当前工序为首序吗?`
        } else {
          content = `该产品已设置\"${checkLast.data.processName}\"为最后一道工序,确认当前工序为最后一道工序吗?`
        }
        if (checkLast.data.firstProcessName && checkLast.data.lastProcessName) {
          content = `该产品已设置\"${checkLast.data.firstProcessName}\"为首序且最后一道工序,确认当前工序为首序且最后一道工序吗?`
        } else if (checkLast.data.firstProcessName) {
          content = `该产品已设置\"${checkLast.data.firstProcessName}\"为首序,确认当前工序为首序吗?`
        } else if (checkLast.data.lastProcessName) {
          content = `该产品已设置\"${checkLast.data.lastProcessName}\"为最后一道工序,确认当前工序为最后一道工序吗?`
        }
        uni.showModal({
          title: '提示',
          content,
          success: (res) => (res.confirm ? cb_success() : cb_fail())
        })
        break
      default:
        cb_success()
    }
  } else {
    cb_success()
  }
}

export default HooksCheckProcessExist
