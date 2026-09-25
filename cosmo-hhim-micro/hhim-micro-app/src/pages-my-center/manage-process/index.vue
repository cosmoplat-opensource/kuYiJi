<template>
  <view class="manage-process bg-f3f3f5 h-full box flex flex-col overflow-hidden">
    <uni-nav-bar />
    <h-status-header title="工序管理" />
    <view class="flex-1 flex flex-col overflow-hidden" @tap="handleCloseDrop">
      <!--搜索区域-->
      <view class="box flex align-center pt-24 pl-32 pr-24 pb-16">
        <h-search @searchInput="searchList" class="flex-1" placeholder="输入工序编码/名称" />
        <h-drop-down ref="dropDownRef" :localdata="options" @change="menuAction($event)">
          <image src="/static/images/icon_3dot.svg" class="icon-48 ml-24" />
        </h-drop-down>
      </view>
      <!--列表区域-->
      <scroll-view
        @refresherrefresh="pageListRefresh"
        @scrolltolower="pageListLoadMore"
        :refresher-triggered="refreshTag"
        refresher-enabled
        scroll-y
        class="flex-1 flex flex-col mt-8 px-16 box overflow-hidden"
        enable-flex
      >
        <template v-if="pageEmpty">
          <h-empty className="pt-240" tipsWord="暂无工序，新增后的可在此显示" />
        </template>
        <template v-else>
          <view class="pb-8" v-for="(item, index) in dataList" :key="index" @tap="handleEdit(item)">
            <view class="bg-fff rounded-16 p-32">
              <!--code name edit-->
              <view class="flex align-center">
                <view class="font-28 color-333 bold">{{ item.processCode }}</view>
                <view class="font-28 color-333 bold flex-1 ml-16">
                  <h-text-display :width="212" :text="item.processName" />
                </view>
                <h-checkbox
                  v-if="checkAble"
                  class="ml-24"
                  :checked="item.checked"
                  @checkedChange="(val) => handleCheckedItem(val, item)"
                />
                <image v-else src="/static/images/icon_edit.svg" class="icon-32 ml-16" />
              </view>
              <view class="mt-24 pt-16 b-t-1 border-f5f5f5">
                <!--描述-->
                <view class="flex align-center">
                  <view class="mark-ebf0f5">描述</view>
                  <view class="font-28 color-5a6f82 ml-16 word-break flex-1">{{ item.processDesc || '-' }}</view>
                </view>
                <!--备注-->
                <view class="flex align-center mt-24" v-if="item.remark">
                  <view class="mark-ebf0f5">备注</view>
                  <view class="font-28 color-5a6f82 ml-16 word-break flex-1">{{ item.remark }}</view>
                </view>
              </view>
            </view>
          </view>
          <uni-load-more :status="dataNoMore" />
        </template>
      </scroll-view>
      <!--删除区域-->
      <view class="pt-16 px-32 pb-32" v-if="checkAble">
        <view class="flex align-center font-24 pr-16">
          <view class="color-333 bold">已选</view>
          <view class="flex-1 ml-4 color-ff0000">{{ checkNum }}</view>
          <view class="color-5a6f82" @tap="handleCheckAll">全选</view>
          <h-checkbox class="ml-24" :checked="checkAll" @checkedChange="handleCheckAll" />
        </view>
        <view class="flex justify-center mt-32">
          <h-button width="296" height="72" text="取消" type="bg-fff color-333" @tap.stop="handleCheckCancel" />
          <h-button
            width="296"
            height="72"
            active="red"
            text="删除"
            type="bg-ff0000 color-fff"
            class="ml-32"
            @tap.stop="handleCheckDeleteAll"
          />
        </view>
      </view>
    </view>
    <h-status-footer />
    <h-popup />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import HDropDown from '@/components/h-dropdown/index.vue'
import { _delete, _post } from '@/utils/common-request'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import HEmpty from '@/components/h-empty.vue'
import { useStore } from 'vuex'

