<template>
  <view class="h-100 flex flex-col overflow-hidden bg-f3f3f5" @touchstart="touchStart" @touchend="touchEnd">
    <!-- 我的关注 -->
    <view class="box pt-8 pl-24 pr-32 pb-16 flex align-center">
      <view
        v-if="followChecked"
        class="flex align-center bg-fff border-1 border-fff pl-16 pr-24 box rounded-32"
        @tap="handleFollowCheck"
      >
        <image :src="formatImage('icon_quality_focus_h', 'svg')" class="icon-48" />
        <view class="ml-8 font-24 color-0066ff">我关注的</view>
      </view>
      <view
        v-else
        class="flex align-center bg-EBF0F5 border-1 border-d3dfeb pl-16 pr-24 box rounded-32"
        @tap="handleFollowCheck"
      >
        <image :src="formatImage('icon_quality_focus', 'svg')" class="icon-48" />
        <view class="ml-8 font-24 color-5a6f82">我关注的</view>
      </view>
      <view class="flex-1" />
      <h-page-video :videoArr="[19]" page-name="质检列表" />
      <image @tap="handleOpenFilter" src="/static/images/icon_filter_b6c0c9.svg" class="icon-48 ml-24" />
    </view>
    <!-- 待检/已检 tab -->
    <view class="flex flex-col relative">
      <view class="box mx-16 bg-fff border-16 h-80 flex align-center pl-32 pr-16">
        <view
          class="font-28 h-100 relative flex align-center"
          :class="tabActive === 1 ? 'color-333 bold' : 'color-999'"
          @tap="handleTabChange(1)"
        >
          <text>待检</text>
          <text v-if="totalCount">({{ totalCount }})</text>
          <view class="h-8 w-100 rounded-4 bg-0066ff tab-line" v-if="tabActive === 1" />
        </view>
        <view
          class="font-28 ml-64 h-100 relative flex align-center"
          :class="tabActive === 2 ? 'color-333 bold' : 'color-999'"
          @tap="handleTabChange(2)"
        >
          <text>已检</text>
          <view class="h-8 w-100 rounded-4 bg-0066ff tab-line" v-if="tabActive === 2" />
        </view>

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
          :class="checkAble ? 'color-0066ff' : 'color-5a6f82'"
          @tap="handleCheckedAble(!checkAble)"
          v-if="tabActive === 1"
        >
          <text class="font-24 bold">{{ checkAble ? '取消' : '批量' }}</text>
        </view>
      </view>
    </view>
    <!--顶部提示条-->
    <!--    <system-notice-area v-model="systemNotice" />-->
    <!--数据列表滚动区域-->
    <scroll-view
      @refresherrefresh="pageListRefresh"
      @scrolltolower="pageListLoadMore"
      :refresher-triggered="refreshTag"
      :scroll-into-view="scrollTop"
      scroll-y
      refresher-enabled
      class="flex-1 flex flex-col mt-8 mb-8 overflow-hidden"
      enable-flex
    >
      <h-empty v-if="pageEmpty" tipsWord="暂无质检数据" class-name="pt-240" />
      <template v-if="dataList.length">
        <list-item
          v-for="(item, index) in dataList"
          :id="`scroll-${item.id}`"
          :key="`${tabActive}-${index}`"
          :dataItem="item"
          :checkAll="checkAble"
          @itemCheck="handleItemCheck"
        />
        <uni-load-more :status="dataNoMore" />
      </template>
    </scroll-view>
    <!--筛选弹窗-->
    <audit-filter ref="auditFilter" @refresh="setParamsAndRefresh" />
    <view class="btn-record">
      <h-button-record text="质检" v-if="checkAble" size="icon-160" @tap="handleAuditBatchCheck" />
    </view>

    <h-popup />
  </view>
</template>
<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import ListItem from './list-item.vue'
import { onShow } from '@dcloudio/uni-app'
import { _get } from '@/utils/common-request'
import { $store, collectPV } from '@/utils/common'
import { formatImage } from '@/utils/common'
import AuditFilter from '@/pages/audit/audit-filter.vue'
import HEmpty from '@/components/h-empty.vue'
import HooksPageSwiper from '@/hooks/pages-swiper'
import HooksPageList from '@/hooks/page-list'
import HButtonRecord from '@/components/h-button-record.vue'
import HPopup from '@/components/h-popup.vue'

