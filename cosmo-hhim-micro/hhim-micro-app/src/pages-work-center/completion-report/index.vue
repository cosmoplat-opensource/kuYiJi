<template>
  <view class="finish-report-list" @touchstart="touchStart" @touchend="touchEnd">
    <view class="flex flex-col relative">
      <uni-nav-bar />
      <h-status-header title="完工报告" />
      <view class="w-100 h-80 px-16 box mt-16">
        <view class="w-100 h-80 flex align-center justify-between pr-16 box bg-fff rounded-16">
          <h-tabs ref="hTabs" :tabData="tabs" :activeIndex="activeIndex" :config="defaultConfig" @tabClick="tabClick" />
          <view class="flex align-center">
            <btn-export @exportToMail="exportMail" class="mr-32" />
            <view class="flex-1 flex align-center justify-end" v-if="activeIndex === 0">
              <view class="font-24">
                <text class="color-333">已选</text>
                <text class="color-ff0000 ml-4 bold">{{ getCheckedCount }}</text>
              </view>
              <view
                class="font-24 ml-24 px-16 py-12 rounded-24"
                :class="checkAll ? 'color-fff bg-0066ff' : 'color-5a6f82 bg-f3f3f5'"
                @tap="handleCheckAll"
                >{{ checkAll ? '全不选' : '全选' }}
              </view>
            </view>
            <view v-else>
              <view
                class="font-24 px-16 py-12 rounded-24"
                :class="checkCancel ? 'color-0066ff bg-f3f3f5' : 'color-5a6f82 bg-f3f3f5'"
                @tap="handleCheckCancel"
                >{{ checkCancel ? '取消' : '撤销' }}
              </view>
            </view>
          </view>
        </view>
      </view>
      <view v-show="activeIndex === 0" class="flex align-center pl-32 box mt-24 mb-16">
        <view
          class="w-200 h-64 font-28 flex align-center rounded-16 bg-fff-80 pr-16 box"
          @tap.stop="showSelect = !showSelect"
        >
          <text class="flex-1 font-24 text-center">{{ selectType.word }}</text>
          <image src="/static/images/icon_input_arr.svg" class="icon-48" />
        </view>
        <view v-show="showSelect" class="relative pop-time w-full h-full" @tap.stop="showSelect = false">
          <view class="time-list bg-fff rounded-16">
            <view
              v-for="(item, index) in typeList"
              :key="index"
              class="time-list-item w-100 font-28 bg-fff flex align-center justify-between pl-32 pr-32 box"
              :class="[index + 1 === typeList.length ? 'rounded-16-bottom' : '', index === 0 ? 'rounded-16-top' : '']"
              @tap.stop="confirmType(item)"
            >
              <view>{{ item.word }}</view>
              <image v-if="item.type === selectType.type" src="/static/images/icon_checked.svg" class="icon-48" />
            </view>
          </view>
        </view>
        <list-product-search
          v-if="selectType.type === 'product'"
          wrapper-padding-right="pr-0"
          @search="searchProductList"
          :needSelect="false"
          class="flex-1"
        />
        <view v-else class="pl-24 box flex-1 flex">
          <h-search placeholder="输入员工名称" class="flex-1" @searchInput="handleInput" />
        </view>
        <h-page-video v-show="activeIndex === 0" :videoArr="[11]" page-name="完工报告-待确认" class="ml-24 mr-32" />
      </view>
      <view v-show="activeIndex === 1">
        <view class="px-32 box my-24 flex-1 flex justify-between overflow-hidden">
          <view class="flex">
            <view
              :class="[
                'h-44 px-16 flex-ac rounded-24 font-28',
                completeSelectType === '1' ? ' bg-dbeaff color-0066ff' : ''
              ]"
              @tap="completeSelectType = '1'"
            >
              产品
            </view>
            <view
              :class="[
                'h-44 px-16 flex-ac rounded-24 font-28 ml-16',
                completeSelectType === '2' ? ' bg-dbeaff color-0066ff' : ''
              ]"
              @tap="completeSelectType = '2'"
            >
              完工报告
            </view>
          </view>
          <time-select :start="startDate" :end="endDate" @search="searchComplete" />
        </view>
        <view v-show="completeSelectType === '1'" class="px-32 mb-16"
          ><list-product-search
            wrapper-padding-right="pr-0"
            wrapperPaddingLeft="pl-0"
            @search="searchCompleteList"
            :needSelect="false"
        /></view>
      </view>
    </view>

    <!--数据列表滚动区域-->
    <scroll-view
      :style="activeIndex === 1 ? '' : 'display:none'"
      scroll-y
      :scroll-into-view="scrollTop"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
      :refresher-triggered="triggered"
      refresher-enabled
      class="area-scroll"
      enable-flex
    >
      <h-empty v-if="pageEmptyFinish" tipsWord="未查询到相关的记工记录，请重新搜索" class-name="pt-240" />
      <template v-else>
        <view v-for="(dataItem, index) in completeList" :key="index" class="px-16 box" @tap="handleDetail(dataItem)">
          <template v-if="completeSelectType === '1'">
            <complete-product-item :dataItem="dataItem" />
          </template>
          <template v-else>
            <view class="w-100 bg-fff px-32 box mb-8">
              <view
                class="flex flex-col pt-24 pb-26 box"
                :class="[dataItem.listShow ? 'border-bottom-dashed mb-8' : '']"
              >
                <view class="flex justify-between align-center word-break box h-48">
                  <view class="font-28 relative flex-1 bold finish">
                    <text>{{ dataItem.reportNo }}</text>
                  </view>
                  <image v-if="!checkCancel" :src="formatImage('icon_detail')" class="icon-32 ml-16" />
                  <h-button
                    v-else-if="checkCancel && dayjs(dataItem.completeTime).month() + 1 === currentMonth"
                    width="96"
                    height="48"
                    text="撤销"
                    font="font-24"
                    @tap.stop="revoke(dataItem)"
                  />
                  <view v-else class="font-24 color-5a6f82">已完成记工计算，无法撤销</view></view
                >

                <view class="flex align-center font-28 color-5a6f82 mt-24">
                  <view class="mark-ebf0f5">操作员</view>
                  <text class="ml-16">{{ dataItem.completeUserNickName }}</text>
                  <text class="ml-16">{{ formatDate(dataItem.completeTime) }}</text>
                </view>
                <view class="flex align-center font-28 mt-24 overflow-hidden">
                  <view class="flex align-center">
                    <view class="mark-ebf0f5">产品</view>
                    <text class="ml-12 bold mx-8">{{ dataItem.productCategoryTotalNum }}</text>
                    <text>款</text>
                  </view>
                  <view class="flex align-center ml-28">
                    <view class="mark-ebf0f5">数量</view><text class="ml-12 bold">{{ dataItem.productTotalNum }}</text>
                  </view>
                  <view class="flex-1"></view>
                  <image
                    src="/static/images/icon_unfold.svg"
                    class="icon-32 ml-16"
                    :class="{ 'icon-rotate-y': dataItem.listShow }"
                    @tap.stop="handleListShow(dataItem)"
                  />
                </view>
              </view>
              <view v-if="dataItem.listShow" class="list-item">
                <view
                  v-for="(item, idx) in dataItem.productInfoList"
                  :key="idx"
                  class="flex align-center justify-between py-24 box border-bottom-f5f5f5"
                >
                  <view class="font-28 relative flex-1 bold">
                    <text>{{ item.productName }}</text>
                    <text class="color-999">({{ item.productSeq }})</text>
                  </view>
                  <view class="font-28 w-176 flex align-center justify-end">
                    <word-icon class="font-24" text="共" />
                    <text class="color-333 bold ml-12 mr-8">{{ item.completeTotalNum }}</text>
                    <text>{{ item.productUnit }}</text>
                  </view>
                </view>
                <view class="flex justify-center align-center h-80">
                  <view
                    @tap.stop="handleListShow(dataItem)"
                    class="w-80 h-32 bg-EBF0F5 flex justify-center align-center font-24 color-5a6f82 rounded-16"
                    >收起</view
                  >
                </view>
              </view>
            </view>
          </template>
        </view>
        <uni-load-more :status="dataNoMoreFinish" :contentText="contentText" />
      </template>
    </scroll-view>
    <scroll-view
      :style="activeIndex === 0 ? '' : 'display:none'"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
      :scroll-into-view="scrollTop"
      :refresher-triggered="triggeredProduct"
      enable-flex
      refresher-enabled
      scroll-y
      class="w-100 px-16 box flex-1 overflow-hidden"
    >
      <!-- 产品列表 -->
      <h-empty v-if="pageEmptyProduct" className="pt-240" tipsWord="未查询到相关的记工记录，请重新搜索" />
      <template v-else>
        <view
          v-for="(dataItem, index) in ProductList"
          :key="index"
          class="bg-fff box pt-32 px-32 mb-8 relative"
          @tap="handleItemCheck(dataItem)"
        >
          <!-- 产品信息 -->
          <view class="flex align-center justify-between">
            <view class="flex align-center">
              <view class="font-32 relative wait bold flex align-center">
                <h-text-display :text="dataItem.productName" :width="220" class="overflow-hidden" />
                <text class="color-999">({{ dataItem.productSeq }})</text>
              </view>
            </view>
            <view v-if="dataItem.checkStatus === 2" class="mark-00bfa5 ml-8"> 已检 </view>
            <view class="flex-1" />
            <h-checkbox :checked="dataItem.checked" @checkedChange="handleItemCheck(dataItem)" />
          </view>
          <!-- 记工人数据 -->
          <view class="flex align-center mt-32">
            <view class="mark-ebf0f5">记工</view>
            <h-text-display :text="dataItem.submitUserNickName" :width="120" class="ml-16 font-28 color-5a6f82" />
            <view class="font-28 color-5a6f82 ml-16">{{ formatDate(dataItem.submitWorkDate) }}</view>
          </view>
          <!--产品数量-->
          <view class="mt-24 flex align-center pr-32 pb-24">
            <area-date-info
              :dataItem="{ pass: dataItem.goodNum, ng: dataItem.ngNum, productUnit: dataItem.productUnit }"
            />
            <word-icon class="ml-24" text="共" />
            <view class="ml-8 color-5a6f82 font-28 bold">
              {{ new bigNumber(dataItem.goodNum).plus(dataItem.ngNum).toString() + (dataItem.unit || '') }}
            </view>
          </view>
          <view v-if="dataItem.remark" class="h-76 flex align-center border-top-f5f5f5 color-5a6f82 font-28"
            >备注：<h-text-display :text="dataItem.remark" class="flex-1 overflow-hidden"
          /></view>
        </view>

        <uni-load-more :status="dataNoMoreProduct" :contentText="contentText" />
      </template>
    </scroll-view>
    <view v-if="activeIndex === 0" class="flex align-top px-48 box py-32 w-100">
      <h-button width="624" height="72" text="完工" @tap.stop="showCompletePopup" />
    </view>
    <!-- 将完工的列表 -->
    <!-- animation=false：H5 下 uni-popup 动画可能卡住导致弹窗不显示，关闭动画确保稳定弹出 -->
    <uni-popup ref="popup" type="bottom" @maskClick="handleCancel" :safe-area="false" :animation="false">
      <view class="open-filter bg-fff flex flex-col justify-start font-28">
        <view class="w-100 h-96 flex align-center justify-center font-28 color-5a6f82 bold">
          确认完工
          <image @click="handleCancel" src="/static/images/icon_close_666.svg" class="icon-48 close" />
        </view>
        <view class="bg-f7f8e7 h-56 pl-32 box font-24 color-5a6f82 flex align-center">
          已选产品：
          <text class="color-ff0000 mx-8"> {{ selectMergeList.length }} </text>
          <text>款</text>
        </view>
        <scroll-view scroll-y class="w-100 pl-32 box flex-1 overflow-hidden">
          <view
            v-for="(selectItem, idx) in selectMergeList"
            :key="idx"
            class="h-96 flex align-center border-bottom-f5f5f5 pr-32 box"
          >
            <view class="font-28 relative flex-1 bold flex align-center">
              <h-text-display :text="selectItem.productName" :width="200" />
              <text class="color-999">({{ selectItem.productSeq }})</text>
            </view>
            <view class="flex-1" />
            <area-date-info
              :dataItem="{ pass: selectItem.goodNum, productUnit: selectItem.productUnit }"
              :showNg="false"
            />
          </view>
          <view class="font-24 color-b6c0c9 my-32 text-center">已显示全部</view>
        </scroll-view>
        <view class="flex justify-center py-32">
          <h-button width="296" height="72" text="取消" type="bg-f3f3f5 color-333" @tap.stop="handleCancel" />
          <h-button width="296" height="72" text="确认完工" class="ml-32" @tap.stop="handleConfirm" />
        </view>
      </view>
    </uni-popup>
    <h-popup tabBar />
    <export-email ref="exportEmail" :exportLoading="exportLoading" @confirmPopup="confirmExport" />
  </view>
  <h-share />
