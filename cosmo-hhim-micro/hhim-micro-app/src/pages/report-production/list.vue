<template>
  <view class="h-100 flex flex-col overflow-hidden bg-f3f3f5 relative">
    <view class="flex flex-col relative mb-16">
      <!--固定4天日期-->
      <view class="area-date">
        <template v-if="uuc">
          <view class="flex area-date__scroll overflow-auto">
            <view
              v-for="(item, index) in dates"
              :key="index"
              :id="item.id"
              class="area-date__item"
              :class="topData.day === item.day ? 'active' : ''"
              @tap="topDateSelect(item, index)"
            >
              <view class="day single">{{ item.day }}</view>
              <view class="week">{{ item.week }}</view>
              <view class="content-value">
                <view v-if="item.value" class="mark" />
                <view v-else class="mark mark--empty" />
                <view v-if="item.value" class="value">
                  <h-text-run :numbers="item.value" :size="3" />
                </view>
              </view>
            </view>
            <view id="scroll-end"></view>
          </view>
        </template>
        <template v-else>
          <scroll-view scroll-x enable-flex class="flex area-date__scroll" :scroll-into-view="scrollLeft">
            <view
              v-for="(item, index) in dates"
              :key="index"
              :id="item.id"
              class="area-date__item"
              :class="topData.day === item.day ? 'active' : ''"
              @tap="topDateSelect(item, index)"
            >
              <view class="day single">{{ item.day }}</view>
              <view class="week">{{ item.week }}</view>
              <view class="content-value">
                <view v-if="item.value" class="mark" />
                <view v-else class="mark mark--empty" />
                <view v-if="item.value" class="value">
                  <h-text-run :numbers="item.value" :size="3" />
                </view>
              </view>
            </view>
            <view id="scroll-end"></view>
          </scroll-view>
        </template>
        <view class="area-date__calendar overflow-hidden bg-fff" @tap="handleCalendar">
          <image src="/static/images/icon_date.svg" class="icon-48" />
        </view>
      </view>
      <!--良/不良数量汇总-->
      <view class="bg-fff py-8 pl-48 pr-24 flex align-center">
        <area-date-info :dataItem="topData" />
        <view class="flex-1" />
        <h-page-video :videoArr="[1, 2, 3, 4, 5]" page-name="记工列表" />
      </view>
      <view class="e-mask" v-if="isExperience && experienceLoading" />
    </view>
    <list-notice-area />
    <system-notice-area v-model="systemNotice" />
    <!--列表区域-->
    <scroll-view
      scroll-y
      @refresherrefresh="onRefresh"
      :refresher-triggered="triggered"
      refresher-enabled
      class="area-scroll"
      enable-flex
      @scrolltolower="onLoadMore"
    >
      <template v-if="dataList.length">
        <list-item
          class="area-scroll__list-item"
          v-for="(item, index) in dataList"
          :key="index"
          :dataItem="item"
          :checkAble="checkAble"
          @itemCheck="handleItemCheck(item)"
          @openDeleteBatch="handleOpenDeleteBatch"
          @deleteItem="handleItemDelete"
        />
      </template>
      <template v-else>
        <h-empty className="pt-240" tipsWord="暂无记工，记工后的数据在此显示" />
      </template>
      <uni-load-more v-if="dataList.length" :status="dataNoMore" />
    </scroll-view>
    <!--批量删除-->
    <view class="mx-32 mb-40 box" v-if="checkAble">
      <view class="flex align-center">
        <view class="font-24 flex-1">
          <text class="color-333">已选</text>
          <text class="color-ff0000 ml-4 bold">{{ getCheckedCount }}</text>
        </view>
        <view class="font-24 ml-16 px-16 py-10 rounded-24 color-5a6f82 bg-f3f3f5">全选</view>
        <h-checkbox :checked="checkAll" @checkedChange="handleCheckAll" class="mr-16" />
      </view>
      <view class="flex justify-center mt-16">
        <h-button width="296" height="72" text="取消" type="bg-fff color-333" @tap.stop="handleCheckCancel" />
        <h-button
          width="296"
          height="72"
          text="删除"
          type=" bg-ff0000 color-fff"
          active="red"
          class="ml-32"
          @tap.stop="handleCheckDeleteAll"
        />
      </view>
    </view>
    <template v-else>
      <view class="btn-record" :class="{ experience: isExperience && experienceLoading }">
        <h-button-record text="记工" size="icon-160" @tap="handleRecord" v-if="checkBtnState === 'record'" />
        <h-button-record text="补录" size="icon-160" @tap="handleRecord" v-if="checkBtnState === 'supplement'" />
      </view>
    </template>
    <h-popup-dialog ref="refPopupDeleteDialog" title="提示" isMaskClick>
      <template #default>
        <view class="color-333 font-28 py-32 text-center">是否确认删除?</view>
      </template>
      <template #footer>
        <view class="flex align-center justify-center">
          <h-button width="120" height="64" text="取消" type="bg-f3f3f5 color-333" @tap.stop="handleDeleteCancel" />
          <h-button
            width="120"
            height="64"
            text="删除"
            type="bg-ff0000 color-fff"
            active="red"
            class="ml-32"
            @tap.stop="handleDeleteConfirm"
          />
        </view>
      </template>
    </h-popup-dialog>
    <h-status-footer v-if="isExperience" />
    <h-popup />
  </view>