const filterData = computed(() => $store.state.audit.filterData)
const auditFilter = ref(null)
function handleOpenFilter() {
  getFollowList()
  auditFilter.value?.open('normal')
}
//重置筛选项
function clearFilter() {
  $store.commit('audit/resetFilterData')
}

// 待审/已审切换
const tabActive = ref(1)
function handleTabChange(index) {
  tabActive.value = index
  setParamsAndRefresh()
}

// 获取待审/已审列表
const totalCount = ref(0)
function setParamsAndRefresh() {
  refreshTag.value = true
  const params = {
    ...filterData.value,
    checkStatus: tabActive.value
  }
  if (!followChecked.value) {
    params.submitUserIds = ''
  }
  pageListSetParams(params)
}
function listCallback(res) {
  // if (pageParams.pageNum === 1) {
  //   setTimeout(() => {
  //     scrollTop.value = 'scroll-' + dataList.value[0]?.id
  //   }, 200)
  // }
  if (tabActive.value === 1) {
    totalCount.value = pageTotal.value
  }
}
const {
  dataList,
  pageListRefresh,
  pageTotal,
  pageParams,
  dataNoMore,
  refreshTag,
  pageEmpty,
  pageListLoadMore,
  pageListSetParams
} = HooksPageList('/ngProduct/manage/qc/list', { run: false, cb: listCallback })

const scrollTop = ref('')
const { touchStart, touchEnd } = HooksPageSwiper((direction) => {
  if (direction === 'left') {
    tabActive.value !== 1 && handleTabChange(1)
  } else if (direction === 'right') {
    tabActive.value !== 2 && handleTabChange(2)
  }
})

onShow(() => {
  // 清理所有的弹窗
  $store.commit('popup/closeAllPopup')
})

// 关注模块
const followList = computed(() => $store.state.audit.followList)
const followChecked = computed(() => $store.state.audit.followChecked || false)
function getFollowList() {
  _get({ url: '/follow/list', data: { followType: 'EMPLOYEE' } }).then((res: IResponseType<[]>) => {
    const arr =
      res.data?.map((v: { itemId; itemName }) => {
        return {
          id: v.itemId,
          nickName: v.itemName
        }
      }) ?? []
    $store.commit('audit/setFollowList', arr)
  })
}
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
  setParamsAndRefresh()
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
//批量质检
function handleAuditBatchCheck() {
  const ids = []
  dataList.value.forEach((v) => {
    if (v.checked) {
      ids.push(v.id)
    }
  })
  if (ids.length === 0) {
    uni.showToast({
      title: '请选择至少一条数据',
      duration: 1500,
      icon: 'none'
    })
    return
  }
  uni.showModal({
    title: '提示',
    content: '是否确认审核?',
    success: function (res) {
      if (res.confirm) {
        _get({ url: '/ngProduct/manage/batchQc', data: { ids: ids.toString() } }).then((res: IResponseType<[]>) => {
          pageListRefresh()
          uni.showToast({
            title: '审核成功',
            duration: 1500,
            icon: 'none'
          })
        })
      }
    }
  })
}
onMounted(() => {
  uni.$on('qualityTestingListRefresh', (options) => {
    pageListRefresh()
  })
  uni.$on('auditFilter', (options) => {
    $store.commit('audit/setFilterData', options || {})
    pageListRefresh()
  })
  $store.commit('audit/setFilterData', { submitNickName: '' })
  getFollowList()
  clearFilter()
  setParamsAndRefresh()
  collectPV('质检列表', '质检列表', '质检列表', 1)
})
onUnmounted(() => {
  uni.$off('qualityTestingListRefresh')
  uni.$off('auditFilter')
})
</script>
<style lang="scss">
.tab-line {
  position: absolute;
  left: 0;
  bottom: px2vw(0);
}
.btn-record {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  bottom: px2vw(200);
  z-index: 2;
}
</style>