</template>
<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import ListProductSearch from '@/components/product-search/list-search.vue'
import bigNumber, { BigNumber } from 'bignumber.js'
import { _get, _post, _put, _delete } from '@/utils/common-request'
import { onLoad, onShow, onTabItemTap, onUnload } from '@dcloudio/uni-app'
import { $state, $store, formatDate, uuc } from '@/utils/common'
import { formatImage } from '@/utils/common'
import HTabs from '@/components/h-tabs.vue'
import HEmpty from '@/components/h-empty.vue'
import HooksPageSwiper from '@/hooks/pages-swiper'
import AreaDateInfo from '@/pages/report-production/components/area-date-info.vue'
import TimeSelect from '@/pages-work-center/completion-report/components/time-select.vue'
import CompleteProductItem from '@/pages-work-center/completion-report/components/complete-product-item.vue'
import HExceptionLabel from '@/components/h-exception-label.vue'
import BtnExport from '@/components/btn-export.vue'
import ExportEmail from '@/components/export-email.vue'
import HooksExportEmail from '@/hooks/export-email'
import dayjs from 'dayjs'
const contentText = ref({
  contentnomore: '已显示全部'
})
// tab切换
const activeIndex = ref(0)
const tabs = ref([
  {
    state: 0,
    name: '待确认'
  },
  {
    state: 1,
    name: '已完成'
  }
])
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
  if (event === 1) {
    getCompleteList()
  }
  if (event === 0) {
    checkAll.value = false
    //选中状态清空
    getProductList()
  }
}
//按产品查
const selectType = ref({
  type: 'product',
  word: '按产品查'
})
const showSelect = ref(false)
const typeList = ref([
  {
    type: 'product',
    word: '按产品查'
  },
  {
    type: 'staff',
    word: '按员工查'
  }
])
function confirmType(item) {
  selectType.value = item
  if (selectType.value.type === 'product') {
    staffName.value = ''
  }
  if (selectType.value.type === 'staff') {
    productNameOrCode.value = ''
  }
  showSelect.value = false
  getProductList()
}
// 全选操作
const checkAll = ref(false)
const getCheckedCount = computed(() => {
  return ProductList.value.filter((v) => v.checked).length
})
function handleItemCheck(item) {
  const obj = ProductList.value.find((v) => v.workSubmitId && v.workSubmitId === item.workSubmitId)
  obj && (obj.checked = !obj.checked)
  checkAll.value = ProductList.value.every((v) => v.checked)
}