</template>
<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import ListItem from './list-item.vue'
import HButtonRecord from '@/components/h-button-record.vue'
import AreaDateInfo from './components/area-date-info.vue'
import dayjs from 'dayjs'
import isBetween from 'dayjs/plugin/isBetween'
import { _delete, _get, _put } from '@/utils/common-request'
import { onShow } from '@dcloudio/uni-app'
import { collectPV, formatImage, setGio } from '@/utils/common'
import HEmpty from '@/components/h-empty.vue'
import { $store, uuc } from '@/utils/common'
import PopupERecord from '@/components/experience-popup/popup-e-record.vue'
import bigNumber from 'bignumber.js'
import HTextRun from '@/components/h-text-run.vue'
import ListNoticeArea from '@/pages/report-production/list-notice-area.vue'
import SystemNoticeArea from '@/components/system-notice-area.vue'
import HooksSystemNotice from '@/hooks/system-notice'
import HooksSettingConfig from '@/hooks/setting-config'
import HPopupDialog from '@/components/h-popup-dialog.vue'

const dates = ref<Partial<TDatesItem>[]>([{}, {}, {}, {}])
const topData = ref<Partial<TTopData>>({ full: '', pass: 0, ng: 0 })
const weekStr = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
const dateSelected = ref('')
const dataList = ref([])

const getExStep = computed(() => $store.state.experience.stepIndex)

// 根据日期获取具体记工数量
function getCountByDate(index = selectedIndex.value) {
  const str = []
  dates.value.forEach((v: TDatesItem) => {
    str.push(v.full)
  })
  _get({ url: '/submit/countByDay', data: { dates: str.join(',') } }).then((res: IResponseType<TDatesRes[]>) => {
    dates.value.forEach((v: TDatesItem) => {
      const item = res.data.find((o) => o.submitDay === v.full)
      if (item) {
        v.value = parseInt(item.totalCounts || '')
        v.pass = item.totalPassNum
        v.ng = item.totalNgNum
      } else {
        v.value = ''
        v.pass = 0
        v.ng = 0
      }
      return v
    })
  })
  topDateSelect(dates.value[index])
}

const selectedIndex = ref(15)

// 顶部日期选择
function topDateSelect(item, index = selectedIndex.value) {
  selectedIndex.value = index
  topData.value = item
  pages.pageNum = 1
  getList()
}

dayjs.extend(isBetween)
const checkBtnState = computed(() => {
  if (dayjs(topData.value.full).isBetween(dayjs().subtract(7, 'day'), dayjs(), 'day', '[)')) {
    // 补录
    return 'supplement'
  } else if (topData.value.full === dayjs().format('YYYY-MM-DD')) {
    // 正常
    return 'record'
  } else {
    return ''
  }
})

// 报工记录列表
function getList() {
  dataNoMore.value = 'loading'
  _get({
    url: '/submit/list',
    data: { submitDay: topData.value.full, submitStatus: checkAble.value ? '1' : '', ...pages }
  })
    .then((res: IResponseType<[]>) => {
      if (pages.pageNum === 1) {
        dataList.value = []
      }
      dataList.value.push(
        ...res.rows.map((v) => {
          v.checked = false
          return v
        })
      )
      dataTotal.value = res.total
      getUserReportTimes()
    })
    .finally(() => {
      dataNoMore.value = dataEnd.value ? 'noMore' : 'more'
      triggered.value = false
    })
}

