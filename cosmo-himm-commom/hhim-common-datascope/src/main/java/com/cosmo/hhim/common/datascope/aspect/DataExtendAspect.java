/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datascope.aspect;

import com.alibaba.fastjson.JSONObject;

import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.cosmo.hhim.common.datascope.annotation.DataExtend;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 数据扩展过滤处理
 *
 * @author cosmo-hhim-open Team
 */
@Aspect
@Component
public class DataExtendAspect {
    /**
     * 全部数据权限
     */
    public static final String EXTEND = "1";


    // 配置织入点
    @Pointcut("@annotation(com.cosmo.hhim.common.datascope.annotation.DataExtend)")
    public void dataExtendPointCut() {
    }

    @Before("dataExtendPointCut()")
    public void doBefore(JoinPoint point) throws Throwable {
        handleDataExtend(point);
    }

    protected void handleDataExtend(final JoinPoint joinPoint) {
        // 获得注解
        DataExtend controllerDataExtend = getAnnotationLog(joinPoint);
        if (controllerDataExtend == null) {
            return;
        }
        String scopeAlias = controllerDataExtend.extendAlias();
        if (StringUtils.isNotEmpty(scopeAlias)) {
            scopeAlias+=".";
        }
        StringBuilder sqlString = new StringBuilder();
        Object[] args = joinPoint.getArgs();
        BaseEntity baseEntity = (BaseEntity) args[0];


        if (StringUtils.isNotNull(baseEntity.getExtendContent())) {
            JSONObject extendContent = JSONObject.parseObject(baseEntity.getExtendContent());
//            Iterator iterator = extendContent.keys();
//            while (iterator.hasNext()) {
//                String key = (String) iterator.next();
//                    String value = extendContent.getString(key);
//                    if(StringUtils.isNotEmpty(value)) {
//                        sqlString.append(" extend_content -> '$.'"+key+"' = '" +value +"'");
//                    }
//            }
//        }

            for (Map.Entry entry : extendContent.entrySet()) {
                String key = (String) entry.getKey();
                String value = entry.getValue().toString();
                if (StringUtils.isNotEmpty(value)) {
//                    sqlString.append(" and extend_content -> '$." + key + "' = '" + value + "'");
                    String[] val = value.split(",");
                    if (val.length > 0) {
                        sqlString.append(" and (");
                        for (int i = 0; i < val.length; i++) {
                            if (i > 0) {
                                sqlString.append(" or");
                            }
                            sqlString.append("  "+scopeAlias+"extend_content->'$."+key+"' LIKE '%"+ val[i] +"%' ");
                        }
                        sqlString.append(" )");
                    } else {
                        sqlString.append(" and "+scopeAlias+"extend_content->'$."+key+"' LIKE '%"+ value +"%' ");
                    }
                }
            }
        }

        if (StringUtils.isNotBlank(sqlString.toString())) {
            baseEntity.setDataExtend("  " + sqlString);
        }

    }


    /**
     * 是否存在注解，如果存在就获取
     */
    private DataExtend getAnnotationLog(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        if (method != null) {
            return method.getAnnotation(DataExtend.class);
        }
        return null;
    }
}
