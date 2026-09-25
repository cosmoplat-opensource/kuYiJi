<template>
  <view class="ask-page">
    <!-- 标题栏：左侧返回 / 中间标题（预留状态栏高度与微信胶囊位置） -->
    <view class="ask-header" :style="headerStyle">
      <view class="ask-header-inner" :style="headerInnerStyle">
        <view class="ask-header-left" @tap="handleBack">
          <uni-icons type="left" color="#333" size="24" />
        </view>
        <text class="ask-header-title">问一问</text>
      </view>
    </view>
    <!-- 操作栏：新会话 / 会话记录 -->
    <view class="ask-actions">
      <view class="ask-action-btn ask-action-btn-primary" @tap="createSession">＋ 新会话</view>
      <view class="ask-action-btn ask-action-btn-ghost" @tap="openSessions">会话记录</view>
    </view>

    <!-- 消息区（滚动条贴屏幕右缘：间距由内容自身留，不在容器 padding 内） -->
    <scroll-view id="ask-body" scroll-y class="ask-body" :scroll-top="scrollTop" @scroll="onBodyScroll">
      <view id="ask-body-inner">
        <!-- 会话信息条（只显示会话名称，隐藏剩余条数） -->
        <view class="ask-session-info" v-if="messages.length">
          <text class="ask-session-info-text">{{ currentTitle }}{{ isFull ? '（已满）' : '' }}</text>
        </view>
        <!-- 空态：一句能力说明 + 本体快捷提问 -->
        <view v-if="!messages.length && !msgLoading" class="ask-empty">
          <text class="ask-empty-intro">我是 Ku易记 问数助手，可以问我产量、良品率、库存、记工等经营数据。</text>
          <view class="ask-empty-head">
            <text class="ask-empty-title">新会话 · 试试问我这些：</text>
            <text class="ask-roll" @tap="rollExamples()">换一批</text>
          </view>
          <view v-for="(q, i) in examples" :key="i" class="ask-example" @tap="send(q)">{{ q }}</view>
        </view>
        <!-- 切换会话：拉取消息中的加载态 -->
        <view v-if="msgLoading" class="ask-msg ask-msg-ai">
          <h-ask-loading label="正在加载会话" />
        </view>
        <!-- 消息 -->
        <view v-for="(m, i) in messages" :key="i" :id="'msg-' + i" class="ask-msg" :class="m.role === 'user' ? 'ask-msg-user' : 'ask-msg-ai'">
          <text class="ask-msg-time">{{ formatTime(m.time) }}</text>
          <view v-if="m.role === 'user'" class="ask-msg-bubble ask-msg-bubble-user">
            <text>{{ m.content }}</text>
          </view>
          <!-- 失败态：本地未上屏到服务端，可就地重试（不重复上屏用户问题） -->
          <view v-else-if="m.failed" class="ask-fail">
            <text class="ask-fail-text">没能取到答案，可能是网络或服务繁忙。</text>
            <view class="ask-fail-retry" @tap="retryMessage(i)">
              <uni-icons type="refreshempty" color="#0066ff" size="14" />
              <text class="ask-fail-retry-text">重试</text>
            </view>
          </view>
          <h-ask-answer
            v-else
            :content="m.content"
            :route="m.route"
            :evidence="m.evidence"
            :clarify="m.clarify"
            :intent="m.intent"
            :asked-question="m.question || lastUserBefore(i)"
            :active="i === messages.length - 1 && !loading"
            @select="(opt) => send(opt, { withContext: true })"
            @ask="(q) => send(q)"
          />
        </view>
        <view v-if="loading" class="ask-msg ask-msg-ai">
          <h-ask-loading label="正在查询" />
        </view>
        <view class="ask-body-pad" />
      </view>
    </scroll-view>

    <!-- 回到最新：用户上翻看历史时不强行拽回，仅提示（点一下回到底部） -->
    <view v-if="!atBottom && messages.length" class="ask-jump" @tap="jumpLatest">
      <text class="ask-jump-text">回到最新</text>
      <uni-icons type="bottom" color="#0066ff" size="14" />
    </view>

    <!-- 输入区（固定底部；H5 键盘弹起时按可视视口偏移，避免被输入法盖住） -->
    <view class="ask-input-bar" :style="kbOffset ? { bottom: kbOffset + 'px' } : {}">
      <view class="ask-input-box">
        <view class="ask-input-wrap">
          <textarea
            v-model="inputText"
            class="ask-input"
            :disabled="isFull"
            :maxlength="200"
            auto-height
            :show-confirm-bar="false"
            placeholder="试试：这个月良品率最低的产品是什么？"
            @confirm="send()"
          />
        </view>
        <view class="ask-send" :class="{ 'ask-send-disabled': isFull || loading || !inputText.trim() }" @tap="send()">
          <text>{{ loading ? '查询中' : '发送' }}</text>
        </view>
      </view>
    </view>

    <!-- 会话记录弹层 -->
    <view v-if="showSessions" class="ask-mask" @tap="showSessions = false">
      <view class="ask-sessions" @tap.stop>
        <view class="ask-sessions-head">
          <text class="ask-sessions-title">会话记录</text>
          <view class="ask-sessions-new" @tap.stop="createSession">＋ 新建会话</view>
        </view>
        <!-- 会话搜索（会话多了之后按名称找） -->
        <view class="ask-sessions-search-wrap" v-if="sessionList.length > 5">
          <input v-model="sessionKeyword" class="ask-sessions-search" placeholder="搜索会话名称" :maxlength="20" />
          <view v-if="sessionKeyword" class="ask-sessions-search-clear" @tap="sessionKeyword = ''">清空</view>
        </view>
        <scroll-view scroll-y class="ask-sessions-list" v-if="filteredSessions.length">
          <view
            v-for="s in filteredSessions"
            :key="s.id"
            class="ask-session-item"
            :class="{ 'ask-session-item-active': s.id === currentId }"
            @tap="switchTo(s.id)"
          >
            <view class="ask-session-item-main">
              <view class="ask-session-item-title-row">
                <text class="ask-session-title">{{ s.title }}</text>
                <text v-if="s.id === currentId" class="ask-session-current">当前</text>
              </view>
              <text class="ask-session-meta">
                {{ formatTime(s.lastTime) }} · {{ s.msgCount }} 条{{ s.msgCount >= MAX_MSGS ? ' · 已满' : '' }}
              </text>
            </view>
            <view class="ask-session-item-ops">
              <view class="ask-session-rename" @tap.stop="openRename(s.id)">重命名</view>
              <view class="ask-session-op ask-session-op-del" @tap.stop="removeSession(s.id)">删除</view>
            </view>
          </view>
        </scroll-view>
        <view v-else class="ask-sessions-empty">{{ sessionKeyword ? '没有匹配的会话' : '暂无历史会话' }}</view>
      </view>
    </view>

    <!-- 重命名会话弹层 -->
    <view v-if="renameId" class="ask-mask-rename" @tap="closeRename">
      <view class="ask-rename" @tap.stop>
        <text class="ask-rename-title">修改会话名称</text>
        <input v-model="renameValue" class="ask-rename-input" placeholder="请输入会话名称" maxlength="20" />
        <view class="ask-rename-btns">
          <view class="ask-rename-btn ask-rename-btn-cancel" @tap="closeRename">取消</view>
          <view class="ask-rename-btn ask-rename-btn-ok" @tap="confirmRename">确定</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
