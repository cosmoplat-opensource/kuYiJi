<template>
  <uni-popup ref="popup" :is-mask-click="false">
    <view class="rounded-8 relative pt-16 w-528 box bg-fff">
      <!--      内容,图片-->
      <view class="flex gap-16 pl-32 pr-24">
        <view class="font-48 flex-1">
          <text class="color-333 font-32 bold">开启个性化推荐，为你提供智能定制化服务</text>
        </view>
        <img :src="formatImage('img_good')" class="icon-112" />
      </view>
      <!--      不再提醒-->
      <view class="flex align-center mt-16 pb-24 b-b-1 border-f5f5f5 px-32">
        <h-checkbox-icon :checked="checked" @checkedChange="handleChecked" />
        <view class="ml-16 color-5a6f82 font-24">不再提示</view>
      </view>
      <!--      按钮区-->
      <view class="py-32 flex-center gap-32">
        <h-button
          height="64"
          width="176"
          type="bg-f3f3f5 color-333"
          active="grey"
          text="稍后体验"
          @tap="handleCancel"
        />
        <h-button height="64" width="120" text="开启" @tap="handleConfirm" />
      </view>
      <img :src="formatImage('img_people_womon')" class="icon-women icon-112" />
    </view>
  </uni-popup>
</template>

<script setup lang="ts">
import { $store, formatImage } from '@/utils/common'
import HCheckboxIcon from '@/components/h-checkbox-icon.vue'
import { ref } from 'vue'
import { _get } from '@/utils/common-request'
import dayjs from 'dayjs'

const checked = ref(false)
function handleChecked(bool) {
  checked.value = bool
}

// 确认开启
function handleConfirm() {
  doPersonalized(1)
}
// 稍后体验
function handleCancel() {
  if (checked.value) {
    doPersonalized(0)
  } else {
    uni.setStorageSync('ku_personalized_later', dayjs().format('YYYY-MM-DD'))
    popup.value.close()
  }
}

function doPersonalized(option) {
  _get({ url: '/user/enablePersonalized', data: { option } })
  $store.commit('user/setUserInfo', { personalRecommend: option })
  popup.value.close()
}

const popup = ref(null)
function init() {
  const time = uni.getStorageSync('ku_personalized_later')
  if (time !== dayjs().format('YYYY-MM-DD')) {
    popup.value.open()
  }
}
defineExpose({ init })
</script>

<style scoped lang="scss">
.icon-women {
  position: absolute;
  left: 0;
  top: px2vw(-112);
}
</style>
