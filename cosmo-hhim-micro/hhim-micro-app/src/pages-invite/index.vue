<template>
  <view class="w-100 h-full relative box bg-f3f3f5 flex flex-col" style="padding: 0 8px">
    <uni-nav-bar />
    <h-status-header title="邀请伙伴" />
    <view class="w-100 rounded-16 bg-fff pl-32 pr-32 box flex flex-col">
      <view class="h-96 flex align-center border-bottom-f5f5f5">
        <view class="font-28 color-5a6f82 w-104">角色</view>
        <view class="flex-1 font-28 flex pl-16">
          <view
            v-for="(item, index) in roleList.filter((v) => v.show)"
            :key="index"
            :class="{ 'ml-24': index > 0 }"
            class="flex align-center"
            @click="selectRole(item)"
          >
            <h-radio-box class="mr-16" :checked="dataModel.roleCode === item.roleCode" />
            <text>{{ item.roleName }}</text>
          </view>
        </view>
      </view>
      <view class="h-96 flex align-center" @tap.stop="showSelect = !showSelect">
        <view class="font-28 color-5a6f82 w-104">有效期</view>
        <view class="flex-1 font-28 flex pl-16">
          <view
            v-for="(item, index) in timeList"
            :key="index"
            :class="{ 'ml-36': index > 0 }"
            class="flex align-center"
            @click="confirmTime(item)"
          >
            <h-radio-box class="mr-16" :checked="dataModel.validTime === item.id" />
            <text>{{ item.time }}</text>
          </view>
        </view>
        <!-- <view class="flex-1 font-28 flex align-center pl-32">
          <text v-if="dataModel.time" class="flex-1 font-28">{{ dataModel.time }}</text>
          <text v-else class="flex-1 color-999 font-28">请选择有效期</text>
          <image src="/static/images/icon_input_arr.svg" class="icon-48" />
        </view> -->
        <!-- <view v-show="showSelect" class="relative pop-time w-full h-full" @tap.stop="showSelect = false">
          <view class="time-list bg-fff">
            <view
              v-for="(item, index) in timeList"
              :key="index"
              class="time-list-item w-100 font-28 bg-fff flex align-center justify-between pl-32 pr-32 box"
              @tap.stop="confirmTime(item)"
            >
              <view>{{ item.time }}</view>
              <image v-if="item.id === dataModel.validTime" src="/static/images/icon_checked.svg" class="icon-48" />
            </view>
          </view>
        </view> -->
      </view>
    </view>
    <scroll-view scroll-y class="w-100 flex-1 relative overflow-hidden mt-16">
      <view class="w-100 flex-1 overflow-hidden flex flex-col rounded-16 bg-fff px-32 box">
        <view class="h-104 flex align-center border-bottom-f5f5f5 relative">
          <image src="/pages-invite/static/images/icon_2qrcode_3d82ea.svg" class="icon-48" />
          <view class="font-32 color-333 bold ml-16">邀请二维码</view>
          <view class="flex-1" />
          <h-page-video :videoArr="[26]" page-name="邀请伙伴" class="mr-24" />
          <h-button
            height="48"
            width="128"
            text="生成海报"
            font="font-24"
            @tap="createPoster"
            type="border-1 border-0066ff color-0066ff bg-fff"
          />
        </view>
        <view class="h-544 flex w-100 justify-center align-center">
          <!-- 二维码未返回时（加载中/接口失败）不渲染，避免拼出 data:image/jpeg;base64,undefined 导致 ERR_INVALID_URL -->
          <image v-if="inviteResData.buffer" :src="`data:image/jpeg;base64,${inviteResData.buffer}`" class="icon-416" />
        </view>
        <view v-if="uuc" class="font-24 color-b6c0c9 text-center mb-48">微信扫码，立即体验</view>
        <view class="font-28 color-333 text-center bold">{{ inviteResData.tenantName }}</view>
        <view class="w-100 my-48 h-1 bg-f5f5f5"></view>
        <view class="mb-48 box flex flex-col justify-center align-center">
          <view class="flex align-center">
            <view class="mark-24a8ff">角色</view>
            <view class="color-24a8ff ml-8 font-28">{{ inviteResData.roleName }} </view>
          </view>
          <view class="flex justify-center align-center font-28 w-100 mt-32">
            <view class="color-5a6f82">邀请码：</view>
            <view class="w-120 color-333">{{ inviteResData.inviteCode }}</view>
            <image @tap="copy" src="/pages-invite/static/images/icon_copy.svg" class="icon-32 ml-12" />
          </view>
        </view>
      </view>
      <view class="w-100 font-24 color-b6c0c9 w-100 text-center mt-48 mb-48">可生成海报发送给伙伴，邀请其扫码加入</view>
    </scroll-view>
    <uni-popup ref="popup" type="center" :safe-area="false" class="w-full">
      <view class="w-full h-full flex justify-center align-center p-16 box">
        <view class="w-100 rounded-16 bg-fff pb-48">
          <view class="w-100 flex align-center justify-center font-28 color-5a6f82 bold h-96 relative">
            生成海报
            <image
              @click="closePopup"
              src="/static/images/icon_close_666.svg"
              class="icon-48 close"
              @tap="closePopup"
            />
          </view>
          <!-- #ifdef H5 -->
          <!-- H5：canvas 绘制完成后显示海报图（长按可保存，或点"下载海报"） -->
          <view class="w-100 border-dashed-F0F0F0 box mb-32" v-if="posterPreview">
            <image :src="posterPreview" mode="widthFix" class="w-100" />
          </view>
          <!-- #endif -->
          <!-- #ifndef H5 -->
          <view class="w-100 border-dashed-F0F0F0 box mb-32">
            <canvas class="w-100" id="firstCanvas" canvas-id="firstCanvas" :style="{ height: canvasH + 'px' }"></canvas>
          </view>
          <!-- #endif -->
          <view class="w-100 flex align-center justify-center">
            <!-- #ifdef H5 -->
            <h-button width="296" height="72" text="下载海报" @tap.stop="downloadH5" />
            <!-- #endif -->
            <!-- #ifndef H5 -->
            <h-button width="296" height="72" text="保存到相册" @tap.stop="download" />
            <!-- #endif -->
          </view>
          <!-- #ifdef H5 -->
          <view class="font-24 color-b6c0c9 mt-32 text-center w-100">长按图片可保存，或点击下载海报</view>
          <!-- #endif -->
          <!-- #ifndef H5 -->
          <view class="font-24 color-b6c0c9 mt-32 text-center w-100">保存后将图片发送给伙伴</view>
          <!-- #endif -->
        </view>
      </view>
    </uni-popup>
  </view>