// 快捷示例问题由本体（ai-ontology/metrics.json）生成，保持"本体单一来源"
// 生成物在主包 src/utils/（主包组件 h-ask-answer 也要用它，分包模块不可被主包引用）
import { QUICK_QUESTIONS } from '@/utils/ai-capability'
import { _get, _post, _delete } from '@/utils/common-request'

const MAX_MSGS = 200 // 会话上限（与后端约定一致；200 条约 100 轮）

interface AskMessage {
  role: 'user' | 'ai'
  content: string
  time: string // 'yyyy-MM-dd HH:mm:ss'
  route?: any
  evidence?: any
  clarify?: any
  /** 命中意图（追问建议依据；可能是纯 code 或落库的槽位 JSON 串） */
  intent?: string | null
  /** 本条答案对应的用户问题（追问建议排除用） */
  question?: string
  /** 本地失败态（未取到答案，可重试） */
  failed?: boolean
}
interface AskSession {
  id: string
  title: string
  msgCount: number
  lastTime: string
}

// 示例问题：来自本体的快捷提问池；每次随机取 5 条展示（可「换一批」）
const examples = ref<string[]>([])
/** 从其它页面带进来的问题（?q=xxx）：只填进输入框，不自动发送 */
const pendingQ = ref('')
/** 全局池随机取 N 条（洗牌后取前 N） */
function rollExamplesGlobal(n = 5) {
  const pool = QUICK_QUESTIONS.slice()
  for (let i = pool.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    const t = pool[i]
    pool[i] = pool[j]
    pool[j] = t
  }
  examples.value = pool.slice(0, Math.min(n, pool.length))
}

/** 统一入口：有实例池（真实业务名）时用组合推荐，否则退回全局池 */
function rollExamples(n = 5) {
  if (instancePool.value.length) {
    rollMixedExamples(n)
  } else {
    rollExamplesGlobal(n)
  }
}

/* ---------------- 推荐问题与真实数据联动 ----------------
 * 思路：全局问题池（本体登记问法，保证答得出）+ 用"本窗口有数据的真实业务对象"实例化模板。
 * 1) 只用 submitCnt>0 的实体（杜绝"推荐了却无数据"）
 * 2) 每个模板都对应一个已登记能力（保证可回答）
 * 3) 组合规则：至少 2 条全局问题；同一实体不重复；随机 5 条
 * 4) 接口失败/超时 → 退回全局池（功能不缺失）
 */
const instancePool = ref<string[]>([])
const GRAPH_CACHE_KEY = 'ai_data_graph_cache_v1'
const GRAPH_TTL_MS = 5 * 60 * 1000

