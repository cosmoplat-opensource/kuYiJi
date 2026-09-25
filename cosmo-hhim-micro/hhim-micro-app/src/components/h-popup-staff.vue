<template>
  <uni-popup ref="popupStaff" type="bottom" @maskClick="handleCancel" :safe-area="false">
    <view class="bg-fff box flex flex-col justify-start font-28 rounded-16-top">
      <view class="p-24 flex align-center b-b-1 border-f0f0f0">
        <view class="w-56" />
        <view class="py-12 px-16 flex-1 text-center">
          <text class="font-28 color-5a6f82">{{ title }}</text>
        </view>
        <image @click="handleCancel" src="/static/images/icon_close_666.svg" class="icon-48" />
      </view>
      <view class="min-h-500">
        <view class="px-32 mt-32">
          <h-search @searchInput="handleInput" placeholder="输入员工姓名/编码" bgClass="bg-f3f3f5" />
        </view>
        <view class="flex flex-col mt-24 h-500 overflow-auto" v-if="staffList.length">
          <view
            v-for="(item, index) in staffList"
            :key="item.id"
            :class="{ 'b-t-1 border-f5f5f5': index > 0 }"
            class="mx-32 box py-24 font-28 bold flex align-center"
            @tap="handleSelectProduct(item)"
          >
            <template v-if="itemSelect.id === item.id">
              <view class="color-0066ff">{{ item.nickName }}</view>
              <view class="color-0066ff flex-1">({{ item.userName }})</view>
              <image :src="formatImage('icon_checked', 'svg')" class="icon-48" />
            </template>
            <template v-else>
              <view class="color-333">{{ item.nickName }}</view>
              <view class="color-999">({{ item.userName }})</view>
              <view class="icon-48" />
            </template>
          </view>
        </view>
        <view v-else class="py-128">
          <h-empty tipsWord="暂无相关人员，请重新搜索" />
        </view>
      </view>
      <view class="bg-fff py-32 flex-center">
        <h-button width="296" height="72" text="取消" type="bg-f3f3f5 color-333" @tap.stop="handleCancel" />
        <h-button width="296" height="72" text="确认选择" class="ml-32" @tap.stop="confirmPopup" />
      </view>
    </view>
    <h-status-footer background-color="#ffffff" />
  </uni-popup>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { _get } from '@/utils/common-request'
const popupStaff = ref(null)
const emits = defineEmits(['staffSelected'])
import { formatImage } from '@/utils/common'
import HEmpty from '@/components/h-empty.vue'
const searchInput = ref('')
const staffList = ref([])
const itemSelect = ref({})

const props = defineProps({
  title: {
    type: String,
    default: '添加员工'
  }
})
function open(item) {
  itemSelect.value = item || {}
  searchInput.value = item?.nickName ?? ''
  initData()
  popupStaff.value.open('bottom')
}
function handleInput(str) {
  searchInput.value = str
  initData()
}
function handleInputClear() {
  searchInput.value = ''
  initData()
}
function handleCancel() {
  popupStaff.value?.close('bottom')
}

function handleSelectProduct(val) {
  itemSelect.value = val
}

function initData() {
  _get({ url: '/user/list', data: { key: searchInput.value } }).then((res) => {
    staffList.value = res.rows
  })
}

function confirmPopup() {
  if (!itemSelect.value.nickName) {
    uni.showToast({ title: '请选择员工', icon: 'none' })
    return
  }
  emits('staffSelected', { ...itemSelect.value, t: new Date().getTime() })
  handleCancel()
}

defineExpose({ open })
</script>
