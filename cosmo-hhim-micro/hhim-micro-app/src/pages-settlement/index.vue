<template>
  <view class="h-full bg-f3f3f5 box flex flex-col overflow-hidden">
    <uni-nav-bar />
    <h-status-header title="计件结算" />
    <view class="flex-1 flex flex-col mx-16 box overflow-hidden">
      <view class="h-80 flex align-center justify-between pr-24 box bg-fff rounded-16 mt-16">
        <h-tabs ref="hTabs" :tabData="tabs" :activeIndex="activeIndex" :config="defaultConfig" @tabClick="tabClick" />
        <btn-export v-if="activeIndex !== 2" @exportToMail="exportMail" />
      </view>
      <view class="box px-16 flex align-center mt-24" :class="[activeIndex !== 2 ? 'mb-24' : 'mb-16']">
        <template v-if="activeIndex !== 2">
          <view class="switch-card" :class="{ active: switchType === 0 }" @tap="handleSwitchChange(0)">产品</view>
          <view class="switch-card" :class="{ active: switchType === 1 }" @tap="handleSwitchChange(1)">员工</view>
          <view class="flex-1" />
          <h-page-video
            :videoArr="[14]"
            :page-name="`计件结算-${activeIndex === 0 ? '待结算' : '已结算'}-${switchType === 0 ? '产品' : '员工'}`"
            class="mr-24"
          />
          <!--产品维度展示月份选择-->
          <picker
            v-if="!((switchType === 1 && !activeIndex) || activeIndex === 2)"
            mode="date"
            fields="month"
            :value="selectMonth"
            @change="bindDateChangeMonth"
            class="bg-fff color-333 bold rounded-24 px-16 box font-24 h-44 line-44 mr-24"
          >
            {{ selectMonth }}
          </picker>
        </template>

        <!--员工维度展示员工名称输入框-->
        <template v-else>
          <h-search class="flex-1" placeholder="输入员工姓名" bgClass="bg-fff-80" @searchInput="handleInput" />
          <h-page-video :videoArr="[14]" page-name="计件结算-调整历史" class="mx-24" />
        </template>
        <image @tap="showSearchPopup" src="/static/images/icon_filter_b6c0c9.svg" class="icon-48" />
      </view>

      <scroll-view
        @refresherrefresh="pageListRefresh"
        @scrolltolower="pageListLoadMore"
        :refresher-triggered="refreshTag"
        refresher-enabled
        scroll-y
        class="flex-1 overflow-hidden"
        enable-flex
      >
        <template v-if="pageEmpty">
          <h-empty className="pt-240" :tipsWord="tipsWord" />
        </template>
        <template v-else>
          <!--待结算-->
          <template v-if="activeIndex === 0">
            <!--产品维度-->
            <list-product-item v-if="switchType === 0" />
            <!--员工维度-->
            <list-staff-item v-else @openEditPopup="openEditPopup" />
          </template>
          <!--已结算-->
          <template v-else-if="activeIndex === 1">
            <!--产品维度-->
            <list-product-item-settled v-if="switchType === 0" @toLink="toProductSettledDeatil" />
            <!--员工维度-->
            <list-staff-item-settled v-else />
          </template>
          <template v-else>
            <!--调整历史-->
            <list-item-settled-history />
          </template>
          <!--下拉加载更多-->
          <uni-load-more :status="dataNoMore" />
        </template>
      </scroll-view>
    </view>
    <!--统计数据源自完工报告，如有问题请先调整完工报告-->
    <notice-area v-if="activeIndex === 0" />
    <!-- 待结算 产品 确认按钮 -->
    <view v-if="activeIndex === 0 && switchType === 0" class="flex justify-center py-32 box">
      <h-button width="624" height="72" text="确认" class="ml-32" @tap.stop="handleConfirm" />
    </view>
    <!-- 待结算 员工 结算按钮 -->
    <view v-if="activeIndex === 0 && switchType === 1" class="flex justify-center py-32 box">
      <h-button width="624" height="72" text="结算" class="ml-32" @tap.stop="handleSettlement" />
    </view>
    <h-status-footer />
    <h-popup />
    <export-email ref="exportEmail" :exportLoading="exportLoading" @confirmPopup="confirmExport" />
    <uni-popup ref="popupNum" type="bottom" :safe-area="false" :animation="false">
      <view class="edit-class w-100 bg-ffffff rounded-16-top">
        <view class="h-96 w-100 flex align-center relative">
          <text class="color-5a6f82 font-28 bold flex-1 text-center">调整计件数量</text>
          <image @click="closePopup" src="/static/images/icon_close_666.svg" class="icon-48 close" />
        </view>
        <view class="">
          <view class="bg-f3f3f5 px-32 py-16 box flex flex-col justify-center">
            <view class="flex align-center bold font-28">
              <view class="color-333">
                <h-text-display :text="currentItem.productName" :width="288" />
              </view>
              <view class="color-999">({{ currentItem.productCode }})</view>
            </view>
            <view class="flex align-center mt-18">
              <view class="color-5a6f82 font-28 w-240 single">{{ currentItem.operateProcessName }}</view>
              <view class="flex-1" />
              <view class="flex align-center font-28">
                <view class="mark-ffffff">记工</view>
                <text class="bold ml-16 font-28">{{ currentItem.employeeName }}</text>
              </view>
            </view>
          </view>
          <view class="mt-24 px-32">
            <view class="flex align-center justify-between box border-bottom-f5f5f5">
              <view class="color-5a6f82 font-28 text-require pl-16 box w-176">数量</view>
              <input
                class="flex-1 h-64 box text-left bg-fff font-28"
                placeholder="请输入数量"
                type="digit"
                :max="currentItem.adjustedNum"
                v-model="currentItem.adjustedNum"
                @blur="handleProductNumCheck(currentItem)"
                placeholder-style="color:#B8B8B8"
              />
            </view>
            <view class="color-999 font-24 ml-176 mt-8">数量只能调小</view>
            <view class="flex flex-col box mt-32">
              <view class="color-5a6f82 font-28 text-require pl-16 box">调整说明</view>
              <view class="mt-16 border-1 border-f5f5f5 rounded-16">
                <input
                  class="w-100 h-80 px-16 box font-28"
                  placeholder="输入调整说明"
                  v-model="currentItem.remark"
                  placeholder-style="color:#B8B8B8"
                  maxlength="30"
                />
              </view>
            </view>
          </view>
        </view>
        <view class="pb-32 w-100 mt-64 flex justify-center align-center font-32">
          <h-button width="296" height="72" text="取消" type="bg-f3f3f5 color-333" @tap.stop="closePopup" />
          <h-button width="296" height="72" text="确认并保存" class="ml-32" @tap.stop="confirmPopup" />
        </view>
      </view>
    </uni-popup>
    <h-popup-product ref="popupProduct" :showAdd="false" @productSelected="confirmProductSearch" />
    <!-- Filter 用 PascalCase：<filter> 小写标签在 H5 下会被解析成 SVG filter 元素而非组件，导致 ref 拿不到 open 方法 -->
    <Filter ref="staffFilter" @confirm="staffConfirm" />
    <h-popup />
  </view>