function handleCheckAll() {
  checkAll.value = !checkAll.value
  ProductList.value.forEach((v) => {
    v.checked = checkAll.value
  })
}
//搜索待确认-按产品
const productNameOrCode = ref('')
function searchProductList(str) {
  pagesProduct.pageNum = 1
  productNameOrCode.value = str
  getProductList()
}
//搜索待确认-按员工查
const staffName = ref('')
function handleInput(str) {
  pagesProduct.pageNum = 1
  staffName.value = str
  getProductList()
}

//获取待确认产品数据
const ProductList = ref([])
const pageEmptyProduct = ref(false)
const dataNoMoreProduct = ref('')
function getProductList() {
  dataNoMoreProduct.value = 'loading'
  let mockParams = {
    url: '/complete/report/waitDeal/list',
    data: {
      productSeqOrName: productNameOrCode.value,
      nickName: staffName.value,
      ...pagesProduct
    }
  }

  _get(mockParams)
    .then((res) => {
      if (pagesProduct.pageNum === 1) {
        ProductList.value = []
      }
      ProductList.value.push(
        ...res.rows.map((v) => {
          v.checked = false
          return v
        })
      )
      // ProductList.value.push(...res.rows)
      dataTotalProduct.value = res?.total || 0
      if (pagesProduct.pageNum === 1 && ProductList.value.length) {
        setTimeout(() => {
          scrollTop.value = 'scroll-product-0'
        }, 200)
      }
    })
    .finally(() => {
      triggeredProduct.value = false
      pageEmptyProduct.value = !ProductList.value.length
      dataNoMoreProduct.value = dataEndProduct.value ? 'noMore' : 'more'
    })
}

