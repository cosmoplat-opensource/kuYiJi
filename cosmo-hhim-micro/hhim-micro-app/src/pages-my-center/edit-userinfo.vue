<template>
  <view class="bg-F3F3F5 h-full flex flex-col">
    <uni-nav-bar />
    <h-status-header :title="checkSelf ? '编辑个人信息' : '编辑员工信息'" />
    <scroll-view scroll-y class="px-16 box overflow-hidden flex-1">
      <view class="px-32 pb-32 box bg-fff rounded-16">
        <!--头像-->
        <view
          class="flex flex-col align-center justify-center border-bottom-f5f5f5 pt-48 pb-32 relative"
          @tap="onChooseImage"
        >
          <image :src="submitItem.avatar" class="icon-128 mb-24 rounded-full" />
          <text class="color-5a6f82 font-28">修改头像</text>
        </view>
        <!--账号-->
        <view class="border-bottom-f5f5f5 h-96 flex align-center">
          <view class="w-176 color-5a6f82 font-28">账号</view>
          <view class="color-5a6f82 font-28">{{ submitItem.userName }}</view>
        </view>
        <!--昵称-->
        <view class="border-bottom-f5f5f5 h-96 flex align-center">
          <view class="w-176 color-5a6f82 font-28">昵称</view>
          <view class="flex-1 flex align-center h-80">
            <input
              v-model="submitItem.nickName"
              class="flex-1"
              placeholder="请输入昵称"
              maxlength="20"
              placeholder-class="font-28 color-b8b8b8"
            />
            <image
              v-if="submitItem.nickName"
              src="/static/images/icon_del_b6c0c9.svg"
              class="icon-48"
              @tap="handleSubmitClear('nickName')"
            />
          </view>
        </view>
        <!--性别-->
        <view class="border-bottom-f5f5f5 h-96 flex align-center">
          <view class="w-176 color-5a6f82 font-28">性别</view>
          <view class="flex-1 font-28 flex">
            <view class="flex align-center" @tap="handleSexChange('0')">
              <h-radio-box class="mr-24" :checked="submitItem.sex !== '1'" />
              <text>男</text>
            </view>
            <view class="flex align-center ml-48" @tap="handleSexChange('1')">
              <h-radio-box class="mr-24" :checked="submitItem.sex === '1'" />
              <text>女</text>
            </view>
          </view>
        </view>
        <!--手机号-->
        <view class="border-bottom-f5f5f5 h-96 flex align-center">
          <view class="w-176 color-5a6f82 font-28">手机号</view>
          <view class="color-5a6f82 font-28">{{ submitItem.phonenumber }}</view>
        </view>
        <!--账号有效期-->
        <view class="border-bottom-f5f5f5 h-96 flex align-center">
          <view class="w-176 color-5a6f82 font-28">账号有效期</view>
          <view class="flex-1">{{ formatDate(submitItem.validDate, 'YYYY-MM-DD') }}</view>
        </view>
        <!--角色-->
        <view class="border-bottom-f5f5f5 h-96 flex align-center">
          <view class="w-176 color-5a6f82 font-28">角色</view>
          <view class="flex-1 font-28" v-if="checkSelf || checkAdmin">{{ getRoleList(submitItem.roleCode) }}</view>
          <view class="flex-1 font-28 flex" v-else>
            <view class="flex align-center" @tap="handleRoleChange('20')">
              <h-radio-box class="mr-24" :checked="submitItem.roleCode === '20'" />
              <text>审产员</text>
            </view>
            <view class="flex align-center ml-16" @tap="handleRoleChange('25')">
              <h-radio-box class="mr-24" :checked="submitItem.roleCode === '25'" />
              <text>质检员</text>
            </view>
            <view class="flex align-center ml-16" @tap="handleRoleChange('30')">
              <h-radio-box class="mr-24" :checked="submitItem.roleCode === '30'" />
              <text>员工</text>
            </view>
          </view>
        </view>
        <!--备注-->
        <view class="pt-40 flex flex-col">
          <view class="color-5a6f82 font-28">备注</view>
          <h-textarea class="mt-16" v-model="submitItem.remark" :maxlength="30" placeholder="输入备注" />
        </view>
      </view>
    </scroll-view>
    <view class="flex align-end py-32 justify-center">
      <h-button width="296" height="72" text="取消" type="bg-fff color-333" @tap.stop="cancel" />
      <h-button width="296" height="72" text="保存" class="ml-32" @tap.stop="submit" />
    </view>
    <h-status-footer />
    <h-cropper
      mode="free"
      :width="200"
      :height="200"
      :maxWidth="200"
      :maxHeight="200"
      :url="cropperUrl"
      @cancel="oncancel"
      @ok="onok"
    />
  </view>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, reactive, ref } from 'vue'
