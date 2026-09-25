<template>
  <view class="h-100 flex flex-col overflow-hidden bg-f3f3f5" @touchstart="touchStart" @touchend="touchEnd">
    <!-- 产品/员工 tab -->
    <view class="box pt-16 pl-48 pr-32 pb-24 flex align-center">
      <audit-switch :check="switchChange" :tabActive="tabActive" @switchChange="handleSwitchChange" />
      <template v-if="!switchChange || tabActive === 0">
        <view class="ml-24 flex align-center" v-if="followChecked" @tap="handleFollowCheck">
          <image :src="formatImage('icon_quality_focus_h', 'svg')" class="icon-48" />
          <view class="ml-8 font-28 color-5a6f82">我关注的</view>
        </view>
        <image v-else :src="formatImage('icon_quality_focus', 'svg')" @tap="handleFollowCheck" class="icon-48 ml-24" />
      </template>
      <!--员工维护展示我关注的-->
      <view class="flex-1" />
      <h-page-video :videoArr="[7, 8]" page-name="审产-产品-待审" />
      <image @tap="open" src="/static/images/icon_filter_b6c0c9.svg" class="icon-48 ml-24" />
    </view>
    <!-- 待审/已审 tab -->
    <view class="flex flex-col relative">
      <view class="box mx-16 bg-fff border-16 h-80 flex align-center pl-32 pr-16">
        <view class="font-28" :class="tabActive === 1 ? 'color-333 bold' : 'color-999'" @tap="handleTabChange(1)">
          <text>待审</text>
          <text v-if="totalCount">({{ totalCount }})</text>
        </view>
        <view class="font-28 ml-64" :class="tabActive === 0 ? 'color-333 bold' : 'color-999'" @tap="handleTabChange(0)"
          >已审</view
        >
        <view class="flex-1 flex align-center justify-end" v-if="checkAble && tabActive === 1">
          <view class="font-24">
            <text class="color-333">已选</text>
            <text class="color-ff0000 ml-4 bold">{{ getCheckedCount }}</text>
          </view>
          <view
            class="font-24 ml-16 px-16 py-10 rounded-24"
            :class="checkAll ? 'color-fff bg-0066ff' : 'color-5a6f82 bg-f3f3f5'"
            @tap="handleCheckAll"
            >{{ checkAll ? '全不选' : '全选' }}
          </view>
        </view>
        <view v-else class="flex-1" />
        <view
          class="w-80 h-44 flex align-center justify-center ml-16 rounded-24 bg-f3f3f5"
          :class="revokeAble ? 'color-0066ff' : 'color-5a6f82'"
          @tap="handleRevoke()"
          v-if="tabActive === 0"
        >
          <text class="font-24 bold">{{ revokeAble ? '取消' : '撤销' }}</text>
        </view>
        <view
          class="w-80 h-44 flex align-center justify-center ml-16 rounded-24 bg-f3f3f5"
          :class="checkAble ? 'color-0066ff' : 'color-5a6f82'"
          @tap="handleCheckedAble(!checkAble)"
          v-if="tabActive === 1 && !switchChange"
        >
          <text class="font-24 bold">{{ checkAble ? '取消' : '批量' }}</text>
        </view>
      </view>
    </view>
    <!-- 重点员工数据展示 -->
    <audit-notice-area
      v-if="userNoticeItem.name"
      :dataItem="userNoticeItem"
      class="mt-8"
      @clearSearch="handleClearSearch"
    />
    <audit-notice-area
      class="mt-8"
      v-if="governType && !switchChange && tabActive"
      :dataItem="{ governType, governCount: dataList.length }"
      @clearSearch="handleClearSearch"
    />
    <system-notice-area v-model="systemNotice" />
    <!--数据列表滚动区域-->
    <scroll-view
      @scrolltolower="onLoadMore"
      @refresherrefresh="onRefresh"
      :refresher-triggered="triggered"
      scroll-y
      refresher-enabled
      class="flex-1 flex flex-col mt-8 mb-8 overflow-hidden"
      enable-flex
    >
      <h-empty v-if="pageEmpty" tipsWord="暂无审核数据" class-name="pt-240" />
      <template v-else-if="dataList.length">
        <template v-if="switchChange && tabActive === 1">
          <list-item
            v-for="(item, index) in dataList"
            :id="`scroll-${item.id}`"
            :tabType="switchChange"
            :key="`${tabActive}-${index}`"
            :dataItem="item"
            @auditCheck="handleProductItemCheck(item)"
            @tap="handleProductItemCheck(item)"
          />
        </template>
        <template v-else>
          <list-item-staff
            v-for="(item, index) in dataList"
            :checkAll="checkAble"
            :id="`scroll-${item.id}`"
            :revokeAble="revokeAble"
            :tabType="switchChange"
            :key="`${tabActive}-${index}`"
            :dataItem="item"
            @itemCheck="handleItemCheck"
            @itemRevoke="handleItemRevoke"
            @auditCheck="handleAuditCheck"
            @tap="handleJumpDetailList(item)"
          />
        </template>
        <uni-load-more :status="dataNoMore" />
      </template>
    </scroll-view>
    <view class="btn-record">
      <h-button-record text="审核" v-if="checkAble" size="icon-160" @tap="handleAuditBatchCheck" />
    </view>
    <!--筛选弹窗-->
    <audit-filter ref="auditFilter" :onlyProduct="switchChange && tabActive === 1" @refresh="onRefresh" />
    <!--数据治理按钮暂时屏蔽-->
    <view class="health-data">
      <btn-data-health :value="healthPre" url="/pages-data-governance/audit/index" type="审核" />
    </view>
    <h-popup />
    <h-popup-dialog ref="refPopupDialog" title="提示" isMaskClick>
      <template #default>
        <view class="color-333 font-28 py-32">
          <rich-text :nodes="popupDialogTitle" />
        </view>
      </template>
      <template #footer>
        <view class="flex align-center justify-center">
          <h-button
            width="120"
            height="64"
            text="送检"
            type="border-1 border-0066ff color-0066ff bg-fff"
            @tap.stop="handleDialogSendCheck"
            v-if="allowAudit"
          />
          <!--          todo--  改颜色-->
          <h-button
            width="120"
            height="64"
            text="驳回"
            active="red"
            class="ml-32"
            type="color-ff0000 border-1 border-ff0000 bg-fff"
            @tap.stop="handleDialogReject"
          />
          <h-button
            width="120"
            height="64"
            text="通过"
            class="ml-32"
            @tap.stop="handleDialogConfirm"
            v-if="allowAudit"
          />
          <h-button width="120" height="64" text="去修正" class="ml-32" @tap.stop="handleDialogEdit" v-else />
        </view>
      </template>
    </h-popup-dialog>
  </view>
