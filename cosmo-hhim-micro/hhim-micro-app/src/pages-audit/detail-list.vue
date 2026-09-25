<template>
  <view class="h-full flex flex-col overflow-hidden bg-f3f3f5">
    <uni-nav-bar />
    <h-status-header title="记工明细" />
    <view class="color-5a6f82 font-24 mt-16 px-48 flex align-center">
      <text>产品</text>
      <view class="color-333 mx-8 bold">
        <h-text-display :text="productName" :width="212" />
      </view>
      <text class="mx-16">|</text>
      <text>报工工序</text>
      <view class="ml-8 color-333 bold">
        <h-text-display :text="processName" :width="212" />
      </view>
    </view>
    <!--搜索区域-->
    <view class="box flex align-center pt-24 pl-32 pr-24 pb-16 flex">
      <h-search @searchInput="handleInput" class="flex-1" bgClass="bg-fff-80" placeholder="输入员工姓名" />
    </view>
    <view class="flex align-center px-16 box h-80 bg-fff rounded-16-top mx-16">
      <template v-if="!checkAble">
        <image src="/static/images/icon_tips.svg" class="icon-32 ml-16" />
        <view class="color-5a6f82 font-24 ml-8">审核后自动跳到下一个</view>
      </template>
      <template v-else>
        <view class="font-24 ml-16" v-if="checkAble">
          <text class="color-333">已选</text>
          <text class="color-ff0000 bold ml-4">{{ getCheckedCount }}</text>
        </view>
      </template>
      <view class="flex-1" />
      <view
        class="font-24 ml-16 px-16 py-10 rounded-24"
        :class="checkAll ? 'color-fff bg-0066ff' : 'color-5a6f82 bg-f3f3f5'"
        v-if="checkAble"
        @tap="handleCheckAll"
      >
        {{ checkAll ? '全不选' : '全选' }}
      </view>
      <view
        class="font-24 bold px-16 py-12 ml-16 rounded-24 bg-f3f3f5 line-height-24"
        :class="checkAble ? 'color-0066ff' : 'color-5a6f82'"
        @tap="handleCheckedAble(!checkAble)"
      >
        {{ checkAble ? '取消' : '批量' }}
      </view>
    </view>
    <!--数据列表滚动区域-->
    <view class="flex-1 flex flex-col" v-if="pageEmpty">
      <h-empty v-if="pageEmpty" tipsWord="暂无记工明细数据" class-name="pt-240" />
    </view>
    <scroll-view v-else scroll-y class="flex-1 flex flex-col mt-8 overflow-hidden" enable-flex>
      <list-item
        v-for="(item, index) in dataList"
        :checkAll="checkAble"
        :key="index"
        :dataItem="item"
        @itemCheck="handleItemCheck"
        @isLastCheck="handleItemLastCheck"
        @isFirstCheck="handleItemFirstCheck"
      />
    </scroll-view>
    <view class="px-48 pb-32 box mt-16 flex align-center justify-center">
      <h-button height="72" width="296" text="驳回" @tap="handleReject" type="bg-fff color-ff0000" active="red" />
      <h-button height="72" width="296" text="审核通过" @tap="handleSubmit" class="ml-32" />
    </view>
    <h-status-footer />
    <h-popup />
  </view>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'
import ListItem from './detail-list-item.vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import { _delete, _get, _post } from '@/utils/common-request'

import HEmpty from '@/components/h-empty.vue'
import HPopup from '@/components/h-popup.vue'
import HooksPopup from '@/hooks/popup'
import bigNumber from 'bignumber.js'
import HooksProductStandard from '@/hooks/product-standard'
import HTextDisplay from '@/components/h-text-display.vue'
import { collectClick } from '@/utils/common'
// 获取列表
const dataList = ref([])
const ids = ref('')
const productName = ref('')
const processName = ref('')
const productSeq = ref('')
onLoad((options) => {
  ids.value = options.ids
  productName.value = options.product
  processName.value = options.process
  productSeq.value = options.productSeq
  getList()
  checkProductHasStandard(options.productSeq)
  uni.$on('auditDetailListRefresh', () => {
    pageStatus.value = 'success'
    getList()
  })
})
onUnload(() => {
  uni.$off('auditDetailListRefresh')
})
const pageEmpty = ref(false)
const { checkProductHasStandard, checkIsStandard } = HooksProductStandard()

