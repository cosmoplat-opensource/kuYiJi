<template>
  <view class="h-100 flex flex-col overflow-hidden bg-f3f3f5 relative">
    <template v-if="!Role_Staff">
      <part-top @getIndexData="getIndexData" ref="refPartTop" :indexData="indexData" />
      <scroll-view
        @refresherrefresh="onRefresh"
        :refresher-triggered="triggered"
        refresher-enabled
        scroll-y
        class="flex-1 flex flex-col overflow-hidden mt-16 px-16 box"
        enable-flex
      >
        <!-- 数量统计 -->
        <part-statistics :indexData="indexData" :startDate="startDate" :endDate="endDate" :dateType="dateType" />
        <!-- 今日生产日报 -->
        <part-subscribe class="mt-16" v-if="Role_Review || Role_Quality || Role_Admin" analysis />
        <!-- tab -->
        <view class="bg-fff rounded-16 mt-16">
          <h-tabs
            ref="hTabs"
            :tabData="tabs"
            :activeIndex="activeIndex"
            :config="defaultConfig"
            @tabClick="tabClick"
            class=""
          />
        </view>
        <!-- 我的应用 -->
        <part-apps v-if="(Role_Review || Role_Quality || Role_Admin || Role_Experience) && activeIndex === 0" />
        <!-- 分析模块 -->
        <part-analysis class="mt-12" v-else-if="activeIndex === 1" />
        <!-- 基础数据 -->
        <part-base class="mt-16" ref="refPartBase" v-else />
        <!-- 体验显示的图标 -->
        <!-- <view v-if="Role_Experience" class="flex align-center justify-between mt-16 font-28">
          <view class="flex align-center p-16 bg-fff rounded-16 flex-1 box" @tap="toInvite">
            <image :src="formatImage('icon_work_5')" class="icon-96" />
            <view class="ml-8 color-333 font-28 bold">邀请新伙伴</view>
          </view>
          <view class="flex align-center p-16 bg-fff rounded-16 flex-1 box ml-16" @tap="toBuy">
            <image :src="formatImage('icon_work_6')" class="icon-96" />
            <view class="ml-8 color-333 font-28 bold">去购买</view>
          </view>
        </view> -->
        <part-help />
        <view class="pt-48 pb-32 color-b6c0c9 font-24 flex justify-center align-center">已到底部</view>
      </scroll-view>
      <template v-if="!getOFFICIAL && !Role_Experience">
        <!-- H5 初始位置与邀请浮标（底部往上 300px 处）错开：y=160 → 按钮在底部往上 176px 处，
             避免两个右上角悬浮按钮初始重叠（两者均可拖动，拖动后不受影响） -->
        <h-button-record-move @tap="handleLoadMock" size="icon-112" :y="mockBtnY" :x="mockBtnX" :disabled="false">
          <image v-if="checkMock" src="/static/images/icon_real.svg" class="icon-48" />
          <image v-else src="/static/images/icon_demon.svg" class="icon-48" />
        </h-button-record-move>
      </template>
      <!-- 问一问悬浮入口（与演示切换按钮同款可拖动圆形按钮；x=0 靠右、y=520 中下部，与 mock 按钮(x=屏宽靠左、y=屏高/2+90)错开，避免初始重叠）
           aiEnabled：本体没有任何 ACTIVE+implemented 指标时不显示入口，避免点了进去什么都答不了 -->
      <template v-if="(Role_Admin || Role_Review) && aiEnabled">
        <h-button-record-move @tap="handleAsk" size="icon-112" :x="0" :y="520" :disabled="false">
          <text class="ask-text">AI</text>
        </h-button-record-move>
        <!-- 业务本体图入口：图标（三节点网络，CSS 绘制，不用字体/SVG/emoji）+ 极小字说明 -->
        <h-button-record-move @tap="handleOntology" size="icon-112" :x="0" :y="640" :disabled="false">
          <view class="onto-entry">
            <view class="onto-icon">
              <view class="oi-line oi-l1" />
              <view class="oi-line oi-l2" />
              <view class="oi-dot oi-d1" />
              <view class="oi-dot oi-d2" />
              <view class="oi-dot oi-d3" />
            </view>
            <text class="onto-cap">本体</text>
          </view>
        </h-button-record-move>
      </template>
    </template>
    <part-staff v-if="Role_Staff" />
    <!-- 小程序/App：movable-view（H5 端 movable-view 边界截断，改用下方 fixed + 指针拖动） -->
    <!-- #ifndef H5 -->
    <movable-area ref="invite" class="area">
      <movable-view
        class="float-invite"
        :x="btn_x"
        :y="btn_y"
        direction="vertical"
        :inertia="true"
        :animation="false"
        @tap="handleExperienceGuide"
      >
        <image :src="formatImage('img_pages_novice_guidance', 'svg')" class="img" v-if="Role_Experience" />
        <image :src="formatImage('img_buoy', 'svg')" class="img" v-else />
      </movable-view>
    </movable-area>
    <!-- #endif -->
    <!-- H5：fixed 定位 + touch/mouse 拖动（保持垂直移动语义），始终 clamp 在视口内不截断 -->
    <!-- #ifdef H5 -->
    <view
      class="area-h5"
      :style="{ top: buoyTop + 'px', left: buoyLeft + 'px' }"
      @touchstart.stop="onBuoyStart"
      @touchmove.stop.prevent="onBuoyMove"
      @touchend.stop="onBuoyEnd"
      @mousedown.stop="onBuoyStart"
      @mousemove.stop="onBuoyMove"
      @mouseup.stop="onBuoyEnd"
      @tap="onBuoyTap"
    >
      <image :src="formatImage('img_pages_novice_guidance', 'svg')" class="img" v-if="Role_Experience" />
      <image :src="formatImage('img_buoy', 'svg')" class="img" v-else />
    </view>
    <!-- #endif -->
  </view>
