/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datasource.aop;

import com.cosmo.hhim.common.core.utils.reflect.ReflectUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/*
 * @author cosmo-hhim-open Team
 * @date 2021/3/24 14:16
 *
 */

@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
@Component
public class MybatisInterceptor implements Interceptor {

    public static final String CREATE_INFO_METHOD = "setCreateInfo";
    public static final String UPDATE_INFO_METHOD = "setUpdateInfo";
    private static final Logger log = LoggerFactory.getLogger(MybatisInterceptor.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
            Object parameter = invocation.getArgs()[1];
            if (SqlCommandType.INSERT == sqlCommandType) {
                setBaseInfoByReflect(parameter, CREATE_INFO_METHOD);
            }
            if (SqlCommandType.UPDATE == sqlCommandType) {
                setBaseInfoByReflect(parameter, UPDATE_INFO_METHOD);
            }
        } catch (Exception e) {
            log.warn("set base info failed: {}", e.getMessage(), e);
        }
        return invocation.proceed();
    }

    private void setBaseInfoByReflect(Object parameter, String methods) {
        try {
            if (parameter instanceof Map) {
                Map<String, Object> params = (Map<String, Object>) parameter;
                for (String key : params.keySet()) {
                    if ("list".equals(key)) {

                        List<Object> list = (List<Object>) params.get("list");
                        // 批量执行 (没有此方法则跳过赋值,继续执行sql)
                        list.forEach(t -> ReflectUtils.invokeMethodByName(t, methods, new Object[0]));
                    } else {
                        // 多参数 (没有此方法则跳过赋值,继续执行sql)
                        ReflectUtils.invokeMethodByName(params.get(key), methods, new Object[0]);
                    }
                }
            } else {
                // 单参数 (没有此方法则跳过赋值,继续执行sql)
                ReflectUtils.invokeMethodByName(parameter, methods, new Object[0]);
            }
        } catch (Exception e) {
            log.warn("set base info by reflect failed: {}", e.getMessage(), e);
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

}