export function checkSubmitRule(prod, pre, now, item, passKey = 'passNum', ngKey = 'ngNum', standard = false) {
  if (item[passKey] && !/^\d+(\.\d{1,4})?$/.test(item[passKey])) {
    uni.showToast({
      title: '良品数量应为非负数且最多保留4位小数',
      icon: 'none'
    })
    return false
  }
  if (item[ngKey] && !/^\d+(\.\d{1,4})?$/.test(item[ngKey])) {
    uni.showToast({
      title: '不良品数量应为非负数且最多保留4位小数',
      icon: 'none'
    })
    return false
  }
  if (item[passKey] == 0 && item[ngKey] == 0) {
    uni.showToast({
      title: '良品和不良品的数量不能同时为0',
      icon: 'none'
    })
    return false
  }
  if (!item[passKey] && !item[ngKey]) {
    uni.showToast({
      title: '良品数量/不良品数量\n请至少录入一个',
      icon: 'none'
    })
    return false
  }
  if (!prod.itemName) {
    uni.showToast({
      title: '请录入产品',
      icon: 'none'
    })
    return false
  }
  if (!now.itemName) {
    uni.showToast({
      title: '请录入报工工序',
      icon: 'none'
    })
    return false
  }
  if (pre.itemName === now.itemName && pre.itemSeq === now.itemSeq && !standard) {
    uni.showToast({
      title: '前工序和报工工序不能为同一道工序',
      icon: 'none'
    })
    return false
  }
  return true
}