</template>
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { BigNumber } from 'bignumber.js'
import { _get, _post } from '@/utils/common-request'
import { onShow } from '@dcloudio/uni-app'
import {
  $state,
  $store,
  collectClick,
  collectPV,
  formatImage,
  openBuyMini,
  Role_Admin,
  Role_Experience,
  Role_Quality,
  Role_Review,
  Role_Staff
} from '@/utils/common'
import HTabs from '@/components/h-tabs.vue'
import PopupERecord from '@/components/experience-popup/popup-e-record.vue'
import HButtonRecordMove from '@/components/h-button-record-move.vue'
import PartSubscribe from '@/pages/analysis/part-subscribe.vue'
import PartTop from '@/pages/analysis/part-top.vue'
import PartApps from '@/pages/analysis/part-apps.vue'
import PartBase from '@/pages/analysis/part-base.vue'
import PartAnalysis from '@/pages/analysis/part-analysis.vue'
import PartStatistics from '@/pages/analysis/part-statistics.vue'
import PartStaff from '@/pages/analysis/part-staff.vue'
import PartHelp from '@/pages/analysis/part-help.vue'
import HooksSystemNotice from '@/hooks/system-notice'
// 本体能力投影（由 ai-ontology 生成）：为空说明本体没有 ACTIVE+implemented 指标
import { CAPABILITIES } from '@/utils/ai-capability'
const userInfo = computed(() => {
  return $state.user.userInfo
})

/** 问一问入口开关：本体无可用指标时不显示入口（避免点进去什么都答不了） */
const aiEnabled = computed(() => CAPABILITIES.length > 0)

const getExStep = computed(() => $state.experience.stepIndex)
// 是否开启正式使用
const getOFFICIAL = computed(() => $state.experience.guideCode === 'OFFICIAL')
const { systemNotice, getSystemTips } = HooksSystemNotice(40)
onShow(() => {
  // 清理所有的弹窗
  $store.commit('popup/closeAllPopup')
  getSystemTips()
})
//邀请位置
const btn_x = ref(0)
const btn_y = ref(0)
onMounted(() => {
  uni.getSystemInfo({
    success: (result) => {
      btn_y.value = result.screenHeight - 300
    }
  })
  getSystemTips()
  initData()
  collectPV('工作台', '工作台', '工作台', 1)
})
// H5：新手引导浮标 fixed 定位 + 指针拖动（保持垂直移动语义），clamp 在视口内不截断
// #ifdef H5
const buoyTop = ref(0)
const buoyLeft = ref(0)
const buoySize = () => {
  const w = window.innerWidth || 375
  return { w: (w * 96) / 720, h: (w * 192) / 720 }
}
let buoyStartY = 0
let buoyDragging = false
let buoyLock = false
let buoySuppressTap = false

