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
        <view class="flex-center pl-32 pr-24 mt-32">
          <view class="flex align-center bg-f3f3f5 box input-class popup flex-1 pl-32 pr-24">
            <image src="/static/images/icon_s_b8.svg" class="icon-24" />
            <input
              v-model="searchInput"
              :focus="false"
              @input="handleInput"
              placeholder-style="color:#B8B8B8"
              class="main-input pl-16 font-28"
              placeholder="输入员工姓名或编码"
            />
            <image src="/static/images/icon_del.svg" class="icon-48" v-show="searchInput" @click="handleInputClear" />
          </view>
        </view>
        <view class="flex flex-col mt-24 h-500 overflow-auto" v-if="staffList.length">
          <view
            v-for="(item, index) in staffList"
            :key="item.id"
            :class="{ 'b-t-1 border-f5f5f5': index > 0 }"
            class="mx-32 box py-24 font-32 bold flex align-center"
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
    </view>
  </uni-popup>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { _get } from '@/utils/common-request'
const popupStaff = ref(null)
const emits = defineEmits(['userSelect'])
import { formatImage } from '@/utils/common'
import HEmpty from '@/components/h-empty.vue'
import { number } from 'echarts'
const searchInput = ref('')
const staffList = ref([])
const itemSelect = ref({ id: number })

defineProps({
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
function handleInput(e) {
  searchInput.value = e.detail.value
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
  emits('userSelect', itemSelect.value)
  handleCancel()
}

function initData() {
  _get({ url: '/user/list', data: { key: searchInput.value } }).then((res: IResponseType<[]>) => {
    staffList.value = res.rows
  })
}

defineExpose({ open })
</script>
