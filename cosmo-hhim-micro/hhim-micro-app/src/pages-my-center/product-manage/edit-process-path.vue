<template>
  <view class="h-full flex flex-col overflow-hidden bg-f3f3f5 edit-process-path">
    <uni-nav-bar />
    <h-status-header :title="pageTitle" :showBack="!previewShow" />
    <template v-if="previewShow">
      <view class="flex-1 overflow-hidden">
        <view
          class="overflow-auto box py-48 px-32 mx-16 mt-16 rounded-8 bg-fff m-h-100"
          :class="{ 'flex justify-center': singleFirst }"
          v-if="isSortOrder"
        >
          <preview-path :techData="techData" :singleFirst="singleFirst" />
        </view>
        <view class="overflow-auto box py-48 px-32 mx-16 mt-16 rounded-8 bg-fff m-h-100" v-else>
          <juxta-card :techData="techData" />
        </view>
      </view>
    </template>
    <template v-else>
      <product-display
        :sortType="sortType"
        :productName="productData.productName"
        @productShow="handleProductShow"
        @sortChange="handleSortChange"
      />
      <!--顺序工艺-->
      <order-position
        class="flex-1 overflow-hidden"
        v-if="isSortOrder"
        :sortType="sortType"
        :dataArr="dataArr"
        @addTarget="handleAddTarget"
        @deleteProcess="handleProcessDelete"
        @cardSelect="handleCardSelect"
      />
      <!--并序工艺-->
      <juxta-position
        class="flex-1 overflow-hidden"
        v-else
        :sortType="sortType"
        v-model="dataArr"
        @deleteProcess="handleProcessDelete"
        @cardSelect="handleCardSelect"
      />
      <view class="box flex align-center mx-16 bg-f7f8e7 px-16 h-64" v-if="noticeShow">
        <image src="/static/images/icon_tips.svg" class="icon-32" />
        <view class="flex-1 ml-16">
          <text class="font-24 color-5a6f82">保存为标准工艺后，有助于规范记工和审产</text>
        </view>
        <image @tap="handleCloseNotice" src="/static/images/icon_close_666.svg" class="icon-48" />
      </view>
    </template>
    <view class="py-32 flex-center">
      <h-button
        width="296"
        height="72"
        text="返回"
        type="bg-fff color-333"
        @tap.stop="cancelPreview"
        v-if="previewShow"
      />
      <h-button width="296" height="72" text="预览" type="bg-fff color-0066ff" @tap.stop="handlePreview" v-else />
      <h-button
        width="296"
        height="72"
        :text="techId == 0 ? '保存为标准工艺' : '保存'"
        class="ml-32"
        @tap.stop="handleSubmit"
      />
    </view>
    <h-status-footer />
    <h-popup />
    <popup-product-path :product="productData" ref="refPopupProductPath" @productSelected="handleProductSelected" />
    <popup-process-path
      v-if="isSortOrder"
      :productData="productData"
      :dataArray="dataArr"
      :sourceItem="dataOldItem"
      ref="refPopupProcessPath"
      @processSelected="handleProcessSelected"
    />
    <pupop-product-resemblance ref="refPopupProductResemblance" @popupBack="goBack" :isSortOrder="isSortOrder" />
  </view>
</template>

<script setup lang="ts">
import PopupProcessPath from '@/pages-my-center/product-manage/components/popup-process-path.vue'
import { computed, reactive, ref } from 'vue'
import { _get, _post } from '@/utils/common-request'
import { onLoad } from '@dcloudio/uni-app'

import PopupProductPath from '@/pages-my-center/product-manage/components/popup-product-path.vue'
import PupopProductResemblance from '@/pages-my-center/product-manage/components/pupop-product-resemblance.vue'
import ProductDisplay from '@/pages-my-center/product-manage/edit-process/product-display.vue'
import OrderPosition from '@/pages-my-center/product-manage/edit-process/order-position.vue'
import JuxtaPosition from '@/pages-my-center/product-manage/edit-process/juxta-position.vue'
import JuxtaCard from '@/pages-my-center/product-manage/edit-process/juxta-card.vue'