// 下拉刷新
const triggered = ref(false)

function onRefresh() {
  triggered.value = true
  pages.pageNum = 1
  getList()
}

const scrollLeft = ref('i-0')

// 计算最近四天的日期
function format4Days(dateStr) {
  scrollLeft.value = ''
  for (let i = 0; i < 31; i++) {
    const date = dayjs(dateStr).add(i - 15, 'day')
    dates.value[i] = {
      day: date.format('MM-DD'),
      full: date.format('YYYY-MM-DD'),
      id: `i-${i}`,
      week: weekStr[date.day()],
      value: 0
    }
  }
  nextTick(() => {
    scrollLeft.value = 'i-12'
  })
}

const { systemNotice, getSystemTips } = HooksSystemNotice(10)

onShow(() => {
  $store.commit('popup/closeAllPopup')
  uni.setStorageSync('record_image_list', '')
  getSystemTips()
})

onMounted(() => {
  // 监听事件
  uni.$on('listRefresh', (date) => {
    pages.pageNum = 1
    if (date) {
      const index = dates.value.findIndex((v: TTopData) => v.full === date) || dates.value.length - 1
      getCountByDate(index)
    } else {
      initData()
      // getCountByDate()
    }
  })
  // 监听日历上的日期选择
  uni.$on('topDateChange', (date) => {
    pages.pageNum = 1
    initData(date)
  })
  // 体验的记工成功
  uni.$on('refreshExperience', () => {
    experienceLoading.value = false
    const item = $store.getters['experience/getCurrentStep']
    let times = '1'
    if (item.startDate) {
      const nowTime = new Date().getTime()
      times = new bigNumber((nowTime - item.startDate) / 1000 / 60).toFixed(2)
    }
    $store.commit('popup/setPopupData', { title: '恭喜，首次记工成功！', time: times })
    _put({
      url: '/experience/guide/updateStatus',
      data: { nodeCode: item.nodeCode, nodeStatus: 1, elapsedTime: times }
    })
  })
  initData()
  $store.commit('popup/closeAllPopup')
  getSystemTips()
  collectPV('记工列表', '记工列表', '记工列表', 1)
})
onUnmounted(() => {
  uni.$off('listRefresh')
  uni.$off('topDateChange')
  uni.$off('refreshExperience')
})

function initData(date = new Date()) {
  dateSelected.value = dayjs(date).format('YYYY-MM-DD')
  format4Days(date)
  getCountByDate(15)
}

// 获取登录人报工次数
const reportTimes = ref(0)

function getUserReportTimes() {
  _get({ url: '/submit/times' }).then((res: IResponseType<number>) => {
    reportTimes.value = res.data
  })
}

// 检查是否是体验
const isExperience = computed(() => {
  return $store.getters.isExperience
})
// 是否正在体验中
const experienceLoading = ref(true)

// 日历按钮点击
function handleCalendar() {
  uni.navigateTo({ url: '/pages/report-production/calendar-select' })
}

const { checkBatchSubmit } = HooksSettingConfig()

// 记工按钮点击
function handleRecord() {
  setGio('jr_jg_start')
  // todo-- 切换批量报工和单个报工
  if (checkBatchSubmit.value) {
    uni.navigateTo({ url: `/pages-report-work/batch-create?date=${topData.value.full}` })
  } else {
    uni.navigateTo({ url: `/pages-report-work/index?date=${topData.value.full}` })
  }
}

// 全选操作
const checkAble = ref(false)
const checkAll = ref(false)

function handleOpenDeleteBatch() {
  checkAble.value = true
  pages.pageNum = 1
  getList()
}

const getCheckedCount = computed(() => {
  return dataList.value.filter((v) => v.checked).length
})

function handleCheckCancel() {
  checkAble.value = false
  checkAll.value = false
  getList()
}

function handleCheckAll() {
  checkAll.value = !checkAll.value
  dataList.value.forEach((v) => {
    v.checked = checkAll.value
  })
}
const deleteIds = ref([])
const refPopupDeleteDialog = ref(null)
function handleCheckDeleteAll() {
  const ids = []
  dataList.value.forEach((v) => {
    v.checked && ids.push(v.id)
  })
  if (!ids.length) {
    uni.showToast({ title: '请选择要删除的记录', icon: 'none' })
    return
  }
  deleteIds.value = ids
  refPopupDeleteDialog.value.open()
}

