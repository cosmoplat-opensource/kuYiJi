import { computed, reactive, ref } from 'vue'
import { _get, _post } from '@/utils/common-request'

function HooksPageList(
  url: string,
  { data = {}, method = 'get', pageSize = 10, pageNum = 1, run = true, cb = (res) => {} } = {}
) {
  const dataList = ref([])
  const defaultData = reactive(data)
  const refreshTag = ref(false)
  const pageEmpty = ref(false)
  const urlLink = ref(url)
  const callBack = cb
  const pageParams = reactive({
    pageNum,
    pageSize
  })
  const pageTotal = ref<number>(0)
  const dataNoMore = ref('loading')
  const dataEnd = computed(() => pageParams.pageNum * pageParams.pageSize >= pageTotal.value)

  // 数据加载
  function getPageList(currentData = defaultData) {
    const tempParams = { ...currentData }
    for (const key in tempParams) {
      if (tempParams[key] === '') {
        delete tempParams[key]
      }
    }
    dataNoMore.value = 'loading'
    const params = { url: urlLink.value, data: { ...tempParams, ...pageParams } }
    if (method === 'get') {
      _get(params).then(commonThen).finally(commonFinally)
    } else {
      _post(params).then(commonThen).finally(commonFinally)
    }
  }
  // get/post通用then
  function commonThen(res) {
    if (pageParams.pageNum === 1) {
      dataList.value = res.rows || res.data
    } else {
      dataList.value.push(...(res.rows || res.data))
    }
    pageTotal.value = res.total
    pageEmpty.value = !dataList.value.length
    callBack(res)
  }
  // get/post通用finally
  function commonFinally() {
    dataNoMore.value = dataEnd.value ? 'noMore' : 'more'
    refreshTag.value = false
  }
  // 下拉刷新
  function pageListRefresh() {
    dataList.value = []
    refreshTag.value = true
    pageParams.pageNum = 1
    getPageList()
  }
  // 加载更多
  function pageListLoadMore() {
    if (dataEnd.value) {
      return
    }
    pageParams.pageNum++
    getPageList()
  }
  // 查询参数更新
  function pageListSetParams(params, newUrl?) {
    if ((newUrl && urlLink.value === newUrl) || !newUrl) {
      Object.assign(defaultData, params)
    }

    if (newUrl && urlLink.value !== newUrl) {
      urlLink.value = newUrl
      if (!urlLink.value) {
        Object.assign(defaultData, params)
      } else {
        for (const key in defaultData) {
          delete defaultData[key]
        }
        Object.assign(defaultData, params)
      }
    }
    // Object.assign(defaultData, params)
    // newUrl && (urlLink.value = newUrl)
    pageListRefresh()
  }

  // 默认调用一次
  run && getPageList()

  return {
    dataList,
    pageEmpty,
    getPageList,
    pageParams,
    pageTotal,
    dataNoMore,
    pageListRefresh,
    pageListLoadMore,
    pageListSetParams,
    refreshTag
  }
}

export default HooksPageList