// 页面左右滑动
const scrollTop = ref('')
const hTabs = ref(null)
const { touchStart, touchEnd } = HooksPageSwiper((direction) => {
  if (direction === 'left') {
    pages.pageNum = 1
    hTabs.value.tabClick(0)
  } else if (direction === 'right') {
    pagesProduct.pageNum = 1
    hTabs.value.tabClick(1)
  }
})

// 下拉刷新&加载更多，统一处理
const triggered = ref(false)
const triggeredProduct = ref(false)
const pages = reactive({ pageSize: 10, pageNum: 1 })
const pagesProduct = reactive({ pageSize: 10, pageNum: 1 })
const dataTotal = ref(0)
const dataTotalProduct = ref(0)
const dataEnd = computed(() => pages.pageNum * pages.pageSize >= dataTotal.value)
const dataEndProduct = computed(() => pagesProduct.pageNum * pagesProduct.pageSize >= dataTotalProduct.value)
function onRefresh() {
  if (activeIndex.value === 1) {
    triggered.value = true
    pages.pageNum = 1
    getCompleteList()
  }
  if (activeIndex.value === 0) {
    triggeredProduct.value = true
    pagesProduct.pageNum = 1
    getProductList()
  }
}
function onLoadMore() {
  if (activeIndex.value === 1) {
    if (dataEnd.value) {
      return
    }
    pages.pageNum++
    getCompleteList()
  }
  if (activeIndex.value === 0) {
    if (dataEndProduct.value) {
      return
    }
    pagesProduct.pageNum++
    getProductList()
  }
}
//搜索已完成列表
const startDate = ref(dayjs().startOf('month').format('YYYY-MM-DD'))
const endDate = ref(dayjs().endOf('month').format('YYYY-MM-DD'))
function searchComplete(start, end) {
  startDate.value = start
  endDate.value = end
  getCompleteList()
}
//撤销-取消操作
const checkCancel = ref(false)
function handleCheckCancel() {
  if (!checkCancel.value && completeSelectType.value === '1') {
    //产品维度点击撤销 切换到 完工报告维度
    completeSelectType.value = '2'
  }
  checkCancel.value = !checkCancel.value
}
//已完成查询维度
// productCodeOrName 必须声明在 watch 之前：watch(completeSelectType, { immediate: true })
// 同步执行 getCompleteList() 会访问它，靠后声明导致 H5 端 TDZ 报错整页白屏（与 calendar-month 同款问题）
const productCodeOrName = ref('')
// completeList/pageEmptyFinish/dataNoMoreFinish 也必须声明在 watch 之前：
// watch(completeSelectType, { immediate: true }) 同步执行 getCompleteList()，
// 其第一行访问 dataNoMoreFinish（dataNoMoreFinish.value = 'loading'），
// 靠后声明会导致 H5 端 TDZ 报错整页白屏
const completeList = ref([])
const pageEmptyFinish = ref(false)
const dataNoMoreFinish = ref('')
const completeSelectType = ref('1')
watch(
  completeSelectType,
  (val) => {
    if (val === '1') {
      checkCancel.value = false
    }
    pages.pageNum = 1
    getCompleteList()
  },
  { immediate: true }
)
function searchCompleteList(str) {
  pages.pageNum = 1
  productCodeOrName.value = str
  getCompleteList()
}
//获取已完成列表
function getCompleteList() {
  dataNoMoreFinish.value = 'loading'
  let params = {
    url: '',
    data: { startDate: startDate.value, endDate: endDate.value, ...pages }
  }
  if (completeSelectType.value === '1') {
    params.url = '/complete/report/finishDeal/productSide/list'
    params.data.productCodeOrName = productCodeOrName.value
  }
  if (completeSelectType.value === '2') {
    params.url = '/complete/report/finishDeal/list'
    delete params.data.productCodeOrName
  }
  _get(params)
    .then((res: IResponseType<unknown>) => {
      if (pages.pageNum === 1) {
        completeList.value = []
      }
      completeList.value.push(...res.rows)
      dataTotal.value = res.total
      completeList.value.forEach((item) => {
        item.listShow = false
      })
    })
    .finally(() => {
      triggered.value = false
      pageEmptyFinish.value = !completeList.value.length
      dataNoMoreFinish.value = dataEnd.value ? 'noMore' : 'more'
    })
}
//展开明细
function handleListShow(dataItem) {
  dataItem.listShow = !dataItem.listShow
  if (dataItem.listShow) {
    _get({
      url: `/complete/report/group/${dataItem.reportNo}`
    })
      .then((res) => {
        dataItem.productInfoList = res.data
      })
      .catch(() => {})
      .finally(() => {})
  }
}
//撤销显示条件
const currentMonth = ref(dayjs().month() + 1)

