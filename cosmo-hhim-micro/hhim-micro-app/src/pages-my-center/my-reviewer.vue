<template>
  <view class="bg-F3F3F5 h-full flex flex-col">
    <uni-nav-bar />
    <h-status-header title="我的" />
    <my-userinfo :userInfo="userInfo" />
    <scroll-view scroll-y class="flex-1 overflow-hidden">
      <view v-if="Role_Admin || Role_Review || Role_Quality" class="flex px-16 box mt-16">
        <view
          @tap="toProduct"
          class="flex flex-col bg-fff rounded-16 mr-16 flex-1 font-28 justify-center align-center h-172 tap"
        >
          <image src="/pages-my-center/static/images/icon_product_64.svg" class="icon-64 mb-16" />
          <text>产品 {{ businessInfo.productNum }}</text>
        </view>
        <view
          @tap="toStaff"
          class="flex flex-col bg-fff rounded-16 mr-16 flex-1 font-28 justify-center align-center h-172 tap"
        >
          <image src="/pages-my-center/static/images/icon_employee_64.svg" class="icon-64 mb-16" />
          <text>员工 {{ businessInfo.staffNum }}</text>
        </view>
        <view
          @tap="toProcess"
          class="flex flex-col bg-fff rounded-16 flex-1 font-28 justify-center align-center h-172 tap"
        >
          <image :src="formatImage('icon_gongxu_64', 'svg')" class="icon-64 mb-16" />
          <text>工序 {{ businessInfo.processNum }}</text>
        </view>
      </view>
      <view
        class="rounded-16 bg-fff mx-16 mt-16 py-32 pl-32 pr-24 flex align-center"
        v-if="userInfo.personalRecommend !== 3"
      >
        <img :src="formatImage('icon_robot', 'svg')" class="icon-48" />
        <view class="font-28 bold color-333 ml-16">个性化推荐</view>
        <view class="mark-ebf0f5 ml-32">{{ Personal_Recommend ? '已开启' : '已关闭' }}</view>
        <view class="flex-1" />
        <h-switch :checked="Personal_Recommend" @tap="handlePersonalChange" />
      </view>
      <view class="rounded-16 bg-fff font-28 ml-16 mr-16 mt-16">
        <!--今日生产日报-->
        <part-subscribe v-if="Role_Admin || Role_Review || Role_Quality" />
        <!--      todo--  admin-->
        <view v-if="Role_Admin" @tap="toSet" class="tap w-100 h-112 pl-32 pr-24 box">
          <view class="w-100 h-100 flex align-center border-bottom-f5f5f5">
            <image src="/pages-my-center/static/images/icon_set.svg" class="icon-48 mr-16" />
            <text class="flex-1 bold">设置</text>
            <image src="/static/images/icon_arr.svg" class="icon-48" />
          </view>
        </view>
        <view @tap="toInvite" class="tap w-100 h-112 pl-32 pr-24 box">
          <view class="flex align-center w-100 h-100 border-bottom-f5f5f5">
            <image src="/pages-my-center/static/images/icon_share.svg" class="icon-48 mr-16" />
            <text class="flex-1 bold">邀请新伙伴</text>
            <image src="/static/images/icon_arr.svg" class="icon-48" />
          </view>
        </view>
      </view>
      <view @tap="toFeedback" class="rounded-16 bg-fff tap h-112 font-28 mx-16 pl-32 pr-24 box mt-16">
        <view class="flex align-center w-100 h-100 border-bottom-f5f5f5">
          <image :src="formatImage('icon_feedback', 'svg')" class="icon-48 mr-16" />
          <text class="flex-1 bold">联系我们</text>
          <image src="/static/images/icon_arr.svg" class="icon-48" />
        </view>
      </view>
      <!-- 仅体验角色 -->
      <view v-if="Role_Experience" @tap="toBuy" class="rounded-16 bg-fff tap h-112 font-28 mx-16 pl-32 pr-24 box mt-16">
        <view class="flex align-center w-100 h-100 border-bottom-f5f5f5">
          <image :src="formatImage('icon_buy')" class="icon-48 mr-16" />
          <text class="flex-1 bold">购买体验完整功能</text>
          <image src="/static/images/icon_arr.svg" class="icon-48" />
        </view>
      </view>
      <view
        v-if="Role_Admin || Role_Review"
        @tap="toMsg"
        class="rounded-16 bg-fff tap h-112 font-28 mx-16 pl-32 pr-24 box mt-16"
      >
        <view class="flex align-center w-100 h-100 border-bottom-f5f5f5">
          <image :src="formatImage('icon_msg')" class="icon-48 mr-16" />
          <text class="flex-1 bold">短信订阅</text>
          <image src="/static/images/icon_arr.svg" class="icon-48" />
        </view>
      </view>
      <!-- 切换租户 / 加入租户 -->
      <view @tap="toSwitchTenant" class="rounded-16 bg-fff tap h-112 font-28 mx-16 pl-32 pr-24 box mt-16">
        <view class="flex align-center w-100 h-100 border-bottom-f5f5f5">
          <image src="/pages-my-center/static/images/icon_com.svg" class="icon-48 mr-16" />
          <text class="flex-1 bold">切换租户</text>
          <image src="/static/images/icon_arr.svg" class="icon-48" />
        </view>
      </view>
      <view @tap="toJoinTenant" class="rounded-16 bg-fff tap h-112 font-28 mx-16 pl-32 pr-24 box mt-16">
        <view class="flex align-center w-100 h-100 border-bottom-f5f5f5">
          <image :src="formatImage('icon_add_forbid', 'svg')" class="icon-48 mr-16" />
          <text class="flex-1 bold">加入租户</text>
          <image src="/static/images/icon_arr.svg" class="icon-48" />
        </view>
      </view>
      <!-- 智能帮助中心 -->
      <view @tap="toSmartService" class="rounded-16 bg-fff tap h-112 font-28 mx-16 pl-32 pr-24 box mt-16">
        <view class="flex align-center w-100 h-100 border-bottom-f5f5f5">
          <image :src="formatImage('icon_buy')" class="icon-48 mr-16" />
          <text class="flex-1 bold">智能帮助中心</text>
          <image src="/static/images/icon_arr.svg" class="icon-48" />
        </view>
      </view>
      <view v-if="!uuc" class="logout-tap rounded-16 bg-fff mx-32 mt-32 h-112 flex-center font-28" @tap="handleLogout">
        退出登录
      </view>

      <view class="mt-96 flex justify-center">
        <text class="font-24 color-b6c0c9">v{{ wxVersion || '1.0.0' }}</text>
      </view>
    </scroll-view>
    <HTenantSwitchVue ref="tenantSwitchRef" />
  </view>