</template>
<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, PropType, reactive, ref, watch, watchEffect } from 'vue'
import ListItem from './list-item.vue'
import ListItemStaff from './list-item-staff.vue'
import { onReady, onShow } from '@dcloudio/uni-app'
import AuditSwitch from './audit-switch.vue'
import { _get, _post } from '@/utils/common-request'
import HButtonRecord from '@/components/h-button-record.vue'
import { $store, collectClick, collectPV, escapeHtml } from '@/utils/common'
import bigNumber from 'bignumber.js'
import { formatImage } from '@/utils/common'
import HEmpty from '@/components/h-empty.vue'
import HooksPopup from '@/hooks/popup'
import HooksPageSwiper from '@/hooks/pages-swiper'
import BtnDataHealth from '@/components/btn-data-health.vue'
import AuditNoticeArea from './audit-notice-area.vue'
import HPopupDialog from '@/components/h-popup-dialog.vue'
import SystemNoticeArea from '@/components/system-notice-area.vue'
import HooksSystemNotice from '@/hooks/system-notice'
import AuditFilter from './audit-filter.vue'
import { log } from 'echarts/types/src/util/log'

const filterData = computed(() => $store.state.audit.filterData)
const auditFilter = ref(null)
function open() {
  nextTick(() => {
    getFollowList()
    auditFilter.value.open('normal')
  })
}
//重置筛选项
function clearFilter() {
  $store.commit('audit/resetFilterData')
}
function refresh() {
  pages.pageNum = 1
  getList()
}

function handleClearSearch() {
  clearFilter()
  userNoticeItem.value = { name: '', days: 0, count: 0 }
  ids.value = ''
  governType.value = ''
  governCount.value = 0
  getList()
}

const userNoticeItem = ref({ name: '', days: 0, count: 0 })