function handleItemDelete(id) {
  deleteIds.value = [id]
  refPopupDeleteDialog.value.open()
}

function handleDeleteConfirm() {
  _delete({ url: `/submit/${deleteIds.value}` }).then((res) => {
    handleCheckCancel()
    getCountByDate()
    getList()
    handleDeleteCancel()
  })
}

function handleDeleteCancel() {
  refPopupDeleteDialog.value.close()
}

function handleItemCheck(item) {
  const obj = dataList.value.find((v) => v.id === item.id)
  obj && (obj.checked = !obj.checked)
  checkAll.value = dataList.value.every((v) => v.checked)
}

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
</script>
<style lang="scss" scoped>
.area-date {
  position: relative;
  margin-top: px2vw(16);
  height: px2vw(140);
  background: #ffffff;
  border-radius: px2vw(16) px2vw(16) 0 0;
  border-bottom: 1px solid #f5f5f5;

  &__item {
    &:nth-last-of-type(.area-date__item) {
      margin-right: px2vw(32);
    }

    display: flex;
    flex-direction: column;
    align-items: center;
    padding: px2vw(24) 0;
    width: px2vw(150);

    &.active {
      background-color: #dbeaff;

      .day {
        color: #0066ff;
      }
    }

    .day {
      color: #333;
      font-size: px2vw(28);
      line-height: px2vw(28);
      font-weight: bold;
    }

    .week {
      margin-top: px2vw(8);
      color: #999;
      font-size: px2vw(20);
      line-height: px2vw(20);
    }

    .content-value {
      width: px2vw(120);
      margin-top: px2vw(8);
      padding-left: px2vw(16);
      padding-right: px2vw(16);
      height: px2vw(28);
      display: flex;
      align-items: center;
      justify-content: center;

      .mark {
        width: px2vw(12);
        height: px2vw(12);
        background: #19aa8d;
        border-radius: 50%;

        &--empty {
          background: #ffffff;
          border: px2vw(1) solid #b6c0c9;
        }
      }

      .value {
        margin-left: 8px;
        color: #333;
        font-size: px2vw(28);
        line-height: px2vw(28);
      }
    }
  }

  &__scroll {
    padding-right: px2vw(112);
    box-sizing: border-box;
    height: px2vw(140);

    /* #ifdef H5 */
    /* H5：uni-app H5 端 scroll-view 不支持 enable-flex 属性，内容容器(.uni-scroll-view-content)
       默认 block 布局导致 31 个日期项纵向堆叠、只显示第一项（15 天前）；
       这里手动改为 flex 横排，与小程序 enable-flex 行为一致 */
    :deep(.uni-scroll-view-content) {
      display: flex;
      flex-direction: row;
      align-items: stretch;
    }
    /* #endif */
  }

  &__calendar {
    position: absolute;
    z-index: 2;
    right: 0;
    top: 0;
    width: px2vw(112);
    height: px2vw(140);
    display: flex;
    align-items: center;
    justify-content: center;
    border-left: 1px solid #f5f5f5;

    &:active {
      background-color: #f3f3f5;
    }
  }
}

.area-scroll {
  position: relative;
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding-bottom: px2vw(16);

  &__list-item {
    margin-bottom: px2vw(8);
    padding: 0 px2vw(16);
  }
}

.btn-record {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  bottom: px2vw(100);
  z-index: 2;
  &.experience {
    z-index: 101;
    transform: translateX(-50%);
    animation: scaleAnimation 3s infinite;
  }
}

//体验特效
@keyframes scaleAnimation {
  0% {
    transform: translateX(-50%) scale(1.1);
  }
  50% {
    transform: translateX(-50%) scale(0.9);
  }
  100% {
    transform: translateX(-50%) scale(1.1);
  }
}

.e-mask {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  z-index: 100;
  background: rgba(0, 0, 0, 0.1);
}

.e-img {
  position: absolute;
  z-index: 101;
  left: 50%;
  bottom: px2vw(272);
  transform: translate(-50%);
  width: px2vw(346);
  height: px2vw(200);
}
</style>
