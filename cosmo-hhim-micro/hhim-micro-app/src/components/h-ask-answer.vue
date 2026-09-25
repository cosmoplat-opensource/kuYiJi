<template>
  <!-- 问一问 AI 答案卡片：答案文本（关键数字高亮 + 长答案折叠）+ 统计范围 + 复制/查看图表/依据 + 继续问 + 澄清选项 -->
  <view class="h-ask-answer">
    <!-- 答案文本：数字高亮（日期不参与） -->
    <view class="h-ask-answer-content">
      <text v-for="(seg, i) in segments" :key="i" :class="{ 'h-ask-answer-num': seg.num }">{{ seg.t }}</text>
    </view>
    <!-- 长答案折叠（按字符数折叠，跨端一致；展开后完整显示） -->
    <view v-if="collapsible" class="h-ask-answer-more" @tap="folded = !folded">
      <text>{{ folded ? '展开全文' : '收起' }}</text>
      <uni-icons :type="folded ? 'down' : 'up'" color="#0066ff" size="12" />
    </view>

    <!-- 统计范围：口径自证（登记实现参数回显，让用户确认"问的是不是这段"） -->
    <view v-if="scopeChips.length" class="h-ask-answer-scope">
      <text v-for="(c, i) in scopeChips" :key="i" class="h-ask-answer-scope-chip">{{ c.label }} {{ c.value }}</text>
    </view>

    <!-- 操作区 -->
    <view class="h-ask-answer-ops">
      <view class="h-ask-answer-op" @tap="copyAnswer">
        <text>复制</text>
      </view>
      <view v-if="route" class="h-ask-answer-op" @tap="goChart">
        <text>查看图表</text>
        <uni-icons type="arrowright" color="#0066ff" size="14" />
      </view>
      <view v-if="evidence" class="h-ask-answer-op" @tap="evOpen = !evOpen">
        <text>依据</text>
        <uni-icons :type="evOpen ? 'up' : 'down'" color="#0066ff" size="14" />
      </view>
    </view>

    <!-- 依据展开（三级血缘：指标口径 / 数据来源 / 结果快照，键名中文化） -->
    <view v-if="evidence && evOpen" class="h-ask-answer-evidence">
      <view v-if="evidence.metric" class="h-ask-answer-ev-group">
        <text class="h-ask-answer-ev-label">指标口径</text>
        <text class="h-ask-answer-ev-value">{{ evidence.metric.name }} = {{ evidence.metric.formula }}</text>
      </view>
      <view v-if="evidence.source" class="h-ask-answer-ev-group">
        <text class="h-ask-answer-ev-label">数据来源</text>
        <text class="h-ask-answer-ev-value">{{ sourceNote }}</text>
        <text v-if="evidence.source.api" class="h-ask-answer-ev-sub">数据来源：{{ evidence.source.api }}</text>
      </view>
      <view v-if="snapshotRows.length" class="h-ask-answer-ev-group">
        <text class="h-ask-answer-ev-label">结果快照</text>
        <view v-for="(row, i) in snapshotRows" :key="i" class="h-ask-answer-ev-row">
          <text class="h-ask-answer-ev-row-label">{{ row.label }}</text>
          <text class="h-ask-answer-ev-row-value">{{ row.value }}</text>
        </view>
      </view>
      <text v-if="evidence.asOf" class="h-ask-answer-ev-asof">数据截止 {{ evidence.asOf }}</text>
    </view>

    <!-- 澄清选项（歧义时展示，点选即重新提问） -->
    <view v-if="clarify" class="h-ask-answer-clarify">
      <text class="h-ask-answer-clarify-q">{{ clarify.question }}</text>
      <view class="h-ask-answer-clarify-options">
        <view
          v-for="opt in clarify.options"
          :key="opt"
          class="h-ask-answer-clarify-chip"
          @tap="$emit('select', opt)"
        >
          {{ opt }}
        </view>
      </view>
    </view>

    <!-- 继续问（常驻追问建议：命中意图 → 本体问题模板；仅最新一条答案展示，避免刷屏） -->
    <view v-if="followUps.length" class="h-ask-answer-follow">
      <text class="h-ask-answer-follow-title">继续问：</text>
      <view class="h-ask-answer-follow-chips">
        <view v-for="q in followUps" :key="q" class="h-ask-answer-follow-chip" @tap="$emit('ask', q)">
          {{ q }}
        </view>
      </view>
    </view>

    <!-- 角标 -->
    <text class="h-ask-answer-foot">AI 生成，仅供参考</text>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