// 产品/员工切换,true产品,false员工
const switchChange = ref(uni.getStorageSync('recommendPage_check_dimensions') === '产品')
function handleSwitchChange(val) {
  if (listLoading.value) {
    return
  }
  listLoading.value = true
  switchChange.value = val
  dataList.value = []
  pages.pageNum = 1
  getList()
  if (val) {
    collectPV('产品', '审核列表-产品维度', '审核列表', 0.5)
  } else {
    collectPV('员工', '审核列表-员工维度', '审核列表', 0.5)
  }
}

// 待审/已审切换
const tabActive = ref(1)
function handleTabChange(index) {
  if (listLoading.value) {
    return
  }
  listLoading.value = true
  tabActive.value = index
  revokeAble.value = false
  dataList.value = []
  pages.pageNum = 1
  getList()
}

// 获取待审/已审列表
const dataList = ref([])
const pageEmpty = ref(false)
const totalCount = ref(0)
const ids = ref('')
const listLoading = ref(false)
async function getList() {
  // triggered.value = true
  uni.showLoading({ title: '加载中' })
  dataNoMore.value = 'loading'
  pageEmpty.value = false
  const params = { ...filterData.value }
  if (!followChecked.value) {
    params.submitUserIds = ''
  }
  let res = null
  let data = {
    ...params,
    ...pages,
    submitStatus: tabActive.value,
    cancelCheckType: revokeAble.value ? 0 : 1,
    ids: ids.value
  }
  $store.commit('audit/setCrossPageData', {})
  if (tabActive.value === 1 && switchChange.value) {
    //产品的待审
    res = await _get({ url: '/submit/checkIndex/product/list/v2', data })
  } else {
    // 产品和员工的已审核接口使用同一个  tabActive=0
    res = await _get({ url: '/submit/checkIndex/user/list', data })
  }
  uni.hideLoading()
  listLoading.value = false
  triggered.value = false
  if (pages.pageNum === 1) {
    checkAble.value = false
    checkAll.value = false
    dataList.value = []
  }
  dataList.value.push(
    ...res.rows.map((v) => {
      v.checked = false
      // 用于判断是否显示预警标签
      v.warnFlagStaff = tabActive.value === 1 && switchChange.value ? '0' : '1'
      return v
    })
  )
  pageEmpty.value = !dataList.value.length
  if (tabActive.value === 1) {
    totalCount.value = res.total
  }
  dataTotal.value = res.total
  dataNoMore.value = dataEnd.value ? 'noMore' : 'more'
}
function handleJumpDetailList(item) {
  if (item.ids) {
    uni.navigateTo({
      url: `/pages-audit/detail-list?ids=${item.ids}&product=${item.productName}&process=${item.operateProcessName}&productSeq=${item.productSeq}`
    })
  }
}
//产品待审的点击
function handleProductItemCheck(item) {
  uni.navigateTo({
    url: '/pages-audit/report-detail-audit?dataItem=' + JSON.stringify(item)
  })
}

// 全选操作
const checkAble = ref(false)
const checkAll = ref(false)
const getCheckedCount = computed(() => {
  return dataList.value.filter((v) => v.checked).length
})
function handleCheckedAble(val) {
  checkAble.value = val
  checkAll.value = val
  dataList.value.forEach((v) => {
    v.checked = val
  })
}
function handleCheckAll() {
  checkAll.value = !checkAll.value
  dataList.value.forEach((v) => {
    v.checked = checkAll.value
  })
}
function handleItemCheck(item) {
  const obj = dataList.value.find((v) => (v.id && v.id === item.id) || (v.ids && v.ids === item.ids))
  obj && (obj.checked = !obj.checked)
  checkAll.value = dataList.value.every((v) => v.checked)
}
const refPopupDialog = ref(null)
const popupDialogTitle = ref('')
type TPopupParams = {
  ids: string[]
  id: number
  negativeStockFlag: string
  preProcessStockNum: number
  preProcessWaitCheckPassNum: number
  lowPassRateFlag: string
  passRate: string
  avgPassRateByDay: string
  overProductiveCapacityFlag: string
  avgProductionCapacityByDay: string
  todaySubmitNum: number
  overProductiveCapacityExceptionRecordIds: string
}
type TPopup = {
  type: number
  params: Partial<TPopupParams>
}
const popupDialogItem = reactive<Partial<TPopup>>({})