</template>

<script setup lang="ts">
import MyUserinfo from '@/pages-my-center/components/my-userinfo.vue'
import { computed } from 'vue'
import {
  $state,
  Role_Admin,
  Role_Experience,
  Role_Quality,
  Role_Review,
  Role_Staff,
  uuc,
  formatImage,
  Personal_Recommend,
  $store,
  openBuyMini,
  clearTenantCode
} from '@/utils/common'
import { _get } from '@/utils/common-request'
import { onMounted, ref } from 'vue'
import PartSubscribe from '@/pages/analysis/part-subscribe.vue'
import HSwitch from '@/components/h-switch.vue'
import HTenantSwitchVue from '@/components/h-tenant-switch-vue.vue'

const tenantSwitchRef = ref<InstanceType<typeof HTenantSwitchVue> | null>(null)

const wxVersion = ref('1.0.0')
onMounted(() => {
  getData()
  // H5 无 getAccountInfoSync，仅小程序获取版本号
  // #ifndef H5
  const accountInfo = uni.getAccountInfoSync()
  if (accountInfo) {
    wxVersion.value = accountInfo.miniProgram.version
  }
  // #endif
})

const businessInfo = ref<Partial<IBusinessInfo>>({})
function getData() {
  if (Role_Staff.value) return
  _get({ url: '/user/businessInfo' }).then((res: IResponseType<IBusinessInfo>) => {
    if (res?.data) {
      businessInfo.value = res.data
    }
  })
}
const userInfo = computed(() => {
  return $state.user.userInfo
})
function toProduct() {
  uni.navigateTo({
    url: '/pages-my-center/product-manage/list'
  })
}
function toStaff() {
  if (Role_Experience.value) {
    uni.showToast({ title: '体验用户不可访问', icon: 'none' })
    return
  }
  uni.navigateTo({
    url: '/pages-my-center/manage-staff/index'
  })
}
function toProcess() {
  uni.navigateTo({
    url: '/pages-my-center/manage-process/index'
  })
}
function toSet() {
  uni.navigateTo({
    url: '/pages-my-center/my-set'
  })
}
function toInvite() {
  // 体验用户点击邀请伙伴弹框内容文案修改
  if (Role_Experience.value) {
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
    return
  }
  const code = $state.user.userInfo.roleCode
  uni.navigateTo({
    url: `/pages-invite/index?roleCode=${code === '10' ? '20' : code}`
  })
}
function toFeedback() {
  uni.navigateTo({
    url: '/pages-my-center/my-feedback/submit-feedback'
  })
}
function toBuy() {
  openBuyMini()
}

function toMsg() {
  uni.navigateTo({
    url: '/pages-my-center/message-order/index'
  })
}
function toSmartService() {
  uni.navigateTo({
    url: '/pages-smart-service/index'
  })
}
function toSwitchTenant() {
  tenantSwitchRef.value?.open()
}
function toJoinTenant() {
  uni.navigateTo({
    url: '/pages-login/register-tenant/index?mode=invite'
  })
}
function handleLogout() {
  // 显式清掉多租户路由 header 用的 tenantCode（防御性：clearStorageSync 已经清了，但显式调用更稳，意图更明确）
  clearTenantCode()
  uni.clearStorageSync()
  uni.reLaunch({ url: '/pages-login/index' })
}
// 个性化推荐开关
function handlePersonalChange() {
  const personalRecommend = Personal_Recommend.value ? 0 : 1
  $store.commit('user/setUserInfo', { personalRecommend })
  _get({ url: '/user/enablePersonalized', data: { option: personalRecommend } })
}
</script>

<style lang="scss" scoped>
.logout-tap {
  &:active {
    background-color: #f3f3f5;
  }
}
.personal-switch {
  position: relative;
  width: px2vw(88);
  height: px2vw(48);
  background-color: #51c5ff;
  border-radius: px2vw(32);
  &__block {
    position: absolute;
    left: px2vw(4);
    top: px2vw(4);
    &.active {
      left: px2vw(44);
      width: 40px;
      height: 40px;
      background: #ffffff;
      box-shadow: -2px 4px 4px 1px rgba(0, 0, 0, 0.1);
    }
  }
}
</style>
