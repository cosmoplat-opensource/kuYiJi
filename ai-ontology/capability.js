#!/usr/bin/env node
/**
 * Ku易记 · 本体投影器（零依赖）
 * 用法：node ai-ontology/capability.js
 *
 * 作用：读 metrics.json → 生成"能力清单 JSON"（注入 LLM 的 system prompt 核心内容）
 *       仅投影 ACTIVE 且已实现的指标；租户实体/别名/关系为运行时补丁（此处留占位）
 * 产物：ai-ontology/out/capability-prompt.txt（可直接复制给 LLM）
 */
'use strict';

const fs = require('fs');
const path = require('path');

const META_FILE = path.join(__dirname, 'metrics.json');
const REL_FILE = path.join(__dirname, 'relations.json');
const OUT_DIR = path.join(__dirname, 'out');
const OUT_FILE = path.join(OUT_DIR, 'capability-prompt.txt');

const doc = JSON.parse(fs.readFileSync(META_FILE, 'utf-8'));
const metrics = (doc.metrics || []).filter((m) => m.status === 'ACTIVE' && m.implemented === true);
// 关系层摘要（traversable 关系 → 提示 LLM 可串联的方向）
let relationSummary = '[运行时注入：relation 摘要，如 产品→工艺链→工序；产品→订单→交付日]';
try {
  const relDoc = JSON.parse(fs.readFileSync(REL_FILE, 'utf-8'));
  const t = (relDoc.relations || []).filter((r) => r.traversable === true);
  relationSummary = t.length
    ? t.map((r) => `${r.from}→${r.to}（${((r.via || '').split('（')[0] || '').split('：')[0]}）`).join('；')
    : '（当前无跨步串联）';
} catch (e) {
  /* 文件缺失时保留占位 */
}

// ---- 能力清单投影（LLM 可见的最小信息：能答什么 + 参数 + 示例） ----
const capabilities = metrics.map((m) => ({
  code: m.code,
  name: m.name,
  aliases: m.aliases,
  // 意图侧参数（LLM 可填的字段；接口参数由 paramMap 在代码侧映射）
  params: m.llmParams || [],
  examples: (m.questionTemplates || []).slice(0, 3)
}));

// ---- 组装 system prompt ----
const systemPrompt = `你是 Ku易记 的经营数据问答助手。你只做一件事：把用户的问题翻译成结构化的查询意图。
严格遵守以下约束，直接输出 JSON，不要输出任何其他文字。

【可查询能力】
${capabilities
  .map(
    (c) =>
      `${c.code}   名称=${c.name}   别名=[${c.aliases.join(', ')}]\n` +
      `   参数: ${c.params.length ? c.params.join(', ') : '无'}\n` +
      `   示例: ${c.examples.join(' | ')}`
  )
  .join('\n')}

【本租户实体】${'[运行时注入：产品/工序/员工 字典 Top-N，来自租户数据库]'}
【关系】${relationSummary}

【规则】
- 只能选择以上能力；能力之外将 intent 置为 "NOT_SUPPORTED"
- 不允许回答工资、结算、任何金额类问题
- 实体名必须从【本租户实体】中选择；用户用简称时按最接近的补全
- time 只允许: today / month / year / custom / null(不关心，尽量不设)
- 目标 JSON 结构: {"intent":"code","entities":{},"time":{"type":"...","range":null},"order":{"by":"...","dir":"asc|desc","limit":1-10},"clarify":null}
- 实体指代不清时: clarify={"question":"...","options":["...","..."]}，intent 仍填最可能项`;

// ---- 输出 ----
if (!fs.existsSync(OUT_DIR)) fs.mkdirSync(OUT_DIR, { recursive: true });
fs.writeFileSync(OUT_FILE, systemPrompt, 'utf-8');

console.log('==================================');
console.log(' 本体投影器 · metrics.json → 能力清单');
console.log('==================================');
console.log(` 生效指标: ${capabilities.length}/${doc.metrics.length}（仅 ACTIVE 且已实现）`);
console.log(' 跳过指标: ' + doc.metrics.filter((m) => !(m.status === 'ACTIVE' && m.implemented === true)).map((m) => m.code).join(', '));
console.log('----------------------------------');
console.log(' 能力清单（JSON）:');
console.log(JSON.stringify({ capabilities }, null, 2));
console.log('----------------------------------');
console.log(' 完整 system prompt 已写入: ' + OUT_FILE);