// 弹窗关闭
function handleDialogCancel() {
  refPopupDialog.value?.close()
}
function handleDialogSendCheck() {
  _post({ url: '/ngProduct/manage/submitForInspection', data: { ids: popupDialogItem.params.ids.join(',') } })
    .then((res) => {
      getList()
    })
    .finally(() => {
      handleDialogCancel()
    })
}
// 弹窗驳回
function handleDialogReject() {
  _get({ url: '/submit/rejectRecord', data: { ids: popupDialogItem.params.ids.join(',') } })
    .then((res: IResponseType<unknown>) => {
      if (res.msg.includes('已被驳回')) {
        popupOpen(res.msg)
      } else {
        getList()
      }
    })
    .finally(() => {
      handleDialogCancel()
    })
}
// 弹窗通过
function handleDialogConfirm() {
  switch (popupDialogItem.type) {
    case 1:
      checkRequestSingle(popupDialogItem.params.ids)
      break
    case 2:
      checkRequest(popupDialogItem.params.ids)
      break
    default:
      break
  }
}
//弹窗 去修正 编辑
function handleDialogEdit() {
  let url = `/pages-audit/detail-info?id=${popupDialogItem.params.id}&from=auditListUser&type=edit`
  url += `&negativeStockFlag=${popupDialogItem.params.negativeStockFlag}`
  url += `&preProcessStockNum=${popupDialogItem.params.preProcessStockNum}`
  url += `&preProcessWaitCheckPassNum=${popupDialogItem.params.preProcessWaitCheckPassNum}`
  url += `&lowPassRateFlag=${popupDialogItem.params.lowPassRateFlag}`
  url += `&passRate=${popupDialogItem.params.passRate}`
  url += `&avgPassRateByDay=${popupDialogItem.params.avgPassRateByDay}`
  url += `&overProductiveCapacityFlag=${popupDialogItem.params.overProductiveCapacityFlag}`
  url += `&avgProductionCapacityByDay=${popupDialogItem.params.avgProductionCapacityByDay}`
  url += `&todaySubmitNum=${popupDialogItem.params.todaySubmitNum}`
  url += `&overProductiveCapacityExceptionRecordIds=${popupDialogItem.params.overProductiveCapacityExceptionRecordIds}`
  handleDialogCancel()
  uni.navigateTo({ url })
}
// 审核按钮,支持单次审核
async function handleAuditCheck(item) {
  const ids: string[] = []
  if (Array.isArray(item)) {
    item.forEach((v) => {
      ids.push(switchChange.value ? v.ids : v.id)
    })
  } else {
    ids.push(switchChange.value ? item.ids : item.id)
  }
  if (!checkAuditRules(ids)) return
  if (switchChange.value) {
    uni.navigateTo({ url: '/pages-audit/report-detail-audit?dataItem=' + JSON.stringify(item) })
  } else {
    //员工维度，先去检查是否是标准工艺，是否允许审核
    let res = (await _post({
      url: '/tech/validSubmitRecordTechInfo',
      data: {
        productSeq: item.productSeq,
        processSeq: item.operateProcessSeq,
        preProcessSeq: item.preProcessSeq
      }
    })) as IResponseType<boolean>
    allowAudit.value = res.data
    if (!allowAudit.value) {
      popupDialogTitle.value = `报工记录不符合产品的工艺路线, 请编辑报工记录`
      popupDialogItem.type = 1
      popupDialogItem.params = { ...item }
      popupDialogItem.params.ids = ids
      refPopupDialog.value.open()
      return
    }
    if (item.negativeStockFlag === '0') {
      //负库存 优先级1
      popupDialogTitle.value = `审核后可能会造成${escapeHtml(item.preProcessName)}工序负库存，是否确认审核?`
      popupDialogItem.type = 1
      popupDialogItem.params = { ids }
      refPopupDialog.value.open()
    } else if (item.lowPassRateFlag === '0') {
      // 良品率偏低 优先级2 avgPassRateByDay
      popupDialogTitle.value = `记工数量低于工序平均良品率(${new bigNumber(item.avgPassRateByDay)
        .multipliedBy(100)
        .toNumber()}%)，是否确认审核?`
      popupDialogItem.type = 1
      popupDialogItem.params = { ids }
      refPopupDialog.value.open()
    } else if (item.overProductiveCapacityFlag === '0') {
      // 记工数量超产能 优先级3 avgProductionCapacityByDay
      popupDialogTitle.value = `记工数量已超过工序日均产能(${item.avgProductionCapacityByDay})，是否确认审核?`
      popupDialogItem.type = 1
      popupDialogItem.params = { ids }
      refPopupDialog.value.open()
    } else {
      //正常提示
      popupDialogTitle.value = `是否确认审核?`
      popupDialogItem.type = 1
      popupDialogItem.params = { ids }
      refPopupDialog.value.open()
    }
  }
}
//单次审核请求
function checkRequestSingle(ids) {
  collectClick('单次审核', '审核列表-员工维度', '')
  _get({ url: `/submit/check/${ids.join(',')}` })
    .then((res: IResponseType<unknown>) => {
      if (res.msg.includes('已被审核')) {
        popupOpen(res.msg)
        return
      }
      getList()
    })
    .finally(() => {
      handleDialogCancel()
    })
}
const { popupOpen } = HooksPopup()
function handleAuditBatchCheck() {
  allowAudit.value = true
  const ids = []
  let errNum = 0
  dataList.value.forEach((v) => {
    if (v.checked) {
      if (switchChange.value) {
        //产品维度
        ids.push(v.ids)
        errNum = errNum + v.exceptionNums
      } else {
        ids.push(v.id)
        if (v.negativeStockFlag === '0' || v.lowPassRateFlag === '0' || v.overProductiveCapacityFlag === '0') {
          errNum++
        }
      }
    }
  })
  //未选择
  if (!checkAuditRules(ids)) return
  //已选择
  //审核包含idsLength 条记工明细
  let idsLength = 0
  if (switchChange.value) {
    ids.forEach((id) => {
      let idArray = id.split(',')
      idsLength += idArray.length
    })
  } else {
    idsLength = ids.length
  }

  if (errNum > 0) {
    popupDialogTitle.value = `<div class="color-333">审核包含<span class="mx-8 color-ff0000">${idsLength}</span>条记工明细，有<span class="mx-8 color-ff0000">${errNum}</span>条数据可能存在异常，是否确认审核?</div>`
    popupDialogItem.type = 2
    popupDialogItem.params = { ids }
    refPopupDialog.value.open()
  } else {
    popupDialogTitle.value = '是否确认审核?'
    popupDialogItem.type = 2
    popupDialogItem.params = { ids }
    refPopupDialog.value.open()
  }
}