</template>

<script setup lang="ts">
import HStatusFooter from '@/components/h-status-footer.vue'
import BtnExport from '@/components/btn-export.vue'
import ExportEmail from '@/components/export-email.vue'
import HEmpty from '@/components/h-empty.vue'
import HooksExportEmail from '@/hooks/export-email'
import { computed, onMounted, ref } from 'vue'
import HooksPageList from '@/hooks/page-list'

import HTabs from '@/components/h-tabs.vue'
import NoticeArea from '@/pages-settlement/notice-area.vue'
import dayjs from 'dayjs'
import HPopup from '@/components/h-popup.vue'
import ListProductItem from '@/pages-settlement/list-product-item.vue'
import ListProductItemSettled from '@/pages-settlement/list-product-item-settled.vue'
import ListStaffItem from '@/pages-settlement/list-staff-item.vue'
import ListStaffItemSettled from '@/pages-settlement/list-staff-item-settled.vue'
import ListItemSettledHistory from '@/pages-settlement/list-item-settled-history.vue'
import { $store } from '@/utils/common'
import { _get, _post } from '@/utils/common-request'
import HPopupProduct from '@/components/h-popup-product.vue'
import Filter from '@/pages-settlement/components/filter.vue'
import edit from '@/pages-my-center/manage-process/edit.vue'
/** 导出到email模块 */
const { exportEmail, exportToMail, exportLoading, confirmExportPopup, setExportUrl } = HooksExportEmail()
onMounted(() => {
  selectMonth.value = dayjs().add(-1, 'month').startOf('month').format('YYYY-MM')
  refreshPageList()
})
function exportMail() {
  exportToMail(
    dayjs(selectMonth.value).startOf('month').format('YYYY-MM-DD'),
    dayjs(selectMonth.value).endOf('month').format('YYYY-MM-DD')
  )
}
function confirmExport(receivedBy) {
  if (activeIndex.value === 0) {
    setExportUrl(`/export/openSettlementReport`, {
      receivedBy: receivedBy,
      searchDate: dayjs(selectMonth.value).startOf('month').format('YYYY-MM-DD'),
      userId: filterData.value?.staffInfo?.id || '',
      productSeq: filterData.value?.productInfo?.itemSeq || '',
      operateProcessSeq: filterData.value?.processInfo?.itemSeq || ''
    })
  }
  if (activeIndex.value === 1) {
    setExportUrl(`/export/settledReport`, {
      receivedBy: receivedBy,
      searchDate: dayjs(selectMonth.value).startOf('month').format('YYYY-MM-DD'),
      userId: filterData.value?.staffInfo?.id || '',
      productSeq: filterData.value?.productInfo?.itemSeq || '',
      operateProcessSeq: filterData.value?.processInfo?.itemSeq || ''
    })
  }
  confirmExportPopup()
}
/** tabs模块 */
const tabs = ref([
  {
    state: 0,
    disabled: false,
    name: '待结算'
  },
  {
    state: 1,
    disabled: false,
    name: '已结算'
  },
  {
    state: 2,
    disabled: false,
    name: '调整历史'
  }
])
const activeIndex = ref(0)
const defaultConfig = ref({
  fontSize: 28,
  color: '#999999',
  activeBold: 'bold',
  activeColor: '#333333',
  underLineHeight: 8,
  underLineColor: '#0066ff'
})
function tabClick(event) {
  activeIndex.value = event
  //每次切换都是产品维度默认展示
  switchType.value = 0
  //清空筛选
  productInfo.value = {}
  staffFilter.value = {}
  selectMonth.value = dayjs().add(-1, 'month').startOf('month').format('YYYY-MM')
  handleInputClear()
}

