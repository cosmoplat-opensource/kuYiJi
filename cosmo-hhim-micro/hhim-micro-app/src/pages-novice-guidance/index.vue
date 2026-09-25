<template>
  <view class="h-full flex flex-col relative bg-f3f3f5">
    <view class="flex-1">
      <swiper class="swiper" vertical @change="handleSwiperChange" :current="swiperIndex">
        <swiper-item v-for="(item, index) in videos" :key="index">
          <video
            :id="`video-${index}`"
            class="video"
            :autoplay="index === 0"
            controls
            :data-name="item.video"
            :src="formatVideo(item.video)"
            @tap="handleVideoClick"
            object-fit="contain"
            @ended="handleEnded"
            @timeupdate="handleUpdate"
          />
        </swiper-item>
      </swiper>
    </view>
    <template v-if="controlShow">
      <view class="top-area">
        <uni-nav-bar background-color="#00112A" />
        <view class="p-16 bg-00112a flex align-center">
          <img :src="formatImage('icon_return_fff', 'svg')" class="icon-48" @tap="handleBack" />
          <view class="font-32 color-fff bold flex-1 text-center">{{ pageTitle }}</view>
          <view class="icon-48" />
        </view>
      </view>
      <!--右上角抽屉按钮-->
      <view class="btn-menu flex-center">
        <img :src="formatImage('icon_directory', 'svg')" class="icon-48" @tap="handleDrawerShow" />
      </view>
      <!--左下角视频名称-->
      <view class="video-name">{{ activeName }}</view>
    </template>
    <uni-drawer ref="refShowRight" mode="right" @change="handleDrawerChange" :width="576">
      <view class="drawer-wrapper overflow-auto">
        <template v-for="(parent, pIndex) in menuArr" :key="pIndex">
          <view class="flex align-center py-24 overflow-hidden px-48" @tap="toggleMenuShow(pIndex)">
            <view class="parent__mark">{{ pIndex + 1 }}</view>
            <view class="ml-16 font-32 bold parent__name flex-1">{{ parent.label }}</view>
            <image
              src="/static/images/icon_unfold.svg"
              class="icon-32 ml-32"
              :class="{ 'icon-rotate-y': menuActive === pIndex }"
            />
          </view>
          <template v-if="menuActive === pIndex">
            <view
              v-for="(child, cIndex) in parent.children"
              :key="cIndex"
              class="color-fff font-28 bold px-48 child-wrapper"
              :class="{ 'mb-24': cIndex === parent.children.length - 1 }"
              @tap="handleVideoSelect(child.video)"
            >
              <view class="child-border py-24">{{ videos[child.video].name }}</view>
            </view>
          </template>
        </template>
      </view>
    </uni-drawer>
  </view>
</template>

<script setup lang="ts">
import { collectVideo, formatImage, formatVideo } from '@/utils/common'
import { ref, watch } from 'vue'
import UniNavBar from '@/uni_modules/uni-nav-bar/components/uni-nav-bar/uni-nav-bar.vue'
import { onLoad } from '@dcloudio/uni-app'
import { techVideoList } from '@/utils/formatData'

const pageTitle = ref('帮助中心')
const controlShow = ref(true)

const menuActive = ref(0)
const videos = techVideoList
const menuArr = ref([
  {
    label: '记工',
    children: [{ video: 0 }, { video: 1 }, { video: 2 }, { video: 3 }, { video: 4 }, { video: 5 }]
  },
  {
    label: '审核',
    children: [{ video: 6 }, { video: 7 }]
  },
  {
    label: '车间库存',
    children: [{ video: 8 }, { video: 9 }]
  },
  {
    label: '产品完工',
    children: [
      { video: 10 }
      // { name: '产成品库存在哪可以进行管理？', video: '' }
    ]
  },
  {
    label: '计件结算',
    children: [{ video: 11 }, { video: 12 }]
  },
  {
    label: '质量管理',
    children: [
      { video: 13 },
      { video: 14 },
      { video: 15 },
      { video: 16 },
      { video: 17 },
      { video: 18 },
      { video: 19 },
      { video: 20 }
    ]
  },
  {
    label: '产品和工艺',
    children: [{ video: 21 }, { video: 22 }]
  },
  {
    label: '其他',
    children: [{ video: 23 }, { video: 24 }]
  }
])
const activeName = ref(videos[0].name)