</template>
<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue'
import { _post, _get } from '@/utils/common-request'
import { $store, $state, getEnvVersion, setGio, uuc } from '@/utils/common'
import { onLoad, onShow } from '@dcloudio/uni-app'
import HRadioBox from '@/components/h-radiobox.vue'
import HooksSettingConfig from '@/hooks/setting-config'
// #ifdef H5
// H5：海报背景图用 import 引入（vite 打包成正确资源 URL；
//     运行时直接使用 /pages-invite/static/... 路径会 404，导致背景缺失）
import PosterBgH5 from '@/pages-invite/static/images/poster_bg.jpg'
import PosterBgAppH5 from '@/pages-invite/static/images/poster_bg_app.jpg'
// #endif
// 海报背景图地址:H5 用 import 资源 URL(避免 dev 下按路径 404),
// 小程序/App 用 static 路径(import 的变量仅在 H5 编译分支存在)
// #ifdef H5
const posterBgSrc = uuc ? PosterBgAppH5 : PosterBgH5
// #endif
// #ifndef H5
const posterBgSrc = uuc ? '/pages-invite/static/images/poster_bg_app.jpg' : '/pages-invite/static/images/poster_bg.jpg'
// #endif
const userInfo = computed(() => {
  return $state.user.userInfo
})
const envVersion = computed(() => {
  // 小程序运行环境（release/develop/trial），由微信自动识别
  return getEnvVersion()
})
const { checkSubmitInspect } = HooksSettingConfig()
const showSelect = ref(false)
const dataModel = reactive({
  roleCode: '30',
  validTime: 60,
  time: '1小时'
})
const roleList = ref([])
//角色处理
function selectRole(item) {
  dataModel.roleCode = item.roleCode
  createCode()
}
// MANAGER("10", "企业管理员"),
// AUDITOR("20", "审产"),
// WORKER("30", "记工");
const avatarImg = ref('')
onShow(() => {
  setGio('jr_rivite')
  //检查是否开启质检开关，并后续处理赋值
  getNgTypeAnalysis()

  if (userInfo.value.avatar) {
    uni.getImageInfo({
      src: userInfo.value.avatar,
      success: function (image) {
        avatarImg.value = image.path
      }
    })
  }
})