/** 从缓存读数据图（5 分钟有效），返回 null 表示无可用缓存 */
function readGraphCache() {
  try {
    const raw = uni.getStorageSync(GRAPH_CACHE_KEY)
    if (!raw) {
      return null
    }
    const obj = typeof raw === 'string' ? JSON.parse(raw) : raw
    if (!obj || !obj.at || Date.now() - obj.at > GRAPH_TTL_MS) {
      return null
    }
    return obj.data || null
  } catch (e) {
    return null
  }
}

function writeGraphCache(data: any) {
  try {
    uni.setStorageSync(GRAPH_CACHE_KEY, JSON.stringify({ at: Date.now(), data: data }))
  } catch (e) {
    // 缓存失败不影响功能
  }
}

/** 用真实实体名实例化"已登记问法"（模板与能力一一对应，保证答得出） */
function buildInstancePool(d: any) {
  const out: string[] = []
  const nodes: any[] = (d && d.nodes) || []
  const products = nodes.filter((x) => x.level === 'product' && (x.submitCnt || 0) > 0).slice(0, 3)
  const processes = nodes.filter((x) => x.level === 'process' && (x.submitCnt || 0) > 0).slice(0, 2)
  const employees = nodes.filter((x) => x.level === 'employee' && (x.submitCnt || 0) > 0).slice(0, 2)
  products.forEach((p) => {
    out.push(p.name + ' 这个月良品率多少？')
    out.push(p.name + ' 各工序良品率怎么样？')
    out.push(p.name + ' 的成品库存还有多少？')
  })
  processes.forEach((w) => {
    out.push(w.name + ' 工序良品率多少？')
  })
  employees.forEach((u) => {
    out.push(u.name + ' 这个月报工多少？')
    out.push(u.name + ' 的良品率怎么样？')
  })
  return out
}

/** 拉取数据图（带 800ms 超时与 5 分钟缓存）；成功后重建实例池并重排推荐 */
function loadDataGraph() {
  const cached = readGraphCache()
  if (cached) {
    instancePool.value = buildInstancePool(cached)
    rollMixedExamples()
    return
  }
  const timeout = new Promise((resolve) => setTimeout(() => resolve(null), 800))
  const req = _get({ url: '/ai/ontology/data-graph?days=30' })
    .then((res: any) => (res && (res.data || res)) || null)
    .catch(() => null)
  Promise.race([req, timeout]).then((d: any) => {
    if (d && d.nodes && d.nodes.length) {
      writeGraphCache(d)
      instancePool.value = buildInstancePool(d)
      rollMixedExamples()
    }
  })
}

/** 组合推荐：全局池 + 实例池；至少 2 条全局；同一实体不重复；随机 5 条 */
function rollMixedExamples(n = 5) {
  const globalPool = QUICK_QUESTIONS.slice()
  const inst = instancePool.value.slice()
  if (!inst.length) {
    rollExamplesGlobal(n)
    return
  }
  const shuffle = (arr: string[]) => {
    for (let i = arr.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1))
      const t = arr[i]
      arr[i] = arr[j]
      arr[j] = t
    }
    return arr
  }
  shuffle(globalPool)
  shuffle(inst)
  const picked: string[] = []
  const seenEntity = new Set<string>()
  // 先放 1 条全局（保证通用性）
  if (globalPool.length) {
    picked.push(globalPool.shift() as string)
  }
  // 再放实例问法（同一实体名只出现一次）
  for (const q of inst) {
    if (picked.length >= n - 1) {
      break
    }
    const entity = q.split(' ')[0]
    if (seenEntity.has(entity)) {
      continue
    }
    seenEntity.add(entity)
    picked.push(q)
  }
  // 补足：至少 2 条全局，其余用全局补齐
  while (picked.length < n && globalPool.length) {
    picked.push(globalPool.shift() as string)
  }
  const globalCount = picked.filter((q) => QUICK_QUESTIONS.indexOf(q) >= 0).length
  if (globalCount < 2) {
    for (const q of globalPool) {
      if (globalCount >= 2) {
        break
      }
      if (picked.indexOf(q) < 0) {
        picked.push(q)
      }
    }
  }
  shuffle(picked)
  examples.value = picked.slice(0, n)
}

const sessionList = ref<AskSession[]>([])
const currentId = ref('') // '' = 内存空草稿（未开始对话，不落库）
const messages = ref<AskMessage[]>([])
const showSessions = ref(false)
const inputText = ref('')
const loading = ref(false) // 正在问数（本轮）
const msgLoading = ref(false) // 正在拉取某会话的历史消息
const lastMsgId = ref('')
const scrollTop = ref(0)
const sessionKeyword = ref('')

/** 滚动跟随：只有用户本来就在底部时才自动滚（上翻看历史不被拽走），否则提示"回到最新" */
const atBottom = ref(true)
const hasNew = ref(false)
let bodyViewH = 0
let contentViewH = 0

/** H5 键盘偏移（小程序端由 textarea adjust-position 原生处理，此处恒为 0） */
const kbOffset = ref(0)

