<template>
  <view class="px-24 flex flex-col align-center">
    <view class="video-wrapper">
      <video
        v-if="!isLast"
        class="video"
        id="myVideo"
        object-fit="contain"
        @ended="handleEnded"
        @timeupdate="handleUpdate"
        @loadedmetadata="handleLoad"
        controls
        autoplay
        :src="formatVideo(videoArr[videoIndex]?.src)"
      />
      <view v-else class="last-wrapper flex flex-col align-center">
        <view class="mt-64 color-333 font-56 bold">恭喜</view>
        <view class="mt-48 px-112 line-height-48 bold text-center">
          <text class="color-333 font-28">你已基本了解产品功能，请自由试用KU易记小程序</text>
        </view>
        <img :src="formatImage('guide_img_help_tips')" class="last-wrapper__img mt-32" />
        <view class="mt-40 color-666 font-24">你可以在工作台帮助中心，随时了解更多功能</view>
      </view>
    </view>
    <view class="mt-32 flex justify-center gap-24">
      <view
        class="icon-16 rounded-full"
        :class="index === videoIndex ? 'bg-fff' : 'bg-fff-20'"
        v-for="(item, index) in videoArr"
        :key="index"
      />
    </view>
    <view class="footer flex-center">
      <template v-if="isLast">
        <h-button width="472" height="88" text="立即试用" @tap="handleStart" />
      </template>
      <template v-else>
        <view class="color-fff font-28" @tap="handleJump">跳过</view>
        <h-button width="472" height="88" text="下一个" class="ml-64" @tap="handleNext" />
      </template>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, PropType, ref } from 'vue'
import { formatImage, formatVideo } from '@/utils/common'

const props = defineProps({
  sourceVideo: {
    type: [] as PropType<TVideo[]>,
    default: () => []
  }
})

type TVideo = {
  src?: ''
  video?: ''
  duration?: 0
  playing?: 0
}
const videoIndex = ref(0)
const videoArr = ref<TVideo[]>([])
const isLast = computed(() => {
  return videoIndex.value === videoArr.value.length - 1
})

const emits = defineEmits(['stepChange'])

function handleJump() {
  videoIndex.value = videoArr.value.length - 1
}
function handleNext() {
  videoIndex.value++
}
function handleLoad(e) {
  // 加载完成
  videoArr.value[videoIndex.value].duration = e.target.duration
}
function handleEnded() {
  handleNext()
}
function handleUpdate(e) {
  videoArr.value[videoIndex.value].playing = e.detail.currentTime
}
function handleStart() {
  emits('stepChange', 3, { arr: videoArr.value.filter((v) => v.src) })
}

onMounted(() => {
  const temp = props.sourceVideo.map((v) => {
    v.src = v.video
    v.duration = 0
    v.playing = 0
    return v
  })
  videoArr.value = [...temp, {}]
})
</script>

<style scoped lang="scss">
.video-wrapper {
  width: px2vw(672);
  height: px2vw(1136);
  background-color: #00112a;
  border-radius: px2vw(16);
  overflow: hidden;
}
.footer {
  padding-top: px2vw(40);
  padding-bottom: px2vw(80);
}

.video {
  width: 100%;
  height: 100%;
}
.last-wrapper {
  width: 100%;
  height: 100%;
  background-image: url('/static/micro_app/guide_bg_gongxi.png');
  background-repeat: no-repeat;
  background-size: 100%;
  background-color: #ffffff;
  &__img {
    width: px2vw(576);
    height: px2vw(732);
  }
}
</style>
