/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.ai.sql;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.SQLLimit;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.util.JdbcConstants;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * LLM 生成 SQL 的安全闸（三道闸 + 改写），基于 Druid AST（Apache-2.0，已在依赖树，零新增依赖）。
 *
 * <p>规则（与方案一致）：
 * <ol>
 *   <li><b>语句类型闸</b>：解析后必须"恰好一条 SELECT" —— 任何 DML/DDL（INSERT/UPDATE/DELETE/DROP/TRUNCATE/ALTER/CALL…）
 *       与多语句拼接在**毫秒级直接拒绝**，不调 LLM、不重试；另加危险函数兜底（sleep/benchmark/load_file/into outfile）；</li>
 *   <li><b>对象白名单闸</b>：AST 收集到的表必须 ∈ 白名单，列不得命中敏感列（表级/字段级两层排除）；</li>
 *   <li><b>强制约束（AST 改写，不是字符串拼接）</b>：每个查询块的每张表都强制 AND 上
 *       {@code tenant_code = '<当前租户>'}；无 LIMIT 或超上限则改写为上限行数。</li>
 * </ol>
 *
 * <p>本类是纯函数（无 Spring 依赖），便于单测；策略由 {@link Policy} 传入（后续可从本体 safety.json 生成）。
 */
public final class SqlGuard {

    /** 解析方言（本项目 MySQL 8）；Druid 1.2.x 用 DbType 而非 String */
    private static final com.alibaba.druid.DbType DB_TYPE = JdbcConstants.MYSQL;

    /** 危险函数/语法兜底（AST 之外的第二道网，覆盖解析器可能放过的情况） */
    private static final List<String> FORBIDDEN_TOKENS = Arrays.asList(
            "into outfile", "into dumpfile", "load_file(", "sleep(", "benchmark(", "information_schema", "mysql.",
            "performance_schema", "sys.", "updatexml(", "extractvalue(");

    /** 安全策略：表白名单 / 敏感表 / 敏感列 / 租户列 / 行数上限 */
    @Data
    @Builder
    public static class Policy {
        /** 允许查询的业务表（空=不限制，仅按敏感表排除） */
        @Builder.Default
        private Set<String> allowedTables = new HashSet<>();
        /** 整表排除（如只存敏感数据的表） */
        @Builder.Default
        private Set<String> excludedTables = new HashSet<>();
        /** 字段级排除：表名 → 敏感列集合（表名小写） */
        @Builder.Default
        private java.util.Map<String, Set<String>> excludedColumns = new java.util.HashMap<>();
        /** 强制注入的租户列名 */
        @Builder.Default
        private String tenantColumn = "tenant_code";
        /** 结果行数上限 */
        @Builder.Default
        private int maxRows = 500;
        /** 单次生成的语句数上限（你定的 ≤3） */
        @Builder.Default
        private int maxStatements = 3;
    }

    /** 校验结果：ok=false 时 reason 说明原因（用于回喂 LLM 重写 + 审计） */
    @Data
    public static class GuardResult {
        private boolean ok;
        private String sql;
        private String reason;
        private List<String> tables = new ArrayList<>();
        private List<String> columns = new ArrayList<>();
        private boolean tenantInjected;
        private boolean limitRewritten;

        public static GuardResult reject(String reason) {
            GuardResult r = new GuardResult();
            r.ok = false;
            r.reason = reason;
            return r;
        }
    }

    private final Policy policy;

    public SqlGuard(Policy policy) {
        this.policy = policy == null ? Policy.builder().build() : policy;
    }

