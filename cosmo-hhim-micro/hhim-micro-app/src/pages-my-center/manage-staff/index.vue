<template>
  <view class="manage-staff bg-f3f3f5 h-full box flex flex-col overflow-hidden">
    <uni-nav-bar />
    <h-status-header title="员工管理" />
    <view class="flex-1 flex flex-col overflow-hidden">
      <!--搜索区域-->
      <view class="px-32 flex pb-16 pt-24">
        <h-search @searchInput="getList" class="flex-1" bgClass="bg-fff-80" placeholder="输入员工账号/名称" />
      </view>
      <view class="mx-16 box">
        <data-notice-area :days="notSubmitDayNums" v-if="notSubmitDayNums">
          <view class="btn-no-need" @tap="handleBack">无需处理</view>
        </data-notice-area>
      </view>
      <!--列表区域-->
      <template v-if="dataList.length > 0">
        <uni-list class="flex-1 overflow-auto mt-8 bg-f3f3f5">
          <uni-list-item v-for="(item, index) in dataList" :key="index">
            <template #body>
              <view class="px-16 pb-8 w-100">
                <view class="bg-fff rounded-16 p-32" @tap="handleClickItem(item)">
                  <!--name switch-->
                  <view class="flex align-center">
                    <view class="font-28 color-333 bold"><h-text-display :text="item.nickName" :width="160" /></view>
                    <view class="font-28 color-999 flex-1 word-break bold">({{ item.userName }})</view>
                    <h-checkbox
                      v-if="showDelete"
                      :checked="item.checked"
                      class="ml-16"
                      @checkedChange="handleItemCheck(item)"
                    />
                    <h-switch v-else :checked="item.status !== '1'" @tap.stop="handleSwitchChange(item)" />
                  </view>
                  <!--基础信息-->
                  <view class="mt-24 pt-24 b-t-1 border-f5f5f5">
                    <!--性别 电话-->
                    <view class="flex align-center">
                      <image
                        src="/pages-my-center/static/images/icon_woman.svg"
                        class="icon-32"
                        v-if="item.sex === '1'"
                      />
                      <image
                        src="/pages-my-center/static/images/icon_man.svg"
                        class="icon-32"
                        v-else="item.sex === '0'"
                      />
                      <view v-if="item.sex === '1'" class="ml-8 font-28 color-5a6f82">女</view>
                      <view v-else class="ml-8 font-28 color-5a6f82">男</view>
                      <image src="/pages-my-center/static/images/icon_phone.svg" class="ml-32 icon-32" />
                      <view class="ml-8 font-28 color-5a6f82 flex-1">{{ item.phonenumber }}</view>
                      <view class="mark-24a8ff">角色 </view>
                      <view class="bg-DEF2FF rounded-8 color-418CBD font-28 h-32 flex-center px-8">{{
                        getRoleList(item.roleCode)
                      }}</view>
                    </view>
                    <!--备注-->
                    <view class="flex align-center mt-24" v-if="item.remark">
                      <view class="mark-ebf0f5">备注</view>
                      <view class="font-28 color-333 ml-16 word-break flex-1">{{ item.remark }}</view>
                    </view>
                  </view>
                </view>
              </view>
            </template>
          </uni-list-item>
        </uni-list>
      </template>
      <template v-else>
        <h-empty className="pt-240" tipsWord="暂无员工数据" />
      </template>
      <view v-if="showDelete" class="h-200 w-100">
        <view class="w-100 h-68 flex align-center pt-20 pl-32 pr-48 box font-24">
          <view class="flex-1 bold">
            <text>已选</text>
            <text class="color-ff0000 ml-8 bold">{{ checkCheckedNum }}</text>
          </view>
          <view class="color-5a6f82 flex align-center">
            <view>全选</view>
            <h-checkbox :checked="checkAll" class="ml-16" @checkedChange="handleCheckedAll" />
          </view>
        </view>
        <view class="flex justify-center mt-36 font-32">
          <h-button width="296" height="72" text="取消" type="bg-fff color-333" @tap.stop="cancelDelete" />
          <h-button
            width="296"
            height="72"
            text="删除"
            type="bg-ff0000 color-fff"
            active="red"
            class="ml-32"
            @tap.stop="deleteProduct"
          />
        </view>
      </view>
    </view>
    <h-status-footer />
    <h-popup />
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { _get, _post, _delete } from '@/utils/common-request'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import { $store, $state } from '@/utils/common'
import { getRoleList } from '@/utils/rule'
import HEmpty from '@/components/h-empty.vue'
import DataNoticeArea from '@/pages-my-center/components/data-notice-area.vue'
import HooksPopup from '@/hooks/popup'
import HCheckBox from '@/components/h-checkbox.vue'
import HSwitch from '@/components/h-switch.vue'
const userInfo = computed(() => {
  return $state.user.userInfo
})
// 输入框模组
const searchInput = ref('')
function handleInput(e) {
  const str = e.detail.value
  getList(str)
}
function handleInputClear() {
  searchInput.value = ''
}
// 下拉框模组
const dropDownRef = ref(null)
const options = ref([
  {
    value: 'delete',
    icon: '/static/images/icon_del_32.svg',
    text: '删除'
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
    case 'delete':
      showDelete.value = true
      break

    default:
      break
  }
}
//删除选中模组
const showDelete = ref(false)
const checkAll = ref(false)
const checkCheckedNum = computed(() => {
  return dataList.value.filter((item: any) => item.checked).length
})
//取消删除
function cancelDelete() {
  showDelete.value = false
  handleCheckedAll(false)
}
const { popupOpen } = HooksPopup()
//删除
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
        _delete({ url: '/user/' + ids.toString() }).then((res: IResponseType<unknown>) => {
          if (res.msg.includes('无法删除')) {
            popupOpen(res.msg)
          }
          cancelDelete()
          getList()
        })
      } else if (res.cancel) {
      }
    }
  })
}
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