onMounted(() => {
  uni.getSystemInfo({
    success: (result) => {
      buoyTop.value = result.screenHeight - 300
      buoyLeft.value = (result.windowWidth || window.innerWidth) - buoySize().w
    }
  })
})

function onBuoyStart(e: any) {
  buoyLock = true
  buoyDragging = false
  const t = e.touches ? e.touches[0] : e
  buoyStartY = t.clientY
}
function onBuoyMove(e: any) {
  if (!buoyLock) return
  const t = e.touches ? e.touches[0] : e
  const dy = t.clientY - buoyStartY
  if (Math.abs(dy) > 3) buoyDragging = true
  const maxTop = (window.innerHeight || document.documentElement.clientHeight) - buoySize().h
  buoyTop.value = Math.min(Math.max(buoyTop.value + dy, 0), maxTop)
  buoyStartY = t.clientY
}
function onBuoyEnd() {
  buoyLock = false
  if (buoyDragging) {
    buoySuppressTap = true
    setTimeout(() => {
      buoySuppressTap = false
    }, 300)
  }
}
function onBuoyTap(e: any) {
  if (buoySuppressTap) {
    buoySuppressTap = false
    if (e && e.stopPropagation) e.stopPropagation()
    return
  }
  handleExperienceGuide()
}
// #endif
const popupRecord = ref()
function initData() {
  if (isExperience.value) {
    $store.commit('popup/setPopupData', { title: '恭喜，你已完成了体验旅程！', time: 1 })
    setTimeout(() => {
      popupRecord.value?.open()
    }, 300)
  }
}

// 检查是否是体验
const isExperience = computed(() => {
  return $store.getters.isExperience
})

//获取分析数据
const indexData = ref({})
const dateType = ref(0)
const startDate = ref('')
const endDate = ref('')
function getIndexData(start?, end?, type?) {
  start && (startDate.value = start)
  end && (endDate.value = end)
  type && (dateType.value = type)
  let mockParams = {
    url: '/analysis/index',
    data: {
      startDate: startDate.value,
      endDate: endDate.value,
      timeType: dateType.value
    }
  }
  if (checkMock.value) {
    Object.assign(mockParams, {
      url: '/analysis/loadDisplayData',
      data: { apiUrl: mockParams.url }
    })
  }
  _get(mockParams)
    .then((res: IResponseType<{}>) => {
      if (res?.data) {
        indexData.value = res.data
        indexData.value.momCheckProgressShow = new BigNumber(Math.abs(indexData.value.momCheckProgress))
          .multipliedBy(100)
          .toNumber()
        indexData.value.momPassRateShow = new BigNumber(Math.abs(indexData.value.momPassRate))
          .multipliedBy(100)
          .toNumber()
        indexData.value.checkProgress = new BigNumber(indexData.value.checkProgress).multipliedBy(100).toNumber()
      }
    })
    .finally(() => {
      triggered.value = false
    })
}
function handleExperienceGuide() {
  const code = $store.state.user.userInfo.roleCode
  if (code === '40') {
    uni.navigateTo({ url: '/pages-novice-guidance/index' })
    collectClick('新手引导', '新手引导页', '工作台')
  } else {
    uni.navigateTo({ url: `/pages-invite/index?roleCode=${code === '10' ? '20' : code}` })
  }
}

function toInvite() {
  // 体验用户点击邀请伙伴弹框内容文案修改
  uni.showModal({
    title: '提示',
    content: '购买后才能邀请企业员工，快去购买体验完整功能吧！',
    showCancel: false,
    confirmText: '去购买',
    success: function (res) {
      if (res.confirm) {
        toBuy()
      }
    }
  })
}
function toBuy() {
  openBuyMini()
}
// 下拉刷新
const triggered = ref(false)
const refPartTop = ref(null)
const refPartBase = ref(null)
function onRefresh() {
  triggered.value = true
  refPartTop.value?.selectDataType(dateType.value)
  refPartBase.value?.getData()
}

