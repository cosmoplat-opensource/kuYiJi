<template>
  <view class="production-list">
    <uni-nav-bar />
    <h-status-header title="产品管理" />
    <view class="flex-1 flex flex-col overflow-hidden pt-24" @tap="handleCloseDrop">
      <view class="flex align-center justify-between pr-24 pl-32 border-box relative">
        <h-search @searchInput="searchList" class="flex-1" placeholder="输入产品编码/名称" />
        <h-page-video class="mx-24" :videoArr="[22, 23]" page-name="产品管理" />
        <h-button
          type="border-1 border-0066ff color-0066ff bg-fff"
          font="font-24"
          height="48"
          width="128"
          text="导入工艺"
          @tap="exportToMail"
        />
        <h-drop-down ref="dropDownRef" :localdata="options" @change="menuAction($event)">
          <image src="/static/images/icon_3dot.svg" class="icon-48 ml-24" />
        </h-drop-down>
      </view>
      <!--数据列表滚动区域-->
      <scroll-view
        @refresherrefresh="pageListRefresh"
        @scrolltolower="pageListLoadMore"
        :refresher-triggered="refreshTag"
        refresher-enabled
        scroll-y
        class="area-scroll mt-8 pl-16 pr-16 box"
        enable-flex
      >
        <template v-if="pageEmpty">
          <h-empty className="pt-240" tipsWord="暂无产品，新增后的可在此显示" />
        </template>
        <template v-else>
          <list-item
            class="mb-8"
            v-for="(item, index) in dataList"
            :key="index"
            :dataItem="item"
            :showDelete="showDelete"
            @itemCheck="handleItemCheck"
          />
          <uni-load-more :status="dataNoMore" />
        </template>
      </scroll-view>
      <view v-if="showDelete" class="h-200">
        <view class="h-68 flex align-center pl-32 pr-48 box font-24">
          <view class="flex-1 bold">
            <text>已选</text>
            <text class="color-ff0000 ml-8 bold">{{ checkCheckedNum }}</text>
          </view>
          <view class="color-5a6f82 flex align-center">
            <view>全选</view>
            <h-checkbox :checked="checkAll" class="ml-24" @checkedChange="handleCheckedAll" />
          </view>
        </view>
        <view class="flex justify-center mt-16 font-32">
          <h-button width="296" height="72" text="取消" type="bg-fff color-333" @tap.stop="cancelDelete" />
          <h-button
            width="296"
            height="72"
            text="删除"
            type="bg-ff0000 color-fff"
            class="ml-32"
            active="red"
            @tap.stop="deleteProduct"
          />
        </view>
      </view>
    </view>
    <ExportEmail ref="exportEmail" :needImport="true" :needExport="false" importType="40" />
    <h-status-footer />
    <h-popup />
  </view>
</template>
<script setup lang="ts">
import HCheckBox from '@/components/h-checkbox.vue'
import HDropDown from '@/components/h-dropdown/index.vue'
import { computed, ref } from 'vue'
import ListItem from '@/pages-my-center/product-manage/components/list-item.vue'
import { _delete } from '@/utils/common-request'
import HooksPageList from '@/hooks/page-list'
import HEmpty from '@/components/h-empty.vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import HooksPopup from '@/hooks/popup'
import ExportEmail from '@/components/export-email.vue'

const checkAll = ref(false)
const showDelete = ref(false)
const checkCheckedNum = computed(() => {
  return dataList.value.filter((item: any) => item.checked).length
})

// 下拉框模组
const dropDownRef = ref(null)
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
function handleCloseDrop() {
  if (dropDownRef.value?.showSelector) {
    dropDownRef.value.closeDrop()
  }
}
function menuAction(action) {
  if (action === '') {
    return
  }
  switch (action) {
    case 'add':
      uni.navigateTo({ url: '/pages-my-center/product-manage/add-edit' })
      break
    case 'delete':
      showDelete.value = true
      break
    case 'clean':
      uni.navigateTo({ url: '/pages-data-governance/main/index?type=product' })
      break
    default:
  }
}

//导出
const exportEmail = ref(null)
function exportToMail() {
  exportEmail.value?.openExportToMail({ tabType: '2' })
}

