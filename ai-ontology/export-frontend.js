#!/usr/bin/env node
/**
 * Ku易记 · 本体 → 前端导出脚本（零依赖）
 * 用法：node ai-ontology/export-frontend.js
 *
 * 作用：读 metrics.json（本体·指标层）→ 生成前端可用的能力常量 TS 文件
 *       仅导出 ACTIVE 且已实现 的指标；保持"本体单一来源，前端所见即投影"
 * 产物：cosmo-hhim-micro/hhim-micro-app/src/utils/ai-capability.ts
 *
 * ⚠ 位置不可挪回分包：该文件同时被主包组件（components/h-ask-answer.vue）与
 *   分包页面（pages-analysis/ask/index.vue）引用；微信小程序**禁止主包 require 分包模块**，
 *   放进 pages-analysis（分包）会导致主包组件加载失败（报 module ... is not defined，
 *   且错误发生在脚本加载阶段会拖垮整个 appservice 启动）。故固定放主包 src/utils/。
 */
'use strict';

const fs = require('fs');
const path = require('path');

const META_FILE = path.join(__dirname, 'metrics.json');
const OUT_FILE = path.join(
  __dirname,
  '..', 'cosmo-hhim-micro', 'hhim-micro-app', 'src', 'utils', 'ai-capability.ts'
);

const doc = JSON.parse(fs.readFileSync(META_FILE, 'utf-8'));
const metrics = (doc.metrics || []).filter((m) => m.status === 'ACTIVE' && m.implemented === true);

const capabilities = metrics.map((m) => ({
  code: m.code,
  name: m.name,
  examples: m.questionTemplates || []
}));
// 新会话推荐问题池：**收集所有指标的全部问题模板**（而不是每个只取第一条）
// → 池子更大、前端每次随机取 5 条；且每条都对应已登记能力，保证可回答。
const seenQ = new Set();
const quickQuestions = [];
for (const c of capabilities) {
  for (const q of (c.examples || [])) {
    if (!seenQ.has(q)) {
      seenQ.add(q);
      quickQuestions.push(q);
    }
  }
}

// 追问建议（答案卡片"继续问"）：指标 → 该指标的问题模板（前端按命中意图取用）
const followUps = {};
for (const c of capabilities) {
  if (c.examples.length) {
    followUps[c.code] = c.examples.slice(0, 4);
  }
}

const ts = `// ============================================================
// 本文件由 ai-ontology/export-frontend.js 自动生成，请勿手改
// 数据源：ai-ontology/metrics.json（本体·指标层 v${doc.version}）
// 重新生成：node ai-ontology/sync.js（或 export-frontend.js）
// 生效：本体中 ACTIVE 且 implemented=true 的指标
//
// ⚠ 位置不可挪进分包：本文件同时被主包组件（components/h-ask-answer.vue）与
//   分包页面（pages-analysis/ask/index.vue）引用；微信小程序禁止主包 require 分包模块，
//   放进 pages-analysis/ 会导致主包组件加载失败（module ... is not defined）。
// ============================================================

export interface Capability {
  code: string
  name: string
  examples: string[]
}

export const CAPABILITIES: Capability[] = ${JSON.stringify(capabilities, null, 2)}

export const QUICK_QUESTIONS: string[] = ${JSON.stringify(quickQuestions, null, 2)}

/** 追问建议：命中意图 → 可继续追问的问题（答案卡片"继续问"常驻；来自本体 questionTemplates） */
export const FOLLOW_UPS: Record<string, string[]> = ${JSON.stringify(followUps, null, 2)}
`;

fs.writeFileSync(OUT_FILE, ts, 'utf-8');
console.log('✓ 已生成: ' + path.relative(process.cwd(), OUT_FILE));
console.log('  指标数: ' + capabilities.length + '，快捷提问: ' + quickQuestions.length
  + '，追问建议组: ' + Object.keys(followUps).length);
console.log('  ' + quickQuestions.map((q) => `「${q}」`).join(' '));