//点击撤销
function revoke(dataItem) {
  _get({
    url: `/complete/report/cancelcheck/${dataItem.reportNo}`
  })
    .then((res) => {
      reportCancel(dataItem.reportNo)
    })
    .catch(() => {
      uni.showModal({
        title: '提示',
        content: '确认要撤销该笔入库单吗?',
        success: function (res) {
          if (res.confirm) {
            reportCancel(dataItem.reportNo)
          }
        }
      })
    })
    .finally(() => {})
}
function reportCancel(reportNo) {
  _delete({
    url: `/complete/report/cancel/${reportNo}`
  })
    .then((res) => {
      uni.showToast({ title: '撤销成功', icon: 'none' })
      pages.pageNum = 1
      getCompleteList()
      //刷新工作台的完工报告数量
      uni.$emit('finishedProductStatisticsRefresh')
      uni.$emit('storageFinishStatisticRefresh')
    })
    .finally(() => {})
}
//点击完工单，进入完工单详情，显示相关的记工记录
function handleDetail(dataItem) {
  if (completeSelectType.value === '1') {
    uni.navigateTo({
      url:
        '/pages-work-center/completion-report/detail-complete-product?orderInfo=' +
        JSON.stringify(dataItem) +
        '&startDate=' +
        startDate.value +
        '&endDate=' +
        endDate.value
    })
  } else {
    uni.navigateTo({ url: '/pages-work-center/completion-report/detail?orderInfo=' + JSON.stringify(dataItem) })
  }
}
// 点击完工
const selectList = ref([])
//处理过的合并列表
const selectMergeList = ref([])

