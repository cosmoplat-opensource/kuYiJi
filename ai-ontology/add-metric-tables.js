/**
 * 一次性脚本：给 metrics.json 的每个指标补 dataSource.tables（**表级血缘依据**）
 *
 * 为什么需要它：结构视图里的"口径血缘"目前来自后端 Java 里的手工静态映射 ✗，
 * 本体一改就会画错/画漏。登记 tables 后，血缘可以**自动推导**：
 *     e.source.table ∈ m.dataSource.tables  →  血缘边 e ──▶ m
 *
 * 用法：node ai-ontology/add-metric-tables.js      （幂等：已有 tables 的不覆盖）
 * 之后务必：node ai-ontology/validate.js && node ai-ontology/sync.js
 */
const fs = require('fs');
const path = require('path');

const FILE = path.join(__dirname, 'metrics.json');

// 指标 code → 该指标必然读取的表（按"要算这个指标最少需要哪些数据"填写）
const TABLES = {
  // 经营概览：报工量/良品数/涉及产品工序员工
  SUMMARY: ['micro_work_submit', 'micro_product', 'micro_process_common', 'micro_user'],
  // 产品良品率：按产品分组
  PRODUCT_PASS_RATE: ['micro_work_submit', 'micro_product'],
  // 工序良品率：按工序分组
  PROCESS_PASS_RATE: ['micro_work_submit', 'micro_process_common'],
  // 员工良品率/产量：按员工分组
  EMPLOYEE_PASS_RATE: ['micro_work_submit', 'micro_user'],
  // 记工排名：按员工聚合报工
  SUBMIT_RANK: ['micro_work_submit', 'micro_user'],
  // 不良明细：质检记录（不良类型）+ 报工
  NG_DETAIL: ['micro_quality_control_record', 'micro_work_submit'],
  // 库存：工序在制品 + 成品库存（产品名来自产品表）
  STOCK: ['micro_process_storage', 'micro_finished_product_storage', 'micro_product'],
  // 订单延期预警（计划域，当前不投入，但血缘登记完整）
  DELIVERY_RISK: ['micro_manufacture_order', 'micro_product'],
  // 实体清单：三本字典
  ENTITY_LIST: ['micro_product', 'micro_process_common', 'micro_user'],
  // 变化归因：报工 + 三个分组维度
  ATTRIBUTION: ['micro_work_submit', 'micro_product', 'micro_process_common', 'micro_user'],
  // 集中度分析：报工 + 质检（不良类型）+ 工序
  CONCENTRATE: ['micro_work_submit', 'micro_quality_control_record', 'micro_process_common'],
  // 两期对比：报工 + 分组维度
  COMPARE: ['micro_work_submit', 'micro_product', 'micro_process_common']
};

const doc = JSON.parse(fs.readFileSync(FILE, 'utf-8'));
let changed = 0;
const missing = [];

for (const m of doc.metrics || []) {
  const tables = TABLES[m.code];
  if (!tables) {
    missing.push(m.code);
    continue;
  }
  m.dataSource = m.dataSource || {};
  if (Array.isArray(m.dataSource.tables) && m.dataSource.tables.length) {
    continue; // 幂等：已有登记不覆盖
  }
  m.dataSource.tables = tables;
  changed++;
}

fs.writeFileSync(FILE, JSON.stringify(doc, null, 2) + '\n', 'utf-8');
console.log('已补 dataSource.tables 的指标数:', changed);
if (missing.length) {
  console.log('⚠ 未在本脚本映射表中、需人工补的指标:', missing.join(', '));
}
(doc.metrics || []).forEach((m) => {
  console.log(' -', m.code, '→', JSON.stringify((m.dataSource || {}).tables || []));
});