onLoad((options) => {
  pageTitle.value = options?.title || '帮助中心'
  if (options.videoIndex) {
    swiperIndex.value = Number(options.videoIndex)
  }
})

// 监听浮动区域显示的时候触发3秒后隐藏
const timer = ref(null)
watch(
  () => controlShow.value,
  (val) => {
    if (val) {
      timer.value && clearTimeout(timer.value)
      timer.value = setTimeout(() => (controlShow.value = false), 3000)
    }
  },
  { immediate: true, deep: true }
)

// 控制抽屉
const refShowRight = ref(null)
function handleDrawerShow() {
  controlShow.value = false
  refShowRight.value.open()
}
function handleDrawerChange(val) {
  !val && (controlShow.value = true)
}

function toggleMenuShow(index) {
  if (menuActive.value === index) {
    menuActive.value = -1
  } else {
    menuActive.value = index
  }
}

function handleVideoSelect(index) {
  swiperIndex.value = index
  activeName.value = videos[index].name
  refShowRight.value.close()
}

function handleVideoClick() {
  controlShow.value = true
}

function handleBack() {
  for (const src in reportMaps.value) {
    const { playing, duration } = reportMaps.value[src]
    collectVideo(src, playing, duration, '新手引导页', '工作台')
  }
  uni.navigateBack()
}

const swiperIndex = ref(0)
const swiperLastIndex = ref(0)
function handleSwiperChange(e) {
  const current = e.detail.current
  uni.createVideoContext(`video-${swiperLastIndex.value}`)?.stop()
  uni.createVideoContext(`video-${current}`).play()
  swiperLastIndex.value = current
  activeName.value = videos[current].name
}

function handleEnded() {
  if (swiperIndex.value < videos.length - 1) {
    swiperIndex.value++
  }
}
const reportMaps = ref({})
function handleUpdate(e) {
  reportMaps.value[e.target.dataset.name] = { duration: e.detail.duration, playing: e.detail.currentTime }
}
</script>

<style scoped lang="scss">
.video {
  width: 100%;
  height: 100%;
}
.swiper {
  width: 100%;
  height: 100%;
}
.top-area {
  position: absolute;
  width: 100vw;
  top: 0;
  left: 0;
}
.btn-menu {
  position: absolute;
  width: px2vw(68);
  height: px2vw(68);
  background: rgba(0, 0, 0, 0.5);
  border-radius: px2vw(40);
  border: px2vw(2) solid rgba(255, 255, 255, 0.2);
  top: px2vw(216);
  right: px2vw(32);
}
.video-name {
  position: absolute;
  left: px2vw(32);
  bottom: px2vw(48);
  background: rgba(0, 0, 0, 0.2);
  border-radius: px2vw(24);
  padding: px2vw(10) px2vw(16);
  font-size: px2vw(28);
  line-height: px2vw(28);
  font-weight: 500;
  color: #ffffff;
}

.drawer-wrapper {
  background: #00112a;
  height: 100vh;
  width: px2vw(576);
  padding-top: px2vw(160);
  box-sizing: border-box;
  .parent__mark {
    width: px2vw(32);
    height: px2vw(32);
    text-align: center;
    line-height: px2vw(32);
    background: rgba(255, 255, 255, 0.6);
    border-radius: px2vw(12);
    font-size: px2vw(28);
    color: #333;
    font-weight: 500;
  }
  .parent__name {
    color: rgba(255, 255, 255, 0.6);
  }
  .child-wrapper {
    &:active {
      background-color: rgba(255, 255, 255, 0.06);
    }
  }
  .child-border {
    border-bottom: px2vw(1) solid rgba(255, 255, 255, 0.06);
  }
}
</style>