function showCompletePopup() {
  let list = JSON.parse(JSON.stringify(ProductList.value))
  selectList.value = list.filter((v) => v.checked)
  selectMergeList.value = []
  if (selectList.value.length > 0) {
    for (let i = 0; i < selectList.value.length; i++) {
      let hasCommon = false
      let item = { ...selectList.value[i] }
      for (let j = 0; j < selectMergeList.value.length; j++) {
        if (selectList.value[i].productSeq === selectMergeList.value[j].productSeq) {
          selectMergeList.value[j].goodNum = new bigNumber(selectMergeList.value[j].goodNum)
            .plus(item.goodNum)
            .toNumber()
          hasCommon = true
          break
        }
      }
      if (!hasCommon) {
        selectMergeList.value.push(item)
      }
    }
    popup.value?.open('bottom')
  } else {
    uni.showToast({
      title: '请先勾选数据',
      duration: 2000,
      icon: 'none'
    })
  }
}
const popup = ref(null)
function handleCancel() {
  popup.value?.close('bottom')
}
const finishLoading = ref(false)
function handleConfirm() {
  if (finishLoading.value) {
    return
  }
  let reportData = []
  let reportItem = {}
  selectList.value.forEach((item) => {
    reportItem = {
      productSeq: item.productSeq,
      productName: item.productName,
      completeNum: item.goodNum,
      workSubmitId: item.workSubmitId
    }
    reportData.push(reportItem)
  })
  finishLoading.value = true
  _post({
    url: '/complete/report/add',
    data: reportData
  })
    .then((res) => {
      uni.showToast({
        title: '提交成功',
        duration: 2000,
        icon: 'none'
      })
      handleCancel()
      pagesProduct.pageNum = 1
      getProductList()
      //刷新工作台的完工报告数量
      uni.$emit('finishedProductStatisticsRefresh')
      uni.$emit('storageFinishStatisticRefresh')
    })
    .finally(() => {
      finishLoading.value = false
    })
}
//导出
/** 导出到email模块 */
const { exportEmail, exportToMail, exportLoading, confirmExportPopup, setExportUrl } = HooksExportEmail()
function exportMail() {
  if (activeIndex.value === 1) {
    exportToMail(startDate.value, endDate.value)
    return
  }
  exportToMail()
}
function confirmExport(receivedBy) {
  //待确认
  if (activeIndex.value === 0) {
    setExportUrl(`/complete/report/waitDeal/export`, {
      receivedBy: receivedBy,
      nickName: staffName.value || '',
      productSeqOrName: productNameOrCode.value || ''
    })
  }
  //已完成
  if (activeIndex.value === 1) {
    setExportUrl(`/complete/report/finishDeal/export`, {
      receivedBy: receivedBy,
      startDate: startDate.value,
      endDate: endDate.value
    })
  }
  confirmExportPopup()
}