import { _get, _post, _put } from '@/utils/common-request'
import { onLoad } from '@dcloudio/uni-app'
import { $state, $store, getUserInfo, formatDate } from '@/utils/common'
import HRadioBox from '@/components/h-radiobox.vue'
import { getRoleList } from '@/utils/rule'
import HooksUploadImage from '@/hooks/upload-image'
import HTextarea from '@/components/h-textarea.vue'

const submitItem: Partial<IUserInfo> = reactive({})
onLoad((options) => {
  getUserInfo(options.username).then((res: IUserInfo) => {
    res.sex = res.sex || '0'
    res.avatar = res.avatar || '/static/images/img_profile_default@2x.png'
    Object.assign(submitItem, res)
  })
})

const checkSelf = computed(() => {
  return submitItem.userName === $state.user.userInfo.userName
})

const checkAdmin = computed(() => {
  return submitItem.roleCode === '10'
})

function handleSubmitClear(key) {
  submitItem[key] = ''
}
function handleSexChange(sex) {
  submitItem.sex = sex
}
function handleRoleChange(role) {
  if (role === '25') {
    _get({
      url: `/setting/getIndividuationConfig`,
      data: {}
    }).then((res) => {
      if (res?.data.submitInspectSwitch === '1') {
        //关闭 提示
        uni.showToast({ title: '如需使用质检功能，请联系企业管理员启用送检！', icon: 'none' })
      } else {
        submitItem.roleCode = role
      }
    })
  } else {
    submitItem.roleCode = role
  }
}

// 上传图片模块
const { choseImage, uploadImageList, uploadImage, setImageList } = HooksUploadImage(1)

async function submit() {
  if (!submitItem.roleCode) {
    uni.showToast({ title: '请选择角色', icon: 'none' })
    return
  }
  if (!submitItem.nickName) {
    uni.showToast({ title: '昵称不能为空', icon: 'none' })
    return
  }
  uni.showLoading({ title: '保存中' })
  const imageRes = await uploadImage()
  if (imageRes) {
    submitItem.avatar = imageRes[0]
  }
  _post({ url: '/user/edit', data: submitItem })
    .then((res) => {
      if (checkSelf.value) {
        $store.commit('user/setUserInfo', {
          phonenumber: submitItem.phonenumber,
          nickName: submitItem.nickName,
          roleCode: submitItem.roleCode,
          avatar: submitItem.avatar
        })
      }
      uni.$emit('refreshStaffList')
      uni.navigateBack()
    })
    .finally(() => {
      uni.hideLoading()
    })
}
function cancel() {
  uni.navigateBack({
    delta: 1
  })
}

function onChooseImage() {
  choseImage().then((res) => {
    // #ifdef H5
    // H5：裁剪组件依赖 canvas 导出（App/小程序可用），浏览器端跳过裁剪直接使用原图作为头像
    setImageList([res[0]])
    submitItem.avatar = res[0]
    return
    // #endif
    cropperUrl.value = res[0]
  })
}
const cropperUrl = ref('')
async function onok(ev) {
  cropperUrl.value = ''
  setImageList([ev.path])
  submitItem.avatar = ev.path
}
function oncancel() {
  // url设置为空，隐藏控件
  cropperUrl.value = ''
}
</script>

<style lang="scss">
.uni-date {
  width: auto !important;
  flex: unset !important;
}
</style>
<style lang="scss" scoped>
.bottom-btn {
  height: px2vw(152);
}
.selected-class {
  width: px2vw(28);
  height: px2vw(28);
  background: linear-gradient(144deg, #89b4f5 0%, #629bf0 100%);
  border-radius: 50%;
  box-shadow: px2vw(8) px2vw(8) px2vw(16) px2vw(1) rgba(0, 0, 0, 0.1);
}
.noselected-class {
  width: px2vw(28);
  height: px2vw(28);
  background: linear-gradient(144deg, #f5f7fa 0%, #ffffff 100%);
  box-shadow: px2vw(8) px2vw(8) px2vw(16) px2vw(1) rgba(0, 0, 0, 0.1);
  border-radius: 50%;
}
.h-260 {
  height: px2vw(260);
}

.remark {
  width: 100%;
  box-sizing: border-box;
  min-height: px2vw(144);
  border: px2vw(2) solid #f5f5f5;
  padding: px2vw(24) px2vw(16);
}
.avatar-click {
  position: absolute;
  left: 0;
  top: 0;
  z-index: 2;
  width: 100%;
  height: 100%;
  opacity: 0;
}
</style>