const baseObj = {
  isFirstProcess: '1',
  isLastProcess: '1',
  parentProcessId: 0,
  parentProcessSeq: '0',
  processCode: '',
  processId: '',
  processName: '',
  processSeq: ''
}

const sortType = ref('顺序')
const isSortOrder = computed(() => sortType.value === '顺序')
function handleSortChange() {
  if (!isSortOrder.value) {
    uni.showModal({
      title: '提示',
      content: '转为顺序后将清空已录入的工序，确认转为顺序吗？',
      success: (res) => {
        if (res.confirm) {
          sortType.value = sortType.value === '顺序' ? '并序' : '顺序'
          dataArr.value = [{ ...baseObj, targetArr: [{ ...baseObj }] }]
        }
      }
    })
  } else {
    sortType.value = sortType.value === '顺序' ? '并序' : '顺序'
  }
}
const noticeShow = computed(() => {
  return techId.value == 0 && !uni.getStorageSync('tech_notice_show')
})
function handleCloseNotice() {
  uni.setStorageSync('tech_notice_show', '1')
}

const dataArr = ref<IProcessPath[]>([])
const dataIndex = ref(0)
const dataTargetIndex = ref(0)
const dataOldItem = reactive<IProcessItem>({ ...baseObj })
const refPopupProductResemblance = ref(null)
const productData = reactive<Partial<TProductItem>>({})
const techId = ref(0)
const techData = ref<IProcessPath[]>([])
const techStandard = ref(false)
const previewShow = ref(false)
const techPattern = ref(0)

onLoad((options) => {
  Object.assign(productData, JSON.parse(options.productData) as TProductItem)
  techId.value = options.techId
  techStandard.value = options.standard === 'true'
  techPattern.value = Number(options.techPattern)
  if (options.techId) {
    // 组装需要回显的数据
    techData.value = JSON.parse(options.techData) as IProcessPath[]
    if (techPattern.value === 20) {
      sortType.value = '并序'
      dataArr.value = techData.value
    } else {
      composeDataArr()
    }
  } else {
    // 初始化一组数据
    dataArr.value.push({
      ...baseObj,
      targetArr: [{ ...baseObj }]
    })
  }
})

