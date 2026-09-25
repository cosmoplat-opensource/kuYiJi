<template>
  <view class="ontology-page">
    <view class="own-header">
      <text class="back" @tap="goBack">‹</text>
      <text class="own-title">业务本体</text>
    </view>

    <view class="fixed-area">
      <view class="row1">
        <view v-for="it in statItems" :key="it.lbl" class="stat">
          <text class="num">{{ it.num }}</text><text class="lbl">{{ it.lbl }}</text>
        </view>
        <text class="ver">{{ verText }}</text>
      </view>

      <!-- 视图切换：做成明显的分段开关（之前一个按钮看不出能点） -->
      <view class="seg">
        <text class="seg-item" :class="{ on: mode === 'data' }" @tap="setMode('data')">数据视图（真实记录）</text>
        <text class="seg-item" :class="{ on: mode === 'struct' }" @tap="setMode('struct')">结构视图（本体类型）</text>
      </view>

      <view class="actions">
        <text class="btn" :class="{ on: view === 'graph' }" @tap="showGraph">关系图</text>
        <text class="btn" :class="{ on: view === 'list' }" @tap="view = 'list'">列表</text>
        <text v-if="mode === 'struct'" class="btn" :class="{ on: showCapability }" @tap="toggleCapability">能力层:{{ showCapability ? '显示' : '隐藏' }}</text>
        <text v-if="mode === 'struct'" class="btn" :class="{ on: showLineage }" @tap="toggleLineage">口径血缘:{{ showLineage ? '显示' : '隐藏' }}</text>
        <text class="btn" :class="{ on: animate }" @tap="animate = !animate">流动:{{ animate ? '开' : '关' }}</text>
        <text class="btn" :class="{ on: edgesAlways }" @tap="toggleEdges">{{ edgesAlways ? '关系线:全部' : '关系线:选中显示' }}</text>
        <text class="btn" :class="{ on: showEdgeLabel }" @tap="showEdgeLabel = !showEdgeLabel">{{ showEdgeLabel ? '关系名:显示' : '关系名:隐藏' }}</text>
        <text v-for="s in stories" :key="s.code" class="btn story" @tap="playStory(s)">▶ {{ s.name }}</text>
      </view>

      <view v-if="view === 'graph'" class="pad">
        <!-- 第一行：操作按钮 -->
        <view class="pad-row">
          <text class="pad-btn zoom" @tap="zoomIn">🔍 放大</text>
          <text class="pad-btn zoom" @tap="zoomOut">🔍 缩小</text>
          <text class="pad-btn wide" @tap="fitView">看全貌</text>
        </view>
        <!-- 第二行起：时间窗口（数据视图；当前生效的窗口高亮显示）；详情模式下收起，把空间让给关系图 -->
        <view v-if="mode === 'data' && !current" class="pad-row">
          <text class="pad-btn wide" :class="{ on: windowKey === 'd7' }" @tap="setWindow('d7')">近 7 天</text>
          <text class="pad-btn wide" :class="{ on: windowKey === 'd30' }" @tap="setWindow('d30')">近 30 天</text>
          <text class="pad-btn wide" :class="{ on: windowKey === 'month' }" @tap="setWindow('month')">本月</text>
          <text class="win-tip">当前：{{ windowLabel }}</text>
        </view>
        <!-- 图例（随视图自适应）；详情模式下收起 -->
        <view v-if="!current" class="legend">
          <text v-for="it in legendItems" :key="it.text" class="lg">
            <text class="dot" :style="'background:' + it.color" />{{ it.text }}
          </text>
        </view>
        <view v-if="!current && envNote" class="env-note">{{ envNote }}</view>
      </view>
      <view v-if="errMsg" class="err">{{ errMsg }}</view>
    </view>

    <view v-if="view === 'graph'" class="gouter"
          @touchstart="onTouchStart" @touchmove.stop.prevent="onTouchMove" @touchend="onTouchEnd" @touchcancel="onTouchEnd"
          @tap="onBlankTap">
      <view class="ginner" :style="innerStyle">
        <!-- 连线（业务关系带流向小点；血缘为虚线） -->
        <view v-for="e in edges" :key="'e' + e.key" class="edge" :style="e.style">
          <view v-if="e.flow" class="flow" :style="e.flowStyle" />
        </view>
        <view v-for="l in edgeLabels" :key="'l' + l.key" class="elabel" :style="l.style">
          <text class="el-dot" :style="'background:' + l.color" />
          <text class="el-text">{{ l.text }}</text>
        </view>
        <!-- 节点 -->
        <view v-for="n in gnodes" :key="n.id" class="gnode" :class="{ pulse: n.pulse }" :style="n.style" @tap.stop="tapNode(n.id)">
          <text class="gnode-text">{{ n.name }}</text>
        </view>
      </view>
    </view>

    <view v-if="view === 'list'" class="list-wrap">
      <view v-for="grp in groups" :key="grp.title" class="group">
        <view class="group-title">{{ grp.title }}</view>
        <view v-for="n in grp.items" :key="n.id" class="item" @tap="tapNode(n.id)">
          <text class="item-name">{{ n.name }}</text>
        </view>
      </view>
      <view class="group">
        <view class="group-title">关系（数据怎么连）</view>
        <view v-for="(l, i) in relationRows" :key="i" class="link">
          <text class="lk">{{ l.fromName }}</text>
          <text class="lk-label">{{ l.label }}</text>
          <text class="lk">{{ l.toName }}</text>
          <text v-if="l.traversable" class="badge">可追问</text>
          <text v-else-if="l.lineage" class="badge gray">血缘</text>
        </view>
      </view>
      <view class="foot">{{ note }}（计划域 {{ summary.planningEntities || 0 }} 个实体已隐藏）</view>
    </view>

    <view v-if="current" class="sheet">
      <view class="sheet-head">
        <text class="sheet-name">{{ current.name }}</text>
        <text class="sheet-kind">{{ currentKind }}</text>
        <text class="sheet-close" @tap="current = null">收起</text>
      </view>
      <view v-if="current.formula" class="sheet-line">口径：{{ current.formula }}</view>
      <view v-if="current.note" class="sheet-line">{{ current.note }}</view>
      <view v-for="(f, i) in currentFacts" :key="'f' + i" class="sheet-line">{{ f }}</view>
      <!-- 实例节点 → 对回本体实体 + 该实体登记的能力（业务口径，不展示表名/字段等开发信息） -->
      <view v-if="currentOntology" class="onto-link">
        <text class="ol-title">属于：<text class="ol-entity">{{ currentOntology.name }}</text></text>
        <text v-if="currentEntityCaps.length" class="ol-caps-title">相关能力（{{ currentEntityCaps.length }}）——点一条直接问：</text>
        <text v-for="c in currentEntityCaps" :key="c.id" class="ol-cap" @tap="askCap(c)">
          {{ c.kind === 'operator' ? '分析' : '指标' }} · {{ c.name }}<text v-if="c.ask" class="ol-ask">　问：{{ c.ask }}</text>
        </text>
      </view>
      <!-- 产品：工序流转（按首次报工时间排序 = 真实流程顺序） -->
      <view v-if="productFlow.length" class="flow-strip">
        <text class="flow-title">工序流转（按实际报工先后）：</text>
        <view class="flow-line">
          <template v-for="(f, i) in productFlow" :key="f.id">
            <text class="flow-node" @tap="tapNode(f.id)">
              {{ f.name }}<text class="flow-sub">{{ f.submitCnt }}条{{ f.passRate != null ? ' · ' + (f.passRate * 100).toFixed(1) + '%' : '' }}</text>
            </text>
            <text v-if="i < productFlow.length - 1" class="flow-arrow">▶</text>
          </template>
        </view>
      </view>
      <view v-if="current.fields && current.fields.length" class="chips">
        <text v-for="f in current.fields" :key="f" class="chip">{{ f }}</text>
      </view>
      <view v-if="currentExamples.length" class="examples">
        <text v-for="q in currentExamples" :key="q" class="ex" @tap="tryAsk(q)">问：{{ q }}</text>
      </view>
      <view v-if="relatedCaps.length" class="caps">
        <text class="caps-title">与它相关的能力（{{ relatedCaps.length }}）</text>
        <text v-for="c in relatedCaps" :key="c.id" class="cap" @tap="current = c">{{ c.kind === 'operator' ? '算子' : '指标' }} · {{ c.name }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { _get } from '@/utils/common-request'

const COLORS = { master: '#2563eb', action: '#7c3aed', result: '#059669', metric: '#0284c7', operator: '#d97706',
  product: '#2563eb', process: '#7c3aed', employee: '#0d9488' }
// 节点尺寸（px，逻辑坐标空间）
const NW = 88
const NH = 36
// 不重叠所需的最小间距（含留白）
const MIN_DX = 104
const MIN_DY = 50

export default {
  setup() {
    const nodes = ref([])
    const links = ref([])
    const stories = ref([])
    const summary = ref({})
    const note = ref('')
    const ontologyVersion = ref('')
    const current = ref(null)
    const view = ref('graph')
    const days = ref(30)
    /** 关系线展示策略：false = 只在选中节点时显示它的关系（默认，避免交织）；true = 全部显示 */
    const edgesAlways = ref(false)
    const showEdgeLabel = ref(false)
    const errMsg = ref('')
    const showCapability = ref(false)
    const showLineage = ref(false)
    const animate = ref(true)
    const lastEvt = ref('无')
    const zr = reactive({ story: null })

    const win = uni.getWindowInfo ? uni.getWindowInfo() : uni.getSystemInfoSync()
    const vw = Math.round(win.windowWidth || 375)
    const vh = Math.round(win.windowHeight || 667)
    // 逻辑坐标空间：取视口的 1.5 倍 → 节点有足够空间铺开（重叠大幅减少）；
    // "看全貌" = 缩放到 fitScale（正好铺满可视区），所以放大画布不会产生边界/裁切问题。
    const W = Math.round(vw * 1.5)
    const H = Math.max(560, Math.round((vh - 320) * 1.5))
    // 详情窗打开时图区会被压缩 → 适配比例随之变小（图始终完整可见，不被详情窗遮住）
    // 详情模式下顶部工具条会收起（见模板 v-if="!current"），所以这里多留一些空间给图
    const availH = computed(() => Math.max(230, (vh - 320) - (current.value ? 130 : 0)))
    const fitScale = computed(() => {
      const k = Math.min(vw / W, availH.value / H)
      // 下限 0.45：保证节点文字仍然可读（更小就只能靠放大/拖动了）
      return +Math.max(0.45, Math.min(1, k)).toFixed(3)
    })

    const tx = ref(0)
    const ty = ref(0)
    const scale = ref(fitScale.value)
    const innerStyle = computed(() =>
      `position:absolute; left:50%; top:50%; width:${W}px; height:${H}px;`
      + ` margin-left:${-W / 2}px; margin-top:${-H / 2}px;`
      + ` transform: translate(${tx.value}px,${ty.value}px) scale(${scale.value}); transform-origin:50% 50%;`)

    /* ---------- 布局：椭圆种子 + 确定性"去重叠"松弛（无随机数 → 每次一致） ---------- */
    const layout = computed(() => {
      // 实例节点（产品/工序/员工）始终显示；指标/算子只有打开"能力层"才显示
      const visible = nodes.value.filter((n) => showCapability.value || (n.level !== 'metric'))
      const ids = visible.map((n) => n.id)
      const useLinks = links.value.filter((l) => {
        if (ids.indexOf(l.from) < 0 || ids.indexOf(l.to) < 0) {
          return false
        }
        return l.kind === 'lineage' ? showLineage.value : true
      })
      const cx = W / 2
      const cy = H / 2
      const P = {}
      if (mode.value === 'data') {
        // 【数据视图：三段式列布局】产品 | 工序 | 员工
        // 数据图本质是"产品→工序←员工"的二部图，分层列布局可让连线几乎零交叉（力导向做不到）
        const colOf = { product: 0, process: 1, employee: 2 }
        const colX = [Math.round(W * 0.16), Math.round(W * 0.5), Math.round(W * 0.84)]
        const byCol = [[], [], []]
        visible.forEach((n) => {
          const c = colOf[n.level] == null ? 1 : colOf[n.level]
          byCol[c].push(n)
        })
        byCol.forEach((list, ci) => {
          list.sort((a, b) => (b.submitCnt || 0) - (a.submitCnt || 0))
          const gap = H / (list.length + 1)
          list.forEach((n, i) => {
            P[n.id] = {
              x: colX[ci] + (ci === 1 ? 0 : Math.sin((i + 1) * 2.399) * 10),
              y: Math.round(gap * (i + 1))
            }
          })
        })
      } else {
        // 【结构视图：类型环 + 力导向】主数据外环 / 现场动作中环 / 结果内环 / 能力最外环
        const ring = { master: 0.85, action: 0.5, result: 0.2, metric: 0.98,
          product: 0.22, process: 0.58, employee: 0.98 }
        const buckets = {}
        visible.forEach((n) => {
          const key = n.level == null ? 'master' : n.level
          ;(buckets[key] = buckets[key] || []).push(n)
        })
        Object.keys(buckets).forEach((level) => {
          const list = buckets[level]
          const f = ring[level] == null ? 0.6 : ring[level]
          const rx = (W / 2 - 62) * f
          const ry = (H / 2 - 54) * f
          list.forEach((n, i) => {
            const ang = (Math.PI * 2 * i) / Math.max(1, list.length) - Math.PI / 2 + (level === 'master' ? 0 : 0.4)
            P[n.id] = {
              x: cx + Math.cos(ang) * rx + Math.sin((i + 1) * 2.399) * 16,
              y: cy + Math.sin(ang) * ry + Math.cos((i + 1) * 1.777) * 12
            }
          })
        })
      }
      // 松弛：力导向（有关系用弹簧拉近 → 形成可读团簇）+ 去重叠 + 边界收敛，全部确定性（无随机数）
      const idx = {}
      const arr = ids.map((id, i) => {
        idx[id] = i
        return { x: P[id].x, y: P[id].y, vx: 0, vy: 0 }
      })
      const pairs = useLinks.map((l) => ({ a: idx[l.from], b: idx[l.to] })).filter((p) => p.a != null && p.b != null)
      // 每个节点的连接数（0 = 孤立节点，用"归环"策略排布，避免被斥力推到画布边缘）
      const degree = new Array(arr.length).fill(0)
      pairs.forEach((p) => {
        degree[p.a]++
        degree[p.b]++
      })
      const L = Math.max(130, Math.min(200, Math.sqrt((W * H) / Math.max(4, arr.length)) * 0.5))
      for (let it = 0; it < 240; it++) {
        const fx = new Array(arr.length).fill(0)
        const fy = new Array(arr.length).fill(0)
        for (let i = 0; i < arr.length; i++) {
          for (let j = i + 1; j < arr.length; j++) {
            let dx = arr[j].x - arr[i].x
            let dy = arr[j].y - arr[i].y
            let d = Math.sqrt(dx * dx + dy * dy)
            if (d < 0.01) {
              dx = 0.5
              dy = 0.5
              d = 0.7
            }
            const rep = Math.min(2.6, 9000 / (d * d))
            const ux = dx / d
            const uy = dy / d
            fx[i] -= rep * ux
            fy[i] -= rep * uy
            fx[j] += rep * ux
            fy[j] += rep * uy
          }
        }
        pairs.forEach((p) => {
          const dx = arr[p.b].x - arr[p.a].x
          const dy = arr[p.b].y - arr[p.a].y
          const d = Math.max(0.01, Math.sqrt(dx * dx + dy * dy))
          const k = (d - L) * 0.035
          const ux = dx / d
          const uy = dy / d
          fx[p.a] += k * ux
          fy[p.a] += k * uy
          fx[p.b] -= k * ux
          fy[p.b] -= k * uy
        })
        for (let i = 0; i < arr.length; i++) {
          fx[i] += (cx - arr[i].x) * 0.004
          fy[i] += (cy - arr[i].y) * 0.004
          // 孤立节点（没有连线，例如"能力层开、血缘关"时的能力节点）：
          // 仅靠斥力会被推到四周、中间留空 → 额外施力使其稳定在核心外围的圆环上
          if (degree[i] === 0) {
            const dx = arr[i].x - cx
            const dy = arr[i].y - cy
            const d = Math.max(1, Math.sqrt(dx * dx + dy * dy))
            const ringR = Math.min(W, H) * 0.40
            const f = (d - ringR) * 0.03
            fx[i] -= (f * dx) / d
            fy[i] -= (f * dy) / d
          }
          arr[i].vx = (arr[i].vx + fx[i]) * 0.5
          arr[i].vy = (arr[i].vy + fy[i]) * 0.5
          const sp = Math.sqrt(arr[i].vx * arr[i].vx + arr[i].vy * arr[i].vy)
          if (sp > 12) {
            arr[i].vx = (arr[i].vx / sp) * 12
            arr[i].vy = (arr[i].vy / sp) * 12
          }
          arr[i].x += arr[i].vx
          arr[i].y += arr[i].vy
        }
        for (let i = 0; i < arr.length; i++) {
          for (let j = i + 1; j < arr.length; j++) {
            const a = arr[i]
            const b = arr[j]
            const dx = b.x - a.x
            const dy = b.y - a.y
            const ox = MIN_DX - Math.abs(dx)
            const oy = MIN_DY - Math.abs(dy)
            if (ox > 0 && oy > 0) {
              if (ox < oy) {
                const push = (ox / 2 + 0.5) * (dx >= 0 ? 1 : -1)
                a.x -= push
                b.x += push
              } else {
                const push = (oy / 2 + 0.5) * (dy >= 0 ? 1 : -1)
                a.y -= push
                b.y += push
              }
            }
          }
        }
        for (let i = 0; i < arr.length; i++) {
          arr[i].x = Math.min(W - NW / 2 - 8, Math.max(NW / 2 + 8, arr[i].x))
          arr[i].y = Math.min(H - NH / 2 - 8, Math.max(NH / 2 + 8, arr[i].y))
        }
      }
      const pos = {}
      ids.forEach((id, i) => {
        pos[id] = { x: Math.round(arr[i].x), y: Math.round(arr[i].y) }
      })
      return { visible, useLinks, pos }
    })

    const gnodes = computed(() => layout.value.visible.map((n) => {
      const p = layout.value.pos[n.id]
      const dim = zr.story && zr.story.indexOf(n.id) < 0
      const on = zr.story && zr.story.indexOf(n.id) >= 0
      // 体量分档：报工条数越多，节点越大（0.9~1.3 倍）；能力节点（指标/算子）画小一点，降低与业务节点的视觉冲突
      const cnt = n.submitCnt == null ? 0 : n.submitCnt
      const isCap = n.level === 'metric'
      const k = isCap ? 0.82 : (cnt >= 40 ? 1.3 : cnt >= 15 ? 1.12 : cnt >= 5 ? 1.0 : 0.9)
      const sel = current.value && current.value.id === n.id
      return {
        id: n.id,
        name: n.name,
        pulse: !!on || !!sel,
        style: `position:absolute; left:${p.x - NW / 2}px; top:${p.y - NH / 2}px; width:${NW}px; height:${NH}px;`
          + ` display:flex; align-items:center; justify-content:center; border-radius:18px; z-index:${sel ? 4 : 3};`
          + ` background:${COLORS[n.level] || COLORS.master}; opacity:${dim ? 0.16 : 1};`
          + ` transform:scale(${k});`
      }
    }))

    /** 选中某节点时：只强调与它相连的连线，其余淡化（彻底解决"看不出谁连谁"） */
    function touching(id) {
      if (!id) {
        return false
      }
      return true
    }

    const edges = computed(() => layout.value.useLinks.map((l, i) => {
      const a = layout.value.pos[l.from]
      const b = layout.value.pos[l.to]
      const seg = trimSegment(a, b)
      const deg = Math.atan2(seg.dy, seg.dx) * 180 / Math.PI
      const lineage = l.kind === 'lineage'
      // 高亮优先级：选中节点的相邻边 > 故事线路径 > 普通
      const selId = current.value ? current.value.id : null
      const incident = !!selId && (l.from === selId || l.to === selId)
      const storyDim = zr.story && !(zr.story.indexOf(l.from) >= 0 && zr.story.indexOf(l.to) >= 0)
      // 「关系线:选中显示」的淡化**只作用于业务关系**：血缘线的显隐由「口径血缘」开关决定。
      // （否则浅灰虚线再乘 0.22 透明度 → 实际看不见，表现为"开关点了没变化"）
      const quiet = !edgesAlways.value && !selId && !zr.story && !lineage
      const dim = selId ? !incident : (quiet ? true : storyDim)
      const emph = incident || (zr.story && !storyDim)
      // 血缘：中灰虚线、常态透明度 0.8（看得见但不抢戏）；选中节点时只留相邻血缘
      const opacityPart = lineage
        ? `opacity:${selId ? (incident ? 0.9 : 0.1) : 0.8};`
        : `opacity:${dim ? (quiet ? 0.22 : 0.12) : 1};`
      const linePart = lineage
        ? ' border-top:2rpx dashed #94a3b8;'
        : ` border-top:${emph ? 5 : 3}rpx solid ${emph ? '#1d4ed8' : (COLORS[srcOf(l.from)] || '#60a5fa')};`
      return {
        key: l.from + '>' + l.to + (lineage ? 'L' : 'R'),
        flow: animate.value && !lineage && !quiet,
        // 从节点"边框"开始画到另一个节点边框 → 连线不会被圆角方块盖住
        style: `position:absolute; left:${seg.x}px; top:${seg.y}px; width:${Math.round(seg.len)}px; height:0;`
          + ` transform: rotate(${deg.toFixed(1)}deg); transform-origin:0 0; z-index:${emph ? 2 : 1};`
          + opacityPart + linePart,
        flowStyle: `animation-delay:${(i % 6) * 0.35}s;`
      }
    }))

    /** 从节点 id 取它所属层级的颜色（连线按"源节点颜色"上色，与标签同色 → 一眼归属） */
    function srcOf(id) {
      const n = nodes.value.find((x) => x.id === id)
      return n ? n.level : null
    }

    /** 把"中心→中心"的连线裁到节点边框（圆角方块边界），避免线被节点遮挡 */
    function trimSegment(a, b) {
      const dx = b.x - a.x
      const dy = b.y - a.y
      const hw = NW / 2 + 4
      const hh = NH / 2 + 4
      const tx = Math.abs(dx) > 0.001 ? hw / Math.abs(dx) : 9
      const ty = Math.abs(dy) > 0.001 ? hh / Math.abs(dy) : 9
      const t = Math.min(Math.min(tx, ty), 0.45)
      const x1 = a.x + dx * t
      const y1 = a.y + dy * t
      const x2 = b.x - dx * t
      const y2 = b.y - dy * t
      const ddx = x2 - x1
      const ddy = y2 - y1
      return { x: Math.round(x1), y: Math.round(y1), dx: ddx, dy: ddy, len: Math.sqrt(ddx * ddx + ddy * ddy) }
    }

    const edgeLabels = computed(() => (showEdgeLabel.value ? layout.value.useLinks
      .filter((l) => l.kind !== 'lineage')
      .filter((l) => {
        // 选中某节点时只显示与它相连的关系名；未选中时按"关系线"开关决定是否全显
        const selId = current.value ? current.value.id : null
        if (selId) {
          return l.from === selId || l.to === selId
        }
        return edgesAlways.value
      })
      .map((l, li) => {
        const a = layout.value.pos[l.from]
        const b = layout.value.pos[l.to]
        const seg = trimSegment(a, b)
        // 标签沿连线法线左右交替偏移，减少两条线的标签叠在一起
        const nl = Math.max(0.001, seg.len)
        const nx = -seg.dy / nl
        const ny = seg.dx / nl
        // 关系名贴着线放（偏移 0）→ 看上去"长在线上"，归属一目了然
        const nOff = { x: 0, y: 0 }
        // 标签放在**靠源节点 32% 处**（而不是正中）→ 交叉线多时也能看出它属于哪条
        const t = 0.32
        const mx = seg.x + seg.dx * t
        const my = seg.y + seg.dy * t
        const src = nodes.value.find((x) => x.id === l.from) || {}
        // 选中产品时，给它的工序关系加上先后序号（① ② ③…按实际报工先后）
        let prefix = ''
        const sel = current.value
        if (sel && sel.level === 'product' && l.from === sel.id) {
          const step = productFlow.value.findIndex((f) => f.id === l.to)
          if (step >= 0) {
            prefix = ['①', '②', '③', '④', '⑤', '⑥', '⑦', '⑧', '⑨'][step] || ('(' + (step + 1) + ')')
          }
        }
        return {
          key: 'l' + l.from + '>' + l.to,
          text: prefix + l.label + ' ' + (l.submitCnt == null ? '' : l.submitCnt),
          color: COLORS[src.level] || '#2563eb',
          // 关系名沿连线法线左右交替偏移 ±15px，减少两条线标签压在一起
          style: `position:absolute; left:${Math.round(mx - 50 + nOff.x)}px; top:${Math.round(my - 16 + nOff.y)}px;`
            + ` width:100px; text-align:center; z-index:2;`
            + ` color:${COLORS[src.level] || '#2563eb'};`
        }
      }) : []))

    const counts = computed(() => ({
      entities: nodes.value.filter((n) => n.kind === 'entity').length,
      relations: links.value.filter((l) => l.kind !== 'lineage').length,
      lineage: links.value.filter((l) => l.kind === 'lineage').length,
      capabilities: nodes.value.filter((n) => n.level === 'metric').length
    }))

    /* ---------- 概览数字：随视图自适应（数据视图讲"真实记录数"，结构视图讲"本体规模"） ---------- */
    const statItems = computed(() => {
      if (mode.value === 'data') {
        return [
          { num: summary.value.products || 0, lbl: '产品' },
          { num: summary.value.processes || 0, lbl: '工序' },
          { num: summary.value.employees || 0, lbl: '员工' },
          { num: summary.value.links || 0, lbl: '关系' }
        ]
      }
      return [
        { num: counts.value.entities, lbl: '实体' },
        { num: counts.value.relations, lbl: '关系' },
        { num: counts.value.lineage, lbl: '血缘' },
        { num: counts.value.capabilities, lbl: '能力' },
        { num: summary.value.planningEntities || 0, lbl: '非本期' }
      ]
    })
    /** 右上角小字：数据视图显示数据时间，结构视图显示本体版本 */
    const verText = computed(() => (mode.value === 'data'
      ? (ontologyVersion.value || '')
      : ('v' + (ontologyVersion.value || '-'))))

    /** 结构视图数据（懒加载并缓存）：用于把"实例节点"对回本体实体、并查出该实体的口径与相关能力 */
    const structNodes = ref([])
    const structLinks = ref([])
    let structLoaded = false
    function ensureStruct() {
      if (structLoaded) {
        return
      }
      structLoaded = true
      _get({ url: '/ai/ontology/graph' }).then((res) => {
        const d = (res && (res.data || res)) || {}
        structNodes.value = d.nodes || []
        structLinks.value = d.links || []
      }).catch(() => {
        structLoaded = false
      })
    }

    /** 实例节点 → 本体实体（数据视图与结构视图的对应关系） */
    const ENTITY_BY_LEVEL = { product: 'product', process: 'process', employee: 'employee' }
    const currentOntology = computed(() => {
      const n = current.value
      if (!n || n.kind !== 'instance') {
        return null
      }
      const code = ENTITY_BY_LEVEL[n.level]
      if (!code) {
        return null
      }
      const e = structNodes.value.find((x) => x.id === code || x.id === ('entity:' + code))
      return { code: code, name: (e && e.name) || n.name, table: (e && e.table) || '' }
    })
    /** 该本体实体参与计算的能力（口径血缘反查） */
    const currentEntityCaps = computed(() => {
      const o = currentOntology.value
      if (!o) {
        return []
      }
      const ids = structLinks.value
        .filter((l) => l.kind === 'lineage' && l.from === o.code)
        .map((l) => l.to)
      return structNodes.value.filter((n) => ids.indexOf(n.id) >= 0).map((n) => ({
        id: n.id,
        name: n.name,
        kind: n.kind,
        formula: n.formula || '',
        ask: (n.examples && n.examples.length) ? n.examples[0] : ''
      }))
    })
    /** 点能力 → 用它登记的第一个示例问法直接去问数 */
    function askCap(c) {
      if (c && c.ask) {
        tryAsk(c.ask)
      } else {
        uni.showToast({ title: '该能力暂无示例问法', icon: 'none' })
      }
    }

    /** 未产生数据的业务环节（图外灰字提示：图能回答"哪些环节还没跑起来"） */
    const envNote = computed(() => {
      if (mode.value !== 'data') {
        return ''
      }
      const env = (summary.value && summary.value.env) || null
      if (!env) {
        return ''
      }
      const missing = []
      if (!env.qcCnt) {
        missing.push('质检记录')
      }
      if (!env.completeCnt) {
        missing.push('完工报告')
      }
      if (!env.wipNum) {
        missing.push('工序在制品')
      }
      if (!env.finishedNum) {
        missing.push('成品库存')
      }
      return missing.length ? ('本窗口未产生数据：' + missing.join('、')) : ''
    })

    /* ---------- 图例：随当前视图自适应（数据视图讲"真实记录"，结构视图讲"类型关系"） ---------- */
    const legendItems = computed(() => {
      if (mode.value === 'data') {
        return [
          { color: '#2563eb', text: '产品（如 端盖K-21）' },
          { color: '#7c3aed', text: '工序（如 攻丝/车削/下料）' },
          { color: '#0d9488', text: '员工（如 周中龙）' }
        ]
      }
      const items = [
        { color: '#2563eb', text: '产品/工序/员工/不良类型' },
        { color: '#7c3aed', text: '报工/质检/完工' },
        { color: '#059669', text: '在制/成品' }
      ]
      if (showCapability.value) {
        items.push({ color: '#0284c7', text: '指标' })
        items.push({ color: '#d97706', text: '分析算子' })
      }
      return items
    })
    const legendNotes = computed(() => {
      const notes = ['蓝点=数据流向（沿连线流动）']
      if (mode.value === 'data') {
        notes.push('关系名后的数字=该组合报工条数')
        notes.push('数字来自数据库实时统计（窗口 ' + (summary.value.days || 30) + ' 天）')
      } else {
        notes.push('实线=业务关系 · 虚线=口径血缘')
      }
      return notes
    })
    const diagText = computed(() => '本体页 v9 · 实体 ' + counts.value.entities + ' · 关系 ' + counts.value.relations
      + ' · 血缘 ' + counts.value.lineage + ' ｜ 窗口 ' + windowLabel.value
      + ' ｜ 缩放 ' + scale.value.toFixed(2) + ' ｜ 最近事件: ' + lastEvt.value)

    const groups = computed(() => [
      { title: '① 主数据（谁 / 什么）', items: nodes.value.filter((n) => n.level === 'master') },
      { title: '② 现场动作（做了什么）', items: nodes.value.filter((n) => n.level === 'action') },
      { title: '③ 结果（剩多少 / 成多少）', items: nodes.value.filter((n) => n.level === 'result') },
      { title: '④ 能力落点（AI 用本体答什么）', items: nodes.value.filter((n) => n.level === 'metric') }
    ])
    const nameOf = (id) => (nodes.value.find((n) => n.id === id) || {}).name || id
    const relationRows = computed(() => links.value.map((l) => ({
      fromName: nameOf(l.from),
      toName: nameOf(l.to),
      label: l.label,
      traversable: !!l.traversable,
      lineage: l.kind === 'lineage'
    })))
    const currentKind = computed(() => {
      const n = current.value
      if (!n) {
        return ''
      }
      if (n.kind === 'operator') {
        return '分析算子'
      }
      if (n.kind === 'metric') {
        return '登记指标'
      }
      // 实例节点按真实类型标注（周中龙→员工、端盖K-21→产品、攻丝→工序…）
      const byLevel = {
        product: '产品',
        process: '工序',
        employee: '员工',
        master: '主数据',
        action: '现场动作',
        result: '结果'
      }
      return byLevel[n.level] || (n.kind === 'instance' ? '业务数据' : '本体元素')
    })
    const relatedCaps = computed(() => {
      if (!current.value) {
        return []
      }
      const ids = links.value.filter((l) => l.kind === 'lineage' && l.from === current.value.id).map((l) => l.to)
      return nodes.value.filter((n) => ids.indexOf(n.id) >= 0)
    })

    /* ---------- 交互 ---------- */
    function tapNode(id) {
      const n = nodes.value.find((x) => x.id === id)
      lastEvt.value = 'tap:' + id
      current.value = n || null
      // 详情窗出现后图区变小 → 等 DOM 重排完再适配，保证关系图完整可见（不被窗遮住）
      setTimeout(fitView, 60)
    }
    function onBlankTap() {
      lastEvt.value = 'tap:blank'
      current.value = null
      setTimeout(fitView, 60)
    }
    function showGraph() {
      view.value = 'graph'
    }
    /** 关系线：选中显示（默认，画面干净）↔ 全部显示（看整体网） */
    function toggleEdges() {
      edgesAlways.value = !edgesAlways.value
      if (edgesAlways.value) {
        showEdgeLabel.value = true
      }
      lastEvt.value = 'lines:' + (edgesAlways.value ? 'all' : 'sel')
    }
    function zoomIn() {
      scale.value = Math.min(3, +(scale.value * 1.25).toFixed(3))
    }
    function zoomOut() {
      scale.value = Math.max(fitScale.value, +(scale.value * 0.8).toFixed(3))
    }
    function fitView() {
      tx.value = 0
      ty.value = 0
      // fitScale = min(视口宽/画布宽, 可用高/画布高) → 整块画布正好放进"图区剩余空间"
      scale.value = fitScale.value
    }
    function toggleCapability() {
      showCapability.value = !showCapability.value
      if (showCapability.value) {
        showLineage.value = true
        // 能力层打开时节点明显变多 → 自动关掉关系名，避免文字互相压（可手动再打开）
        showEdgeLabel.value = false
      }
      fitView()
    }
    function toggleLineage() {
      showLineage.value = !showLineage.value
      // 血缘线的两端是"能力节点"：只开血缘而不开能力层 → 端点不可见 → 一条都画不出来
      // （用户会以为开关失效）。所以打开血缘时自动带上能力层。
      if (showLineage.value && !showCapability.value) {
        showCapability.value = true
        uni.showToast({ title: '血缘线连到"能力"，已同时打开能力层', icon: 'none', duration: 2200 })
      }
      fitView()
    }
    function playStory(s) {
      const same = zr.story && zr.story[0] === s.steps[0]
      zr.story = same ? null : s.steps.slice()
      if (zr.story && !showCapability.value && s.steps.some((x) => x.indexOf('metric:') === 0)) {
        showCapability.value = true
        showLineage.value = true
        fitView()
      }
      lastEvt.value = zr.story ? 'story:' + s.code : 'story:off'
    }
    function tryAsk(q) {
      uni.navigateTo({ url: `/pages-analysis/ask/index?q=${encodeURIComponent(q)}` })
    }
    function goBack() {
      // 先看页面栈：栈里有上一页才 navigateBack（否则 H5 下 fail 回调不一定触发 → "返回不了"）
      try {
        const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : []
        if (pages && pages.length > 1) {
          uni.navigateBack({ delta: 1 })
          return
        }
      } catch (e) {
        // 忽略，走下面的兜底
      }
      uni.reLaunch({ url: '/pages/main' })
    }

    const drag = reactive({ active: false, x: 0, y: 0, pinch: 0, scale0: 1 })
    function onTouchStart(e) {
      const ts = e.touches || []
      if (ts.length >= 2) {
        drag.pinch = dist(ts[0], ts[1])
        drag.scale0 = scale.value
        drag.active = false
        return
      }
      if (ts.length === 1) {
        drag.active = true
        drag.x = ts[0].clientX || ts[0].pageX
        drag.y = ts[0].clientY || ts[0].pageY
      }
    }
    function onTouchMove(e) {
      const ts = e.touches || []
      if (ts.length >= 2 && drag.pinch > 0) {
        const d = dist(ts[0], ts[1])
        scale.value = Math.min(3, Math.max(fitScale.value, +(drag.scale0 * (d / drag.pinch)).toFixed(3)))
        lastEvt.value = 'pinch'
        return
      }
      if (!drag.active || ts.length !== 1) {
        return
      }
      const x = ts[0].clientX || ts[0].pageX
      const y = ts[0].clientY || ts[0].pageY
      tx.value += x - drag.x
      ty.value += y - drag.y
      drag.x = x
      drag.y = y
      lastEvt.value = 'drag'
    }
    function onTouchEnd() {
      drag.active = false
      drag.pinch = 0
    }
    function dist(a, b) {
      const dx = (a.clientX || a.pageX) - (b.clientX || b.pageX)
      const dy = (a.clientY || a.pageY) - (b.clientY || b.pageY)
      return Math.sqrt(dx * dx + dy * dy)
    }

    onLoad(() => {
      ensureStruct()
      loadData()
    })

    /** 数据视图（默认）：真实业务记录；结构视图：本体类型关系（保留备查） */
    const mode = ref('data')
    function loadData() {
      const url = mode.value === 'data' ? ('/ai/ontology/data-graph?days=' + days.value) : '/ai/ontology/graph'
      _get({ url: url }).then((res) => {
        const body = res || {}
        const d = body.data || body || {}
        nodes.value = d.nodes || []
        links.value = d.links || []
        stories.value = d.stories || []
        summary.value = d.summary || {}
        note.value = d.note || ''
        ontologyVersion.value = d.ontologyVersion || (mode.value === 'data' ? d.asOf : '')
        errMsg.value = nodes.value.length ? '' : ('接口返回空：' + JSON.stringify(body).slice(0, 120))
        fitView()
      }).catch((e) => {
        errMsg.value = '加载失败：' + String((e && (e.msg || e.errMsg)) || JSON.stringify(e || {})).slice(0, 120)
      })
    }
    function toggleMode() {
      setMode(mode.value === 'data' ? 'struct' : 'data')
    }
    /** 视图切换（分段开关用；同值不重复加载） */
    function setMode(m) {
      if (mode.value === m) {
        return
      }
      mode.value = m
      showCapability.value = false
      showLineage.value = false
      current.value = null
      loadData()
      lastEvt.value = 'mode:' + m
    }
    /** 时间窗口切换（数据视图）：近 7 天 / 近 30 天 / 本月；windowKey 决定按钮选中态 */
    const windowKey = ref('d30')
    function setWindow(key) {
      windowKey.value = key
      if (key === 'd7') {
        days.value = 7
      } else if (key === 'd30') {
        days.value = 30
      } else {
        days.value = Math.max(1, new Date().getDate())
      }
      current.value = null
      loadData()
      lastEvt.value = '窗口:' + windowLabel.value
    }
    const windowLabel = computed(() => {
      if (windowKey.value === 'd7') {
        return '近 7 天'
      }
      if (windowKey.value === 'd30') {
        return '近 30 天'
      }
      return '本月（1 日至今）'
    })

    /** 选中产品时的"工序流转"：按该产品各工序首次报工时间排序（真实流程顺序） */
    const productFlow = computed(() => {
      const n = current.value
      if (!n || n.level !== 'product') {
        return []
      }
      const rows = links.value
        .filter((l) => l.kind !== 'lineage' && l.from === n.id)
        .map((l) => {
          const t = nodes.value.find((x) => x.id === l.to) || {}
          return {
            id: l.to,
            name: t.name || l.to,
            submitCnt: l.submitCnt,
            passRate: t.passRate,
            firstDay: l.firstDay || '',
            wipNum: null
          }
        })
      rows.sort((a, b) => String(a.firstDay).localeCompare(String(b.firstDay)))
      return rows
    })

    /** 实例节点的关键数字（详情抽屉用） */
    const currentFacts = computed(() => {
      const n = current.value
      if (!n || n.kind !== 'instance') {
        return []
      }
      const rows = []
      const nm = n.name || ''
      if (n.submitCnt != null) {
        rows.push(n.level === 'employee' ? '报工条数：' + n.submitCnt : '报工条数：' + n.submitCnt)
      }
      if (n.passRate != null) {
        rows.push('良品率：' + (n.passRate * 100).toFixed(1) + '%（良品 ' + n.passNum + ' / 不良 ' + n.ngNum + '）')
      }
      if (n.finishedNum != null) {
        rows.push('成品库存：' + n.finishedNum + ' 件')
      }
      if (n.wipNum != null) {
        rows.push('工序在制品：' + n.wipNum + ' 件')
      }
      if (n.productCnt != null) {
        rows.push('涉及产品数：' + n.productCnt)
      }
      if (n.processCnt != null) {
        rows.push('涉及工序数：' + n.processCnt)
      }
      rows.push('统计窗口：近 ' + (summary.value.days || 30) + ' 天')
      // 字典规模：库里一共多少 vs 本窗口有多少条数据（看数据完整度）
      const dict = (summary.value && summary.value.dict) || null
      if (dict) {
        if (n.level === 'product') {
          rows.push('产品字典：共 ' + dict.productDict + ' 个，本窗口有报工 ' + (summary.value.products || 0) + ' 个')
        } else if (n.level === 'process') {
          rows.push('工序字典：共 ' + dict.processDict + ' 个，本窗口有报工 ' + (summary.value.processes || 0) + ' 个')
        } else if (n.level === 'employee') {
          rows.push('员工字典：共 ' + dict.employeeDict + ' 人，本窗口有报工 ' + (dict.employeeUsed || 0) + ' 人')
        }
      }
      return rows
    })
    /** 实例节点的"可以这样问"（按类型生成，直接可点去问） */
    const currentExamples = computed(() => {
      const n = current.value
      if (!n || n.kind !== 'instance') {
        return (n && n.examples) || []
      }
      if (n.level === 'product') {
        return [n.name + ' 这个月良品率多少？', n.name + ' 各工序良品率怎么样？', n.name + ' 成品库存还有多少？']
      }
      if (n.level === 'process') {
        return [n.name + ' 工序良品率多少？', n.name + ' 各产品良品率怎么样？', n.name + ' 还有多少在制？']
      }
      return [n.name + ' 这个月报工多少？', n.name + ' 良品率多少？', '这个月谁产量最高？']
    })

    return {
      nodes, links, stories, summary, note, ontologyVersion, current, view, showEdgeLabel, errMsg,
      showCapability, showLineage, animate, lastEvt, diagText, counts, groups, relationRows, currentKind,
      relatedCaps, gnodes, edges, edgeLabels, innerStyle, mode, toggleMode, setMode, currentFacts, currentExamples,
      legendItems, legendNotes, days, setWindow, windowKey, windowLabel, productFlow, statItems, verText,
      currentOntology, currentEntityCaps, askCap, envNote, edgesAlways, toggleEdges,
      tapNode, onBlankTap, showGraph, zoomIn, zoomOut, fitView, toggleCapability, toggleLineage, playStory, tryAsk,
      goBack, onTouchStart, onTouchMove, onTouchEnd
    }
  }
}
</script>

<style scoped>
.ontology-page { display: flex; flex-direction: column; background: #f7f9fc; height: 100vh; overflow: hidden; }
.own-header { flex: none; display: flex; align-items: center; background: #fff;
  padding: calc(env(safe-area-inset-top) + 84rpx) 24rpx 12rpx; }
.back { width: 56rpx; font-size: 44rpx; line-height: 44rpx; color: #111827; }
.own-title { flex: 1; text-align: center; margin-right: 56rpx; font-size: 32rpx; font-weight: 600; color: #111827; }
.fixed-area { flex: none; padding: 8rpx 24rpx; background: #fff; }
.diag { font-size: 22rpx; color: #94a3b8; margin-bottom: 6rpx; }
.row1 { display: flex; align-items: flex-end; }
.stat { text-align: center; margin-right: 24rpx; }
.stat .num { display: block; font-size: 40rpx; font-weight: 600; color: #2563eb; }
.stat .lbl { font-size: 24rpx; color: #9ca3af; }
.ver { margin-left: auto; font-size: 24rpx; color: #c0c4cc; }
.actions { display: flex; flex-wrap: wrap; margin-top: 10rpx; }
/* 视图切换：明显的分段开关 */
.seg { display: flex; margin-top: 12rpx; background: #eef2f7; border-radius: 12rpx; padding: 4rpx; }
.seg-item { flex: 1; text-align: center; font-size: 26rpx; color: #64748b; padding: 12rpx 0; border-radius: 10rpx; }
.seg-item.on { background: #2563eb; color: #fff; font-weight: 600; }
.btn { font-size: 26rpx; color: #2563eb; background: #eff6ff; border-radius: 24rpx; padding: 8rpx 20rpx; margin: 0 12rpx 12rpx 0; }
.btn.on { color: #fff; background: #2563eb; }
.btn.story { color: #7c3aed; background: #f5f3ff; }
.pad { display: block; margin-top: 8rpx; }
.pad-row { display: flex; flex-wrap: wrap; }
.pad-btn { min-width: 100rpx; height: 60rpx; line-height: 60rpx; text-align: center; font-size: 26rpx; color: #2563eb;
  background: #eff6ff; border-radius: 12rpx; margin: 0 12rpx 12rpx 0; padding: 0 16rpx; }
.pad-btn.zoom { min-width: 130rpx; font-size: 27rpx; }
.pad-btn.wide { min-width: 140rpx; }
.pad-btn.on { color: #fff; background: #2563eb; font-weight: 600; }
.win-tip { font-size: 22rpx; color: #2563eb; margin: 0 0 12rpx 4rpx; line-height: 60rpx; }
.env-note { font-size: 21rpx; color: #b45309; background: #fffbeb; border-radius: 8rpx;
  padding: 6rpx 12rpx; margin: 0 0 8rpx 0; }
/* 实例节点 → 本体实体 + 该实体的能力 */
.onto-link { margin-top: 16rpx; padding-top: 12rpx; border-top: 1rpx solid #f1f3f5; }
.ol-title { display: block; font-size: 24rpx; color: #4b5563; }
.ol-entity { font-size: 26rpx; font-weight: 600; color: #7c3aed; }
.ol-table { font-size: 21rpx; color: #9ca3af; }
.ol-caps-title { display: block; font-size: 23rpx; color: #6b7280; margin: 10rpx 0 6rpx; }
.ol-cap { display: block; font-size: 25rpx; color: #0284c7; margin-top: 6rpx; }
.ol-ask { font-size: 21rpx; color: #94a3b8; }
/* 图例：独占整行（不再和按钮挤在一起） */
.legend { display: flex; flex-wrap: wrap; margin-top: 4rpx; }
.lg { display: flex; align-items: center; font-size: 23rpx; color: #475569; margin: 0 20rpx 8rpx 0; }
.legend-notes { margin-top: 2rpx; }
.ln { display: block; font-size: 21rpx; color: #94a3b8; line-height: 1.7; }
.dot { width: 14rpx; height: 14rpx; border-radius: 50%; margin-right: 6rpx; display: inline-block; }
.dot.master { background: #2563eb; }
.dot.action { background: #7c3aed; }
.dot.result { background: #059669; }
.hint { font-size: 21rpx; color: #9ca3af; margin: 4rpx 0 8rpx; }
.err { font-size: 21rpx; color: #dc2626; margin-bottom: 6rpx; }

.gouter { position: relative; flex: 1 1 auto; min-height: 200px; overflow: hidden; background: #fff; touch-action: none; }
.ginner { position: absolute; }
.gnode { position: absolute; }
.gnode-text { font-size: 22rpx; color: #fff; }
/* 关系名：白底胶囊贴在线上（视觉上"长在线上"），左侧小圆点与线同色 → 归属明确 */
.elabel { display: flex; align-items: center; justify-content: center;
  background: rgba(255, 255, 255, 0.92); border-radius: 8rpx; padding: 2rpx 8rpx; box-sizing: border-box; }
.el-dot { width: 12rpx; height: 12rpx; border-radius: 50%; margin-right: 6rpx; flex: none; }
.el-text { font-size: 20rpx; font-weight: 600; }

/* 数据流向光点：沿连线方向来回流动（父元素已按连线角度旋转，子元素沿 X 走即沿边流动） */
.edge .flow { position: absolute; top: -5rpx; left: 0; width: 18rpx; height: 10rpx; border-radius: 10rpx;
  background: #1d4ed8; opacity: 0.75; animation: flowmove 2.6s linear infinite; }
@keyframes flowmove {
  0% { left: 0; opacity: 0; }
  15% { opacity: 0.85; }
  85% { opacity: 0.85; }
  100% { left: 100%; opacity: 0; }
}
.gnode.pulse { animation: nodepulse 1.4s ease-in-out infinite; }
@keyframes nodepulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(37,99,235,0.55); }
  50% { box-shadow: 0 0 0 12rpx rgba(37,99,235,0); }
}

.list-wrap { background: #fff; padding: 8rpx 24rpx; }
.group { margin-bottom: 18rpx; }
.group-title { font-size: 26rpx; font-weight: 600; color: #1f2937; margin: 12rpx 0 8rpx; }
.item { display: flex; align-items: center; padding: 14rpx 0; border-bottom: 1rpx solid #f1f3f5; }
.item-name { font-size: 28rpx; color: #111827; }
.item-table { margin-left: 14rpx; font-size: 21rpx; color: #9ca3af; }
.link { display: flex; align-items: center; flex-wrap: wrap; padding: 12rpx 0; border-bottom: 1rpx solid #f1f3f5; }
.lk { font-size: 26rpx; color: #111827; }
.lk-label { margin: 0 12rpx; font-size: 22rpx; color: #2563eb; }
.badge { margin-left: 12rpx; font-size: 20rpx; color: #059669; background: #ecfdf5; padding: 2rpx 8rpx; border-radius: 6rpx; }
.badge.gray { color: #6b7280; background: #f3f4f6; }
/* 详情：不再用浮层（会盖住关系图）→ 与图区同层排列，图让位但保底可读高度，详情自身可滚动 */
.sheet { flex: 0 1 auto; background: #fff; border-radius: 20rpx 20rpx 0 0;
  box-shadow: 0 -8rpx 24rpx rgba(0,0,0,0.08); padding: 24rpx 28rpx 40rpx;
  max-height: 52vh; overflow-y: auto; -webkit-overflow-scrolling: touch; z-index: 20; }
.sheet-head { display: flex; align-items: center; }
.sheet-name { font-size: 32rpx; font-weight: 600; color: #111827; }
.sheet-kind { margin-left: 16rpx; font-size: 20rpx; color: #7c3aed; background: #f5f3ff; padding: 2rpx 10rpx; border-radius: 6rpx; }
.sheet-close { margin-left: auto; font-size: 24rpx; color: #9ca3af; }
.sheet-line { margin-top: 10rpx; font-size: 24rpx; color: #4b5563; line-height: 1.6; }
.chips { display: flex; flex-wrap: wrap; margin-top: 12rpx; }
.chip { font-size: 21rpx; color: #4b5563; background: #f3f4f6; padding: 4rpx 10rpx; margin: 0 10rpx 10rpx 0; border-radius: 6rpx; }
.examples { margin-top: 8rpx; }
.ex { display: block; margin-top: 8rpx; font-size: 26rpx; color: #2563eb; }
.caps { margin-top: 16rpx; padding-top: 12rpx; border-top: 1rpx solid #f1f3f5; }
.caps-title { display: block; font-size: 24rpx; color: #6b7280; margin-bottom: 8rpx; }
.cap { display: block; font-size: 25rpx; color: #0284c7; margin-top: 6rpx; }
/* 产品工序流转条 */
.flow-strip { margin-top: 16rpx; padding-top: 12rpx; border-top: 1rpx solid #f1f3f5; }
.flow-title { display: block; font-size: 24rpx; color: #6b7280; margin-bottom: 10rpx; }
.flow-line { display: flex; flex-wrap: wrap; align-items: center; }
.flow-node { display: flex; flex-direction: column; align-items: center; font-size: 25rpx; color: #fff;
  background: #7c3aed; border-radius: 12rpx; padding: 8rpx 16rpx; margin: 0 4rpx 8rpx 0; }
.flow-sub { font-size: 19rpx; color: #ede9fe; margin-top: 2rpx; }
.flow-arrow { font-size: 22rpx; color: #94a3b8; margin: 0 8rpx 8rpx 0; }
.foot { padding: 16rpx 30rpx 30rpx 0; font-size: 20rpx; color: #9ca3af; }

/* #ifdef H5 */
.ontology-page { height: 100vh; overflow: hidden; }
.fixed-area { padding-top: 12rpx; }
/* #endif */
</style>