function getList(submitNickName = '') {
  pageEmpty.value = false
  _get({ url: `/submit/getInfos`, data: { ids: ids.value, submitNickName, submitStatus: 1 } })
    .then((res: IResponseType<IDetailListItem[]>) => {
      dataList.value = res.data
        .filter((v) => v.submitStatus !== 0)
        .map((v, i) => {
          v.checked = i === selectedIndex.value
          return v
        })
      if (!dataList.value.length && pageStatus.value === 'success') {
        setTimeout(() => {
          uni.navigateBack()
        }, 800)
      }
    })
    .finally(() => {
      pageEmpty.value = !dataList.value.length
      checkAll.value = false
    })
}
const searchInput = ref('')
function handleInput(str) {
  pageStatus.value = 'normal'
  getList(str)
}
function handleInputClear() {
  searchInput.value = ''
  getList('')
}

// 全选操作
const checkAble = ref(false)
const checkAll = ref(false)
const selectedIndex = ref(0)
const getCheckedCount = computed(() => {
  return dataList.value.filter((v) => v.checked).length
})
function handleCheckedAble(val) {
  checkAble.value = val
  checkAll.value = val
  dataList.value.forEach((v, i) => {
    v.checked = val
  })
  if (!val) {
    dataList.value[0].checked = true
  }
  selectedIndex.value = -1
}
function handleCheckAll() {
  checkAll.value = !checkAll.value
  dataList.value.forEach((v) => {
    v.checked = checkAll.value
  })
}
function handleItemCheck(item) {
  if (!checkAble.value) {
    dataList.value.forEach((v, i) => {
      if (v.id === item.id) {
        selectedIndex.value = i
        v.checked = !v.checked
      } else {
        v.checked = false
      }
    })
  } else {
    const obj = dataList.value.find((v) => v.id === item.id)
    obj && (obj.checked = !obj.checked)
    checkAll.value = dataList.value.every((v) => v.checked)
  }
}
function handleItemLastCheck(item) {
  if (checkIsStandard.value) {
    uni.showModal({
      title: '提示',
      content: '已规定标准工艺路线,不可更改勾选信息',
      showCancel: false
    })
    return
  }
  const obj = dataList.value.find((v) => v.id === item.id)
  obj && (obj.isLastProcess = obj.isLastProcess === '1' ? '0' : '1')
}
function handleItemFirstCheck(item) {
  if (checkIsStandard.value) {
    uni.showModal({
      title: '提示',
      content: '已规定标准工艺路线,不可更改勾选信息',
      showCancel: false
    })
    return
  }
  const obj = dataList.value.find((v) => v.id === item.id)
  obj && (obj.isFirstProcess = obj.isFirstProcess === '1' ? '0' : '1')
}

