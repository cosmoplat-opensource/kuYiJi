<template>
  <view class="h-full bg-f3f3f5 box flex flex-col overflow-hidden">
    <uni-nav-bar />
    <h-status-header title="不良品管理" />
    <view class="flex-1 flex flex-col mx-16 box overflow-hidden">
      <view class="h-80 flex align-center justify-between pr-24 box bg-fff rounded-16 mt-16">
        <h-tabs
          ref="hTabs"
          v-if="tabs.length === 2"
          :tabData="tabs"
          :activeIndex="activeIndex"
          :config="defaultConfig"
          wrapperClass="pl-8"
          itemClass="px-24"
          @tabClick="tabClick"
        />
        <h-tabs
          ref="hTabs"
          v-else
          :tabData="tabs"
          :activeIndex="activeIndex"
          :config="defaultConfig"
          wrapperClass="pl-8"
          itemClass="px-24"
          @tabClick="tabClick"
        />
        <btn-export @exportToMail="exportToMail" v-if="activeIndex === 0" />
      </view>
      <view class="box pl-16 flex align-center mt-24">
        <!--筛选条件-不良品清单-->
        <template v-if="activeIndex === 0">
          <view
            class="w-200 h-64 font-28 flex align-center rounded-16 bg-fff-80 pr-16 box"
            @tap.stop="showSelect = !showSelect"
          >
            <text class="flex-1 font-24 text-center">{{ selectType.word }}</text>
            <image src="/static/images/icon_input_arr.svg" class="icon-48" />
          </view>
          <view v-show="showSelect" class="relative pop-time w-full h-full" @tap.stop="showSelect = false">
            <view class="time-list bg-fff">
              <view
                v-for="(item, index) in typeList"
                :key="index"
                class="time-list-item w-100 font-28 bg-fff flex align-center justify-between pl-32 pr-32 box"
                @tap.stop="confirmType(item)"
              >
                <view>{{ item.word }}</view>
                <image v-if="item.type === selectType.type" src="/static/images/icon_checked.svg" class="icon-48" />
              </view>
            </view>
          </view>
          <template v-if="selectType.type === 'product'">
            <list-product-search
              @search="searchProductList"
              :needSelect="true"
              wrapperPaddingRight="pr-16"
              class="flex-1"
            />
            <h-page-video class="mx-8" :videoArr="[16, 17]" page-name="不良品管理-不良品清单" />
          </template>
          <view v-else class="pl-24 box h-64 flex-1">
            <h-search class="w-100" @searchInput="handleInput" bgClass="bg-fff-80" placeholder="输入员工名称" />
          </view>
        </template>
        <!--筛选条件-返修记录,质检历史-->
        <template v-else>
          <view class="h-64 flex-1 pr-16">
            <h-search class="w-100" @searchInput="handleRepairInput" placeholder="输入产品编码/名称/工序" />
          </view>
          <h-page-video
            class="mx-8"
            :videoArr="[16, 17]"
            :page-name="activeIndex === 1 ? '不良品管理-返修记录' : '不良品管理-质检历史'"
          />
        </template>
      </view>
      <!--筛选条件-质检历史-->
      <template v-if="activeIndex === 2">
        <view class="flex font-24 align-center px-16 box pt-24 pb-8">
          <view
            class="py-10 px-16 box rounded-24"
            :class="dateType === 1 ? 'bg-dbeaff color-0066ff' : 'bg-f3f3f5 color-999'"
            @tap="selectDataType(1)"
          >
            本月
          </view>
          <view
            class="py-10 px-16 box rounded-24 ml-16"
            :class="dateType === 2 ? 'bg-dbeaff color-0066ff' : 'bg-f3f3f5 color-999'"
            @tap="selectDataType(2)"
          >
            本年
          </view>
          <view class="flex-1"></view>
          <view class="flex align-center color-999 font-24">
            <picker
              mode="date"
              :value="startDate"
              :start="startDatePicker"
              :end="endDatePicker"
              @change="bindDateChangeStart"
              class="py-10 px-16 bg-fff color-333 rounded-24 mr-8 px-8 box"
            >
              {{ startDate }}
            </picker>
            ~
            <picker
              mode="date"
              :value="endDate"
              :start="startDatePicker"
              :end="endDatePicker"
              @change="bindDateChangeEnd"
              class="py-10 px-16 text-center bg-fff color-333 rounded-24 ml-8 px-8 box"
            >
              {{ endDate }}
            </picker>
          </view>
        </view>
      </template>
      <scroll-view
        @refresherrefresh="pageListRefresh"
        @scrolltolower="pageListLoadMore"
        :refresher-triggered="refreshTag"
        refresher-enabled
        scroll-y
        class="flex-1 mt-16 overflow-hidden"
        enable-flex
      >
        <template v-if="pageEmpty">
          <h-empty className="pt-240" :tipsWord="tipsWord" />
        </template>
        <template v-else>
          <list-item-order v-if="activeIndex === 0" />
          <list-item-repair v-if="activeIndex === 1" />
          <list-item-history v-if="activeIndex === 2" />
          <!--下拉加载更多-->
          <uni-load-more :status="dataNoMore" />
        </template>
      </scroll-view>
    </view>
    <h-status-footer />
    <h-popup />
    <export-email ref="exportEmail" :exportLoading="exportLoading" @confirmPopup="confirmExportPopup" />
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
import dayjs from 'dayjs'
import HPopup from '@/components/h-popup.vue'
import { $store } from '@/utils/common'
import ListProductSearch from '@/components/product-search/list-search.vue'
import ListItemOrder from '@/pages-rejects/list-item-order.vue'
import { onShow } from '@dcloudio/uni-app'
import ListItemRepair from '@/pages-rejects/list-item-repair.vue'
import HooksSettingConfig from '@/hooks/setting-config'
import ListItemHistory from '@/pages-rejects/list-item-history.vue'