const { dataList, pageListRefresh, pageTotal, dataNoMore, refreshTag, pageEmpty, pageListLoadMore, pageListSetParams } =
  HooksPageList('/product/list', { data: {} })
function searchList(str) {
  pageListSetParams({ key: str })
}

onLoad(() => {
  uni.$on('refreshProductList', () => {
    pageListRefresh()
  })
})
onUnload(() => {
  uni.$off('refreshProductList')
})
//全选
function handleCheckedAll(val) {
  checkAll.value = val
  dataList.value.forEach((item) => {
    item.checked = val
  })
}
//部分选
function handleItemCheck(item) {
  const obj = dataList.value.find((v) => v.id && v.id === item.id)
  obj && (obj.checked = !obj.checked)
  checkAll.value = dataList.value.every((v) => v.checked)
}

//取消删除
function cancelDelete() {
  showDelete.value = false
  handleCheckedAll(false)
}
const { popupOpen } = HooksPopup()
//删除产品
function deleteProduct() {
  let ids = dataList.value.reduce((pre, cur) => {
    if (cur.checked) {
      pre.push(cur.id)
    }
    return pre
  }, [])
  if (!ids.length) {
    uni.showToast({ title: '请先选择需要删除的产品', icon: 'none' })
    return
  }
  uni.showModal({
    title: '提示',
    content: '是否确认删除？',
    success: function (res) {
      if (res.confirm) {
        _delete({ url: '/product/' + ids.toString() }).then((res: IResponseType<unknown>) => {
          if (res.msg.includes('无法删除')) {
            popupOpen(res.msg)
          }
          cancelDelete()
          pageListRefresh()
        })
      } else if (res.cancel) {
      }
    }
  })
}
</script>
<style lang="scss" scoped>
.btn {
  width: px2vw(288);
  background: linear-gradient(180deg, #ffffff 0%, #f5f7fa 100%);
  border-radius: px2vw(44);
}
.cancel {
  background: linear-gradient(180deg, #f5f7fa 0%, #ffffff 100%);
  box-shadow: px2vw(8) px2vw(8) px2vw(16) px2vw(1) rgba(0, 0, 0, 0.1);
}
.delete {
  width: px2vw(288);
  background: linear-gradient(180deg, #ee6258 0%, #fea29b 100%);
  box-shadow: px2vw(8) px2vw(8) px2vw(16) rgba(0, 0, 0, 0.1);
}
.h-68 {
  height: px2vw(68);
}
.tap {
  &:hover {
    background-color: #f3f3f5;
  }
}
.moreBg {
  position: absolute;
  right: px2vw(0);
  top: px2vw(0);
  z-index: 5;
  .more {
    position: absolute;
    right: px2vw(16);
    top: px2vw(84);
    z-index: 5;
    .triangle {
      width: px2vw(24);
      height: px2vw(20);
      position: absolute;
      right: px2vw(20);
      top: px2vw(-18);
    }
    .more-btn {
      width: px2vw(210);
      height: px2vw(240);
      background: #ffffff;
      box-shadow: 0 px2vw(4) px2vw(8) px2vw(1) rgba(216, 221, 229, 1);
      border: 1px solid #e5e5e5;
    }
  }
}

.no-data {
  margin-top: px2vw(320);
  .img {
    width: px2vw(480);
    height: px2vw(156);
  }
}
.bottom-btn {
  height: px2vw(152);
  border-radius: 0;
  opacity: 1;
  .cancel {
    width: px2vw(296);
    height: px2vw(88);
    background: linear-gradient(180deg, #f5f7fa 0%, #ffffff 100%);
    box-shadow: px2vw(8) px2vw(8) px2vw(16) px2vw(1) rgba(0, 0, 0, 0.1);
    border-radius: px2vw(44);
  }
  .confirm {
    width: px2vw(296);
    height: px2vw(88);
    background: linear-gradient(180deg, #629bf0 0%, #89b4f5 100%);
    box-shadow: px2vw(8) px2vw(8) px2vw(16) px2vw(1) rgba(0, 0, 0, 0.1);
    border-radius: px2vw(44);
  }
}
.production-list {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f3f3f5;
  overflow: hidden;
  .area-scroll {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    padding-top: px2vw(16);
    &__list-item {
      margin-bottom: px2vw(8);
      padding: 0 px2vw(16);
    }
  }
}
</style>