// 追问建议/快捷提问来自本体（ai-ontology/metrics.json）投影，保持"本体单一来源"
// 注意：该文件必须在主包（src/utils/）——主包组件不能 require 分包模块（微信小程序限制）
import { FOLLOW_UPS, QUICK_QUESTIONS } from '@/utils/ai-capability'

interface AskRoute {
  path: string
  params?: Record<string, string>
  title?: string
}
interface AskEvidence {
  metric?: { name: string; formula: string; dims?: string[] }
  source?: { api: string; params?: Record<string, unknown>; note?: string }
  snapshot?: { label: string; value: string }[]
  asOf?: string
}
interface AskClarify {
  question: string
  options: string[]
}

const props = withDefaults(
  defineProps<{
    content: string
    route?: AskRoute | null
    evidence?: AskEvidence | null
    clarify?: AskClarify | null
    /** 命中意图（/ai/ask 的 intent，或会话消息里的槽位 JSON 串），用于取追问建议 */
    intent?: string | null
    /** 本条答案对应的用户问题（追问建议里排除重复） */
    askedQuestion?: string
    /** 是否最新一条 AI 答案（只有最新一条展示"继续问"） */
    active?: boolean
  }>(),
  { intent: '', askedQuestion: '', active: true }
)
// select=澄清选项点选；ask=追问建议点选（由页面统一发送，避免组件内直接调接口）
defineEmits<{ select: [option: string]; ask: [question: string] }>()

const evOpen = ref(false)
const folded = ref(false)

/* ---------------- 长答案折叠 ---------------- */
const FOLD_LIMIT = 150
const collapsible = computed(() => (props.content || '').length > FOLD_LIMIT)
const shownContent = computed(() =>
  collapsible.value && folded.value ? (props.content || '').slice(0, FOLD_LIMIT) + '…' : props.content || ''
)
// 换答案（会话切换/追问）时收起折叠态，避免上一条的展开状态串到下一条
watch(
  () => props.content,
  () => {
    folded.value = false
  }
)
watch(
  () => props.evidence,
  () => {
    evOpen.value = false
  }
)

/* ---------------- 关键数字高亮（日期/编号不参与，避免满屏高亮） ---------------- */
interface Segment {
  t: string
  num: boolean
}
// 日期/编号（2026-08-13、08-13）不参与高亮；小数与百分比照常高亮（分隔符不含"."，避免把 3.5 当日期）
const DATE_RE = /(\d{4}[-/]\d{1,2}[-/]\d{1,2}|\d{1,2}[-/]\d{1,2})|(\d+(?:\.\d+)?%?)/
function tokenize(text: string): Segment[] {
  const out: Segment[] = []
  if (!text) return out
  const re = new RegExp(DATE_RE.source, 'g')
  let last = 0
  let m: RegExpExecArray | null
  while ((m = re.exec(text)) !== null) {
    if (m.index > last) out.push({ t: text.slice(last, m.index), num: false })
    out.push({ t: m[0], num: !m[1] }) // 第一分支=日期 → 不高亮
    last = m.index + m[0].length
  }
  if (last < text.length) out.push({ t: text.slice(last), num: false })
  return out
}
const segments = computed(() => tokenize(shownContent.value))

/* ---------------- 统计范围 / 参数回显 ---------------- */
const SCOPE_LABELS: Record<string, string> = {
  productNameOrCode: '产品',
  processNameOrCode: '工序',
  processSeq: '工序',
  userName: '员工',
  ngType: '不良类型',
  entityType: '类型',
  orderNo: '订单'
}
const scopeChips = computed(() => {
  const p = (props.evidence?.source?.params || {}) as Record<string, unknown>
  const chips: { label: string; value: string }[] = []
  const s = p.startDate ? String(p.startDate) : ''
  const e = p.endDate ? String(p.endDate) : ''
  if (s || e) {
    chips.push({ label: '统计范围', value: s && e && s !== e ? `${s} ~ ${e}` : s || e })
  }
  Object.keys(SCOPE_LABELS).forEach((k) => {
    const v = p[k]
    if (v !== undefined && v !== null && String(v) !== '') {
      chips.push({ label: SCOPE_LABELS[k], value: String(v) })
    }
  })
  return chips
})

