<template>
  <view class="h-full bg-f3f3f5 box flex flex-col overflow-hidden">
    <uni-nav-bar />
    <h-status-header title="记工风险" />
    <!--检测列表-->
    <scroll-view
      @refresherrefresh="onRefresh"
      :refresher-triggered="triggered"
      refresher-enabled
      enable-flex
      scroll-y
      class="flex-1 flex flex-col overflow-hidden"
    >
      <!--需要关注的记工-->
      <view class="mt-16 bg-fff rounded-16 mx-16 box overflow-hidden">
        <view class="flex">
          <view class="bg-EBF0F5 font-24 py-4 px-8 color-5a6f82">需要关注的记工</view>
          <view class="bg-f7f8e7 color-e7a11a py-4 px-16 font-24 flex-1">一条待审核记工可能存在多种风险</view>
        </view>
        <!--顶部区域-->
        <view class="py-32 px-48 flex align-center">
          <circle-progress :value="circleSmallPer" :widths="widthsSmall" :breadth="breadthSmall">
            <view class="color-333 font-32 bold">{{ noWaitCheck ? '—' : circleSmallPer + '%' }}</view>
          </circle-progress>
          <!--顶部区域-->
          <view class="ml-32">
            <!--数据健康度（口径：待审记工中无异常条数的占比，不含基础数据预警）-->
            <view class="flex align-center font-28">
              <image src="/static/images/icon_health_green.svg" class="icon-32" />
              <text class="color-5a6f82 ml-8">数据健康度（待审记工）</text>
              <text class="color-333 ml-8 bold">{{ noWaitCheck ? '—' : circleSmallPer + '%' }}</text>
            </view>
            <view v-if="noWaitCheck" class="font-22 color-999 mt-8">当前无待审记工，健康度无需评估</view>
            <!--产品总数-->
            <view class="flex align-center mt-16">
              <view class="mark-fe9f00">异常</view>
              <view class="ml-16 font-28">
                <text class="color-333 bold">{{ healthItem.abnormalSubmitRecords }}</text>
                <text class="ml-8 color-333 bold">条</text>
              </view>
            </view>
            <!--垃圾数据-->
            <view class="flex align-center mt-16">
              <view class="mark-24a8ff">待审</view>
              <view class="ml-16 font-28">
                <text class="color-333 bold">{{ healthItem.totalWaitCheckSubmitRecords }}</text>
                <text class="ml-8 color-333 bold">条</text>
              </view>
            </view>
          </view>
        </view>
        <!--顶部区域分割线-->
        <view
          class="top-deliver"
          v-if="
            hasMoreLastProcess ||
            healthItem.negativeStockRecords ||
            healthItem.lowPassRateRecords ||
            healthItem.overProductiveCapacityRecords
          "
        />
        <!--多尾序-->
        <view class="flex-center pt-32 px-32" v-if="hasMoreLastProcess" @tap="handleProcessList(2)">
          <view class="flex-1 flex align-center b-b-1 border-f5f5f5 pb-32">
            <view class="flex-1">
              <text class="font-28 color-333 bold">多尾序</text>
            </view>
            <view class="flex-center px-16 h-32 bg-EBF0F5 rounded-16 font-24 color-5a6f82">
              <text>共</text>
              <text class="color-ff0000 mx-8 bold">{{ hasMoreLastProcess }}</text>
              <text>款尾序</text>
            </view>
            <image src="/static/images/icon_detail.svg" class="icon-32 ml-32" />
          </view>
        </view>
        <!--超报风险-->
        <view class="flex-center pt-32 px-32" v-if="healthItem.negativeStockRecords">
          <view class="flex-1 flex align-center b-b-1 border-f5f5f5 pb-32">
            <view class="flex-1">
              <text class="font-28 color-333 bold">超报风险</text>
            </view>
            <view class="flex-center px-16 h-32 bg-EBF0F5 rounded-16 font-24 color-5a6f82">
              <text>共</text>
              <text class="color-ff0000 mx-8 bold">{{ healthItem.negativeStockRecords }}</text>
              <text>条记工</text>
            </view>
            <h-button height="48" width="96" text="处理" class="ml-32" font="font-24" @tap="handleJumpAuditList(1)" />
          </view>
        </view>
        <!--良品率偏低-->
        <view v-if="healthItem.lowPassRateRecords" class="flex-center pt-32 px-32">
          <view class="flex-1 flex align-center b-b-1 border-f5f5f5 pb-32">
            <view class="flex-1">
              <text class="font-28 color-333 bold">良品率偏低</text>
            </view>
            <view class="flex-center px-16 h-32 bg-EBF0F5 rounded-16 font-24 color-5a6f82">
              <text>共</text>
              <text class="color-ff0000 mx-8 bold">{{ healthItem.lowPassRateRecords }}</text>
              <text>条记工</text>
            </view>
            <h-button height="48" width="96" text="处理" class="ml-32" font="font-24" @tap="handleJumpAuditList(2)" />
          </view>
        </view>
        <!--记工数超产能-->
        <view v-if="healthItem.overProductiveCapacityRecords" class="flex-center pt-32 px-32">
          <view class="flex-1 flex align-center b-b-1 border-f5f5f5 pb-32">
            <view class="flex-1">
              <text class="font-28 color-333 bold single">记工数超产能</text>
            </view>
            <view class="flex-center px-16 h-32 bg-EBF0F5 rounded-16 font-24 color-5a6f82">
              <text>共</text>
              <text class="color-ff0000 mx-8 bold">{{ healthItem.overProductiveCapacityRecords }}</text>
              <text>条记工</text>
            </view>
            <h-button height="48" width="96" text="处理" class="ml-32" font="font-24" @tap="handleJumpAuditList(3)" />
          </view>
        </view>
      </view>
      <!--预警处理-->
      <view
        class="mt-16 bg-fff rounded-16 mx-16 box"
        v-if="hasNoFirstProcess + hasNoLastProcess !== 0 || (userInfo.roleCode !== '40' && userList.length > 0)"
      >
        <view class="flex">
          <view class="bg-EBF0F5 font-24 py-4 px-8 color-5a6f82 rounded-16-top-left">预警处理</view>
        </view>
        <!--无首序/尾序-->
        <view class="flex-center pt-32 px-32" @tap="handleProcessList(1)" v-if="hasNoFirstProcess + hasNoLastProcess">
          <view class="flex-1 flex align-center b-b-1 border-f5f5f5 pb-32">
            <view class="flex-1 font-28 color-333 bold">无首序/尾序</view>
            <view class="flex-center px-16 h-32 bg-EBF0F5 rounded-16 font-24 color-5a6f82">
              <text>共</text>
              <text class="color-ff0000 mx-8 bold">{{ hasNoFirstProcess + hasNoLastProcess }}</text>
              <text>款产品</text>
            </view>
            <image src="/static/images/icon_detail.svg" class="icon-32 ml-32" />
          </view>
        </view>
        <view
          class="flex-center pt-32 px-32 overflow-hidden"
          id="operation-show"
          @tap="handleListShow"
          v-if="userInfo.roleCode !== '40' && userList.length > 0"
        >
          <view class="flex-1 flex pb-32 align-center top-deliver" :class="{ hide: !listShow }">
            <view class="flex-1">
              <text class="font-28 color-333 bold">重点员工监控</text>
            </view>
            <view class="flex-center px-16 h-32 bg-EBF0F5 rounded-16 font-24 color-5a6f82">
              <text>共</text>
              <text class="color-ff0000 mx-8 bold">{{ userList.length }}</text>
              <text>名员工</text>
            </view>
            <image src="/static/images/icon_unfold.svg" class="icon-32 ml-32" :class="{ 'icon-rotate-y': listShow }" />
          </view>
        </view>
        <view v-if="listShow" class="box bg-fff relative">
          <view class="flex-center pb-8 px-32" v-for="(item, index) in userList" :key="index">
            <view class="flex-1 flex-center py-24" :class="index !== 0 ? 'b-t-1 border-f5f5f5' : ''">
              <h-text-display class="font-28 color-333 bold" :text="item.submitNickUser" :width="160" />
              <view class="font-28 color-999 bold flex-center">
                (<h-text-display :text="item.submitUser" :width="120" />)
              </view>
              <h-exception-label
                :text="item.typeLabel"
                :bg="item.typeBg"
                border="detail"
                className="py-4 px-4"
                class="ml-16"
              />
              <view class="flex-1" />
              <h-button height="48" width="96" text="处理" class="ml-32" font="font-24" @tap="handleUser(item)" />
            </view>
          </view>
          <view class="font-24 color-b6c0c9 py-48 box text-center">已显示全部</view>
        </view>
      </view>
    </scroll-view>
    <h-status-footer />
    <h-popup />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import CircleProgress from '../circle-progress.vue'
