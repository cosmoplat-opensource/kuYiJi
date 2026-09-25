<template>
  <view class="h-full bg-f3f3f5 flex flex-col relative">
    <!--状态栏,占位-->
    <uni-nav-bar />
    <!--用户模块-->
    <h-status-header title="弹窗demo" />
    <!--内容区域-->
    <view class="flex-1 flex flex-col align-center">
      <view class="mt-32 color-0066ff" @tap="handleNotice(1)">普通提示-短文本</view>
      <view class="mt-32 color-0066ff" @tap="handleNotice(2)">普通提示-长文本</view>
      <view class="mt-32 color-0066ff" @tap="handleNotice(3)">通知弹窗</view>
      <view class="mt-32 color-0066ff" @tap="handleNotice(4)">确认弹窗</view>
      <view class="mt-32 color-0066ff" @tap="handleNotice(5)">自定义确认弹窗</view>
      <view class="mt-32 color-0066ff flex align-center">
        <view>超出隐藏:</view>
        <h-text-display class="ml-32" text="长文本超出隐藏,width里传设计稿的px值" :width="100" />
      </view>
    </view>
    <!--底部安全区域,ios的x之后会留白-->
    <h-status-footer />
    <h-popup />
    <h-popup-dialog ref="refPopupDialog" title="提示">
      <template>
        <view class="color-333 font-28 py-32">内容需要纯文字意外的高度自定义或者按钮2个不够用的时候可以用</view>
      </template>
      <template #footer>
        <view class="flex align-center justify-center">
          <h-button width="296" height="72" text="取消" type="bg-f3f3f5 color-333" @tap.stop="handleDialogCancel" />
          <h-button width="296" height="72" text="驳回" class="ml-32" @tap.stop="handleDialogCancel" />
          <h-button width="296" height="72" text="通过" class="ml-32" @tap.stop="handleDialogCancel" />
        </view>
      </template>
    </h-popup-dialog>
  </view>
</template>

<script setup lang="ts">
/** 自定义弹窗使用 */
import HPopupDialog from '@/components/h-popup-dialog.vue'
import HooksPopupDialog from '@/hooks/popup-dialog'
import { ref } from 'vue'
/** 文本超长展示*/
import HTextDisplay from '@/components/h-text-display.vue'
/** 长文本弹窗 */
import HooksPopup from '@/hooks/popup'
const { popupOpen } = HooksPopup()
const refPopupDialog = ref(null)
const { handleDialogCancel, handleDialogOpen } = HooksPopupDialog(refPopupDialog)

function handleNotice(type) {
  switch (type) {
    case 1:
      // 明确字数很少的用这个,10个字左右
      uni.showToast({ title: '短文本用我', icon: 'none' })
      break
    case 2:
      // 不确定字数长度或者已知很长的用这个,自动换行
      // *****注意:页面中引用h-popup组件,并且在页面中调用popupOpen方法*****
      // ****注意2:h-popup是页面级组件,如果list中的每个item需要弹窗提示,则在list的页面中引入,不要再每个小item页面引入****
      popupOpen('长文本选我,支持自动换行--长文本选我,支持自动换行--长文本选我,支持自动换行--长文本选我,支持自动换行')
      break
    case 3:
      uni.showModal({
        title: '提示',
        content: '提示内容',
        confirmText: '知道了',
        showCancel: false
      })
      break
    case 4:
      uni.showModal({
        title: '提示',
        content: '确认要删除吗',
        success: function (res) {
          if (res.confirm) {
          } else {
          }
        }
      })
      break
    case 5:
      handleDialogOpen()
      break
  }
}
</script>

<style lang="scss" scoped></style>
