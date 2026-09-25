#!/usr/bin/env node
/**
 * Ku易记 · 本体文件同步脚本（零依赖）
 * 用法：node ai-ontology/sync.js
 *
 * 本体唯一权威 = 仓库根 ai-ontology/*.json；
 * 本脚本一键同步到后端 resources（OntologyService 加载源）并生成前端 capability.ts。
 * 每次修改本体后执行一次（可挂到 CI）。
 */
'use strict';

const fs = require('fs');
const path = require('path');

const SRC = __dirname;
const BACKEND = path.join(SRC, '..', 'cosmo-hhim-micro', 'hhim-micro-be', 'micro-application', 'src', 'main', 'resources', 'ontology');

const files = ['metrics.json', 'entities.json', 'relations.json'];
let ok = 0;
for (const f of files) {
  const src = path.join(SRC, f);
  if (!fs.existsSync(src)) {
    console.log('⚠ 跳过（不存在）: ' + f);
    continue;
  }
  fs.copyFileSync(src, path.join(BACKEND, f));
  ok++;
}
console.log('✓ 已同步后端 resources/ontology: ' + ok + ' 个文件 -> ' + path.relative(process.cwd(), BACKEND));

// 生成前端能力投影（复用导出脚本）——产物固定放主包 src/utils/ai-capability.ts
// （主包组件与分包页面都要引用；微信小程序禁止主包 require 分包模块）
const { execSync } = require('child_process');
execSync('node ' + path.join(SRC, 'export-frontend.js'), { stdio: 'inherit' });
console.log('✓ 前端能力投影已重新生成（src/utils/ai-capability.ts）');
