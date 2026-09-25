/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datasource.config;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.ResourceServlet;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.alibaba.druid.support.http.stat.WebAppStat;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.cosmo.hhim.common.datasource.aop.MybatisInterceptor;
import com.cosmo.hhim.common.datasource.aop.SchemaIntercept;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

//表示这个类为一个配置类
@EnableTransactionManagement
@Configuration
// 配置mybatis的接口类放的地方

public class DataSourceConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceConfig.class);

    @Autowired
    private SchemaIntercept schemaIntercept;
    @Autowired
    private MybatisPlusInterceptor mybatisPlusInterceptor;
    @Value("${mybatis.typeAliasesPackage:com.cosmo.hhim.common.security.pojo,com.cosmo.hhim.micro}")
    private String typeAliasesPackage;
    @Value("${mybatis.mapperLocations:classpath*:mapper/**/*.xml}")
    private String mapperLocations;
    @Autowired
    private MybatisInterceptor mybatisInterceptor;


    @Bean(name = "db0")
    @ConfigurationProperties(prefix = "spring.datasource.druid.db0")
    public DataSource db0() {
        return new DruidDataSource();
    }

    @Bean(name = "db1")
    @ConfigurationProperties(prefix = "spring.datasource.druid.db1")
    public DataSource db1() {
        return new DruidDataSource();
    }

    @Bean(name = "statFilter")
    public StatFilter wallFilter() {
        StatFilter statFilter = new StatFilter();
        statFilter.setLogSlowSql(true);
        statFilter.setSlowSqlMillis(5000);
        statFilter.setDbType("mysql");
        return statFilter;
    }


    /**
     * Druid 监控页（/druid/*）：默认不注册，避免生产环境暴露 SQL 监控等敏感信息。
     * 需要时通过配置 druid.monitor.enabled=true 开启，且必须配置访问账号密码（账号密码勿使用默认值）
     */
    @Bean
    @ConditionalOnProperty(name = "druid.monitor.enabled", havingValue = "true")
    public ServletRegistrationBean monitor(@Value("${druid.monitor.username:}") String druidUsername,
                                           @Value("${druid.monitor.password:}") String druidPassword) {
        if (!StringUtils.hasText(druidUsername) || !StringUtils.hasText(druidPassword)) {
            throw new IllegalArgumentException("开启 Druid 监控页（druid.monitor.enabled=true）必须配置访问账号密码：druid.monitor.username / druid.monitor.password");
        }
        StatViewServlet druidServlet = new StatViewServlet();
        ServletRegistrationBean druidServletRegistration = new ServletRegistrationBean(druidServlet);
        druidServletRegistration.addInitParameter("allow", "");
        druidServletRegistration.addUrlMappings("/druid/*");
        druidServletRegistration.addInitParameter(ResourceServlet.PARAM_NAME_USERNAME, druidUsername);
        druidServletRegistration.addInitParameter(ResourceServlet.PARAM_NAME_PASSWORD, druidPassword);
        druidServletRegistration.addInitParameter("resetEnable", "false");
        return druidServletRegistration;
    }


    /**
     * 动态数据源配置
     *
     * @return
     */
    @Bean
    @Primary
    public DataSource multipleDataSource(@Qualifier("db0") DataSource db0,
                                         @Qualifier("db1") DataSource db1

    ) {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("db0", db0);
        targetDataSources.put("db1", db1);
        LOGGER.info("targetDataSources:{}", targetDataSources);
        dynamicDataSource.setTargetDataSources(targetDataSources);
        dynamicDataSource.setDefaultTargetDataSource(db0); // 程序默认数据源，这个要根据程序调用数据源频次，经常把常调用的数据源作为默认

        return dynamicDataSource;
    }


    @Bean("sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        Properties prop = new Properties();
        //prop.setProperty("mapUnderscoreToCamelCase", "true");
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(multipleDataSource(db0(), db1()
        ));
        sqlSessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(mapperLocations));
        sqlSessionFactory.setTypeAliasesPackage(typeAliasesPackage);
        //sqlSessionFactory.setConfigurationProperties(prop);
        sqlSessionFactory.setPlugins(new Interceptor[]{
                schemaIntercept, mybatisPlusInterceptor, mybatisInterceptor
        });
        return sqlSessionFactory.getObject();
    }

}
