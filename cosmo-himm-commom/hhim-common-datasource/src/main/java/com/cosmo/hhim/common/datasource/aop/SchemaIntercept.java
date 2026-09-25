/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datasource.aop;//package com.cosmo.hhim.common.core.interceptor;


import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.utils.ServletUtils;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.datasource.config.TabelOfNewProcessor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.Properties;

/**
 * @author cosmo-hhim-open Team
 * @note mybatis自定义拦截器 非侵入式设置schema
 *
 * 行为变更（2026-06-05 decouple-from-ops-platform）：当 schema / customer 为空时，
 * 不再抛 RuntimeException（这在多租户框架下过于严格）—— 而是：
 *   1. log.warn 提示
 *   2. SQL 不加 schema 前缀，原样执行（依赖单库 MyBatis-Plus tenant_line 过滤）
 *   3. 让业务代码自己处理兜底（如认证流中可设默认值）
 *
 * 这样能让"用户态查询"（如 wxMiniAppLogin 查 micro_user_tenant_index）不被拦截器阻断。
 */
@Component
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
@SuppressWarnings("unchecked")
public class SchemaIntercept implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(SchemaIntercept.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //获取statementHandler
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        //获取绑定sql
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        String sql = boundSql.getSql();
        //sql=sql.toLowerCase();
        String schema = (String) ThreadContext.get(Constants.TARGET_SCHEMA);
        String customer = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);

        // decouple-from-ops-platform (C.13): 容错处理 — schema/customer 空时不再抛异常
        if (StringUtils.isEmpty(schema) || StringUtils.isEmpty(customer)) {
            String requestUri = "unknown";
            try {
                requestUri = ServletUtils.getRequest() == null ? "no-request" : ServletUtils.getRequest().getRequestURI();
            } catch (Exception ignored) { }
            log.warn("[SchemaIntercept] schema/customer 为空，跳过 schema 前缀注入（单库场景可正常运行） | schema={}, customer={}, requestUri={}", schema, customer, requestUri);
            // 不抛异常，SQL 原样执行 —— 适用于单库场景（不依赖 schema 前缀）
            // 多库场景：业务代码应在调用 SQL 前设置 ThreadContext，否则可能命中错误的数据源
            return invocation.proceed();
        }

        if(!sql.contains("INFORMATION_SCHEMA")){
            sql = TabelOfNewProcessor.changeTabelName(sql, schema);
        }

        metaObject.setValue("delegate.boundSql.sql", sql);

//        SchemaContextHolder.setSchema(null);
//        DataSourceContextHolder.setDbType0(null);

        return invocation.proceed();
    }


    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
