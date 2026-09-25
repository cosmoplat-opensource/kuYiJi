<template>
  <!-- 小程序/App：movable-view（H5 端 movable-view 初始 x 超界会锁死横向拖动、边界处截断，改用下方 touch 拖动） -->
  <!-- #ifndef H5 -->
  <movable-area class="area">
    <movable-view
      class="move-view"
      :class="size"
      :x="btn_x"
      :y="btn_y"
      direction="all"
      :inertia="true"
      :disabled="disabled"
    >
      <view class="h-button--record" :class="size">
        <slot>
          <text class="text">{{ props.text }}</text>
        </slot>
      </view>
    </movable-view>
  </movable-area>
  <!-- #endif -->
  <!-- H5：fixed 定位 + touch/mouse 拖动，任意方向移动且始终限制在视口内（不截断） -->
  <!-- #ifdef H5 -->
  <view
    class="area-h5"
    :class="size"
    :style="{ top: posTop + 'px', left: posLeft + 'px' }"
    @touchstart.stop="onDragStart"
    @touchmove.stop.prevent="onDragMove"
    @touchend.stop="onDragEnd"
    @mousedown.stop="onDragStart"
    @mousemove.stop="onDragMove"
    @mouseup.stop="onDragEnd"
    @tap="onTapH5"
  >
    <view class="h-button--record" :class="size">
      <slot>
        <text class="text">{{ props.text }}</text>
      </slot>
    </view>
  </view>
  <!-- #endif -->
</template>
<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
const props = defineProps({
  //center=true时 x值为元素宽度 else x值为元素左边缘距离左边屏幕距离
  x: {
    type: Number,
    default: 0
  },
  //是否横向居中
  center: {
    type: Boolean,
    default: false
  },
  // 距离底部距离+元素高度
  y: {
    type: Number,
    default: 0
  },
  text: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    default: 'icon-144'
  },
  disabled: {
    //是否可滑动
    type: Boolean,
    default: true
  }
})

// 点击事件转发给父组件(如工作台"演示/实际数据"切换)
// H5：uni-app 把父组件 @tap 编译成 onClick，子组件 emit 必须用 'click' 才能匹配（emit('tap') 会去找 onTap 找不到）。
// #ifdef H5
const emit = defineEmits(['click'])
// #endif
// #ifndef H5
// 小程序/App 端沿用内置 tap 语义（@tap → bindtap，emit('tap') → 原生 tap 通道）
const emit = defineEmits(['tap'])
// #endif

const btn_x = ref(0)
watch(
  () => props.x,
  (val) => {
    btn_x.value = (safeWinWidth() || 375) - (props.x || 0)
  },
  { immediate: true }
)
const btn_y = ref(0)
watch(
  () => props.y,
  (val) => {
    const winInfo = uni.getWindowInfo()
    const deviceInfo = uni.getDeviceInfo()
    if (deviceInfo.model === 'iPhone X' || deviceInfo.model === 'iPhone XS Max') {
      btn_y.value = (safeWinHeight() || 667) - (props.y || 0) - 34 - 16
    } else {
      btn_y.value = (safeWinHeight() || 667) - (props.y || 0) - 16
    }
  },
  { immediate: true }
)

/**
 * 安全取屏幕宽：getWindowInfo 在部分工具/基础库下字段可能为 undefined，
 * 回退 getSystemInfoSync（虽弃用但有值），再兜底 375
 */
function safeWinWidth() {
  let w = uni.getWindowInfo()?.screenWidth
  if (!w) {
    try {
      w = uni.getSystemInfoSync().screenWidth
    } catch (e) {
      /* ignore */
    }
  }
  return w || 375
}

/** 安全取屏幕高：同上，兜底 667 */
function safeWinHeight() {
  let h = uni.getWindowInfo()?.screenHeight
  if (!h) {
    try {
      h = uni.getSystemInfoSync().screenHeight
    } catch (e) {
      /* ignore */
    }
  }
  return h || 667
}