import { _get } from '@/utils/common-request'
import { $store, formatPx2Vw } from '@/utils/common'
import HExceptionLabel from '@/components/h-exception-label.vue'
import dayjs from 'dayjs'

import HTextDisplay from '@/components/h-text-display.vue'
import HPopup from '@/components/h-popup.vue'
import { onShow } from '@dcloudio/uni-app'
import { $state } from '@/utils/common'

const widthsSmall = ref(formatPx2Vw(160))
const breadthSmall = ref(formatPx2Vw(48))

const hasNoFirstProcess = ref(0)
const hasNoLastProcess = ref(0)
const hasMoreLastProcess = ref(0)
const userList = ref([])
const userInfo = computed(() => {
  return $state.user.userInfo
})

const healthItem = ref<Partial<THealthItem>>({
  dataHealth: 0,
  totalWaitCheckSubmitRecords: 0,
  abnormalSubmitRecords: 0,
  negativeStockRecords: 0,
  overProductiveCapacityRecords: 0,
  lowPassRateRecords: 0
})
function initHealthData() {
  _get({ url: '/warn/dataHealth' }).then((res: IResponseType<THealthItem>) => {
    healthItem.value = res.data
  })
}

const triggered = ref(false)
function onRefresh() {
  triggered.value = true
  initData()
}
function initData() {
  // 无首序
  _get({ url: '/warn/product/notHaveFirstProcess' }).then((res: IResponseType<[]>) => {
    hasNoFirstProcess.value = res.data?.length ?? 0
  })
  // 无尾序
  _get({ url: '/warn/product/notHaveLastProcess' }).then((res: IResponseType<[]>) => {
    hasNoLastProcess.value = res.data?.length ?? 0
  })
  // 多尾序
  _get({ url: '/warn/product/multiLastProcess' }).then((res: IResponseType<[]>) => {
    hasMoreLastProcess.value = res.data?.length ?? 0
  })
  initHealthData()
  if (userInfo.value.roleCode === '40') {
    return
  }
  const startDate = dayjs().add(-14, 'day').format('YYYY-MM-DD')
  const endDate = dayjs().format('YYYY-MM-DD')
  type TUserListItem = {
    submitNickUser: string
    submitUser: string
    notSubmitDayNums: string
    modifiedNums: string
    avgDayProductiveCapacity: string
  }
  Promise.all([
    _get({ url: '/warn/employee/findConsecutiveSevenDaysNoRecordEmployees' }),
    _get({ url: '/warn/employee/findModifiedRecordOverThreeTimesEmployees', data: { startDate, endDate } }),
    _get({ url: '/warn/employee/findOverDayCapacityEmployees' })
  ])
    .then((res: [IResponseType<[]>, IResponseType<[]>, IResponseType<[]>]) => {
      userList.value = []
      // 连续七天没有报工的员工 res[0]
      res[0].data.forEach((v: TUserListItem) => {
        userList.value.push({
          submitNickUser: v.submitNickUser,
          submitUser: v.submitUser,
          notSubmitDayNums: v.notSubmitDayNums,
          typeLabel: '近期未记工',
          typeBg: '#9BADBC'
        })
      })
      // 近期十次报工中有3次被修改过记录的员工 res[1]
      res[1].data.forEach((v: TUserListItem) => {
        userList.value.push({
          submitNickUser: v.submitNickUser,
          submitUser: v.submitUser,
          modifiedNums: v.modifiedNums,
          typeLabel: '频繁出错',
          typeBg: '#F26A60'
        })
      })
      // 报工数量超过人均日产能的员工 res[2]
      res[2].data.forEach((v: TUserListItem) => {
        userList.value.push({
          submitNickUser: v.submitNickUser,
          submitUser: v.submitUser,
          avgDayProductiveCapacity: v.avgDayProductiveCapacity,
          typeLabel: '记工数超产能',
          typeBg: '#F26A60'
        })
      })
    })
    .finally(() => {
      triggered.value = false
    })
}