/** 滚动到底部：交替两个超过内容高度的大值，确保每次变化都触发滚动（超出部分自动 clamp 到底） */
function scrollToBottom(force = false) {
  if (!force && (!atBottom.value || hasNew.value)) {
    hasNew.value = true
    return
  }
  hasNew.value = false
  setTimeout(() => {
    scrollTop.value = scrollTop.value >= 99999 ? 99998 : 99999
    measureBody()
  }, 50)
}

/** 量取可视区/内容高度（判断是否在底部用） */
function measureBody() {
  uni
    .createSelectorQuery()
    .select('#ask-body')
    .boundingClientRect()
    .select('#ask-body-inner')
    .boundingClientRect()
    .exec((res: any[]) => {
      if (res && res[0]) bodyViewH = res[0].height || 0
      if (res && res[1]) contentViewH = res[1].height || 0
    })
}

function onBodyScroll(e: any) {
  const d = (e && e.detail) || {}
  const st = d.scrollTop || 0
  const ch = d.scrollHeight || contentViewH
  const vh = bodyViewH || ch - st // 兜底：无高度信息时视为到底
  const bottom = ch - st - vh <= 80
  atBottom.value = bottom
  if (bottom) hasNew.value = false
}

function jumpLatest() {
  atBottom.value = true
  hasNew.value = false
  scrollToBottom(true)
}

/** 某条 AI 消息之前的用户问题（服务端历史消息没存问题字段，回溯取） */
function lastUserBefore(index: number) {
  for (let i = index - 1; i >= 0; i--) {
    const m = messages.value[i]
    if (m && m.role === 'user') return m.content
  }
  return ''
}

const currentSession = computed(() => sessionList.value.find((s) => s.id === currentId.value))
const isFull = computed(() => (currentSession.value?.msgCount ?? 0) >= MAX_MSGS)
const currentTitle = computed(() => (currentSession.value ? currentSession.value.title : '新会话'))
const filteredSessions = computed(() => {
  const kw = sessionKeyword.value.trim()
  return kw ? sessionList.value.filter((s) => (s.title || '').indexOf(kw) >= 0) : sessionList.value
})

/* ---------------- 会话管理（后端接口版） ---------------- */
async function loadSessions() {
  try {
    const res: any = await _get({ url: '/ai/chat/session/list' })
    sessionList.value = (res?.data || [])
      .filter((s: any) => (s.msgCount || 0) > 0) // 空会话（0 条）不展示
      .map((s: any) => ({
        id: s.sessionId,
        title: s.title || '新会话',
        msgCount: s.msgCount || 0,
        lastTime: s.lastMsgTime || ''
      }))
  } catch (e) {
    sessionList.value = []
  }
}

async function createSession() {
  // 当前已是空草稿（未开始对话）→ 保持一个，不重复创建
  if (!currentId.value && messages.value.length === 0) {
    showSessions.value = false
    return
  }
  // 新建内存空草稿（不调后端；首次发送时才创建会话）
  currentId.value = ''
  messages.value = []
  lastMsgId.value = ''
  sessionKeyword.value = ''
  showSessions.value = false
  atBottom.value = true
  hasNew.value = false
}

/** 会话切换序号：快速连点/慢响应时丢弃过期结果，避免旧消息覆盖新会话 */
let switchSeq = 0

async function switchTo(id: string) {
  currentId.value = id
  showSessions.value = false
  lastMsgId.value = ''
  messages.value = []
  atBottom.value = true
  hasNew.value = false
  const seq = ++switchSeq
  msgLoading.value = true
  try {
    const res: any = await _get({ url: `/ai/chat/session/${id}/messages` })
    if (seq !== switchSeq || currentId.value !== id) return // 已切到别的会话 → 丢弃
    const list: any[] = res?.data || []
    messages.value = list.map((m: any) => ({
      role: m.role === 'user' ? 'user' : 'ai',
      content: m.content || '',
      time: m.msgTime || '',
      route: parseJson(m.routeJson),
      evidence: parseJson(m.evidenceJson),
      clarify: parseJson(m.clarifyJson),
      intent: m.intent || ''
    }))
    scrollToBottom(true) // 切会话加载历史后直接到底
  } catch (e) {
    if (seq === switchSeq) uni.showToast({ title: '加载消息失败', icon: 'none' })
  } finally {
    if (seq === switchSeq) msgLoading.value = false
  }
}

function parseJson(s: string) {
  if (!s) return null
  try {
    return JSON.parse(s)
  } catch (e) {
    return null
  }
}