// H5：movable-view 在 H5 端存在“初始 x 超界后横向拖不动、边界外截断”的问题，
// 改为 fixed 定位 + 指针事件实现任意方向拖动，并始终 clamp 在视口内（不会截断）
// #ifdef H5
const posLeft = ref(0)
const posTop = ref(0)
const btnSize = computed(() => (props.size === 'icon-144' ? 72 : 56))
let dragStartX = 0
let dragStartY = 0
let isDragging = false
let dragLock = false
let suppressTap = false

onMounted(() => {
  // wx.getSystemInfo 已弃用（微信基础库 3.x），改用 getWindowInfo
  const winInfo = uni.getWindowInfo()
  // x 语义与小程序端一致：x>0（演示切换）→ 靠左；x=0（AI 入口）→ 靠右，避免两个按钮初始重叠
  const w = window.innerWidth || winInfo.windowWidth || 375
  if (props.x && props.x > 0) {
    posLeft.value = 16
  } else {
    posLeft.value = w - btnSize.value - 16
  }
  posTop.value = (winInfo.windowHeight || window.innerHeight) - props.y - 16
})

function clampH5Pos(v: number, min: number, max: number) {
  return Math.min(Math.max(v, min), max)
}
function getDragPoint(e: any) {
  const t = e.touches ? e.touches[0] : e
  return { x: t.clientX, y: t.clientY }
}
function onDragStart(e: any) {
  if (props.disabled) return
  dragLock = true
  isDragging = false
  const p = getDragPoint(e)
  dragStartX = p.x
  dragStartY = p.y
}
function onDragMove(e: any) {
  if (props.disabled || !dragLock) return
  const p = getDragPoint(e)
  const dx = p.x - dragStartX
  const dy = p.y - dragStartY
  if (Math.abs(dx) > 3 || Math.abs(dy) > 3) isDragging = true
  const maxLeft = (window.innerWidth || document.documentElement.clientWidth) - btnSize.value
  const maxTop = (window.innerHeight || document.documentElement.clientHeight) - btnSize.value
  posLeft.value = clampH5Pos(posLeft.value + dx, 0, maxLeft)
  posTop.value = clampH5Pos(posTop.value + dy, 0, maxTop)
  dragStartX = p.x
  dragStartY = p.y
}
function onDragEnd() {
  dragLock = false
  if (isDragging) {
    suppressTap = true
    setTimeout(() => {
      suppressTap = false
    }, 300)
  }
}
function onTapH5(e: any) {
  // 拖动结束后的浏览器 click（tap）不触发点击，避免误触
  if (suppressTap) {
    suppressTap = false
    if (e && e.stopPropagation) e.stopPropagation()
    return
  }
  // 非拖动：转发事件给父组件
  // #ifdef H5
  emit('click', e)
  // #endif
  // #ifndef H5
  emit('tap', e)
  // #endif
}
// #endif
</script>
<style lang="scss" scoped>
.area {
  width: calc(100vw - 64rpx);
  height: calc(100vh - 176rpx - 32rpx);
  overflow: hidden;
  pointer-events: none;
  position: fixed;
  top: px2vw(176);
  left: px2vw(32);
  z-index: 10;
  .move-view {
    pointer-events: auto;
  }
}
/* #ifdef H5 */
/* H5 专用：fixed 定位 + 指针拖动，touch-action: none 防止页面滚动抢走手势 */
.area-h5 {
  position: fixed;
  z-index: 10;
  cursor: grab;
  touch-action: none;
}
/* #endif */
.h-button--record {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #0066ff;
  border-radius: 50%;
  .text {
    color: #fff;
    font-size: px2vw(40);
    line-height: px2vw(40);
    font-weight: 500;
  }
  &:active {
    background-color: #0044e7;
  }
}
.icon-144 {
  width: 72px;
  height: 72px;
}
.icon-112 {
  width: 56px;
  height: 56px;
}
</style>
