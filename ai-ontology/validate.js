#!/usr/bin/env node
/**
 * Ku易记 · 本体校验脚本 v2（零依赖，三文件统一校验）
 * 用法：node ai-ontology/validate.js
 *
 * 校验对象：
 *  - metrics.json   （指标层）
 *  - entities.json  （实体层）
 *  - relations.json （关系层）
 *
 * 指标规则：JSON 可解析/version/code唯一/别名全局唯一/formula 必填/dims 枚举/
 *          llmParams 必填/必须登记 api/status 枚举/ACTIVE 必须 implemented/
 *          问题模板非空
 * 实体规则：code 唯一且必填/name 必填/source.table 必填/keyField 必填
 * 关系规则：from/to 必须引用已定义实体/arity 枚举/note 建议填写/
 *          traversable 关系两端实体必须已定义
 */
'use strict';

const fs = require('fs');
const path = require('path');

const DIR = __dirname;
const DIM_ENUM = ['time', 'day', 'product', 'process', 'employee', 'order', 'ngType', 'stock'];
const STATUS_ENUM = ['ACTIVE', 'DRAFT'];
const ARITY_ENUM = ['1:1', '1:N', '1:0..1', '1:1..N', '1:0..N'];

const errors = [];
const warns = [];
function fail(msg) { errors.push(msg); }
function warn(msg) { warns.push(msg); }
function load(name) {
  try { return JSON.parse(fs.readFileSync(path.join(DIR, name), 'utf-8')); }
  catch (e) { fail(`${name} 解析失败: ${e.message}`); return null; }
}
function checkDoc(doc, name) {
  if (!doc) return { entitiesCode: new Set() };
  if (!doc.version) fail(`${name}: 缺少 version`);
  return { entitiesCode: new Set() };
}

/* ---------------- 指标层 ---------------- */
const metricsDoc = load('metrics.json');
if (metricsDoc) {
  if (!Array.isArray(metricsDoc.metrics) || metricsDoc.metrics.length === 0) fail('metrics.json: metrics 必须是非空数组');
  const seenCode = new Set();
  const seenAlias = new Set();
  for (const m of metricsDoc.metrics || []) {
    const tag = `metrics.${m.code || '(未命名)'}`;
    if (!m.code || !/^[A-Z][A-Z0-9_]*$/.test(m.code)) fail(`${tag}: code 必填且大写字母/数字/下划线`);
    if (seenCode.has(m.code)) fail(`${tag}: code 重复`);
    seenCode.add(m.code);
    if (!m.name) fail(`${tag}: 缺少 name`);
    if (!Array.isArray(m.aliases) || m.aliases.length === 0) fail(`${tag}: aliases 非空数组`);
    for (const a of m.aliases || []) {
      if (seenAlias.has(a)) fail(`${tag}: 别名「${a}」冲突`);
      seenAlias.add(a);
    }
    if (!m.formula) fail(`${tag}: 缺少 formula`);
    if (!Array.isArray(m.dims) || m.dims.length === 0) fail(`${tag}: dims 非空数组`);
    for (const d of m.dims || []) if (!DIM_ENUM.includes(d)) fail(`${tag}: 非法维度「${d}」`);
    if (!Array.isArray(m.llmParams)) fail(`${tag}: 缺少 llmParams`);
    if (!m.dataSource || !m.dataSource.api) fail(`${tag}: 必须登记 dataSource.api`);
    // 口径血缘依据：登记了 tables，结构视图的血缘线才能自动推导（否则该指标在图上没有血缘 ✗）
    if (!m.dataSource || !Array.isArray(m.dataSource.tables) || m.dataSource.tables.length === 0) {
      fail(`${tag}: 必须登记 dataSource.tables（口径血缘依据，至少一张表）`);
    }
    if (!STATUS_ENUM.includes(m.status)) fail(`${tag}: status 必须是 ${STATUS_ENUM.join('|')}`);
    if (m.status === 'ACTIVE' && m.implemented !== true) fail(`${tag}: ACTIVE 必须 implemented=true`);
    if (m.status === 'DRAFT' && m.implemented === true) warn(`${tag}: DRAFT 但 implemented=true，发布前记得改状态`);
    if (!Array.isArray(m.questionTemplates) || m.questionTemplates.length === 0) fail(`${tag}: 缺少问题模板`);
  }
}