// 审核/批量审核条件判断
function checkAuditRules(ids) {
  if (!ids.length) {
    uni.showToast({ title: '请选择需要审核的数据', icon: 'none' })
    return false
  }
  return true
}
//批量审核请求
function checkRequest(ids) {
  _get({ url: `/submit/check/${ids.join(',')}` })
    .then((res: IResponseType<unknown>) => {
      if (res.msg.includes('已被审核')) {
        popupOpen(res.msg)
        return
      }
      getList()
    })
    .finally(() => {
      handleDialogCancel()
    })
  collectClick('批量审核', '审核列表-员工维度', '')
}

// 撤销操作
const revokeAble = ref(false)
function handleRevoke() {
  revokeAble.value = !revokeAble.value
  getList()
}
function handleItemRevoke(item) {
  uni.showModal({
    title: '提示',
    content: '是否确认撤销?',
    success: async (res) => {
      if (res.confirm) {
        await _get({ url: '/submit/undoCheckedRecord', data: { id: item.id } })
        await getList()
      }
    }
  })
}

const { touchStart, touchEnd } = HooksPageSwiper((direction) => {
  if (direction === 'left') {
    tabActive.value !== 1 && handleTabChange(1)
  } else if (direction === 'right') {
    tabActive.value !== 0 && handleTabChange(0)
  }
})

const healthPre = ref(0)
const governType = ref('')
const governCount = ref(0)
const { systemNotice, getSystemTips } = HooksSystemNotice(20)