// 下拉列表模组
const dataList = ref([])
function getList(nickName = '') {
  !nickName && uni.showLoading({ title: '加载中' })
  _get({ url: '/user/list', data: { key: nickName } })
    .then((res: IResponseType<{ checked: boolean }>) => {
      dataList.value =
        res.rows?.map((v) => {
          v.checked = false
          return v
        }) ?? []
    })
    .finally(() => {
      !nickName && uni.hideLoading()
    })
}
function handleClickItem(item) {
  $store.commit('staff/updateStaff', item)
  uni.navigateTo({ url: `/pages-my-center/edit-userinfo?username=${item.userName}` })
}
function handleSwitchChange(item) {
  if (item.id === userInfo.value.id) {
    uni.showToast({ title: '用户不能操作自己', icon: 'none' })
    return
  }
  uni.showModal({
    title: '提示',
    content: '是否确认' + (item.status === '0' ? '停用' : '启用') + '该账户？',
    success: function (res) {
      if (res.confirm) {
        item.status = item.status === '0' ? '1' : '0'
        _post({ url: '/user/edit', data: item }).then((res) => {
          if (notSubmitDayNums.value && item.status === '1') {
            uni.navigateBack()
          }
        })
      } else if (res.cancel) {
      }
    }
  })
}

const notSubmitDayNums = ref('')

onLoad((options) => {
  searchInput.value = options.userName ?? ''
  notSubmitDayNums.value = options.notSubmitDayNums
  getList(options.userName || '')
  uni.$on('refreshStaffList', () => {
    getList(options.userName || '')
  })
})
onUnload(() => {
  uni.$off('refreshStaffList')
})
function handleBack() {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.manage-staff {
  .input-area {
    height: px2vw(64);
    background: #ebecee;
    box-shadow: 0 px2vw(1) 0 px2vw(1) rgba(255, 255, 255, 1), inset 0 px2vw(8) px2vw(12) px2vw(1) rgba(204, 209, 217, 1);
    border-radius: px2vw(16);
    padding: 0 px2vw(24) 0 px2vw(32);
  }
}
.btn-no-need {
  width: px2vw(128);
  height: px2vw(48);
  line-height: px2vw(48);
  text-align: center;
  background-color: #ffffff;
  border-radius: px2vw(24);
  color: #0066ff;
  font-size: px2vw(24);
  &:active {
    background-color: #0044e7;
    color: #fff;
  }
}
</style>
