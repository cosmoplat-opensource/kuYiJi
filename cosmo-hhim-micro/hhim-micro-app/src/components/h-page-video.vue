<template>
  <view class="icon-64">
    <img :src="formatImage('icon_buoy_video')" class="icon-64 icon-video" @tap="handleShowVideo" />
    <!-- #ifndef H5 -->
    <h-popup-dialog ref="refPopupDialog" width="624" :title="videoItem.name || '视频'" class="zzz" isMaskClick :animation="false">
      <template #default>
        <view class="py-32 w-100 h-852 box">
          <video
            v-if="videoShow"
            :key="videoCurrent"
            id="myVideo"
            object-fit="contain"
            @ended="handleEnded"
            @timeupdate="handleUpdate"
            @error="handleVideoError"
            play-btn-position="center"
            class="video"
            controls
            autoplay
            :data-name="videoItem.video"
            :src="videoSrc"
          />
        </view>
      </template>
      <template #footer>
        <view class="flex-center flex-1">
          <h-button
            width="120"
            height="64"
            type="bg-f3f3f5 color-333"
            active="grey"
            text="关闭"
            @tap="handleDialogClose"
          />
          <template v-if="videoArr.length > 1">
            <view class="flex-1 ml-32 flex align-center gap-16">
              <view
                v-for="(item, index) in videoArr"
                :key="item"
                class="icon-16 rounded-full"
                :class="index === videoCurrent ? 'bg-0066ff' : 'bg-e5e5e5'"
              />
            </view>
            <view class="flex gap-48">
              <view class="icon-64 bg-0066ff rounded-full flex-center" @tap="handleCurrentChange(-1)">
                <img :src="formatImage('icon_pre', 'svg')" class="icon-32" />
              </view>
              <view class="icon-64 bg-0066ff rounded-full flex-center" @tap="handleCurrentChange(1)">
                <img :src="formatImage('icon_pre', 'svg')" class="icon-32 rotate-y-180" />
              </view>
            </view>
          </template>
        </view>
      </template>
    </h-popup-dialog>
    <!-- #endif -->
    <!-- #ifdef H5 -->
    <!-- H5：uni-popup/uni-transition 的动画层在 H5 下 slot 内容变化时可能卡在初始态 opacity:0
         导致弹窗内容不可见。这里 H5 完全绕开 uni-popup，用普通 view 自定义弹窗
         （fixed + mask + card，无任何动画层），视频用 JS 动态创建原生 video DOM，
         浏览器原生控制条，播放稳定可靠 -->
    <view v-if="videoShow" class="video-popup-h5">
      <view class="video-popup-h5__mask" @click="handleDialogClose"></view>
      <view class="video-popup-h5__card">
        <view class="video-popup-h5__title">{{ videoItem.name || '视频' }}</view>
        <view ref="videoWrap" class="video-popup-h5__area"></view>
        <view class="video-popup-h5__footer">
          <view class="video-popup-h5__btn" @click="handleDialogClose">关闭</view>
          <template v-if="videoArr.length > 1">
            <view class="video-popup-h5__dots">
              <view
                v-for="(item, index) in videoArr"
                :key="item"
                class="video-popup-h5__dot"
                :class="{ 'video-popup-h5__dot--active': index === videoCurrent }"
              />
            </view>
            <view class="video-popup-h5__nav">
              <view class="video-popup-h5__navbtn" @click="handleCurrentChange(-1)">
                <img :src="formatImage('icon_pre', 'svg')" class="video-popup-h5__arrow" />
              </view>
              <view class="video-popup-h5__navbtn" @click="handleCurrentChange(1)">
                <img :src="formatImage('icon_pre', 'svg')" class="video-popup-h5__arrow rotate-y-180" />
              </view>
            </view>
          </template>
        </view>
      </view>
    </view>
    <!-- #endif -->
  </view>
</template>

<script setup lang="ts">
import { collectVideo, formatImage, formatVideo } from '@/utils/common'
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import HPopupDialog from '@/components/h-popup-dialog.vue'
import { techVideoList } from '@/utils/formatData'