// ------------------------导出到email模块--------------
const { exportEmail, exportToMail, exportLoading, confirmExportPopup, setExportUrl } = HooksExportEmail()
const { checkSubmitInspect } = HooksSettingConfig()
onMounted(() => {
  setExportUrl('/ngProduct/manage/export')
  selectDataType(1)
})

onShow(() => {
  refreshPageList()
})

// ------------------------tabs模块----------------------
const tabs = ref([
  {
    state: 0,
    disabled: false,
    name: '不良品清单'
  },
  {
    state: 1,
    disabled: false,
    name: '返修记录'
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
  handleInputClear()
}
function checkTabs() {
  if (checkSubmitInspect.value && !tabs.value.find((v) => v.name === '质检历史')) {
    tabs.value.push({
      state: 2,
      disabled: false,
      name: '质检历史'
    })
  }
  if (!checkSubmitInspect.value && tabs.value.find((v) => v.name === '质检历史')) {
    tabs.value.pop()
  }
}

// ------------------------------分页列表模块------------------
const dataListCallBack = (res) => {
  $store.commit('settlement/setSettlementList', dataList.value)
}
const { dataList, pageListRefresh, pageTotal, dataNoMore, refreshTag, pageEmpty, pageListLoadMore, pageListSetParams } =
  HooksPageList('', {
    run: false,
    pageSize: 20,
    cb: dataListCallBack
  })

// ----------------------------切换产品/员工-----------------
// 空数据提示
const tipsWord = computed(() => {
  switch (activeIndex.value) {
    case 0:
      // 不良品清单/统计
      return '暂无不良品清单数据'
    case 1:
      // 返修记录
      return '暂无返修记录数据'
    case 2:
      // 质检历史
      return '暂无质检历史数据'
  }
})
const searchInput = ref('')
const repairSearchInput = ref('')
function handleRepairInput(str) {
  repairSearchInput.value = str
  refreshPageList()
}
function handleClearRepairInput() {
  repairSearchInput.value = ''
  refreshPageList()
}
function handleInputClear() {
  searchInput.value = ''
  repairSearchInput.value = ''
  refreshPageList()
}
function refreshPageList() {
  checkTabs()
  $store.commit('settlement/setSettlementList', [])
  if (activeIndex.value === 0 && selectType.value.type === 'product') {
    // 不良品清单产品维度
    pageListSetParams({ productNameOrCode: productNameOrCode.value }, '/ngProduct/manage/list/product')
  } else if (activeIndex.value === 0 && selectType.value.type === 'staff') {
    // 不良品清单员工维度
    pageListSetParams({ submitNickName: staffName.value }, '/ngProduct/manage/list/user')
  } else if (activeIndex.value === 1) {
    // 返修记录
    pageListSetParams({ queryKey: repairSearchInput.value }, '/ngProduct/manage/repairRecord/list')
  } else if (activeIndex.value === 2) {
    // 质检历史
    pageListSetParams(
      { startDate: startDate.value, endDate: endDate.value, queryKey: repairSearchInput.value },
      '/ngProduct/manage/qc/history/list'
    )
  }
}

// ----------------查询维度筛选----------------
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
  refreshPageList()
  showSelect.value = false
}
//搜索待确认-按产品
const productNameOrCode = ref('')
function searchProductList(str) {
  productNameOrCode.value = str.itemName || ''
  refreshPageList()
}
//搜索待确认-按员工查
const staffName = ref('')
function handleInput(str) {
  staffName.value = str
  refreshPageList()
}

// --------------------时间选择--------------------------
const dateType = ref(1)
const startDate = ref('')
const endDate = ref('')
function selectDataType(type) {
  dateType.value = type
  if (type === 1) {
    startDate.value = dayjs().startOf('month').format('YYYY-MM-DD')
    endDate.value = dayjs().endOf('month').format('YYYY-MM-DD')
  }
  if (type === 2) {
    startDate.value = dayjs().startOf('year').format('YYYY-MM-DD')
    endDate.value = dayjs().endOf('year').format('YYYY-MM-DD')
  }
  refreshPageList()
}
//设置可选择的起始时间
const startDatePicker = computed(() => {
  return getDate('start')
})
const endDatePicker = computed(() => {
  return getDate('end')
})
function getDate(type) {
  switch (type) {
    case 'start':
      return dayjs().subtract(60, 'year').format('YYYY-MM-DD')
    case 'end':
      return dayjs().add(2, 'year').format('YYYY-MM-DD')
    default:
      return ''
  }
}
//选择自定义时间
function bindDateChangeStart(e) {
  dateType.value = 3
  if (endDate.value) {
    let before = dayjs(e.detail.value).isBefore(endDate.value)
    let same = dayjs(e.detail.value).isSame(dayjs(endDate.value))
    if (before || same) {
      startDate.value = e.detail.value
      refreshPageList()
    } else {
      uni.showToast({ title: '开始日期必须晚于结束日期', icon: 'none', duration: 2000 })
    }
  } else {
    startDate.value = e.detail.value
    refreshPageList()
  }
}
function bindDateChangeEnd(e) {
  dateType.value = 3
  if (startDate.value) {
    let after = dayjs(e.detail.value).isAfter(startDate.value)
    let same = dayjs(e.detail.value).isSame(dayjs(startDate.value))
    if (after || same) {
      endDate.value = e.detail.value
      refreshPageList()
    } else {
      uni.showToast({ title: '开始日期必须晚于结束日期', icon: 'none', duration: 2000 })
    }
  } else {
    endDate.value = e.detail.value
    refreshPageList()
  }
}
</script>

<style lang="scss" scoped>
.switch-card {
  padding: px2vw(8) px2vw(16);
  border-radius: px2vw(24);
  font-size: px2vw(28);
  line-height: px2vw(28);
  color: #5a6f82;
  &.active {
    background-color: #dbeaff;
    color: #0066ff;
  }
  + .switch-card {
    margin-left: px2vw(16);
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
.bg-fff-16 {
  background: rgba(255, 255, 255, 0.16);
}
.vertical-line {
  width: px2vw(1);
  height: px2vw(72);
  background: #e5e5e5;
}
.bg-view {
  width: px2vw(104);
  height: px2vw(32);
  line-height: px2vw(32);
}
</style>
