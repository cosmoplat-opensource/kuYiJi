<template>
  <view class="h-full flex flex-col overflow-hidden bg-f3f3f5 relative">
    <template v-if="tabActive.text !== '工作台'">
      <uni-nav-bar />
      <!--人名头像-->
      <h-user-info />
    </template>
    <view class="flex-1 overflow-hidden">
      <report-list v-if="tabActive.text === '记工'" />
      <audit-list v-else-if="tabActive.text === '审产'" />
      <stock-list v-else-if="tabActive.text === '车间库存'" />
      <analysis v-else-if="tabActive.text === '工作台'" />
      <quality-testing-list v-else-if="tabActive.text === '质检'" />
      <view v-else class="flex justify-center py-32 font-28 color-333">数据加载中...</view>
    </view>
    <h-tab-bar v-if="tabList.length && tabShow" />
    <h-popup />
    <!--体验引导-->
    <!--    <experience-guidance class="z-index-2" />-->
    <popup-personalized ref="refPopupPersonalized" />
    <h-status-footer />
    <h-share />
  </view>
</template>

<script setup lang="ts">
import HStatusFooter from '@/components/h-status-footer.vue'
import HTabBar from '@/components/h-tab-bar.vue'
import AuditList from '@/pages/audit/list.vue'
import ReportList from '@/pages/report-production/list.vue'
import StockList from '@/pages/stock-list/list.vue'
import Analysis from '@/pages/analysis/index.vue'
import QualityTestingList from '@/pages/quality-testing/list.vue'
import ExperienceGuidance from '@/pages/experience-guidance/index.vue'
import { computed, ref, watch } from 'vue'
import { $store, Role_Experience } from '@/utils/common'
import HooksShare from '@/hooks/share'
import { onShareAppMessage, onShareTimeline, onShow } from '@dcloudio/uni-app'
import HooksSettingConfig from '@/hooks/setting-config'
import PopupPersonalized from '@/pages/popup-personalized.vue'

const { shareTimeline, shareAppMessage } = HooksShare()
onShareAppMessage(() => shareAppMessage)
onShareTimeline(() => shareTimeline)

// decouple-from-ops-platform-cleanup (B.6 hotfix): 登录前不调用 getIndividuationConfig（hook 内部已加 guard）
const { getIndividuationConfig } = HooksSettingConfig()

const tabActive = computed(() => $store.state.tabBar.tabActive)
const tabList = computed(() => $store.state.tabBar.tabList)
const tabShow = computed(() => $store.state.tabBar.tabShow)

const refPopupPersonalized = ref(null)
onShow(() => {
  const token = uni.getStorageSync('micro_token')
  if (!token) {
    uni.reLaunch({ url: '/pages-login/index' })
  } else {
    // decouple-from-ops-platform-cleanup: 登录后（登录态有效）才拉个性化配置
    getIndividuationConfig()
    // #ifdef H5
    // H5：刷新/重新打开网页后恢复上次所在 tab（tabActive 在刷新后重置为空）。
    // 校验 tabList 含该 tab（角色切换后 last_tab 可能失效）
    const lastTab = uni.getStorageSync('last_tab')
    if (
      lastTab &&
      $store.state.tabBar.tabList.some((v) => v.text === lastTab) &&
      $store.state.tabBar.tabActive.text !== lastTab
    ) {
      $store.commit('tabBar/setTabActiveByName', lastTab)
    }
    // #endif
  }
})
watch(
  tabActive,
  (val) => {
    if ($store.state.user.userInfo.personalRecommend === 2) {
      refPopupPersonalized.value.init()
    }
  },
  { deep: true }
)
</script>

<style lang="scss" scoped></style>