function nowStr() {
  const d = new Date()
  const p = (n: number) => (n < 10 ? '0' + n : '' + n)
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

/* ---------------- 会话重命名 ---------------- */
const renameId = ref('')
const renameValue = ref('')
function openRename(id: string) {
  const s = sessionList.value.find((x) => x.id === id)
  if (!s) return
  renameId.value = id
  renameValue.value = s.title
}
function closeRename() {
  renameId.value = ''
  renameValue.value = ''
}
async function confirmRename() {
  const v = renameValue.value.trim()
  if (!v) {
    uni.showToast({ title: '名称不能为空', icon: 'none' })
    return
  }
  try {
    await _post({ url: `/ai/chat/session/${renameId.value}/rename`, data: { title: v.slice(0, 20) } })
    const s = sessionList.value.find((x) => x.id === renameId.value)
    if (s) s.title = v.slice(0, 20)
  } catch (e) {
    uni.showToast({ title: '重命名失败', icon: 'none' })
  }
  closeRename()
}

function removeSession(id: string) {
  uni.showModal({
    title: '提示',
    content: '确定删除该会话记录吗？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await _delete({ url: `/ai/chat/session/${id}` })
        sessionList.value = sessionList.value.filter((s) => s.id !== id)
        if (currentId.value === id) createSession()
      } catch (e) {
        uni.showToast({ title: '删除失败', icon: 'none' })
      }
    }
  })
}

/* ---------------- 发送（真实 /ai/ask） ---------------- */
async function send(q?: string, opts?: { withContext?: boolean; silent?: boolean }) {
  let content = q ?? inputText.value.trim()
  // 澄清选项选择：带上上一条用户问题合成（"上个月法兰盘产量是多少（法兰盘DN15）"），保留时间/范围上下文
  if (opts?.withContext) {
    const lastUser = [...messages.value].reverse().find((m) => m.role === 'user')
    if (lastUser && lastUser.content !== content) {
      content = `${lastUser.content}（${content}）`
    }
    // 澄清是一次性交互：点击后本条卡片失效，防止重复触发
    const lastClarify = [...messages.value].reverse().find((m) => m.clarify)
    if (lastClarify) {
      lastClarify.clarify = null
    }
  }
  if (!content || loading.value) return
  if (isFull.value) {
    uni.showToast({ title: '当前会话已达上限，请开启新会话', icon: 'none', duration: 2500 })
    return
  }
  inputText.value = ''

  // 空草稿首次发送 → 先创建后端会话（空会话永不落库）
  if (!currentId.value) {
    try {
      const res: any = await _post({ url: '/ai/chat/session' })
      const s: any = res?.data
      if (!s?.sessionId) {
        uni.showToast({ title: '创建会话失败', icon: 'none' })
        return
      }
      currentId.value = s.sessionId
      sessionList.value.unshift({ id: s.sessionId, title: s.title || '新会话', msgCount: 0, lastTime: s.lastMsgTime || '' })
    } catch (e) {
      uni.showToast({ title: '网络异常，请稍后再试', icon: 'none' })
      return
    }
  }

  // 重试时不重复上屏用户问题（问题已在上一条气泡里）
  if (!opts?.silent) {
    messages.value.push({ role: 'user', content, time: nowStr() })
    scrollToBottom(true) // 用户消息上屏即滚动
  }
  loading.value = true
  const askSessionId = currentId.value
  try {
    const res: any = await _post({ url: '/ai/ask', data: { question: content, sessionId: askSessionId } })
    const vo: any = res?.data
    if (!vo) throw new Error('empty')
    messages.value.push({
      role: 'ai',
      content: vo.answer || '（未返回答案）',
      time: nowStr(),
      route: vo.route || null,
      evidence: vo.evidence || null,
      clarify: vo.clarify || null,
      intent: vo.intent || '',
      question: content
    })
    const s = sessionList.value.find((x) => x.id === askSessionId)
    if (s) {
      if (s.title === '新会话') s.title = content.length > 12 ? content.slice(0, 12) + '…' : content
      s.msgCount = (s.msgCount || 0) + 2
      s.lastTime = nowStr()
    }
    loadSessions() // 与服务端同步（后端为权威）
    if (s && (s.msgCount || 0) >= MAX_MSGS) uni.showToast({ title: '当前会话已达上限', icon: 'none' })
  } catch (e) {
    // 失败就地留痕（气泡 + 重试），不再只弹一个转瞬即逝的 toast
    messages.value.push({
      role: 'ai',
      failed: true,
      content: '',
      question: content,
      time: nowStr()
    })
    loadSessions() // 失败也同步（可能后端已落库）
  } finally {
    loading.value = false
    lastMsgId.value = 'msg-' + (messages.value.length - 1)
    scrollToBottom() // AI 消息上屏后滚动（用户正上翻看历史时不打断）
  }
}

/** 失败重试：移除失败卡 → 用同一问题重新问（不重复上屏用户问题） */
function retryMessage(index: number) {
  if (loading.value) return // 上一轮还在查询中，避免把失败卡删掉后无事发生
  const m = messages.value[index]
  if (!m || !m.failed || !m.question) return
  messages.value.splice(index, 1)
  send(m.question, { silent: true })
}

