<template>
  <view class="flex box pl-32 pr-32 pt-48 pb-40" @click="edit">
    <image :src="userInfo.avatar || '/static/images/img_default.svg'" class="avatar rounded-50 icon-128 mr-32" />
    <view class="color-333 flex-1 flex flex-col">
      <view class="flex justify-between align-center mt-4">
        <view class="font-36">{{ userInfo.nickName }}</view>
        <h-button
          v-if="!Role_Experience"
          type="border-1 border-0066ff color-0066ff bg-fff"
          font="font-24"
          width="80"
          height="48"
          text="编辑"
        />
      </view>
      <view class="flex align-center mt-18">
        <view class="gonghao font-24 bg-fff rounded-24 text-center">{{ userInfo.phonenumber }}</view>
        <view class="flex align-center ml-32">
          <view class="mark-24a8ff">角色</view>
          <view class="ml-8 font-28 color-24a8ff">{{ userInfo.roleName }} </view>
        </view>
      </view>
      <view class="font-28 color-333 mt-24 flex align-center">
        <image src="/pages-my-center/static/images/icon_com.svg" class="icon-32 mr-8" />{{ userInfo.tenantName }}
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { $store, $state, getUserInfo, Role_Experience } from '@/utils/common'

const userInfo = computed(() => {
  return $state.user.userInfo
})

const dataItem = reactive({})
onMounted(() => {
  getUserInfo(userInfo.value.userName).then((res) => {
    Object.assign(dataItem, res)
    $store.commit('user/setUserInfo', dataItem)
  })
})
const emits = defineEmits(['edit'])
function edit() {
  if (Role_Experience.value) {
    return
  }
  uni.navigateTo({
    url: `/pages-my-center/edit-userinfo?username=${userInfo.value.userName}`
  })
}
</script>

<style lang="scss" scoped>
.jiaose {
  width: px2vw(56);
  height: px2vw(32);
  line-height: px2vw(32);
  background: #64b5ea;
}
.avatar {
  border: px2vw(4) solid rgba(255, 255, 255, 0.56);
}
.gonghao {
  padding: px2vw(8) px2vw(16);
}
.edit {
  height: px2vw(48);
  line-height: px2vw(48);
  border: px2vw(2) solid #0066ff;
  &:active {
    background-color: #0066ff;
    color: #ffffff;
  }
}
</style>