// 审核操作
const pageStatus = ref('normal')
function handleReject() {
  let ids = []
  if (checkAble.value) {
    ids = dataList.value.filter((v) => v.checked).map((v) => v.id)
  } else {
    ids = [dataList.value[0].id]
  }
  if (!ids.length) {
    uni.showToast({ title: '请选择需要驳回的数据', icon: 'none' })
    return
  }
  uni.showModal({
    title: '提示',
    content: ids.length === 1 ? '确认要驳回该条记工吗?' : '是否确认驳回?',
    success: function (res) {
      if (res.confirm) {
        _get({ url: '/submit/rejectRecord', data: { ids: ids.join(',') } }).then((res: IResponseType<unknown>) => {
          if (res.msg.includes('已被驳回')) {
            popupOpen(res.msg)
          } else {
            uni.showToast({ title: '驳回成功', icon: 'none' })
            pageStatus.value = 'success'
            getList()
            uni.$emit('auditListRefresh')
            // uni.$emit('auditDetailRefresh')
          }
        })
      }
    }
  })
}
async function handleSubmit() {
  let checkList = []
  if (checkAble.value) {
    checkList = dataList.value.filter((v) => v.checked)
  } else {
    checkList = [dataList.value[0]]
  }
  if (!checkList.length) {
    uni.showToast({ title: '请选择需要审核的数据', icon: 'none' })
    return
  }
  const { operateProcessSeq, productSeq, isLastProcess, isFirstProcess } = checkList[0]
  const temp_productItem = { itemSeq: productSeq }
  const lastItem = { processSeq: operateProcessSeq, itemSeq: operateProcessSeq, isLastProcess, isFirstProcess }
  let error = false
  const ids = checkList.map((v) => {
    if (v.isLastProcess !== lastItem.isLastProcess || v.isFirstProcess !== lastItem.isFirstProcess) {
      error = true
    }
    return {
      id: v.id,
      isLastProcess: v.isLastProcess,
      isFirstProcess: v.isFirstProcess,
      exceptionNumsAll: v.exceptionNumsAll,
      lowPassRateFlag: v.lowPassRateFlag,
      negativeStockFlag: v.negativeStockFlag,
      overProductiveCapacityFlag: v.overProductiveCapacityFlag,
      preProcessName: v.preProcessName,
      avgPassRateByDay: v.avgPassRateByDay,
      avgProductionCapacityByDay: v.avgProductionCapacityByDay
    }
  })
  if (error) {
    uni.showModal({
      title: '提示',
      content: '请统一所选项目的首序或尾序',
      showCancel: false,
      confirmText: '我知道了'
    })
    return
  }
  // 23-03-15 正荣说取消负库存等的提示
  submitRequest(ids)
  // await checkProcessExist(
  //   temp_productItem,
  //   lastItem,
  //   () => submitNext(ids),
  //   () => {
  //     dataList.value
  //       .filter((v) => v.checked)
  //       .map((v) => {
  //         v.isFirstProcess = '1'
  //         v.isLastProcess = '1'
  //       })
  //   }
  // )
}
function submitNext(ids) {
  let exceptionNumsAll = 0
  ids.forEach((v) => {
    if (v.lowPassRateFlag === '0' || v.negativeStockFlag === '0' || v.overProductiveCapacityFlag === '0') {
      exceptionNumsAll++
    }
  })
  if (checkAble.value) {
    //判断是否有异常，有则提示数量
    if (exceptionNumsAll > 0) {
      uni.showModal({
        title: '提示',
        content: '审核包含' + ids.length + '条记工明细，有' + exceptionNumsAll + '条数据可能存在异常，是否确认审核?',
        success: function (res) {
          if (res.confirm) {
            submitRequest(ids)
          }
        }
      })
    } else {
      uni.showModal({
        title: '提示',
        content: '是否确认审核通过?',
        success: function (res) {
          if (res.confirm) {
            submitRequest(ids)
          }
        }
      })
    }
  } else {
    let item = ids[0]
    if (item.negativeStockFlag === '0') {
      //负库存 优先级1
      uni.showModal({
        title: '提示',
        content: '审核后可能会造成' + item.preProcessName + '工序负库存，是否确认审核?',
        success: (res) => {
          if (res.confirm) {
            submitRequest(ids)
          }
        }
      })
    } else if (item.lowPassRateFlag === '0') {
      // 良品率偏低 优先级2 avgPassRateByDay
      uni.showModal({
        title: '提示',
        content:
          '记工数量低于工序平均良品率(' +
          new bigNumber(item.avgPassRateByDay).multipliedBy(100).toNumber() +
          '%)，是否确认审核?',
        success: (res) => {
          if (res.confirm) {
            submitRequest(ids)
          }
        }
      })
    } else if (item.overProductiveCapacityFlag === '0') {
      // 记工数量超产能 优先级3 avgProductionCapacityByDay
      uni.showModal({
        title: '提示',
        content: ' 记工数量已超过工序日均产能(' + item.avgProductionCapacityByDay + ')，是否确认审核?',
        success: (res) => {
          if (res.confirm) {
            submitRequest(ids)
          }
        }
      })
    } else {
      submitRequest(ids)
    }
  }
}
const { popupOpen } = HooksPopup()
function submitRequest(ids) {
  collectClick('批量审核', '记工明细', '记工审核')
  _post({ url: '/submit/check', data: ids }).then((res: IResponseType<unknown>) => {
    if (res.msg.includes('已被审核')) {
      popupOpen(res.msg)
    } else {
      uni.showToast({ title: '审核成功', icon: 'none' })
      pageStatus.value = 'success'
      getList()
      uni.$emit('auditListRefresh')
      uni.$emit('auditDetailRefresh')
    }
  })
}
</script>
<style lang="scss" scoped></style>
