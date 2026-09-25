<!--
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 *
 * @author cosmo-hhim-open Team
 -->
<template>
  <uni-popup ref="popup" type="bottom" :safe-area="false">
    <view class="tenant-switch bg-fff rounded-16-top">
      <!-- 头部 -->
      <view class="flex align-center justify-between px-32 pt-32 pb-16">
        <text class="font-32 bold color-333">切换租户</text>
        <image
          src="/static/images/icon_close_666.svg"
          class="icon-48"
          @tap="handleClose"
        />
      </view>

      <!-- 租户列表 -->
      <scroll-view scroll-y class="tenant-list px-32" :style="{ maxHeight: '60vh' }">
        <view
          v-for="(tenant, index) in tenantList"
          :key="tenant.tenantCode"
          class="tenant-item flex align-center py-24 border-bottom-f5f5f5 tap"
          @tap="handleSelectTenant(tenant)"
        >
          <view class="flex-1">
            <view class="flex align-center">
              <text class="font-28 color-333">{{ tenant.tenantName }}</text>
              <view
                v-if="tenant.tenantCode === currentTenantCode"
                class="mark-ebf0f5 ml-16"
              >当前</view>
            </view>
            <text v-if="tenant.customerName" class="font-24 color-b8b8b8 mt-4">{{ tenant.customerName }}</text>
          </view>
          <image
            v-if="tenant.tenantCode === currentTenantCode"
            src="/static/images/icon_checked_20.svg"
            class="icon-32"
          />
          <image
            v-else
            src="/static/images/icon_arr.svg"
            class="icon-48"
          />
        </view>

        <!-- 空状态 -->
        <view v-if="tenantList.length === 0" class="flex flex-col align-center justify-center py-64">
          <text class="font-28 color-b8b8b8">暂无其他租户</text>
        </view>
      </scroll-view>

      <!-- 底部按钮 -->
      <view class="px-32 pt-16 pb-32">
        <view
          class="join-btn flex align-center justify-center h-88 rounded-16 tap"
          @tap="handleJoinTenant"
        >
          <image :src="formatImage('icon_add_forbid', 'svg')" class="icon-40 mr-8" />
          <text class="font-28 color-0066ff">加入新租户</text>
        </view>
      </view>
    </view>
  </uni-popup>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { $store, formatTabDefaultJump, getUserInfo, formatImage } from '@/utils/common'
import { _post } from '@/utils/common-request'

const popup = ref(null)

const currentTenantCode = computed(() => {
  return uni.getStorageSync('micro_tenantCode') || ''
})

const tenantList = ref<ITenantOption[]>([])

interface ITenantOption {
  tenantCode: string
  tenantName: string
  customerName?: string
  isPrimary?: number
}

const emit = defineEmits(['switchSuccess'])

function open() {
  // 每次打开时重新读取租户列表
  loadTenantList()
  popup.value?.open('bottom')
}

function handleClose() {
  popup.value?.close()
}

function loadTenantList() {
  const stored = uni.getStorageSync('micro_tenantList')
  if (stored && Array.isArray(stored) && stored.length > 0) {
    tenantList.value = stored as ITenantOption[]
  } else {
    // 兜底：用当前租户构造一个单元素列表
    const code = uni.getStorageSync('micro_tenantCode')
    const name = uni.getStorageSync('micro_tenantName')
    const customerName = uni.getStorageSync('micro_customerName')
    if (code && name) {
      tenantList.value = [{ tenantCode: code, tenantName: name, customerName: customerName || '' }]
    } else {
      tenantList.value = []
    }
  }
}

let switching = false
function handleSelectTenant(tenant: ITenantOption) {
  if (switching) return
  if (tenant.tenantCode === currentTenantCode.value) {
    // 已是当前租户，直接关闭
    handleClose()
    return
  }

  uni.showModal({
    title: '切换租户',
    content: `确定切换到「${tenant.tenantName}」吗？切换后将返回首页。`,
    success: (modal) => {
      if (!modal.confirm) return
      switching = true
      uni.showLoading({ title: '切换中...', mask: true })

      _post({
        url: '/login/switch-tenant',
        data: { tenantCode: tenant.tenantCode }
      })
        .then((res: IResponseType<ILogin>) => {
          const { token, userName } = res.data || {}
          if (!token) {
            uni.showToast({ title: '切换失败，请重试', icon: 'none' })
            return
          }

          // 更新 storage
          uni.setStorageSync('micro_token', token)
          uni.setStorageSync('micro_user', userName)
          uni.setStorageSync('micro_tenantCode', tenant.tenantCode)
          uni.setStorageSync('micro_tenantName', tenant.tenantName)
          uni.setStorageSync('micro_customerName', tenant.customerName || '')

          // 更新 store
          $store.commit('user/setUserInfo', {
            token,
            userName,
            tenantCode: tenant.tenantCode,
            tenantName: tenant.tenantName
          })

          // 关闭弹窗
          handleClose()

          // 获取完整用户信息并跳转首页
          getUserInfo(userName).then((info: IUserInfo) => {
            $store.commit('user/setUserInfo', { ...info })
            formatTabDefaultJump(info)
            emit('switchSuccess', tenant)
          })
        })
        .catch((err) => {
          const msg = err?.msg || '切换失败，请稍后重试'
          uni.showToast({ title: msg, icon: 'none' })
        })
        .finally(() => {
          switching = false
          uni.hideLoading()
        })
    }
  })
}

function handleJoinTenant() {
  handleClose()
  // 跳转到注册/加入租户页面
  setTimeout(() => {
    uni.navigateTo({
      url: '/pages-login/register-tenant/index?mode=invite'
    })
  }, 300)
}

defineExpose({ open })
</script>

<style lang="scss" scoped>
.tenant-switch {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.tenant-list {
  max-height: 60vh;
}

.tenant-item {
  &:active {
    background-color: #f3f3f5;
    margin-left: -16rpx;
    margin-right: -16rpx;
    padding-left: 16rpx;
    padding-right: 16rpx;
    border-radius: 8rpx;
  }
}

.join-btn {
  border: px2vw(2) dashed #004baa;
  background-color: #f5f8ff;
  border-radius: px2vw(16);

  &:active {
    background-color: #e8efff;
  }
}
</style>