/* ---------------- 实体层 ---------------- */
const entitiesDoc = load('entities.json');
const entityCodes = new Set();
// 旧版本体里 submit/qualityControl/completeReport 曾是"隐性事实实体"（只在关系里出现）。
// 新版已在 entities.json 里正式定义 → 这里改为独立的 implicitCodes，仅用于关系端点的兼容，
// 不再并入 entityCodes（否则正式定义它们时会被误判为"code 重复"）。
const implicitCodes = new Set(['submit', 'qualityControl', 'completeReport']);
if (entitiesDoc) {
  if (!Array.isArray(entitiesDoc.entities) || entitiesDoc.entities.length === 0) fail('entities.json: 必须是非空数组');
  for (const e of entitiesDoc.entities || []) {
    const tag = `entities.${e.code || '(未命名)'}`;
    if (!e.code || !/^[a-zA-Z][a-zA-Z0-9_]*$/.test(e.code)) fail(`${tag}: code 必填`);
    if (entityCodes.has(e.code)) fail(`${tag}: code 重复`);
    entityCodes.add(e.code);
    if (!e.name) fail(`${tag}: 缺少 name`);
    if (!e.source || !e.source.table) fail(`${tag}: 必须登记 source.table（业务字典来源）`);
    if (!e.source || !e.source.keyField) fail(`${tag}: 必须登记 source.keyField（检索键）`);
  }
}

/* ---------------- 血缘可用性：每个指标的 dataSource.tables 至少要能匹配到一个实体表 ---------------- */
if (metricsDoc && entitiesDoc) {
  const entityTables = new Set();
  for (const e of entitiesDoc.entities || []) {
    const t = e.source && e.source.table;
    if (t) {
      entityTables.add(t);
    }
  }
  for (const m of metricsDoc.metrics || []) {
    const tables = (m.dataSource && m.dataSource.tables) || [];
    if (!tables.length) {
      continue;   // 上面已报"缺少 tables"
    }
    const matched = tables.filter((t) => entityTables.has(t));
    const tag = `metrics.${m.code}`;
    if (matched.length === 0) {
      // 计划域指标在实体层只登记了 planning 实体时也会走到这里 → 用 warn 提示，不算硬错误
      warn(`${tag}: dataSource.tables 没有任何一张表能在实体层找到 → 该指标在图上不会有血缘线`);
    } else if (m.scope !== 'planning' && matched.length < tables.length) {
      const miss = tables.filter((t) => !entityTables.has(t));
      warn(`${tag}: 这些表未在实体层登记（血缘会少画线）: ${miss.join(', ')}`);
    }
  }
}

/* ---------------- 关系层 ---------------- */const relationsDoc = load('relations.json');
if (relationsDoc) {
  if (!Array.isArray(relationsDoc.relations) || relationsDoc.relations.length === 0) fail('relations.json: 必须是非空数组');
  for (const r of relationsDoc.relations || []) {
    const tag = `relations.${r.from || '?'}->${r.to || '?'}`;
    if (!r.from || !r.to) { fail(`${tag}: from/to 必填`); continue; }
    const defined = (c) => entityCodes.has(c) || implicitCodes.has(c);
    if (!defined(r.from)) fail(`${tag}: from「${r.from}」未定义实体或非事实`);
    if (!defined(r.to)) fail(`${tag}: to「${r.to}」未定义实体或非事实`);
    if (!ARITY_ENUM.includes(r.arity)) fail(`${tag}: arity 必须是 ${ARITY_ENUM.join('/')}`);
    if (r.traversable !== true && r.traversable !== false) fail(`${tag}: traversable 必须为 true/false`);
    if (!r.via) warn(`${tag}: 建议填写 via（关联依据）`);
  }
}

/* ---------------- 输出 ---------------- */
console.log('==================================');
console.log(' 本体校验报告（metrics/entities/relations）');
console.log('==================================');
console.log(` metrics: ${metricsDoc ? (metricsDoc.metrics || []).length : 0} 指标 | entities: ${entitiesDoc ? (entitiesDoc.entities || []).length : 0} 实体 | relations: ${relationsDoc ? (relationsDoc.relations || []).length : 0} 关系`);
if (warns.length) { console.log(' 警告:'); warns.forEach(w => console.log('   ⚠ ' + w)); }
if (errors.length) {
  console.log(' 错误:'); errors.forEach(e => console.log('   ✗ ' + e));
  console.log('----------------------------------');
  console.log(' 结论: ✗ 校验失败（' + errors.length + ' 项错误）');
  process.exit(1);
} else {
  console.log('----------------------------------');
  console.log(' 结论: ✓ 校验通过');
}