async function getFollowList() {
  await _get({ url: '/follow/list', data: { followType: 'EMPLOYEE' } }).then((res: IResponseType<[]>) => {
    const arr =
      res.data?.map((v: { itemId; itemName }) => {
        return {
          id: v.itemId,
          nickName: v.itemName
        }
      }) ?? []
    // 如果是从记工风险之类的页面跳转过来不自动选中关注
    if (uni.getStorageSync('ku_follow_disable')) {
      uni.setStorageSync('ku_follow_disable', '')
      $store.commit('audit/setFollowChecked', false)
    } else {
      $store.commit('audit/setFollowChecked', true)
      if (arr.length) {
        switchChange.value = false
        const ids = arr.map((v: { id }) => v.id)
        getListBySubmitUserIds(ids.join(','))
      }
    }
    $store.commit('audit/setFollowList', arr)
  })
}

const followList = computed(() => $store.state.audit.followList)
const followChecked = computed(() => $store.state.audit.followChecked || false)
function handleFollowCheck() {
  $store.commit('audit/setFollowChecked', !followChecked.value)
  if (followChecked.value) {
    // 回显参数
    if (followList.value.length) {
      const ids = followList.value.map((v: { id }) => v.id)
      getListBySubmitUserIds(ids.join(','))
    } else {
      // 弹出筛选框,跳到我关注的列表
      auditFilter.value.open('follow')
    }
  } else {
    getListBySubmitUserIds()
  }
}

function getListBySubmitUserIds(submitUserIds = '') {
  $store.commit('audit/setFilterData', { submitUserIds })
  pages.pageNum = 1
  getList()
}
const allowAudit = ref(true) //记工审核异常处理跳转到员工时，不符合标准工艺的报工不允许审核通过，提示审核员进行修改 ‘true'标识允许审核
onMounted(async () => {
  await getFollowList()
  if (uni.getStorageSync('recommendPage_check_dimensions') === '员工') {
    switchChange.value = false
    tabActive.value = 1
  }
  uni.$on('auditListRefresh', (options) => {
    const { modifiedNums, avgDayProductiveCapacity, submitNickUser } = options || {}
    if (modifiedNums || avgDayProductiveCapacity) {
      userNoticeItem.value = { name: submitNickUser, days: modifiedNums, count: avgDayProductiveCapacity }
      $store.commit('audit/setFilterData', { submitNickName: submitNickUser })
      switchChange.value = false
    }
    pages.pageNum = 1
    getList()
  })
  const governJump = uni.getStorageSync('governJump')
  if (governJump && governJump.page === 'audit') {
    $store.commit('audit/setFollowChecked', false)
    uni.setStorageSync('governJump', '')
    switchChange.value = false
    pages.pageNum = 1
    tabActive.value = 1
    ids.value = governJump.ids
    governType.value = governJump.governType
    governCount.value = governJump.governCount
    getList()
  } else if ($store.state.audit.crossPageData && JSON.stringify($store.state.audit.crossPageData) !== '{}') {
    $store.commit('audit/setFollowChecked', false)
    let options = $store.state.audit.crossPageData
    switchChange.value = Boolean(options.switchChange)
    delete options.switchChange
    if (options.ids) {
      ids.value = options.ids
    }
    delete options.ids
    $store.commit('audit/setFilterData', options || {})
    pages.pageNum = 1
    getList()
  } else {
    pages.pageNum = 1
    ids.value = ''
    governType.value = ''
    governCount.value = 0
    userNoticeItem.value = { name: '', days: 0, count: 0 }
    $store.commit('audit/setFilterData', { submitNickName: '' })
    clearFilter()
    !followList.value.length && getList()
    handleDialogCancel()
  }
  // 清理所有的弹窗
  $store.commit('popup/closeAllPopup')
  getSystemTips()
  collectPV('产品', '审核列表-产品维度', '审核列表', 0.5)
  collectPV('员工', '审核列表-员工维度', '审核列表', 0.5)
})

onUnmounted(() => {
  uni.$off('auditListRefresh')
})

// 上拉加载模块
const dataTotal = ref(0)
const pages = reactive({ pageSize: 10, pageNum: 1 })
const dataNoMore = ref('more')
const dataEnd = computed(() => pages.pageNum * pages.pageSize >= dataTotal.value)
function onLoadMore() {
  if (dataEnd.value) {
    return
  }
  pages.pageNum++
  getList()
}
// 下拉刷新
const triggered = ref(false)
function onRefresh() {
  pages.pageNum = 1
  triggered.value = true
  getList()
}
</script>
<style lang="scss" scoped>
.btn-record {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  bottom: px2vw(200);
  z-index: 2;
}
.wrapper {
}
</style>