/* ---------------- 其他 ---------------- */
function formatTime(t: string) {
  if (!t) return ''
  // iOS 兼容：空格替换为 T
  const d = new Date(t.replace(' ', 'T'))
  if (isNaN(d.getTime())) return t.slice(5, 16)
  const now = new Date()
  const p = (n: number) => (n < 10 ? '0' + n : '' + n)
  const hm = `${p(d.getHours())}:${p(d.getMinutes())}`
  const sameDay = d.toDateString() === now.toDateString()
  if (sameDay) return hm
  const yesterday = new Date(now.getTime() - 86400000)
  if (d.toDateString() === yesterday.toDateString()) return '昨天 ' + hm
  if (d.getFullYear() === now.getFullYear()) return `${p(d.getMonth() + 1)}-${p(d.getDate())} ` + hm
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ` + hm
}
function openSessions() {
  loadSessions()
  showSessions.value = true
}
function handleBack() {
  // 深链 / 整页重载直接落在本页时页面栈只有 1 个页面，navigateBack 无处可回（按钮像失灵）
  // → 兜底回工作台（启动时已落地 tab 状态，pages/main 可正常渲染）
  if (getCurrentPages().length <= 1) {
    uni.reLaunch({ url: '/pages/main' })
    return
  }
  uni.navigateBack()
}

/* ---------------- 顶部导航安全区（状态栏高度 + 微信胶囊避让） ---------------- */
const headerStyle = ref<Record<string, string>>({})
const headerInnerStyle = ref<Record<string, string>>({})
/* H5 键盘：只存在于浏览器（小程序端 textarea 由 adjust-position 原生处理） */
let onViewportChange: (() => void) | null = null

/**
 * 页面参数：从本体详情/能力卡片跳进来时携带 ?q=问题
 * 用 uni-app 标准的 onLoad(options)（H5 与小程序都可靠；之前用 getCurrentPages 在 H5 下取不到）
 */
onLoad((options: any) => {
  const q = options && (options.q || options.question)
  if (q) {
    pendingQ.value = decodeURIComponent(String(q))
    inputText.value = pendingQ.value
  }
})

onMounted(() => {
  // 兜底：某些端上 onLoad 参数在挂载后才就绪，或输入框被初始化逻辑清空 → 稍后再补一次
  if (pendingQ.value) {
    inputText.value = pendingQ.value
    setTimeout(() => {
      if (pendingQ.value && !inputText.value) {
        inputText.value = pendingQ.value
      }
    }, 400)
  }
  // 推荐问题：先按全局池随机取 5 条，随后异步补入"带真实业务名"的问法（数据图 5 分钟缓存）
  rollExamplesGlobal()
  loadDataGraph()

  const sys = uni.getSystemInfoSync()
  const sbh = (sys.statusBarHeight as number) || 0
  let navH = 44 // 导航内容高度(px)，H5 端无状态栏
  let rightPad = 24 // 右侧留白(px)
  // #ifdef MP-WEIXIN
  try {
    const rect = uni.getMenuButtonBoundingClientRect()
    navH = (rect.top - sbh) * 2 + (rect.bottom - sbh)
    rightPad = sys.windowWidth - rect.left + 8
  } catch (e) {}
  // #endif
  headerStyle.value = { height: sbh + navH + 'px' }
  headerInnerStyle.value = { height: navH + 'px', paddingTop: sbh + 'px', paddingRight: rightPad + 'px' }

  // #ifdef H5
  // 移动端浏览器/微信内置浏览器键盘弹起时，fixed 输入栏不会自动上移 → 按可视视口高度补偏移
  const vv: any = (window as any).visualViewport
  onViewportChange = () => {
    const offset = vv ? Math.max(0, Math.round(window.innerHeight - vv.height - (vv.offsetTop || 0))) : 0
    kbOffset.value = offset
    if (offset > 0) scrollToBottom(true) // 键盘顶起后跟到最后一条
    measureBody()
  }
  window.addEventListener('resize', onViewportChange)
  if (vv) {
    vv.addEventListener('resize', onViewportChange)
    vv.addEventListener('scroll', onViewportChange)
  }
  measureBody()
  // #endif
})

onUnmounted(() => {
  // #ifdef H5
  if (onViewportChange) {
    window.removeEventListener('resize', onViewportChange)
    const vv: any = (window as any).visualViewport
    if (vv) {
      vv.removeEventListener('resize', onViewportChange)
      vv.removeEventListener('scroll', onViewportChange)
    }
  }
  // #endif
})

// 初始化：拉取会话列表（进入为内存空草稿，不创建会话）
loadSessions()
</script>

<style lang="scss" scoped>
.ask-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f3f3f5;
  overflow: hidden;
}
/* 标题栏 */
.ask-header {
  flex-shrink: 0;
  .ask-header-inner {
    display: flex;
    align-items: center;
    padding-left: px2vw(8);
    position: relative;
    .ask-header-left {
      width: px2vw(48);
      height: px2vw(48);
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .ask-header-title {
      position: absolute;
      left: 50%;
      transform: translateX(-50%);
      font-size: px2vw(32);
      color: #333;
      font-weight: bold;
    }
  }
}
/* 操作栏：两个按钮 */
.ask-actions {
  display: flex;
  margin: px2vw(16) px2vw(32) 0;
  flex-shrink: 0;
  .ask-action-btn {
    flex: 1;
    height: px2vw(76);
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: px2vw(14);
    font-size: px2vw(28);
    font-weight: 500;
  }
  .ask-action-btn + .ask-action-btn {
    margin-left: px2vw(16);
  }
  .ask-action-btn-primary {
    background: #0066ff;
    color: #fff;
  }
  .ask-action-btn-ghost {
    background: #eaf3ff;
    color: #0066ff;
  }
}
/* 消息区：无内边距，滚动条贴屏幕右缘；左右间距由内容自身 margin 留 */
.ask-body {
  flex: 1;
  overflow: hidden;
  box-sizing: border-box;
}
.ask-session-info {
  margin: px2vw(16) px2vw(32) 0;
  .ask-session-info-text {
    font-size: px2vw(22);
    color: #999;
  }
}
.ask-empty {
  margin: px2vw(48) px2vw(32) 0;
  .ask-empty-intro {
    display: block;
    font-size: px2vw(26);
    color: #5a6f82;
    line-height: 1.6;
    margin-bottom: px2vw(32);
  }
  .ask-empty-title {
    font-size: px2vw(28);
    color: #666;
    display: block;
  }
  /* 标题行：左侧说明 + 右侧「换一批」 */
  .ask-empty-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: px2vw(16);
  }
  .ask-roll {
    font-size: px2vw(26);
    color: #0066ff;
    padding: px2vw(6) px2vw(16);
    background: #eaf2ff;
    border-radius: px2vw(24);
  }
  .ask-example {
    background: #fff;
    border-radius: px2vw(16);
    padding: px2vw(20) px2vw(24);
    margin-bottom: px2vw(12);
    font-size: px2vw(28);
    color: #333;
  }
}
/* 失败态（本地未落库）：可就地重试 */
.ask-fail {
  display: flex;
  align-items: center;
  background: #fff5f5;
  border: 1rpx solid #ffd9d9;
  border-radius: px2vw(16);
  padding: px2vw(20) px2vw(24);
  max-width: 82%;
  .ask-fail-text {
    flex: 1;
    font-size: px2vw(26);
    color: #c0392b;
    line-height: 1.5;
  }
  .ask-fail-retry {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    gap: px2vw(4);
    margin-left: px2vw(24);
    padding: px2vw(8) px2vw(20);
    border: 1rpx solid #0066ff;
    border-radius: px2vw(24);
    .ask-fail-retry-text {
      font-size: px2vw(24);
      color: #0066ff;
    }
  }
}
.ask-msg {
  margin: px2vw(24) px2vw(32) 0;
  display: flex;
  flex-direction: column;
  &.ask-msg-user {
    align-items: flex-end;
  }
  &.ask-msg-ai {
    align-items: flex-start;
  }
  .ask-msg-time {
    font-size: px2vw(20);
    color: #b0b0b0;
    margin-bottom: px2vw(8);
  }
  .ask-msg-bubble {
    border-radius: px2vw(16);
    padding: px2vw(20) px2vw(24);
    font-size: px2vw(28);
    color: #333;
    max-width: 80%;
    word-break: break-all;
  }
  .ask-msg-bubble-user {
    background: #0066ff;
    color: #fff;
  }
  .ask-msg-bubble-ai {
    background: #fff;
  }
}
.ask-body-pad {
  height: px2vw(120);
}
/* 回到最新（悬浮在输入区上方；仅用户上翻时出现） */
.ask-jump {
  position: fixed;
  right: px2vw(32);
  bottom: calc(px2vw(140) + env(safe-area-inset-bottom));
  z-index: 11;
  display: flex;
  align-items: center;
  gap: px2vw(4);
  padding: px2vw(10) px2vw(20);
  background: #fff;
  border: 1rpx solid #dbe8ff;
  border-radius: px2vw(28);
  box-shadow: 0 px2vw(2) px2vw(12) rgba(0, 102, 255, 0.12);
  .ask-jump-text {
    font-size: px2vw(22);
    color: #0066ff;
  }
}
/* 输入区：固定底部，白色卡片 + 边框 */
.ask-input-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 10;
  padding: px2vw(12) px2vw(24) calc(px2vw(12) + env(safe-area-inset-bottom));
  .ask-input-box {
    display: flex;
    align-items: center;
    background: #fff;
    border: 1rpx solid #e5e5e5;
    border-radius: px2vw(20);
    padding: px2vw(8);
    box-shadow: 0 px2vw(2) px2vw(12) rgba(0, 0, 0, 0.04);
    .ask-input-wrap {
      flex: 1;
      min-width: 0;
      max-height: px2vw(200); // 上限约 3 行，超出内部滚动
      overflow-y: auto;
      background: #f5f6f8;
      border: 1rpx solid #eceef1;
      border-radius: px2vw(14);
      .ask-input {
        width: 100%;
        box-sizing: border-box;
        min-height: px2vw(76); // 单行高度 = 与发送按钮同高
        padding: px2vw(12) px2vw(20);
        font-size: px2vw(28);
        line-height: px2vw(52);
      }
    }
    .ask-send {
      align-self: stretch; // 高度跟随输入框（单行/多行始终等高）
      margin-left: px2vw(12);
      background: #0066ff;
      border: 1rpx solid #0066ff;
      border-radius: px2vw(14);
      padding: 0 px2vw(32);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      text {
        color: #fff;
        font-size: px2vw(28);
      }
    }
    .ask-send-disabled {
      opacity: 0.5;
    }
  }
}
/* 会话记录弹层（底部） */
.ask-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 20;
}
.ask-sessions {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  border-radius: px2vw(24) px2vw(24) 0 0;
  padding-bottom: env(safe-area-inset-bottom);
  .ask-sessions-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: px2vw(24) px2vw(32);
    .ask-sessions-title {
      font-size: px2vw(32);
      font-weight: bold;
      color: #333;
    }
    .ask-sessions-new {
      font-size: px2vw(26);
      color: #fff;
      background: #0066ff;
      padding: px2vw(12) px2vw(28);
      border-radius: px2vw(12);
      font-weight: 500;
    }
  }
  .ask-sessions-list {
    max-height: 60vh;
    box-sizing: border-box;
  }
  /* 会话搜索 */
  .ask-sessions-search-wrap {
    display: flex;
    align-items: center;
    margin: 0 px2vw(32) px2vw(16);
    background: #f3f3f5;
    border-radius: px2vw(12);
    padding: 0 px2vw(20);
    .ask-sessions-search {
      flex: 1;
      height: px2vw(68);
      font-size: px2vw(26);
      color: #333;
    }
    .ask-sessions-search-clear {
      font-size: px2vw(24);
      color: #0066ff;
      padding-left: px2vw(16);
    }
  }
  .ask-session-item {
    position: relative;
    display: flex;
    align-items: center;
    padding: px2vw(24) px2vw(32);
    border-bottom: 1rpx solid #f0f0f0;
    .ask-session-item-main {
      flex: 1;
      min-width: 0;
      overflow: hidden;
      .ask-session-item-title-row {
        display: flex;
        align-items: center;
        .ask-session-title {
          /* 超长省略（不再写死宽度，避免与右侧按钮重叠） */
          max-width: px2vw(300);
          font-size: px2vw(28);
          color: #333;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
        .ask-session-current {
          flex-shrink: 0;
          margin-left: px2vw(12);
          padding: 0 px2vw(8);
          font-size: px2vw(20);
          color: #0066ff;
          background: #eaf3ff;
          border-radius: px2vw(6);
        }
      }
      .ask-session-meta {
        font-size: px2vw(22);
        color: #999;
        margin-top: px2vw(8);
        display: block;
      }
    }
    /* 右侧操作：改名 + 删除（弹性布局，不再绝对定位） */
    .ask-session-item-ops {
      display: flex;
      align-items: center;
      flex-shrink: 0;
      margin-left: px2vw(16);
      .ask-session-rename {
        background: #eaf3ff;
        color: #0066ff;
        font-size: px2vw(24);
        padding: px2vw(6) px2vw(16);
        border-radius: px2vw(8);
        flex-shrink: 0;
      }
      .ask-session-op {
        font-size: px2vw(24);
        padding: px2vw(8) 0 px2vw(8) px2vw(24);
      }
      .ask-session-op-del {
        color: #ee6258;
      }
    }
  }
  .ask-session-item-active {
    .ask-session-title {
      color: #0066ff;
    }
  }
  .ask-sessions-empty {
    padding: px2vw(80) 0;
    text-align: center;
    font-size: px2vw(26);
    color: #999;
  }
}
/* 重命名弹层（位于会话弹层之上） */
.ask-mask-rename {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 30;
  .ask-rename {
    position: absolute;
    left: px2vw(32);
    right: px2vw(32);
    top: 50%;
    transform: translateY(-50%);
    background: #fff;
    border-radius: px2vw(16);
    padding: px2vw(32);
    .ask-rename-title {
      font-size: px2vw(30);
      font-weight: bold;
      color: #333;
    }
    .ask-rename-input {
      margin-top: px2vw(24);
      height: px2vw(80);
      background: #f3f3f5;
      border-radius: px2vw(12);
      padding: 0 px2vw(24);
      font-size: px2vw(28);
    }
    .ask-rename-btns {
      display: flex;
      margin-top: px2vw(32);
      .ask-rename-btn {
        flex: 1;
        height: px2vw(80);
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: px2vw(12);
        font-size: px2vw(28);
      }
      .ask-rename-btn-cancel {
        background: #f3f3f5;
        color: #666;
        margin-right: px2vw(16);
      }
      .ask-rename-btn-ok {
        background: #0066ff;
        color: #fff;
      }
    }
  }
}
/* H5 端滚动条美化（与客服页一致：细滚动条、浅轨道、半透明滑块） */
.ask-body,
.ask-sessions-list {
  ::-webkit-scrollbar {
    width: px2vw(6) !important;
    height: px2vw(6) !important;
    background-color: #f3f3f5;
  }
  ::-webkit-scrollbar-track {
    border-radius: px2vw(3);
    background-color: #f3f3f5;
  }
  ::-webkit-scrollbar-thumb {
    border-radius: px2vw(3);
    background: rgba(61, 130, 234, 0.3);
  }
}
</style>