import HooksPageList from '@/hooks/page-list'
import HooksPopup from '@/hooks/popup'

// 输入框模组
const searchInput = ref('')

// 下拉框模组
const options = ref([
  {
    value: 'add',
    icon: '/static/images/icon_add_32.svg',
    text: '新增'
  },
  {
    value: 'delete',
    icon: '/static/images/icon_del_32.svg',
    text: '删除'
  },
  {
    value: 'clean',
    icon: '/static/images/icon_health_green.svg',
    text: '清理'
  }
])
const dropDownRef = ref(null)
const store = useStore()
function menuAction(action) {
  if (action === '') {
    return
  }
  switch (action) {
    case 'add':
      store.commit('process/updateProcess', { processCode: '', processName: '', processDesc: '', remark: '' })
      uni.navigateTo({ url: '/pages-my-center/manage-process/edit' })
      break
    case 'delete':
      checkAble.value = true
      break
    case 'clean':
      uni.navigateTo({ url: '/pages-data-governance/main/index?type=process' })
      break
    default:
  }
}

// 下拉列表模组
const { dataList, pageListRefresh, dataNoMore, refreshTag, pageEmpty, pageListLoadMore, pageListSetParams } =
  HooksPageList('/process/list', { data: {} })
function searchList(str) {
  pageListSetParams({ key: str })
}
function handleInputClear() {
  searchInput.value = ''
  pageListSetParams({ key: undefined })
}

function handleCloseDrop() {
  if (dropDownRef.value.showSelector) {
    dropDownRef.value.closeDrop()
  }
}
function handleEdit(item) {
  if (checkAble.value) {
    handleCheckedItem(!item.checked, item)
  } else {
    const { id, processCode, processName, processDesc, remark } = item
    store.commit('process/updateProcess', { id, processCode, processName, processDesc, remark })
    uni.navigateTo({ url: '/pages-my-center/manage-process/edit' })
  }
}
onLoad(() => {
  uni.$on('refreshList', () => {
    pageListRefresh()
  })
})
onUnload(() => {
  uni.$off('refreshList')
})

// 删除功能模块
const checkAble = ref(false)
const checkAll = ref(false)
const checkNum = computed(() => {
  return dataList.value.filter((v) => v.checked).length
})
function handleCheckCancel() {
  checkAble.value = false
}
function handleCheckAll() {
  checkAll.value = !checkAll.value
  dataList.value.forEach((v) => {
    v.checked = checkAll.value
  })
}
const { popupOpen } = HooksPopup()
function handleCheckDeleteAll() {
  const ids = []
  dataList.value.forEach((v) => {
    v.checked && ids.push(v.id)
  })
  uni.showModal({
    title: '提示',
    content: '是否确认删除?',
    success: function (res) {
      if (res.confirm) {
        _delete({ url: `/process/${ids.join(',')}` }).then((res) => {
          if (res.msg.includes('无法删除')) {
            popupOpen(res.msg)
          }
          pageListRefresh()
        })
      }
    }
  })
}
function handleCheckedItem(val, item) {
  const obj = dataList.value.find((v) => v.id === item.id)
  obj.checked = val
  checkAll.value = dataList.value.every((v) => v.checked)
}
</script>

<style lang="scss">
.manage-process {
  .input-area {
    height: px2vw(64);
    background: #ebecee;
    box-shadow: 0 px2vw(1) 0 px2vw(1) #ffffff, inset 0 px2vw(8) px2vw(12) px2vw(1) #ccd1d9;
    border-radius: px2vw(16);
    padding: 0 px2vw(24) 0 px2vw(32);
  }
  .popup-content {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    max-width: 80vw;
    word-break: break-word;
    padding: px2vw(16);
    min-height: px2vw(50);
    border-radius: px2vw(16);
    background-color: #fff;
  }
}

.uni-popup__wrapper {
  border-radius: px2vw(16);
}
</style>