const props = defineProps({
  videoArr: {
    type: Array<number>,
    required: true,
    default: () => {}
  },
  pageName: {
    type: String,
    default: ''
  }
})
// open/close 事件:供页面在弹窗打开时隐藏原生组件(如图表 canvas),避免原生组件盖住视频弹窗
const emit = defineEmits(['open', 'close'])
const videoList: IVideoType[] = techVideoList
const videoCurrent = ref<number>(0)
const videoItem = computed((): IVideoType => {
  return videoList.find((v) => v.index === props.videoArr[videoCurrent.value]) || ({} as IVideoType)
})
// 视频 URL：CDN 视频文件名含中文/顿号等特殊字符，未编码的 URL 会被 CDN 拒绝（400）导致播放器加载失败。
// 这里仅对非 ASCII/特殊字符做百分号编码，保留 : / . % 等结构字符，不会二次编码已编码部分。
const videoSrc = computed(() => formatVideo(videoItem.value.video))
const refPopupDialog = ref(null)
const videoShow = ref(false)
function handleShowVideo() {
  // #ifdef H5
  // H5：自定义 view 弹窗（无 uni-popup 动画层），立即显示并渲染原生 video
  videoShow.value = true
  emit('open')
  renderNativeVideo()
  return
  // #endif
  // 小程序/App：先打开弹窗，再渲染 video。
  // 400ms 等 uni-popup 300ms 动画完全结束后再创建 video：动画期间创建的 video 在 Chrome 下
  // 合成层不更新画面（黑屏但播放正常），延迟创建可规避
  refPopupDialog.value.open()
  emit('open')
  setTimeout(() => {
    videoShow.value = true
  }, 400)
}
// #ifdef H5
// H5：uni-app 编译器会把模板 <video> 强制转成 uni-video 组件（此版本播放黑屏），
// 这里用 JS 动态创建原生 video DOM：controls + muted + autoplay（浏览器原生控制条，播放稳定可靠）
const videoWrap = ref(null)
function renderNativeVideo() {
  nextTick(() => {
    const wrap = videoWrap.value
    if (!wrap) {
      console.warn('[h-page-video] video wrap not ready')
      return
    }
    // uni-app H5 的 <view> 是原生元素（ref 直接是 DOM），兼容组件实例（$el）两种情况
    const el = wrap.$el || wrap
    el.innerHTML = ''
    const v = document.createElement('video')
    v.src = videoSrc.value
    v.controls = true
    v.autoplay = true
    v.muted = true
    v.playsInline = true
    v.setAttribute('webkit-playsinline', 'true')
    v.style.cssText =
      'width:100%;height:100%;object-fit:contain;background:#000;transform:translateZ(0);will-change:transform;'
    v.dataset.name = videoItem.value.video || ''
    v.addEventListener('ended', handleEnded)
    v.addEventListener('timeupdate', handleUpdate)
    v.addEventListener('error', handleVideoError)
    // H5 诊断：确认视频元数据/解码/播放状态（videoWidth=0 说明无视频轨或解码失败 → 黑屏）
    v.addEventListener('loadedmetadata', () => {
    })
    v.addEventListener('playing', () => {
    })
    v.addEventListener('stalled', () => {
    })
    el.appendChild(v)
    // 强制 reflow，确保 video 合成层立即刷新（规避 Chrome 合成层延迟导致的黑屏）
    void v.getBoundingClientRect()
  })
}
watch(videoCurrent, () => {
  renderNativeVideo()
})
// #endif
function handleVideoError(e) {
  console.warn('[h-page-video] 视频加载失败', e?.detail || e)
  uni.showToast({ title: '视频加载失败，请稍后重试', icon: 'none' })
}
function handleDialogClose() {
  for (const src in reportMaps.value) {
    const { playing, duration } = reportMaps.value[src]
    collectVideo(src, playing, duration, '指导视频', props.pageName)
  }
  videoShow.value = false
  emit('close')
  // #ifndef H5
  refPopupDialog.value.close()
  // #endif
}

const reportMaps = ref({})
function handleEnded() {
  handleCurrentChange(1)
}
function handleUpdate(e) {
  // 兼容两种事件：uni-app video 用 e.detail，原生 video 用 e.target
  const el = e.target || {}
  const src = (el.dataset && el.dataset.name) || ''
  const duration = e.detail && e.detail.duration != null ? e.detail.duration : el.duration || 0
  const currentTime = e.detail && e.detail.currentTime != null ? e.detail.currentTime : el.currentTime || 0
  reportMaps.value[src] = { duration, playing: currentTime }
}
function handleCurrentChange(num) {
  const total = videoCurrent.value + num
  if (total >= 0 && total < props.videoArr.length) {
    videoCurrent.value = total
  }
}
</script>
<style lang="scss">
.video {
  width: 100%;
  height: 100%;
}
.h-852 {
  height: px2vw(852);
}
.rotate-y-180 {
  transform: rotateY(180deg);
}
@keyframes icon-video-animation {
  0% {
    transform: scale(1);
  }
  20% {
    transform: scale(1.2);
  }
  40% {
    transform: scale(0.8);
  }
  60% {
    transform: scale(1.2);
  }
  80% {
    transform: scale(0.8);
  }
  100% {
    transform: scale(1);
  }
}
.icon-video {
  animation: icon-video-animation 1.5s;
}
/* #ifdef H5 */
/* ===== H5 自定义视频弹窗（绕开 uni-popup/uni-transition，无动画层，杜绝内容消失问题） ===== */
.video-popup-h5 {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;

  &__mask {
    position: absolute;
    left: 0;
    top: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.4);
  }

  &__card {
    position: relative;
    display: flex;
    flex-direction: column;
    width: 86.67vw; /* 624/720 设计稿比例 */
    max-height: 92vh;
    background-color: #fff;
    border-radius: px2vw(16);
    padding: px2vw(32);
    box-sizing: border-box;
  }

  &__title {
    font-size: px2vw(28);
    color: #5a6f82;
    text-align: center;
    padding-bottom: px2vw(16);
  }

  &__area {
    flex: 1;
    min-height: 0;
    width: 100%;
    background-color: #000;
    border-radius: px2vw(8);
    overflow: hidden;
  }

  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: px2vw(24);
  }

  &__btn {
    font-size: px2vw(28);
    color: #333;
    padding: px2vw(8) px2vw(24);
    background-color: #f3f3f5;
    border-radius: px2vw(8);
  }

  &__dots {
    display: flex;
    align-items: center;
    gap: px2vw(16);
  }

  &__dot {
    width: px2vw(16);
    height: px2vw(16);
    border-radius: 50%;
    background-color: #e5e5e5;

    &--active {
      background-color: #0066ff;
    }
  }

  &__nav {
    display: flex;
    gap: px2vw(48);
  }

  &__navbtn {
    width: px2vw(64);
    height: px2vw(64);
    border-radius: 50%;
    background-color: #0066ff;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__arrow {
    width: px2vw(32);
    height: px2vw(32);
  }
}
/* #endif */
</style>
