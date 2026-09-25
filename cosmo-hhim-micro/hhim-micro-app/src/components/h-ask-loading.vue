<template>
  <view class="h-ask-loading">
    <!-- 3x3 像素网格：波峰扫过；Orbit 变体中心格常暗 -->
    <view class="h-ask-loading-grid">
      <view
        v-for="(d, i) in cells"
        :key="i"
        class="h-ask-loading-cell"
        :class="{ 'h-ask-loading-cell-dim': d === null }"
        :style="d === null ? '' : `animation-delay:${d}ms;animation-duration:${dur}ms`"
      />
    </view>
    <!-- 闪烁文案 -->
    <text class="h-ask-loading-label">{{ label }}</text>
    <!-- 实时计时 -->
    <text class="h-ask-loading-elapsed">{{ elapsed }}</text>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'

/**
 * 会话 AI 回答加载态：像素网格 loader（源自 react 版 LoadingState 的 Drive/Dots/Orbit 变体）
 * - Drive：方形格人字波前；Dots：同波前圆形格；Orbit：环绕外圈
 * - 文案闪烁 + 计时器（0.1s 精度）
 */

export interface AskLoadingPattern {
  delays: (number | null)[]
  dur: number
  round: boolean
}

function chevron(): (number | null)[] {
  return Array.from({ length: 9 }, (_, i) => {
    const r = Math.floor(i / 3)
    const c = i % 3
    return (c + Math.abs(r - 1)) * 90
  })
}
function orbit(): (number | null)[] {
  const order = [0, 1, 2, 5, 8, 7, 6, 3]
  return Array.from({ length: 9 }, (_, i) => {
    const k = order.indexOf(i)
    return k === -1 ? null : k * 110
  })
}

const PATTERNS: Record<string, AskLoadingPattern> = {
  Drive: { delays: chevron(), dur: 650, round: false },
  Dots: { delays: chevron(), dur: 650, round: true },
  Orbit: { delays: orbit(), dur: 950, round: false }
}

const props = withDefaults(
  defineProps<{
    label?: string
    variant?: 'Drive' | 'Dots' | 'Orbit'
  }>(),
  {
    label: '正在查数',
    variant: 'Drive'
  }
)

const cfg = computed(() => PATTERNS[props.variant] ?? PATTERNS.Drive)
const cells = computed(() => cfg.value.delays)
const dur = computed(() => cfg.value.dur)
const round = computed(() => cfg.value.round)

/* 计时器 */
const ds = ref(0)
let timer: ReturnType<typeof setInterval> | null = null
onMounted(() => {
  timer = setInterval(() => ds.value++, 100)
})
onUnmounted(() => {
  if (timer) clearInterval(timer)
})
const total = computed(() => ds.value / 10)
const elapsed = computed(() =>
  total.value < 60 ? `${total.value.toFixed(1)}s` : `${Math.floor(total.value / 60)}m ${(total.value % 60).toFixed(1)}s`
)
</script>

<style lang="scss" scoped>
.h-ask-loading {
  display: flex;
  align-items: center;
  gap: px2vw(10);

  .h-ask-loading-grid {
    display: grid;
    grid-template-columns: repeat(3, px2vw(8));
    gap: px2vw(3);

    .h-ask-loading-cell {
      width: px2vw(8);
      height: px2vw(8);
      background: #0066ff;
      border-radius: px2vw(2);
      opacity: 0.15;
      animation-name: h-ask-pixel-on;
      animation-timing-function: ease-in-out;
      animation-iteration-count: infinite;
    }
    .h-ask-loading-cell-dim {
      animation: none;
    }
  }

  .h-ask-loading-label {
    font-size: px2vw(24);
    color: #b6c0c9;
    animation: h-ask-shimmer 1.4s linear infinite;
  }

  .h-ask-loading-elapsed {
    font-size: px2vw(22);
    color: #b6c0c9;
    font-family: monospace;
  }
}

@keyframes h-ask-pixel-on {
  0%,
  100% {
    opacity: 0.15;
  }
  50% {
    opacity: 1;
  }
}
@keyframes h-ask-shimmer {
  0%,
  100% {
    color: #b6c0c9;
  }
  50% {
    color: #333333;
  }
}
</style>