    /**
     * 校验并改写一条 SQL。
     *
     * @param rawSql     LLM 生成的原始 SQL
     * @param tenantCode 当前租户（强制注入）
     */
    public GuardResult check(String rawSql, String tenantCode) {
        if (rawSql == null || rawSql.trim().isEmpty()) {
            return GuardResult.reject("SQL 为空");
        }
        String sql = rawSql.trim();
        if (sql.length() > 20000) {
            return GuardResult.reject("SQL 过长");
        }
        String lower = sql.toLowerCase();
        for (String token : FORBIDDEN_TOKENS) {
            if (lower.contains(token)) {
                return GuardResult.reject("包含禁止的语法/函数：" + token);
            }
        }

        // ── 闸①：解析 + 语句类型 + 多语句 ──
        List<SQLStatement> statements;
        try {
            statements = SQLUtils.parseStatements(sql, DB_TYPE);
        } catch (Exception e) {
            return GuardResult.reject("SQL 解析失败：" + e.getMessage());
        }
        if (statements == null || statements.isEmpty()) {
            return GuardResult.reject("未解析出任何语句");
        }
        if (statements.size() > 1) {
            return GuardResult.reject("只允许单条 SELECT，检测到 " + statements.size() + " 条语句");
        }
        SQLStatement stmt = statements.get(0);
        if (!(stmt instanceof SQLSelectStatement)) {
            return GuardResult.reject("只允许查询语句（SELECT），检测到：" + stmt.getClass().getSimpleName());
        }
        if (statements.size() > policy.getMaxStatements()) {
            return GuardResult.reject("语句数超过上限 " + policy.getMaxStatements());
        }

        // ── 闸②：对象白名单（表 + 字段） ──
        SchemaStatVisitor visitor = new SchemaStatVisitor(DB_TYPE);
        try {
            stmt.accept(visitor);
        } catch (Exception e) {
            return GuardResult.reject("无法分析 SQL 结构：" + e.getMessage());
        }
        Set<String> tables = new LinkedHashSet<>();
        visitor.getTables().keySet().forEach(n -> tables.add(n.getName().toLowerCase()));
        for (String t : tables) {
            if (policy.getExcludedTables().contains(t)) {
                return GuardResult.reject("表 " + t + " 不允许查询（敏感表，已从本体与提示词中排除）");
            }
            if (!policy.getAllowedTables().isEmpty() && !policy.getAllowedTables().contains(t)) {
                return GuardResult.reject("表 " + t + " 不在允许查询的白名单内");
            }
        }
        // Druid 1.2.x：getColumns() 返回 Collection<TableStat.Column>（不是 Map）
        Set<String> columnKeys = new LinkedHashSet<>();
        Set<String> columnNames = new LinkedHashSet<>();
        if (visitor.getColumns() != null) {
            for (com.alibaba.druid.stat.TableStat.Column c : visitor.getColumns()) {
                String owner = c.getTable() == null ? "" : c.getTable().toLowerCase();
                String name = c.getName() == null ? "" : c.getName().toLowerCase();
                columnNames.add(name);
                columnKeys.add(owner.isEmpty() ? name : owner + "." + name);
            }
        }
        for (java.util.Map.Entry<String, Set<String>> e : policy.getExcludedColumns().entrySet()) {
            if (!tables.contains(e.getKey().toLowerCase())) {
                continue;
            }
            for (String col : e.getValue()) {
                String c = col.toLowerCase();
                if (columnKeys.contains(e.getKey().toLowerCase() + "." + c) || columnNames.contains(c)) {
                    return GuardResult.reject("字段 " + e.getKey() + "." + col + " 属于敏感字段，不允许查询");
                }
            }
        }

        // ── 闸③：强制约束（AST 改写） ──
        boolean injected = false;
        boolean limitRewritten = false;
        List<SQLSelectQueryBlock> blocks = new ArrayList<>();
        collectQueryBlocks(stmt, blocks);
        for (SQLSelectQueryBlock block : blocks) {
            List<SQLExprTableSource> sources = new ArrayList<>();
            collectTableSources(block.getFrom(), sources);
            for (SQLExprTableSource table : sources) {
                String qualifier = table.getAlias() != null ? table.getAlias() : table.getExpr().toString();
                SQLExpr condition = new SQLBinaryOpExpr(
                        new SQLIdentifierExpr(qualifier + "." + policy.getTenantColumn()),
                        SQLBinaryOperator.Equality,
                        new SQLCharExpr(tenantCode == null ? "" : tenantCode));
                SQLExpr where = block.getWhere();
                block.setWhere(where == null ? condition : new SQLBinaryOpExpr(where, SQLBinaryOperator.BooleanAnd, condition));
                injected = true;
            }
            // 行数上限：无 LIMIT 或超上限 → 改写
            SQLLimit limit = block.getLimit();
            if (limit == null) {
                block.setLimit(new SQLLimit(new SQLIntegerExpr(policy.getMaxRows())));
                limitRewritten = true;
            } else if (limit.getRowCount() instanceof SQLIntegerExpr
                    && ((SQLIntegerExpr) limit.getRowCount()).getNumber().intValue() > policy.getMaxRows()) {
                block.setLimit(new SQLLimit(new SQLIntegerExpr(policy.getMaxRows())));
                limitRewritten = true;
            }
        }

        GuardResult result = new GuardResult();
        result.ok = true;
        result.sql = SQLUtils.toMySqlString(stmt);
        result.tables = new ArrayList<>(tables);
        result.columns = new ArrayList<>(columnKeys);
        result.tenantInjected = injected;
        result.limitRewritten = limitRewritten;
        return result;
    }