onShow(() => {
  initData()
})

function handleJumpAuditList(type) {
  let ids = ''
  let governType = ''
  switch (type) {
    case 1:
      // 超报风险
      ids = healthItem.value.negativeStockRecordIds
      governType = '超报风险'
      break
    case 2:
      // 良品率偏低
      ids = healthItem.value.lowPassRateRecordIds
      governType = '良品率偏低'
      break
    case 3:
      // 记工数超产能
      ids = healthItem.value.overProductiveCapacityRecordIds
      governType = '记工数超产能'
      break
  }
  uni.setStorageSync('ku_follow_disable', '1')
  uni.setStorageSync('governJump', { page: 'audit', ids, governType, governCount: ids.split(',').length })
  $store.commit('tabBar/setTabActiveByName', '审产')
}

const listShow = ref(false)
function handleListShow() {
  listShow.value = !listShow.value
}

// 工序预警是否可见
const processShow = computed(() => {
  return hasNoFirstProcess.value || hasNoLastProcess.value || hasMoreLastProcess.value
})

// 数据健康度圆环
const circleSmallPer = computed(() => {
  return Math.min(Math.floor(healthItem.value.dataHealth * 100), 100)
})

// 无待审记工时健康度无意义（公式 = 待审总数中无异常的比例；分子分母都是 0）
// → 显示"—"并说明，避免"满分 100%"的错觉（下方预警可能是基础数据问题，与健康度不同口径）
const noWaitCheck = computed(() => !healthItem.value.totalWaitCheckSubmitRecords)

