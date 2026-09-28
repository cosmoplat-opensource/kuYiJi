/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * @author cosmo-hhim-open Team
 */

/**
 * 一次性脚本：给三个**分析算子**补 `routing`（能算什么 / 需要什么 / 不能算什么）
 *
 * 为什么要它：意图映射的提示词此前只给算子的"名称+别名+示例" ✗，
 * LLM 看到"为什么"这类关键词就可能选归因算子，但**算子未必真的能算这个问题** ✗
 * → 把算子的能力边界写进本体，提示词里带上，让 LLM 判断"去了之后一定能算、且算得符合问题" ✓
 * → 若没有合适的能力，就置 NOT_SUPPORTED / 走生成 SQL ✓
 *
 * 用法：node ai-ontology/add-operator-routing.js   （幂等：已有 routing 的不覆盖）
 * 之后：node ai-ontology/validate.js && node ai-ontology/sync.js
 */
const fs = require('fs');
const path = require('path');

const FILE = path.join(__dirname, 'metrics.json');

const ROUTING = {
  ATTRIBUTION: [
    '能算：把【某个指标】在【两期之间】的总变化，按【一个分组维度】分解成各项贡献（Σ贡献 = 总变化），并指出主因。',
    '需要：一个指标 + 一个分组维度（产品/工序/员工/日期…）+ 前后两期时间范围（如本月 vs 上月、某天 vs 前一天）。',
    '不能算：① 只问"某个数值是多少"（那不是归因，应选对应指标）；② 没有两期可比范围时无法分解；',
    '        ③ 样本量不足（分组样本过小）或主因占比不足时，只能如实回答"主因不明确"，不会强行归因。'
  ].join('\n'),
  COMPARE: [
    '能算：同一个指标在【两个时间范围】上的数值与变化（增减量 / 变化率 / 是否上升下降）。',
    '需要：一个指标 + 两期时间范围（本月vs上月、本周vs上周、今天vs昨天…）。',
    '不能算：① 不解释原因（要原因用归因算子）；② 不做多维度下钻分解（要下钻用归因或集中度）。'
  ].join('\n'),
  CONCENTRATE: [
    '能算：某个指标在【一个维度】上的集中程度——排在前面的对象占多大比例、主要来自哪里。',
    '需要：一个指标 + 一个分组维度（产品/工序/员工/不良类型…）+ 一个时间范围。',
    '不能算：① 不回答两期变化；② 不解释原因；③ 维度取值过少时集中度意义有限，会如实说明。'
  ].join('\n')
};

const doc = JSON.parse(fs.readFileSync(FILE, 'utf-8'));
let changed = 0;
for (const m of doc.metrics || []) {
  const text = ROUTING[m.code];
  if (!text) {
    continue;
  }
  if (m.routing) {
    continue; // 幂等
  }
  m.routing = text;
  changed++;
}
fs.writeFileSync(FILE, JSON.stringify(doc, null, 2) + '\n', 'utf-8');
console.log('已补 routing 的算子数:', changed);
(doc.metrics || []).forEach((m) => {
  if (m.routing) {
    console.log(' -', m.code, '→', m.routing.split('\n')[0]);
  }
});