const pageTitle = computed(() => {
  return previewShow.value ? '预览工艺路线' : '编辑工艺路线'
})
/** 根据已有的数据组合编辑页面回显的数据结构 */
function composeDataArr(obj?) {
  if (obj && obj.parentProcessId === '0') return
  let parentArr = []
  if (obj) {
    parentArr = techData.value.filter((v) => v.processId === obj.parentProcessId)
  } else {
    parentArr = techData.value.filter((v) => v.isLastProcess === '0')
  }
  const firstItem = { ...parentArr[0], targetArr: [] }
  parentArr.forEach((p) => {
    const obj2 = techData.value.find((t) => t.processId === p.parentProcessId)
    if (obj2) {
      if (obj2.parentProcessId === 0) obj2.isFirstProcess = '0'
      firstItem.targetArr.push(obj2)
      composeDataArr(p)
    }
  })
  // 如果不是首序且没有下一级,添加一个空节点
  if (firstItem.isFirstProcess !== '0' && !firstItem.targetArr.length) {
    firstItem.targetArr = [{ processId: '', processName: '', processCode: '', processSeq: '' }]
  }
  dataArr.value.unshift(firstItem)
}
/** 节点选择 */
const refPopupProcessPath = ref(null)
const nodeType = ref('left')
const copy = ref(false)
function handleCardSelect(index, type, tIndex?) {
  if (isSortOrder.value) {
    if (type === 'left' && index > 0) return
    dataIndex.value = index
    nodeType.value = type
    dataTargetIndex.value = tIndex
    if (type === 'left') {
      Object.assign(dataOldItem, dataArr.value[dataIndex.value])
    }
    Object.assign(dataOldItem, dataArr.value[dataIndex.value].targetArr[dataTargetIndex.value])
    refPopupProcessPath.value.handlePopupOpen()
  } else {
    const item = dataArr.value[index]
  }
}
const refPopupProductPath = ref(null)
function handleProductShow() {
  refPopupProductPath.value.handlePopupOpen()
}
function handleProductSelected(item) {
  _get({ url: `/tech/${item.id}` }).then((res: IResponseType<{ standard: boolean; techData: IProcessPath[] }>) => {
    techData.value = res.data.techData.map((v) => {
      return {
        ...v,
        isFirstProcess: v.parentProcessId === 0 ? '0' : '1'
      }
    })
    if (res.data.techPattern === 20) {
      sortType.value = '并序'
      dataArr.value = techData.value
    } else {
      sortType.value = '顺序'
      dataArr.value = []
      composeDataArr()
    }
    copy.value = true
  })
}
/** 节点选择回调 */
function handleProcessSelected(item) {
  const { processId, processName, processCode, processSeq, isFirstProcess } = item
  // 如果是尾序(左侧节点)
  if (nodeType.value === 'left') {
    if (dataIndex.value === 0) {
      const obj = dataArr.value[0]
      obj.isLastProcess = '0'
      obj.processId = processId
      obj.processName = processName
      obj.processCode = processCode
      obj.processSeq = processSeq
      obj.isFirstProcess = isFirstProcess
      if (isFirstProcess === '0') {
        obj.targetArr = []
      } else {
        if (!obj.targetArr.length) {
          obj.targetArr = [{ ...baseObj }]
        }
      }
      Object.assign(dataArr.value[0], obj)
    }
  } else {
    // 右侧节点
    Object.assign(dataArr.value[dataIndex.value].targetArr[dataTargetIndex.value], item)
    const obj = dataArr.value.find((v) => v.processId === dataOldItem.processId)
    if (obj) {
      obj.processId = processId
      obj.processName = processName
      obj.processCode = processCode
      obj.processSeq = processSeq
      obj.isFirstProcess = isFirstProcess
      if (isFirstProcess === '0') {
        obj.targetArr = []
      } else {
        if (!obj.targetArr.length) {
          obj.targetArr = [{ ...baseObj }]
        }
      }
    } else {
      if (isFirstProcess === '0') {
        dataArr.value.push({ ...item, targetArr: [] })
      } else {
        dataArr.value.push({ ...item, targetArr: [{ ...baseObj }] })
      }
    }
  }
}
/** 节点删除 */
function handleProcessDelete(index) {
  if (index === 0) {
    dataArr.value = []
    dataArr.value.push({
      ...baseObj,
      targetArr: [{ ...baseObj }]
    })
    return
  }
  const item = dataArr.value[index]
  deleteProcessDp(item.processId)
  // 删除右侧的节点
  dataArr.value.forEach((v) => {
    const index = v.targetArr.findIndex((t) => t.processId === item.processId)
    index > -1 && v.targetArr.splice(index, 1)
  })
}
function deleteProcessDp(pid) {
  const index = dataArr.value.findIndex((v) => v.processId === pid)
  if (index === -1) return
  dataArr.value[index].targetArr.forEach((t, ti) => {
    deleteProcessDp(t.processId)
  })
  dataArr.value.splice(index, 1)
}
/** 添加节点 */
function handleAddTarget(index) {
  dataArr.value[index].targetArr.push({ ...baseObj })
}
const submitLoading = ref(false)
/** 提交 */
function handleSubmit() {
  // 并序提交判断
  if (!isSortOrder.value && dataArr.value.length) {
    if (dataArr.value.length === 1) {
      uni.showToast({ title: '请至少添加两个工序', icon: 'none' })
      return
    }
    if (dataArr.value.filter((v) => v.isLastProcess === '0').length > 1) {
      uni.showToast({ title: '有且仅有一个尾序', icon: 'none' })
      return
    }
    if (dataArr.value.filter((v) => v.isFirstProcess === '0').length === 0) {
      uni.showToast({ title: '最少有一个首序', icon: 'none' })
      return
    }
  }
  uni.showModal({
    title: '提示',
    content: '是否确认保存为标准工序',
    success: (res) => {
      if (res.confirm) {
        handleSubmitNext()
      }
    }
  })
}
function handleSubmitNext() {
  if (submitLoading.value) return
  submitLoading.value = true
  const chainList = createChainList()
  const params = {
    standard: techStandard.value,
    clone: copy.value,
    productSeq: productData.productSeq,
    productName: productData.productName,
    productId: productData.id,
    techPattern: isSortOrder.value ? 10 : 20,
    chainList
  }
  if (techId.value) {
    Object.assign(params, { techId: techId.value })
  }
  uni.showLoading({ title: '保存中' })
  _post({ url: '/tech/bind', data: params })
    .then((res: IResponseType<{ coverProductList: []; similarProductList: [] }>) => {
      const { coverProductList, similarProductList } = res.data
      if (coverProductList?.length) {
        refPopupProductResemblance.value.handlePopupOpen(coverProductList, 'coverProductList', chainList)
      } else if (similarProductList?.length) {
        refPopupProductResemblance.value.handlePopupOpen(similarProductList, 'similarProductList', chainList)
      } else {
        goBack()
      }
    })
    .finally(() => {
      setTimeout(() => {
        uni.hideLoading()
        submitLoading.value = false
      }, 1000)
    })
}