// 跳转工序预警
function handleProcessList(type) {
  uni.navigateTo({ url: `/pages-data-governance/process/index?type=${type}` })
}

function handleUser(item) {
  switch (item.typeLabel) {
    case '近期未记工':
      const params = `userName=${item.submitUser}&notSubmitDayNums=${item.notSubmitDayNums}`
      uni.navigateTo({ url: `/pages-my-center/manage-staff/index?${params}` })
      break
    case '频繁出错':
      uni.$emit('auditListRefresh', { submitNickUser: item.submitNickUser, modifiedNums: item.modifiedNums })
      uni.setStorageSync('ku_follow_disable', '1')
      $store.commit('tabBar/setTabActiveByName', '审产')
      break
    case '记工数超产能':
      uni.$emit('auditListRefresh', {
        submitNickUser: item.submitNickUser,
        avgDayProductiveCapacity: item.avgDayProductiveCapacity
      })
      uni.setStorageSync('ku_follow_disable', '1')
      $store.commit('tabBar/setTabActiveByName', '审产')
      break
  }
}
</script>

<style lang="scss" scoped>
.top-deliver {
  border-bottom: px2vw(2) dashed #e5e5e5;
  &.hide {
    border-bottom: px2vw(2) dashed #ffffff;
  }
}
.mark {
  width: px2vw(16);
  height: px2vw(24);
  background: #00bfa5;
  border-radius: 0 px2vw(4) px2vw(4) 0;
}
.index-mark {
  position: absolute;
  width: px2vw(38);
  height: px2vw(32);
  left: px2vw(-19);
  top: px2vw(32);
  text-align: center;
  line-height: px2vw(32);
  background: #ebf0f5;
  border-radius: 0 px2vw(4) px2vw(4) 0;
  color: #5a6f82;
  font-size: px2vw(20);
}
</style>
