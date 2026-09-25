<template>
  <view class="manage-process-edit bg-f3f3f5 h-full box flex flex-col overflow-hidden">
    <uni-nav-bar />
    <h-status-header :title="checkEdit ? '编辑工序' : '新增工序'" />
    <view class="flex-1">
      <view class="flex flex-col overflow-auto mt-16 box mx-16 rounded-16 bg-fff pt-16 pb-32 px-32">
        <!--工序编码-->
        <view class="flex align-center">
          <view class="font-28 color-5a6f82 w-176">工序编码</view>
          <view class="flex-1 flex align-center h-80">
            <input
              v-model="submitItem.processCode"
              class="flex-1"
              placeholder="如未填写则自动生成"
              maxlength="20"
              placeholder-class="font-28 color-b8b8b8"
            />
            <image
              v-if="submitItem.processCode"
              src="/static/images/icon_del_b6c0c9.svg"
              class="icon-48"
              @tap="handleSubmitClear('processCode')"
            />
          </view>
        </view>
        <!--工序名称-->
        <view class="flex align-center b-t-1 border-f5f5f5">
          <view class="font-28 color-5a6f82 w-176 text-require pl-16 box">工序名称</view>
          <view class="flex-1 flex align-center h-80">
            <input
              v-model="submitItem.processName"
              class="flex-1"
              placeholder="输入工序名称"
              maxlength="30"
              placeholder-class="font-28 color-b8b8b8"
            />
            <image
              v-if="submitItem.processName"
              src="/static/images/icon_del_b6c0c9.svg"
              class="icon-48"
              @tap="handleSubmitClear('processName')"
            />
          </view>
        </view>
        <!--工序描述-->
        <view class="b-t-1 border-f5f5f5 pt-40 flex flex-col">
          <view class="font-28 color-5a6f82 w-176">工序描述</view>
          <h-textarea v-model="submitItem.processDesc" class="mt-16" :maxlength="300" placeholder="输入工序描述" />
        </view>
        <!--备注-->
        <view class="b-t-1 border-f5f5f5 pt-40 flex flex-col">
          <view class="font-28 color-5a6f82 w-176">备注</view>
          <h-textarea v-model="submitItem.remark" class="mt-16" :maxlength="100" placeholder="输入备注" />
        </view>
      </view>
    </view>
    <view class="flex p-32 justify-center">
      <h-button width="296" height="72" text="取消" type="bg-fff color-333" @tap.stop="handleCancel" />
      <h-button width="296" height="72" text="保存" class="ml-32" @tap.stop="handleSubmit" />
    </view>
    <h-status-footer />
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { useStore } from 'vuex'
import { _post } from '@/utils/common-request'

import HTextarea from '@/components/h-textarea.vue'
import { onLoad } from '@dcloudio/uni-app'

const store = useStore()

// 父组件传过来的对象
const dataItem = computed(() => {
  return store.state.process.item
})
const checkEdit = computed(() => {
  return dataItem.value.id
})

// 本地使用对象
const submitItem = reactive({
  processCode: '',
  processName: '',
  processDesc: '',
  remark: ''
})
function handleSubmitClear(key) {
  submitItem[key] = ''
}

onLoad((options) => {
  submitItem.processName = options.processName
})

onMounted(() => {
  Object.assign(submitItem, dataItem.value)
})

// 按钮模块
function handleCancel() {
  uni.navigateBack()
}
async function handleSubmit() {
  if (!submitItem.processName) {
    uni.showToast({ title: '请录入工序名称', icon: 'none' })
    return
  }
  uni.showLoading({ title: '数据提交中' })
  if (checkEdit.value) {
    await _post({ url: '/process/edit', data: submitItem })
  } else {
    await _post({ url: '/process/add', data: { ...submitItem, createdType: '1' } })
  }
  uni.hideLoading()
  uni.showToast({ title: `工序信息${checkEdit ? '添加' : '修改'}成功`, icon: 'none' })
  uni.$emit('refreshList')
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.manage-process-edit {
}
</style>