onLoad((options) => {
  if (options.roleCode) {
    dataModel.roleCode = options.roleCode
  }
  // 预加载海报背景图/默认头像：小程序 canvas 首次 drawImage 本地/远程图片时，
  // 若图片未解码会画成空白（海报背景缺失），先 getImageInfo 缓存再绘制
  uni.getImageInfo({
    // 用 import 引入的资源 URL：页面级 static 目录在 H5 dev（vite）下不被按路径服务，硬编码路径会 404
    src: posterBgSrc,
    success: () => {},
    fail: () => {}
  })
  uni.getImageInfo({
    src: '/static/images/img_profile_default@2x.png',
    success: () => {},
    fail: () => {}
  })
})
//时间处理
const timeList = ref([
  // {
  //   time: '15分钟',
  //   id: 15
  // },
  // {
  //   time: '30分钟',
  //   id: 30
  // },
  {
    time: '1小时',
    id: 60
  },
  {
    time: '12小时',
    id: 720
  }
])
function confirmTime(item) {
  dataModel.validTime = item.id
  dataModel.time = item.time
  // showSelect.value = false
  createCode()
}
const inviteResData = reactive({})
function createCode() {
  let inviteData = { ...dataModel }
  inviteData.inviteToPage = 'pages/welcome/index'
  const env = uuc ? envVersion.value : getEnvVersion()
  inviteData.inviteToEnv = env === 'develop' ? 'trial' : env
  inviteData.checkPath = false
  delete inviteData.time
  uni.showLoading({
    title: '加载中'
  })
  _post({ url: '/login/wxminiapp/invitecode', data: inviteData }).then((res) => {
    uni.hideLoading()
    if (res.code === 200) {
      let data = res.data
      if (data.roleCode === '20') {
        data.roleName = '审产员'
      }
      if (data.roleCode === '25') {
        data.roleName = '质检员'
      }
      if (data.roleCode === '30') {
        data.roleName = '员工'
      }
      Object.assign(inviteResData, data)
      saveBase64Img()
    }
  })
}
//邀请码渲染
//图片格式转换（小程序码）
function saveBase64Img() {
  // #ifdef H5
  // H5：小程序码文件写入仅微信小程序可用（wx.env/getFileSystemManager），
  // H5 下海报生成已降级为提示（createPoster），无需 filePath
  return
  // #endif
  inviteResData.filePath = `${wx.env.USER_DATA_PATH}/hym_pay_qrcode_${new Date().getTime()}.png`
  uni.getFileSystemManager().writeFile({
    filePath: inviteResData.filePath,
    data: inviteResData.buffer,
    encoding: 'base64',
    success: (res) => {},
    fail: (err) => {
    }
  })
}
//复制
function copy() {
  uni.setClipboardData({
    data: inviteResData.inviteCode,
    success: function () {
    }
  })
}
const popup = ref(null)
//邀请码渲染
function createPoster() {
  // #ifdef H5
  // H5：用原生 canvas 绘制海报（背景/头像/文字/二维码）→ 生成图片显示，可长按保存或下载
  popup.value.open('center')
  drawPosterH5()
  return
  // #endif
  popup.value.open('center')
  setTimeout(() => {
    // 动态测量 canvas 实际渲染宽度：绘制坐标必须与画布尺寸一致，
    // 否则内容会被缩放/偏移（如二维码跑到左上角、右侧留白）
    uni.createSelectorQuery()
      .select('#firstCanvas')
      .boundingClientRect((res) => {
        if (res && res.width > 0) {
          screenWidth.value = res.width
          canvasH.value = res.width * 1.333
        }
      })
      .exec(() => {
        // 等 canvas 高度随 canvasH 更新后再绘制
        setTimeout(() => toDrawCanvas(inviteResData.filePath), 50)
      })
  }, 500)
}
function closePopup() {
  popup.value.close('center')
}