function goBack() {
  uni.navigateBack()
}
/** 生成提交的数据 */
function createChainList() {
  const chainList = []
  // 如果连尾序都没有直接返回空数组
  if (dataArr.value.length === 1 && !dataArr.value[0].processName) {
    return chainList
  }
  dataArr.value.forEach((l) => {
    // 如果是并序
    if (!isSortOrder.value) {
      const obj = { ...l, parentProcessId: -1, parentProcessSeq: '-1', isLastProcess: l.isLastProcess || '1' }
      if (l.isFirstProcess === '0') {
        obj.parentProcessId = 0
        obj.parentProcessSeq = '0'
      }
      chainList.push(obj)
    } else if (
      l.isFirstProcess === '0' ||
      !l.targetArr.length ||
      (l.targetArr.length === 1 && !l.targetArr[0].processName)
    ) {
      const obj = { ...l, parentProcessId: 0, parentProcessSeq: '0', isLastProcess: l.isLastProcess || '1' }
      delete obj.targetArr
      delete obj.isFirstProcess
      chainList.push(obj)
    } else {
      l.targetArr.forEach((r) => {
        const obj = {
          ...l,
          parentProcessId: r.processId,
          parentProcessSeq: r.processSeq
        }
        delete obj.isFirstProcess
        delete obj.targetArr
        chainList.push(obj)
      })
    }
  })
  return chainList
}
const singleFirst = ref(false)
/** 预览 */
function handlePreview() {
  if (dataArr.value.length === 1 && dataArr.value[0].processName === '') {
    uni.showToast({
      title: '请先添加工序',
      icon: 'none',
      duration: 2000
    })
    return
  }
  techData.value = createChainList()
  singleFirst.value = techData.value.filter((v) => v.parentProcessId === 0).length === 1
  previewShow.value = true
}
/** 预览关闭 */
function cancelPreview() {
  previewShow.value = false
}
</script>

<style lang="scss">
.edit-process-path {
  .parent-add {
    position: absolute;
    right: px2vw(-80);
    top: 50%;
    transform: translateY(-50%);
  }
  .btn-sort:active {
    background-color: #dbeaff;
  }
}
</style>