// 加载测试数据
const checkMock = computed(() => $state.mock.mockOpen)
function handleLoadMock() {
  if (!checkMock.value) {
    uni.showToast({ title: '已切换为演示数据', icon: 'none', duration: 3000 })
  } else {
    uni.showToast({ title: '已切换为实际数据', icon: 'none', duration: 2000 })
  }
  $store.commit('mock/setMockOpen', !checkMock.value)
  getIndexData()
}

// 问一问入口
function handleAsk() {
  uni.navigateTo({ url: '/pages-analysis/ask/index' })
}
// 业务本体图入口（与 AI 按钮同款悬浮按钮，y 错开避免初始重叠）
// 页面放主包（pages/ontology）：分包页面在小程序端曾出现注册不生效导致空白
function handleOntology() {
  uni.navigateTo({ url: '/pages/ontology/index' })
}

// 演示切换按钮：小程序端初始 x 传屏幕宽（组件内 btn_x = screenWidth - x = 0，即放最左侧），
// y 用屏幕高（与组件内基准一致，避免 windowHeight/screenHeight 混算导致按钮顶到顶部）
const mockBtnX = uni.getWindowInfo().screenWidth || 375
const mockBtnY = Math.round((uni.getWindowInfo().screenHeight || 667) / 2 + 90)

// tab切换
const tabs = ref([
  {
    state: 0,
    name: '生产管理'
  },
  {
    state: 1,
    name: '分析'
  },
  {
    state: 2,
    name: '基础数据'
  }
])
const activeIndex = ref(0)
const defaultConfig = ref({
  fontSize: 28,
  color: '#5A6F82',
  activeBold: 'bold',
  activeColor: '#333333',
  underLineHeight: 8,
  underLineColor: '#0066FF',
  itemClass: 'px-24'
})
function tabClick(event) {
  activeIndex.value = event
}
</script>
<style lang="scss" scoped>
/* 问一问按钮文字 */
.ask-text {
  color: #fff;
  font-size: px2vw(52);
  font-weight: 700;
  line-height: 1;
  letter-spacing: 2rpx;
}

/* 业务本体图入口：三个节点 + 两条连线（纯 CSS 绘制，不依赖字体/SVG/emoji，H5 与小程序一致） */
.onto-entry {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.onto-icon {
  position: relative;
  width: px2vw(58);
  height: px2vw(46);
}
.oi-dot {
  position: absolute;
  width: px2vw(16);
  height: px2vw(16);
  border-radius: 50%;
  background: #fff;
}
.oi-d1 { left: px2vw(21); top: 0; }
.oi-d2 { left: 0; top: px2vw(28); }
.oi-d3 { left: px2vw(42); top: px2vw(28); }
.oi-line {
  position: absolute;
  height: 2rpx;
  width: px2vw(36);
  background: rgba(255, 255, 255, 0.92);
  transform-origin: 0 50%;
  left: px2vw(29);
  top: px2vw(8);
}
.oi-l1 { transform: rotate(112deg); }
.oi-l2 { transform: rotate(68deg); }
.onto-cap {
  margin-top: 2rpx;
  font-size: px2vw(20);
  line-height: 1.1;
  color: #fff;
}
.area {
  width: px2vw(96);
  height: calc(100vh - 176rpx - 32rpx);
  overflow: hidden;
  pointer-events: none;
  position: fixed;
  top: px2vw(176);
  right: px2vw(0);
  z-index: 10;
  .float-invite {
    width: px2vw(96);
    height: px2vw(192);
    pointer-events: auto;
    .img {
      width: px2vw(96);
      height: px2vw(192);
    }
  }
}
/* #ifdef H5 */
/* H5 专用：fixed 定位 + 指针拖动，touch-action: none 防止页面滚动抢走手势 */
.area-h5 {
  position: fixed;
  z-index: 10;
  cursor: grab;
  touch-action: none;
  .img {
    width: px2vw(96);
    height: px2vw(192);
  }
}
/* #endif */
</style>
