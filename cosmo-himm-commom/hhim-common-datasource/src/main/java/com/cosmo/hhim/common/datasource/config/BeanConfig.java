/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datasource.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.datasource.aop.MybatisInterceptor;
import com.cosmo.hhim.common.datasource.aop.SchemaIntercept;
import com.cosmo.hhim.common.datasource.http.NoErrorResultErrorHandler;
import com.cosmo.hhim.common.datasource.http.RestTemplateProxy;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Configuration
public class BeanConfig {

    @Value("#{'${mybatis.tenantLineIgnoreTables:}'.split(',')}")
    private List<String> tenantLineIgnoreTableList;

    /**
     * 租户行注入总开关，默认 true（兼容其他使用 tenant_code 列的项目）。
     * 当前项目所有表均无 tenant_code 列，因此设为 false 彻底禁用。
     */
    @Value("${mybatis.tenantLineEnabled:true}")
    private boolean tenantLineEnabled;

    @Bean
    public RestTemplate getRestTemplateBean() {
        RestTemplate restTemplate = new RestTemplateProxy();
        restTemplate.setErrorHandler(new NoErrorResultErrorHandler());
        return restTemplate;
    }

    @Bean
    public SchemaIntercept getBean() {
        return new SchemaIntercept();
    }



    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(
                new TenantLineHandler() {
                    // 获取租户 ID 值表达式，只支持单个 ID 值
                    @Override
                    public Expression getTenantId() {
                        String tenantId = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
                        if (tenantId == null || tenantId.isEmpty()) {
                            return new StringValue("");
                        }
                        return new StringValue(tenantId);
                    }

                    // 忽略表级租户行注入：
                    // 1. 项目级开关关闭 → 忽略所有表
                    // 2. ThreadContext 中无租户 ID → 忽略所有表（单库/单租户场景）
                    // 3. 否则按 tenantLineIgnoreTables 配置决定
                    @Override
                    public boolean ignoreTable(String tableName) {
                        if (!tenantLineEnabled) {
                            return true;
                        }
                        String tenantId = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
                        if (tenantId == null || tenantId.isEmpty()) {
                            return true;
                        }
                        if (CollectionUtils.isEmpty(tenantLineIgnoreTableList)) {
                            return false;
                        }
                        return tenantLineIgnoreTableList.stream().anyMatch(e -> e.equals(tableName));
                    }

                    @Override
                    public String getTenantIdColumn() {
                        return "tenant_code";
                    }
                }));
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    public MybatisInterceptor getMybatisInterceptorBean() {
        return new MybatisInterceptor();
    }
}