/** 分页列表模块 */
const dataListCallBack = (res) => {
  $store.commit('settlement/setSettlementList', dataList.value)
}
const { dataList, pageListRefresh, pageTotal, dataNoMore, refreshTag, pageEmpty, pageListLoadMore, pageListSetParams } =
  HooksPageList('/complete/report/statistics/product', {
    run: false,
    cb: dataListCallBack
  })
// 月份选择
const selectMonth = ref(dayjs().startOf('month').format('YYYY-MM-DD'))
function bindDateChangeMonth(event) {
  selectMonth.value = event.detail.value
  const startDate = dayjs(selectMonth.value).startOf('month').format('YYYY-MM-DD')
  const endDate = dayjs(selectMonth.value).endOf('month').format('YYYY-MM-DD')
  // pageListSetParams({ startDate, endDate, onlyNormal: true })
  refreshPageList()
}

const switchType = ref(0)
// 切换产品/员工
function handleSwitchChange(type) {
  switchType.value = type
  //清空筛选
  productInfo.value = {}
  staffFilter.value = {}
  handleInputClear()
}

const tipsWord = computed(() => {
  if (activeIndex.value === 0 && switchType.value === 0) {
    // 待结算产品维度
    return '暂无可结算完工数据'
  } else if (activeIndex.value === 0 && switchType.value === 1) {
    // 待结算员工维度
    return '未查询到相关员工的完工记录，请重新搜索'
  } else if (activeIndex.value === 1 && switchType.value === 0) {
    // 已结算产品维度
    return '暂无已结算完工数据'
  } else if (activeIndex.value === 1 && switchType.value === 1) {
    // 已结算员工维度
    return '未查询到相关员工的完工记录，请重新搜索'
  } else if (activeIndex.value === 2) {
    // 已结算调整历史
    return '未查询到相关记录，请重新搜索'
  }
})
//筛选处理
function showSearchPopup() {
  if (activeIndex.value === 0 && switchType.value === 0) {
    // 待结算产品维度,产品弹窗
    popupProduct.value?.open(productInfo.value)
  } else if (activeIndex.value === 0 && switchType.value === 1) {
    // 待结算员工维度 员工，产品，工序
    staffFilter.value.open(filterData.value)
  } else if (activeIndex.value === 1 && switchType.value === 0) {
    // 已结算产品维度,产品弹窗
    popupProduct.value?.open(productInfo.value)
  } else if (activeIndex.value === 1 && switchType.value === 1) {
    // 已结算员工维度员工，产品，工序
    filterData.value.selectMonth = selectMonth.value
    staffFilter.value.open(filterData.value)
  } else if (activeIndex.value === 2) {
    staffFilter.value.open(filterData.value)
  }
}
//单个产品筛选
const popupProduct = ref(null)
const productInfo = ref({})
function confirmProductSearch(itemSelected) {
  productInfo.value = itemSelected

  refreshPageList()
}
//多个筛选
const staffFilter = ref(null)
const filterData = ref({})
function staffConfirm(data) {
  filterData.value = data
  if (data.selectMonth) {
    selectMonth.value = data.selectMonth
  }
  if (filterData.value?.staffInfo?.id && activeIndex.value === 2) {
    searchInput.value = ''
  }

  refreshPageList()
}
//调整历史的查询
const searchInput = ref('')
function handleInput(str) {
  searchInput.value = str
  if (searchInput.value) {
    filterData.value.staffInfo = {}
    staffFilter.value.clearStaff()
  }
  refreshPageList()
}
function handleInputClear() {
  searchInput.value = ''
  refreshPageList()
}
//统一获取数据方法
function refreshPageList() {
  $store.commit('settlement/setSettlementList', [])
  if (activeIndex.value === 0 && switchType.value === 0) {
    // 待结算产品维度
    const startDate = dayjs(selectMonth.value).startOf('month').format('YYYY-MM-DD')
    const endDate = dayjs(selectMonth.value).endOf('month').format('YYYY-MM-DD')
    pageListSetParams(
      { startDate, endDate, productSeq: productInfo.value?.itemSeq || '', onlyNormal: true },
      '/complete/report/statistics/product'
    )
  } else if (activeIndex.value === 0 && switchType.value === 1) {
    // 待结算员工维度
    pageListSetParams(
      {
        userId: filterData.value?.staffInfo?.id || '',
        productSeq: filterData.value?.productInfo?.itemSeq || '',
        operateProcessSeq: filterData.value?.processInfo?.itemSeq || ''
      },
      '/settlement/open/user'
    )
  } else if (activeIndex.value === 1 && switchType.value === 0) {
    // 已结算产品维度
    pageListSetParams(
      {
        searchDate: dayjs(selectMonth.value).startOf('month').format('YYYY-MM-DD'),
        productSeq: productInfo.value?.itemSeq ? productInfo.value?.itemSeq : ''
      },
      '/settlement/settled/product'
    )
  } else if (activeIndex.value === 1 && switchType.value === 1) {
    // 已结算员工维度
    pageListSetParams(
      {
        searchDate: dayjs(selectMonth.value).startOf('month').format('YYYY-MM-DD'),
        userId: filterData.value?.staffInfo?.id || '',
        productSeq: filterData.value?.productInfo?.itemSeq || '',
        operateProcessSeq: filterData.value?.processInfo?.itemSeq || ''
      },
      '/settlement/settled/user'
    )
  } else if (activeIndex.value === 2) {
    // 已结算调整历史
    pageListSetParams(
      {
        searchKey: searchInput.value,
        userId: filterData.value?.staffInfo?.id || '',
        productSeq: filterData.value?.productInfo?.itemSeq || '',
        operateProcessSeq: filterData.value?.processInfo?.itemSeq || ''
      },
      '/settlement/history'
    )
  }
}
//点击确认切换到员工
function handleConfirm() {
  switchType.value = 1
  refreshPageList()
}
/** 结算 */
function handleSettlement() {
  uni.showLoading({
    title: '结算中',
    mask: true
  })
  _get({
    url: '/settlement/generate',
    data: {
      startDate: dayjs(selectMonth.value).startOf('month').format('YYYY-MM-DD'),
      endDate: dayjs(selectMonth.value).endOf('month').format('YYYY-MM-DD')
    }
  })
    .then((res) => {
      //结算成功
      uni.showToast({
        title: '结算成功',
        duration: 2000,
        icon: 'none'
      })
      refreshPageList()
      uni.$emit('settlementStatisticRefresh')
    })
    .catch((err) => {
      uni.hideToast()
      uni.showModal({
        title: '提示',
        content: err.msg,
        showCancel: false,
        success: function (res) {
          if (res.confirm) {
          }
        }
      })
    })
    .finally(() => {
      uni.hideLoading()
    })
}
//调整计件数量
const popupNum = ref(null)
const currentItem = ref({})
function openEditPopup(item) {
  currentItem.value = item
  //添加条件是因为切换到员工的时候不知道为什么会主动调这个方法
  if (item.productName) popupNum.value.open('bottom')
}
function closePopup() {
  popupNum.value.close('bottom')
}
function confirmPopup() {
  currentItem.value.adjustedNum = Number(currentItem.value.adjustedNum)
  if (currentItem.value.adjustedNum < 0) {
    uni.showToast({
      title: '数量不能为负数',
      duration: 2000,
      icon: 'none'
    })
    return
  } else if (currentItem.value.adjustedNum === currentItem.value.oldNum) {
    uni.showToast({
      title: '数据变更才可提交',
      duration: 2000,
      icon: 'none'
    })
    return
  } else if (currentItem.value.adjustedNum > currentItem.value.oldNum) {
    uni.showToast({
      title: '数量只能调小',
      duration: 2000,
      icon: 'none'
    })
    return
  } else if (!currentItem.value.remark) {
    uni.showToast({
      title: '请输入调整说明',
      duration: 2000,
      icon: 'none'
    })
    return
  }
  _post({
    url: '/settlement/edit',
    data: currentItem.value
  }).then((res) => {
    //结算成功
    uni.showToast({
      title: '调整成功',
      duration: 2000,
      icon: 'none'
    })
    closePopup()
    refreshPageList()
  })
}
function toProductSettledDeatil(item) {
  uni.navigateTo({
    url: `/pages-settlement/detail-product-item?productData=${JSON.stringify(item)}&searchDate=${selectMonth.value}`
  })
}
</script>

<style lang="scss" scoped>
.switch-card {
  padding: px2vw(0) px2vw(16);
  border-radius: px2vw(24);
  font-size: px2vw(28);
  height: px2vw(44);
  line-height: px2vw(44);
  color: #5a6f82;
  &.active {
    background-color: #dbeaff;
    color: #0066ff;
  }
  + .switch-card {
    margin-left: px2vw(16);
  }
}
.close {
  position: absolute;
  top: px2vw(24);
  right: px2vw(24);
}
</style>