//绘制图形
// 海报绘制基准宽度 = 弹窗内 canvas 实际可用宽：
// 屏幕 - 弹窗内容左右 padding(16*2) - 虚线边框(2)。原用 屏幕-16 会大于 canvas 实际渲染宽，
// 导致右侧背景/二维码被裁剪缺失
const screenWidth = ref(uni.getWindowInfo().screenWidth - 34)
const canvasH = ref(screenWidth.value * 1.333)
function toDrawCanvas(codeImg) {
  // 这里是创建 canvas 绘图上下文
  const ctx = uni.createCanvasContext('firstCanvas')
  //大背景
  let imgurl = uuc ? '/pages-invite/static/images/poster_bg_app.jpg' : '/pages-invite/static/images/poster_bg.jpg'
  ctx.drawImage(imgurl, 0, 0, screenWidth.value, canvasH.value)
  //邀请人头像
  ctx.save()
  ctx.beginPath()
  ctx.arc(30, 30, 14, 0, 4 * Math.PI)
  ctx.clip()
  ctx.drawImage(userInfo.value.avatar ? avatarImg.value : '/static/images/img_profile_default@2x.png', 16, 16, 28, 28)
  ctx.restore()

  //角色信息
  //高度都加了文字的设计图高度计算的
  ctx.setFillStyle('#24a8ff')
  ctx.fillRect(16, (568 * canvasH.value) / 917, 28, 16)

  ctx.setFillStyle('#ffffff')
  ctx.setFontSize(12)
  ctx.fillText('角色', 18, (593 * canvasH.value) / 917)

  ctx.setFillStyle('#24a8ff')
  ctx.setFontSize(14)
  ctx.fillText(inviteResData.roleName || '', 48, (594 * canvasH.value) / 917)
  ////邀请码文字
  ctx.setFillStyle('#5A6F82')
  ctx.setFontSize(12)
  ctx.fillText('邀请码', screenWidth.value - 106, (592 * canvasH.value) / 917)

  ctx.setFillStyle('#EBF0F5')
  ctx.fillRect(screenWidth.value - 64, (568 * canvasH.value) / 917, 50, 16)

  ctx.setFillStyle('#5A6F82')
  ctx.setFontSize(12)
  ctx.fillText(inviteResData.inviteCode || '', screenWidth.value - 62, (593 * canvasH.value) / 917)

  ctx.setFillStyle('#5A6F82')
  ctx.setFontSize(12)
  ctx.fillText(inviteResData.inviteCodeExpireTime + '前有效', screenWidth.value - 163, (636 * canvasH.value) / 917)
  //邀请码图片
  ctx.drawImage(codeImg, screenWidth.value - 112, canvasH.value - 112, 96, 96)
  // 显示邀请人
  ctx.setFillStyle('#333333') //文字颜色
  ctx.setFontSize(14)
  ctx.font = 'normal bold 14px sans-serif'
  ctx.fillText(userInfo.value.nickName + ' 邀你体验-Ku易记', 52, 36, screenWidth.value - 68)
  //公司信息
  ctx.setFillStyle('#333333')
  ctx.setFontSize(16)
  ctx.font = 'normal bold 16px sans-serif'
  ctx.fillText(inviteResData.tenantName || '', 16, (540 * canvasH.value) / 917, screenWidth.value - 32)
  ctx.draw() //结束绘画
}
//先处理权限问题
function download() {
  uni.authorize({
    scope: 'scope.writePhotosAlbum',
    success() {
      //已授权，直接保存图片
      saveImg()
    },
    fail() {
      //拒绝授权，提示用户去权限设置里授权
      uni.showModal({
        title: '提示',
        content: '您拒绝了存储邀请码的授权，去开启',
        showCancel: false,
        success: function (res) {
          //打开设置
          uni.openSetting({
            success(res) {
              let userWrite = res.authSetting['scope.writePhotosAlbum']
              if (userWrite) {
                // 继续进行授权成功后的操作
                //用户保存图片
                saveImg()
              } else {
                // 用户拒接授权 给提示
                uni.showToast({
                  title: '您已拒绝存储邀请码授权,保存失败',
                  icon: 'none'
                })
              }
            }
          })
        }
      })
    }
  })
}
//保存图片
function saveImg() {
  uni.canvasToTempFilePath({
    x: 0,
    y: 0,
    width: screenWidth.value,
    height: canvasH.value,
    destWidth: screenWidth.value * 3,
    destHeight: canvasH.value * 3,
    canvasId: 'firstCanvas',
    quality: 0.3,
    success: function (res) {
      // 在H5平台下，tempFilePath 为 base64
      uni.saveImageToPhotosAlbum({
        filePath: res.tempFilePath,
        success: (res) => {
          uni.showToast({
            icon: 'none',
            title: '保存成功'
          })
        },
        fail() {
          uni.showToast({
            icon: 'none',
            title: '保存失败'
          })
        }
      })
    }
  })
}
// #ifdef H5
// H5 生成海报：原生 canvas 绘制（背景/头像/文字/二维码）→ base64 图片（长按保存/下载）
const posterPreview = ref('')
function drawPosterH5() {
  posterPreview.value = ''
  const w = screenWidth.value
  const h = canvasH.value
  const canvas = document.createElement('canvas')
  canvas.width = w
  canvas.height = h
  const ctx = canvas.getContext('2d')
  const finish = () => {
    posterPreview.value = canvas.toDataURL('image/png')
  }
  // 背景（H5 用 import 引入的资源 URL，避免运行时路径 404）
  const bg = new Image()
  bg.src = posterBgSrc
  bg.onload = () => {
    ctx.drawImage(bg, 0, 0, w, h)
    drawAvatar()
  }
  bg.onerror = () => drawAvatar()
  // 邀请人头像（远程图跨域会污染 canvas，失败回退默认头像）
  function drawAvatar() {
    const avatar = new Image()
    avatar.crossOrigin = 'anonymous'
    avatar.src = userInfo.value.avatar || '/static/images/img_profile_default@2x.png'
    avatar.onload = () => {
      ctx.save()
      ctx.beginPath()
      ctx.arc(30, 30, 14, 0, 4 * Math.PI)
      ctx.clip()
      ctx.drawImage(avatar, 16, 16, 28, 28)
      ctx.restore()
      drawTexts()
    }
    avatar.onerror = () => drawTexts()
  }
  // 文字（坐标与小程序版一致：设计稿 917 高度等比缩放）
  function drawTexts() {
    ctx.fillStyle = '#24a8ff'
    ctx.fillRect(16, (568 * h) / 917, 28, 16)
    ctx.fillStyle = '#ffffff'
    ctx.font = '12px sans-serif'
    ctx.fillText('角色', 18, (593 * h) / 917)
    ctx.fillStyle = '#24a8ff'
    ctx.font = '14px sans-serif'
    ctx.fillText(inviteResData.roleName || '', 48, (594 * h) / 917)
    ctx.fillStyle = '#5A6F82'
    ctx.font = '12px sans-serif'
    ctx.fillText('邀请码', w - 106, (592 * h) / 917)
    ctx.fillStyle = '#EBF0F5'
    ctx.fillRect(w - 64, (568 * h) / 917, 50, 16)
    ctx.fillStyle = '#5A6F82'
    ctx.fillText(inviteResData.inviteCode || '', w - 62, (593 * h) / 917)
    ctx.fillText((inviteResData.inviteCodeExpireTime || '') + '前有效', w - 163, (636 * h) / 917)
    ctx.fillStyle = '#333333'
    ctx.font = 'bold 14px sans-serif'
    ctx.fillText((userInfo.value.nickName || '') + ' 邀你体验-Ku易记', 52, 36, w - 68)
    ctx.fillStyle = '#333333'
    ctx.font = 'bold 16px sans-serif'
    ctx.fillText(inviteResData.tenantName || '', 16, (540 * h) / 917, w - 32)
    drawCode()
  }
  // 二维码（后端返回 base64）
  function drawCode() {
    const codeImg = inviteResData.buffer ? 'data:image/jpeg;base64,' + inviteResData.buffer : ''
    if (!codeImg) {
      finish()
      return
    }
    const code = new Image()
    code.onload = () => {
      ctx.drawImage(code, w - 112, h - 112, 96, 96)
      finish()
    }
    code.onerror = () => finish()
    code.src = codeImg
  }
}
function downloadH5() {
  if (!posterPreview.value) {
    uni.showToast({ title: '海报生成中，请稍候', icon: 'none' })
    return
  }
  const a = document.createElement('a')
  a.href = posterPreview.value
  a.download = '邀请海报.png'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}