/* ---------------- 依据（键名中文化 + 比率转百分比） ---------------- */
const KEY_LABELS: Record<string, string> = {
  totalCounts: '记工总数',
  totalPassNum: '良品数',
  totalNgNum: '不良数',
  passRate: '良品率',
  submitNum: '报工数',
  ngNum: '不良数',
  checkPassNum: '良品数',
  checkNgNum: '不良数',
  totalNum: '记工总数',
  productCnt: '产品数',
  finishedNum: '成品库存',
  stockNum: '库存',
  planNum: '计划数',
  finishNum: '完成数',
  remainNum: '剩余数',
  riskDays: '超期(天)',
  orderNo: '订单号',
  itemSeq: '产品编号',
  productSeq: '产品编号',
  productName: '产品',
  processName: '工序',
  nickName: '员工',
  deliveryDate: '交期',
  predictDate: '预计完成',
  submitDay: '日期',
  name: '名称'
}
function toPercent(v: string) {
  const n = Number(v)
  return isFinite(n) ? (n * 100).toFixed(1).replace(/\.0$/, '') + '%' : v
}
/** 后端 snapshot value 形如 "totalCounts 31 · passRate 0.913" → "记工总数 31，良品率 91.3%" */
function prettyValue(v: string) {
  if (!v) return ''
  return v
    .split('·')
    .map((p) => p.trim())
    .filter(Boolean)
    .map((pair) => {
      const sp = pair.indexOf(' ')
      if (sp < 0) return pair
      const key = pair.slice(0, sp)
      const val = pair.slice(sp + 1).trim()
      return `${KEY_LABELS[key] || key} ${/Rate$/.test(key) ? toPercent(val) : val}`
    })
    .join('，')
}
const snapshotRows = computed(() =>
  (props.evidence?.snapshot || []).map((s, i) => ({
    label: s.label && s.label !== '-' ? s.label : `第 ${i + 1} 条`,
    value: prettyValue(s.value)
  }))
)
// "与本页统计同源（登记于 micro_ai_metric_api）" → 去掉工程师注解，保留人话
const sourceNote = computed(() => {
  const note = props.evidence?.source?.note || ''
  const clean = note.replace(/（[^）]*）/g, '').trim()
  return clean || '与页面统计同源（已审核口径）'
})

/* ---------------- 继续问（追问建议） ---------------- */
/** intent 可能是纯 code（如 SUMMARY），也可能是落库的槽位 JSON 串 */
function intentCode(raw?: string | null) {
  if (!raw) return ''
  const t = String(raw).trim()
  if (t.startsWith('{')) {
    try {
      const o = JSON.parse(t)
      return o && o.intent ? String(o.intent) : ''
    } catch (e) {
      return ''
    }
  }
  return t
}
const followUps = computed(() => {
  if (!props.active) return []
  const code = intentCode(props.intent)
  const mine = code && FOLLOW_UPS[code] ? FOLLOW_UPS[code].slice() : []
  const pool = mine.concat(QUICK_QUESTIONS.filter((q) => mine.indexOf(q) < 0))
  return pool.filter((q) => q !== props.askedQuestion).slice(0, 3)
})

/* ---------------- 操作 ---------------- */
function copyAnswer() {
  const text = props.content || ''
  if (!text) return
  uni.setClipboardData({
    data: text,
    success: () => {
      // #ifdef H5
      uni.showToast({ title: '已复制', icon: 'none' })
      // #endif
      // 小程序端 wx.setClipboardData 自带"内容已复制"提示，不重复弹
    },
    fail: () => uni.showToast({ title: '复制失败，请长按选择', icon: 'none' })
  })
}

// 跳转：path + JSON 序列化 params（目标页 onLoad 解析）
function goChart() {
  const { path, params } = props.route ?? {}
  if (!path) return
  const q = params && Object.keys(params).length ? '?params=' + encodeURIComponent(JSON.stringify(params)) : ''
  uni.navigateTo({
    url: path + q,
    fail: () => uni.showToast({ title: '页面打开失败', icon: 'none' })
  })
}
</script>