onMounted(() => {
  getProductList()
})
</script>
<style lang="scss" scoped>
.pl-0 {
  padding-left: 0;
}
.number-mark {
  background: #ebf0f5;
  border-radius: 0 px2vw(4) px2vw(4) 0;
  height: px2vw(32);
  position: absolute;
  z-index: 2;
  left: px2vw(-48);
  top: px2vw(26);
}
.input-area {
  height: px2vw(64);
  background: #ebecee;
  box-shadow: 0 px2vw(1) 0 px2vw(1) rgba(255, 255, 255, 1), inset 0 px2vw(8) px2vw(12) px2vw(1) rgba(204, 209, 217, 1);
  border-radius: px2vw(16);
  padding: 0 px2vw(24) 0 px2vw(32);
}
.open-filter {
  border-radius: px2vw(16) px2vw(16) 0 0;
  max-height: px2vw(878);
  .close {
    position: absolute;
    top: px2vw(24);
    right: px2vw(24);
  }
}
.pop-time {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 2;
  background-color: transparent;
}
.time-list {
  width: px2vw(288);
  position: absolute;
  top: px2vw(360);
  left: px2vw(32);
  z-index: 999;
  box-shadow: 0 px2vw(8) px2vw(16) px2vw(1) rgba(182, 192, 201, 0.5);
  .time-list-item {
    height: px2vw(96);
    line-height: px2vw(96);
    border-bottom: px2vw(1) solid #f5f5f5;
    &:active {
      background-color: #f3f3f5;
    }
  }
}
.wait:before {
  top: px2vw(4);
}
.finish:before {
  top: px2vw(2);
}
.finish-report-list {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f3f3f5;
  overflow: hidden;
  .area-scroll {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }
}
</style>