// #endif

//检查是否开启质检
function getNgTypeAnalysis() {
  _get({
    url: `/setting/getIndividuationConfig`,
    data: {}
  }).then((res) => {
    // 防御：后端可能在租户无配置记录时返回 data 为 null/undefined
    const submitInspectSwitch = res?.data?.submitInspectSwitch ?? '1'
    $store.commit('setting/setSubmitInspectSwitch', submitInspectSwitch)

    if (!checkSubmitInspect.value) {
      //质检开关关闭时，角色默认员工
      if (dataModel.roleCode === '25') {
        dataModel.roleCode = '30'
      }
    }
    assignmentVal()
  }).catch(() => {
    // 即使配置接口异常，页面核心功能（二维码/邀请码）仍需正常初始化
    assignmentVal()
  })
}
function assignmentVal() {
  if (userInfo.value.roleCode === '30') {
    //员工角色
    roleList.value = [
      {
        roleCode: '30',
        roleName: '员工',
        show: true
      }
    ]
  } else {
    roleList.value = [
      {
        roleCode: '20',
        roleName: '审产员',
        show: true
      },
      {
        roleCode: '25',
        roleName: '质检员',
        show: checkSubmitInspect.value
      },
      {
        roleCode: '30',
        roleName: '员工',
        show: true
      }
    ]
  }
  createCode()
}
</script>