<style lang="scss" scoped>
.h-ask-answer {
  background: #fff;
  border-radius: px2vw(16);
  padding: px2vw(20) px2vw(24) px2vw(12);
  max-width: 82%;
  text-align: left;

  .h-ask-answer-content {
    font-size: px2vw(28);
    color: #333;
    line-height: 1.5;
    white-space: pre-wrap;
    word-break: break-all;

    .h-ask-answer-num {
      color: #0066ff;
      font-weight: 600;
    }
  }

  .h-ask-answer-more {
    display: flex;
    align-items: center;
    gap: px2vw(4);
    margin-top: px2vw(8);
    font-size: px2vw(24);
    color: #0066ff;
  }

  .h-ask-answer-scope {
    display: flex;
    flex-wrap: wrap;
    gap: px2vw(8);
    margin-top: px2vw(12);
    .h-ask-answer-scope-chip {
      font-size: px2vw(20);
      color: #5a6f82;
      background: #f2f5f9;
      border-radius: px2vw(8);
      padding: px2vw(4) px2vw(12);
    }
  }

  .h-ask-answer-ops {
    display: flex;
    align-items: center;
    margin-top: px2vw(16);
    padding-top: px2vw(12);
    border-top: 1rpx solid #f0f0f0;
    .h-ask-answer-op {
      display: flex;
      align-items: center;
      gap: px2vw(4);
      margin-right: px2vw(32);
      font-size: px2vw(24);
      color: #0066ff;
      padding: px2vw(4) 0;
    }
  }

  .h-ask-answer-evidence {
    margin-top: px2vw(12);
    padding: px2vw(16);
    background: #f7f9fc;
    border-radius: px2vw(12);

    .h-ask-answer-ev-group {
      display: flex;
      flex-direction: column;
      margin-bottom: px2vw(12);
      &:last-of-type {
        margin-bottom: 0;
      }
      .h-ask-answer-ev-label {
        font-size: px2vw(20);
        color: #999;
      }
      .h-ask-answer-ev-value {
        font-size: px2vw(22);
        color: #333;
        margin-top: px2vw(4);
        word-break: break-all;
      }
      .h-ask-answer-ev-sub {
        font-size: px2vw(20);
        color: #a8b0ba;
        margin-top: px2vw(4);
        word-break: break-all;
      }
      .h-ask-answer-ev-row {
        display: flex;
        align-items: flex-start;
        margin-top: px2vw(6);
        .h-ask-answer-ev-row-label {
          flex-shrink: 0;
          max-width: px2vw(200);
          font-size: px2vw(22);
          color: #5a6f82;
          font-weight: 500;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        .h-ask-answer-ev-row-value {
          flex: 1;
          font-size: px2vw(22);
          color: #333;
          margin-left: px2vw(12);
          word-break: break-all;
        }
      }
    }
    .h-ask-answer-ev-asof {
      display: block;
      font-size: px2vw(20);
      color: #b0b0b0;
      margin-top: px2vw(8);
    }
  }

  .h-ask-answer-clarify {
    margin-top: px2vw(12);
    padding: px2vw(16);
    background: #fffaeb;
    border-radius: px2vw(12);

    .h-ask-answer-clarify-q {
      display: block;
      font-size: px2vw(22);
      color: #8a6d3b;
      margin-bottom: px2vw(8);
    }
    .h-ask-answer-clarify-options {
      display: flex;
      flex-wrap: wrap;
      gap: px2vw(8);
      .h-ask-answer-clarify-chip {
        padding: px2vw(8) px2vw(16);
        background: #fff;
        border: 1rpx solid #e0d5b8;
        border-radius: px2vw(24);
        font-size: px2vw(22);
        color: #8a6d3b;
      }
    }
  }

  .h-ask-answer-follow {
    margin-top: px2vw(12);
    padding-top: px2vw(12);
    border-top: 1rpx solid #f0f0f0;
    .h-ask-answer-follow-title {
      font-size: px2vw(20);
      color: #999;
    }
    .h-ask-answer-follow-chips {
      display: flex;
      flex-direction: column;
      margin-top: px2vw(8);
      .h-ask-answer-follow-chip {
        margin-top: px2vw(8);
        padding: px2vw(12) px2vw(16);
        background: #f2f7ff;
        border: 1rpx solid #dbe8ff;
        border-radius: px2vw(12);
        font-size: px2vw(24);
        color: #0066ff;
        &:first-child {
          margin-top: 0;
        }
      }
    }
  }

  .h-ask-answer-foot {
    display: block;
    font-size: px2vw(18);
    color: #c0c6cf;
    margin-top: px2vw(12);
  }
}
</style>