    // ------------------------------------------------------------------ AST 工具

    /**
     * 收集所有查询块（含子查询）：Druid 1.2.x 没有 SQLObject#getChildren()，
     * 用 visitor 遍历（visitor 会自动下钻子查询）。
     */
    private void collectQueryBlocks(SQLStatement stmt, List<SQLSelectQueryBlock> out) {
        stmt.accept(new com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter() {
            @Override
            public boolean visit(SQLSelectQueryBlock x) {
                out.add(x);
                return true;
            }
        });
    }

    /**
     * 取查询块里的表源（含 JOIN 树）。
     *
     * <p>Druid 1.2.x 的 JOIN 在 {@code getFrom()} 的 {@link com.alibaba.druid.sql.ast.statement.SQLJoinTableSource} 树里，
     * 没有 {@code getJoins()}。
     */
    private void collectTableSources(com.alibaba.druid.sql.ast.statement.SQLTableSource source,
                                     List<SQLExprTableSource> out) {
        if (source == null) {
            return;
        }
        if (source instanceof SQLExprTableSource) {
            out.add((SQLExprTableSource) source);
        } else if (source instanceof com.alibaba.druid.sql.ast.statement.SQLJoinTableSource) {
            com.alibaba.druid.sql.ast.statement.SQLJoinTableSource join =
                    (com.alibaba.druid.sql.ast.statement.SQLJoinTableSource) source;
            collectTableSources(join.getLeft(), out);
            collectTableSources(join.getRight(), out);
        }
        // SQLSubqueryTableSource 内部的查询块由 collectQueryBlocks 收集，单独处理
    }

    /** 校验一批 SQL（≤ maxStatements 条），全部通过才返回；任一失败即整批拒绝并给出原因 */
    public List<GuardResult> checkBatch(List<String> sqls, String tenantCode) {
        List<GuardResult> results = new ArrayList<>();
        if (sqls == null || sqls.isEmpty()) {
            results.add(GuardResult.reject("没有生成任何 SQL"));
            return results;
        }
        if (sqls.size() > policy.getMaxStatements()) {
            results.add(GuardResult.reject("一次最多 " + policy.getMaxStatements() + " 条 SQL，实际 " + sqls.size() + " 条"));
            return results;
        }
        for (String sql : sqls) {
            GuardResult r = check(sql, tenantCode);
            results.add(r);
            if (!r.isOk()) {
                return results;   // 批量中任一条不通过 → 整批不执行（硬失败，不"部分放行"）
            }
        }
        return results;
    }
}