<style lang="scss" scoped>
.border-dashed-F0F0F0 {
  border-top: px2vw(2) dashed #f0f0f0;
  border-bottom: px2vw(2) dashed #f0f0f0;
}
.close {
  position: absolute;
  top: px2vw(24);
  right: px2vw(24);
}
.h-1 {
  height: px2vw(2);
}
.w-120 {
  width: px2vw(120);
}
.pop-time {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 2;
  background-color: transparent;
}
.time-list {
  width: calc(100% - 100px);
  position: absolute;
  top: px2vw(360);
  left: px2vw(184);
  z-index: 999;
  box-shadow: 0 px2vw(8) px2vw(16) px2vw(1) rgba(182, 192, 201, 0.5);
  .time-list-item {
    height: px2vw(96);
    line-height: px2vw(96);
    border-bottom: px2vw(1) solid #f5f5f5;
    &:active {
      background-color: #f3f3f5;
    }
  }
}
.create {
  width: px2vw(296);
  height: px2vw(88);
  background: linear-gradient(180deg, #89b4f5 0%, #0066ff 100%);
  box-shadow: px2vw(8) px2vw(8) px2vw(168) px2vw(1) rgba(0, 0, 0, 0.1);
  border-radius: px2vw(44);
  opacity: 1;
}

.h-104 {
  height: px2vw(104);
}

.h-544 {
  height: px2vw(544);
}

.h-208 {
  height: px2vw(208);
}

.pl-136 {
  padding-left: px2vw(136);
}

.jiaose {
  width: px2vw(56);
  height: px2vw(32);
  line-height: px2vw(32);
  background: #64b5ea;
}

.jiaosename {
  // width: px2vw(100);
  height: px2vw(48);
  line-height: px2vw(48);
  background: #def2ff;
}

.icon-416 {
  width: px2vw(416);
  height: px2vw(416);
}

.text1 {
  width: px2vw(144);
  height: px2vw(48);
  line-height: px2vw(48);
  background: #ffffff;
  border-radius: px2vw(32);
  opacity: 1;
  border: px2vw(2) solid #0066ff;
  &:active {
    background-color: #0066ff;
    color: #ffffff;
  }
}

.download {
  width: px2vw(296);
  height: px2vw(88);
  box-shadow: px2vw(8) px2vw(8) px2vw(16) px2vw(1) rgba(0, 0, 0, 0.1);
  border-radius: px2vw(44);
  background: linear-gradient(180deg, #f5f7fa 0%, #ffffff 100%);
}
</style>
