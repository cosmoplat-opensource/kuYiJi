/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.ai.sql;

import com.cosmo.hhim.micro.base.domain.mapper.ai.MicroAiDailyMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 生成 SQL 用的 schema 摘要与安全策略装配。
 *
 * <p>职责：
 * <ol>
 *   <li>从 {@code information_schema} 取<b>白名单表</b>的列信息；</li>
 *   <li>构造给 LLM 的 schema 摘要 —— <b>敏感表整表不出现、敏感字段不出现</b>
 *        （你定的两层排除：整表排除 + 字段级排除，一处配置三处生效：本体/提示词/SqlGuard）；</li>
 *   <li>据此生成 {@link SqlGuard.Policy}（表白名单 + 敏感表 + 敏感列 + 租户列 + 行数/语句数上限）。</li>
 * </ol>
 *
 * <p>敏感列不进摘要这一点很关键：LLM 看不到的手机号/密码字段，就不会去查它。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaProvider {

    private final MicroAiDailyMapper dailyMapper;
    private final AiSqlProperties properties;
    /** 用于取数据源读表结构（ObjectProvider：避免多数据源装配歧义） */
    private final org.springframework.beans.factory.ObjectProvider<org.apache.ibatis.session.SqlSessionFactory> sqlSessionFactories;

    /** 摘要里每张表最多展示的列数（避免提示词过长） */
    private static final int MAX_COLUMNS_PER_TABLE = 40;

    /** 缓存：schema 摘要在进程内复用（表结构很少变；本体热加载/重启时刷新） */
    private volatile String cachedBrief;
    private volatile SqlGuard.Policy cachedPolicy;

    /** 启动时打印生成通道的有效配置（enabled 是代码默认值，yml 可覆盖；这里让实际取值可见） */
    @javax.annotation.PostConstruct
    public void logEffectiveConfig() {
        log.info("[AI-SQL] 生成 SQL 通道 enabled={}，白名单 {} 张表（排除 {} 张），敏感列配置 {} 组，"
                        + "maxStatements={}，maxRows={}，maxRetry={}",
                properties.isEnabled(), properties.getWhitelistTables().size(),
                properties.getExcludedTables().size(), properties.excludedColumnsAsMap().size(),
                properties.getMaxStatements(), properties.getMaxRows(), properties.getMaxRetry());
    }

    /** 给 LLM 的 schema 摘要（白名单表 + 列名 + 类型 + 注释；敏感列已剔除） */
    public String schemaBrief() {
        if (cachedBrief != null) {
            return cachedBrief;
        }
        synchronized (this) {
            if (cachedBrief != null) {
                return cachedBrief;
            }
            cachedBrief = buildBrief();
            return cachedBrief;
        }
    }

    /** SqlGuard 使用的安全策略（同样基于 safety 配置） */
    public SqlGuard.Policy policy() {
        if (cachedPolicy != null) {
            return cachedPolicy;
        }
        synchronized (this) {
            if (cachedPolicy != null) {
                return cachedPolicy;
            }
            cachedPolicy = SqlGuard.Policy.builder()
                    .allowedTables(new HashSet<>(properties.getWhitelistTables()))
                    .excludedTables(new HashSet<>(properties.getExcludedTables()))
                    .excludedColumns(properties.excludedColumnsAsMap())
                    .tenantColumn(properties.getTenantColumn())
                    .maxRows(properties.getMaxRows())
                    .maxStatements(properties.getMaxStatements())
                    .build();
            return cachedPolicy;
        }
    }

    /** 清缓存（本体热加载 / 配置刷新时调用） */
    public void refresh() {
        cachedBrief = null;
        cachedPolicy = null;
    }

    // ------------------------------------------------------------------ 内部

    private String buildBrief() {
        List<String> tables = new ArrayList<>();
        for (String t : properties.getWhitelistTables()) {
            String lower = t.toLowerCase();
            if (properties.getExcludedTables().contains(lower)) {
                continue;   // 敏感表整表排除：连名字都不给 LLM
            }
            tables.add(lower);
        }
        if (tables.isEmpty()) {
            return "";
        }
        Map<String, Set<String>> excluded = properties.excludedColumnsAsMap();
        Map<String, List<String>> byTable = new LinkedHashMap<>();
        boolean ok = false;
        // 走 JDBC 元数据而不是 SQL：MyBatis-Plus 的 SQL 拦截器会解析并改写每条语句，
        // 遇到 information_schema 会抛 "Failed to process, Error SQL"（实测踩到），
        // 而 DatabaseMetaData 完全不经过 SQL 解析器，稳定且不依赖表权限。
        try (java.sql.Connection conn = dataSource() == null ? null : dataSource().getConnection()) {
            if (conn == null) {
                throw new IllegalStateException("未取到数据源（SqlSessionFactory 不可用）");
            }
            java.sql.DatabaseMetaData md = conn.getMetaData();
            String catalog = conn.getCatalog();
            for (String table : tables) {
                List<String> cols = new ArrayList<>();
                try (java.sql.ResultSet rs = md.getColumns(catalog, null, table, null)) {
                    while (rs.next()) {
                        String column = str(rs.getString("COLUMN_NAME")).toLowerCase();
                        if (column.isEmpty()) {
                            continue;
                        }
                        Set<String> excludedCols = excluded.get(table);
                        if (excludedCols != null && excludedCols.contains(column)) {
                            continue;   // 敏感字段单列排除：不进提示词
                        }
                        String type = simplify(str(rs.getString("TYPE_NAME")));
                        String comment = str(rs.getString("REMARKS")).replaceAll("[\\r\\n]", " ");
                        if (cols.size() < MAX_COLUMNS_PER_TABLE) {
                            cols.add(column + " " + type + (comment.isEmpty() ? "" : " //" + comment));
                        }
                    }
                }
                byTable.put(table, cols);
            }
            ok = true;
        } catch (Exception e) {
            log.warn("[AI-SQL] 读取表结构失败（改用配置里的 schema-hint，如有）：{}", e.getMessage());
        }
        if (!ok) {
            String hint = properties.getSchemaHint();
            return hint == null ? "" : hint;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> e : byTable.entrySet()) {
            if (e.getValue().isEmpty()) {
                continue;
            }
            sb.append(e.getKey()).append("(").append(String.join(", ", e.getValue())).append(")\n");
        }
        return sb.toString();
    }

    /** 从 MyBatis 环境取数据源（ObjectProvider 避免多数据源时的装配歧义） */
    private javax.sql.DataSource dataSource() {
        try {
            org.apache.ibatis.session.SqlSessionFactory factory =
                    sqlSessionFactories.getIfAvailable();
            if (factory == null || factory.getConfiguration() == null
                    || factory.getConfiguration().getEnvironment() == null) {
                return null;
            }
            return factory.getConfiguration().getEnvironment().getDataSource();
        } catch (Exception e) {
            return null;
        }
    }

    private String simplify(String type) {
        if (type == null || type.isEmpty()) {
            return "";
        }
        return type.replaceAll("\\(.*\\)", "");
    }

    private String str(Object v) {
        return v == null ? "" : v.toString();
    }

    /**
     * 生成 SQL 通道的配置（前缀 {@code ai.sql}）。
     *
     * <p>默认白名单只放"业务分析需要"的少量表；敏感表（用户/权限/日志等）默认排除。
     * 后续接本体 {@code safety.json} 时，这里只要换成读本体即可（一处配置三处生效）。
     */
    @Data
    @Component
    @ConfigurationProperties(prefix = "ai.sql")
    public static class AiSqlProperties {

        /** 通道开关（默认开：本体覆盖不到的问题自动走"LLM 生成 SQL"，失败仍回边界话术） */
        private boolean enabled = true;

        /** 表级白名单（分析可用） */
        private List<String> whitelistTables = Arrays.asList(
                "micro_work_submit", "micro_product", "micro_process_common",
                "micro_process_storage", "micro_finished_product_storage", "micro_user");

        /**
         * 整表排除：**只存敏感数据**或与业务分析无关的表（连表名都不进提示词）。
         *
         * <p>注意：像 micro_user 这种"部分字段敏感"的表，不整表排除，改用它下面的字段级排除。
         */
        private List<String> excludedTables = Arrays.asList(
                "micro_ai_chat_message", "micro_ai_chat_session", "micro_ai_sql_audit",
                "micro_user_platfrom_re", "micro_user_event_tracking");

        /**
         * 字段级排除：{表: [列]} —— 格式 {@code table:col1,col2;table2:col3}
         *
         * <p>这些列既不出现在提示词里，也会被 SqlGuard 拦截（出现即拒绝查询）——一处配置三处生效。
         */
        private String excludedColumns = "micro_user:user_name,phonenumber,avatar,mobile;"
                + "micro_work_submit:submit_pictures";

        /** 强制注入的租户列 */
        private String tenantColumn = "tenant_code";

        /** 单次查询返回行数上限 */
        private int maxRows = 500;

        /** 单次生成语句数上限（你定的 ≤3） */
        private int maxStatements = 3;

        /** 因闸拒绝而重写的最大次数 */
        private int maxRetry = 1;

        /** 生成 SQL 的执行超时（毫秒） */
        private int queryTimeoutMs = 5000;

        /**
         * 表结构读取失败时的兜底（可选）：直接写一段紧凑 schema 文本，
         * 格式与自动摘要一致，例如 "micro_work_submit(id, product_seq, ...)\nmicro_product(...)"。
         */
        private String schemaHint = "";

        /** 解析 excludedColumns 配置为 Map */
        public Map<String, Set<String>> excludedColumnsAsMap() {
            Map<String, Set<String>> map = new HashMap<>();
            if (excludedColumns == null || excludedColumns.trim().isEmpty()) {
                return map;
            }
            for (String part : excludedColumns.split(";")) {
                String[] kv = part.split(":");
                if (kv.length != 2) {
                    continue;
                }
                String table = kv[0].trim().toLowerCase();
                Set<String> cols = new HashSet<>();
                for (String c : kv[1].split(",")) {
                    if (!c.trim().isEmpty()) {
                        cols.add(c.trim().toLowerCase());
                    }
                }
                if (!cols.isEmpty()) {
                    map.put(table, cols);
                }
            }
            return map;
        }
    }
}
