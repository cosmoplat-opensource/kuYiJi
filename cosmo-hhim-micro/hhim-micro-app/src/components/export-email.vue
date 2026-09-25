<template>
  <!-- 导出 -->
  <view v-if="showPopup" class="export w-full h-full">
    <view class="main-content bg-fff w-full flex flex-col">
      <view class="h-96 flex align-center pl-24 pr-24 box border-bottom-f5f5f5 relative">
        <view
          v-if="needExport"
          class="btn flex justify-center align-center rounded-8 font-28 mr-16"
          :class="[type === '1' ? 'color-fff bg-0066ff' : 'color-5a6f82 bg-f3f3f5']"
          @tap="type = '1'"
        >
          导出
        </view>
        <view
          v-if="needImport"
          class="btn flex justify-center align-center rounded-8 font-28"
          :class="[type === '2' ? 'color-fff bg-0066ff' : 'color-5a6f82 bg-f3f3f5']"
          @tap="changeType('2')"
        >
          导入
        </view>
        <image @click="closePopup" src="/static/images/icon_close_666.svg" class="icon-48 close" />
      </view>
      <template v-if="type === '1'">
        <view class="h-64 px-32 box flex align-center color-5a6f82 font-24 bg-f7f8e7">
          <image @click="closePopup" src="/static/images/icon_tips.svg" class="icon-32 mr-16" />
          将导出的文件发送到邮箱
        </view>
        <view class="flex-1 border-bottom-f5f5f5 pl-32 pr-32 box flex flex-col justify-center">
          <view v-if="start || end" class="flex h-80 align-center">
            <view class="w-144 flex">
              <view class="mark-ebf0f5">所选范围</view>
            </view>
            <text class="flex-1 color-333 font-28">{{ start }}~{{ end }}</text>
          </view>
          <view v-else class="icon-16" />
          <view class="flex py-26 box align-center relative">
            <text class="w-144 font-28 color-5a6f82">接收邮箱</text>
            <input
              v-model.trim="receivedBy"
              :cursor-spacing="10"
              hold-keyboard
              class="flex-1"
              placeholder-class="font-28 color-b8b8b8"
              placeholder="输入邮箱地址"
            />
            <image
              v-if="receivedBy"
              src="/static/images/icon_del_b6c0c9.svg"
              class="icon-48"
              @tap="handleInputClear()"
            />
          </view>
        </view>
        <view class="bottom-btn w-100 mt-32 flex justify-center align-center font-32">
          <h-button width="296" height="72" text="取消" type="bg-f3f3f5 color-333" @tap.stop="closePopup" />
          <h-button width="296" height="72" text="确认并发送" class="ml-32" @tap.stop="confirmPopup" />
        </view>
      </template>
      <template v-else>
        <view class="px-32 pt-32 box">
          <view class="font-28 color-5a6f82 line-height-44">为了方便你操作，请到电脑端打开以下链接导入产品库存：</view>
          <view class="mt-40 mb-40 color-333" style="word-wrap: break-word">{{ linkUrl }}</view>
          <view class="h-64 flex align-center color-5a6f82 font-24">
            <image @click="closePopup" src="/static/images/icon_tips.svg" class="icon-32 mr-8" />
            此链接24小时内有效，过期后不可进入
          </view>
        </view>
        <view class="border-bottom-f5f5f5 mt-24" style="height: 0"></view>
        <view class="bottom-btn w-100 mt-32 flex justify-center align-center font-32">
          <h-button width="296" height="72" text="复制链接" @tap.stop="copyLink" />
        </view>
      </template>
    </view>
  </view>
</template>
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { $store, formatPx2Vw } from '@/utils/common'
import { _get } from '@/utils/common-request'
const props = defineProps({
  exportLoading: {
    type: Boolean,
    default: false
  },
  needExport: {
    type: Boolean,
    default: true
  },
  needImport: {
    type: Boolean,
    default: false
  },
  importType: {
    type: [String, Number],
    default: '10'
  }
})
//导入 导出 类型
const type = ref('1')
//导出
const receivedBy = ref('')
const showPopup = ref(false)
const start = ref('')
const end = ref('')
function openExportToMail(obj?) {
  const { startDate = '', endDate = '', tabType = '1' } = obj || {}
  if (tabType == '2') {
    changeType(String(tabType))
  }
  $store.commit('tabBar/setTabShow', false)
  showPopup.value = true
  start.value = startDate
  end.value = endDate
  receivedBy.value = uni.getStorageSync('ku_email_receive') || ''
}
function closePopup() {
  $store.commit('tabBar/setTabShow', true)
  showPopup.value = false
}
const emits = defineEmits(['confirmPopup'])

function handleInputClear() {
  receivedBy.value = ''
}

function confirmPopup() {
  if (!receivedBy.value) {
    uni.showToast({
      title: '请输入邮箱地址',
      icon: 'none'
    })
    return
  }
  uni.setStorageSync('ku_email_receive', receivedBy.value)
  if (props.exportLoading) return
  emits('confirmPopup', receivedBy.value)
}
function copyLink() {
  const data = linkUrl.value
  if (!data) {
    uni.showToast({ title: '链接未生成，请稍后再试', icon: 'none' })
    return
  }

  uni.setClipboardData({
    data: data,
    success: () => {
      uni.showToast({ title: '复制成功', icon: 'none' })
    },
    fail: (err) => {
      uni.showToast({ title: '复制失败，请重试', icon: 'none' })
    }
  })
}
const linkUrl = ref('')
function changeType(val) {
  type.value = val
  _get({ url: '/basic/import/importLinkTemp', data: { taskType: props.importType } }).then((res) => {
    linkUrl.value = res.msg
  })
}
defineExpose({ openExportToMail, closePopup })
</script>
<style lang="scss" scoped>
.bg-tip {
  width: px2vw(104);
  height: px2vw(32);
  line-height: px2vw(32);
  background: #ebf0f5;
  border-radius: px2vw(4);
  border: px2vw(2) solid #d3dfeb;
}
.export {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 999;
  background: rgba(0, 0, 0, 0.3);
}
.bottom-btn {
  height: px2vw(152);
  border-radius: 0px 0px 0px 0px;
}
.main-content {
  border-radius: px2vw(16) px2vw(16) 0 0;
  position: absolute;
  left: 0;
  bottom: 0;
}
.h-451 {
  height: px2vw(451);
}
.h-371 {
  height: px2vw(371);
}
.btn {
  width: px2vw(88);
  height: px2vw(48);
}
.close {
  position: absolute;
  top: px2vw(24);
  right: px2vw(24);
}
</style>
