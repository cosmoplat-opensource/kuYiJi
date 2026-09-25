/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datascope.aspect;

import com.cosmo.hhim.common.core.utils.CheckObjectUtils;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.cosmo.hhim.common.datascope.annotation.CimDataScope;
import com.cosmo.hhim.common.security.pojo.LoginUser;
import com.cosmo.hhim.common.security.pojo.SysDataAuthority;
import com.cosmo.hhim.common.security.pojo.SysRole;
import com.cosmo.hhim.common.security.pojo.SysUser;
import com.cosmo.hhim.common.security.utils.SecurityUtils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据过滤处理
 *
 * @author cosmo-hhim-open Team
 */
@Aspect
@Component
public class CimDataScopeAspect {


    // 配置织入点
    @Pointcut("@annotation(com.cosmo.hhim.common.datascope.annotation.CimDataScope)")
    public void dataScopePointCut() {
    }

    @Before("dataScopePointCut()")
    public void doBefore(JoinPoint point) throws Throwable {
        handleDataScope(point);
    }

    protected void handleDataScope(final JoinPoint joinPoint) {
        // 获得注解
        CimDataScope controllerDataScope = getAnnotationLog(joinPoint);
        if (controllerDataScope == null) {
            return;
        }
        dataScopeFilter(joinPoint, controllerDataScope);
    }

    /**
     * @return void
     * @author cosmo-hhim-open Team
     * @description //数据权限过滤
     * @date 16:15 2021/7/28
     * @param joinPoint
     * @param dataScope
     **/
    public static void dataScopeFilter(JoinPoint joinPoint, CimDataScope dataScope) {
        StringBuilder sqlString = new StringBuilder();

        String scopeAlias = dataScope.scopeAlias();
        String scopeType = dataScope.scopeType();

        if (StringUtils.isEmpty(scopeAlias) || StringUtils.isEmpty(scopeType)) {
            sqlString.append(StringUtils.format(
                    " 1='注解参数缺失：scopeType，scopeAlias 都不可为空'"));
        } else {

            String[] scopes = getScopes(scopeType, SecurityUtils.getAuthority());
            if (StringUtils.isEmpty(scopes[1])) {
                sqlString.append(StringUtils.format(
                        " 1='注解参数scopeType错误（F、W、L），'"));
            } else {
                //康派斯也可以查询产线为空的工单
                if (CheckObjectUtils.isNotEmpty(scopeType) &&"L".equals(scopeType)) {
                    sqlString.append(StringUtils.format(
                            " {}.FACTORY_CODE = '" + scopes[2] + "'  AND ({}.{} IN ('" + scopes[0] + "') or {}.{} is null )", scopeAlias, scopeAlias, scopes[1], scopeAlias, scopes[1]));
                }else {
                    sqlString.append(StringUtils.format(
                            " {}.FACTORY_CODE = '" + scopes[2] + "'  AND {}.{} IN ('" + scopes[0] + "') ", scopeAlias, scopeAlias, scopes[1]));
                }
            }
        }

        if (StringUtils.isNotBlank(sqlString.toString())) {
            BaseEntity baseEntity = (BaseEntity) joinPoint.getArgs()[0];
            baseEntity.setDataScope(" AND " + sqlString);
        }

    }

    /**
     * @return java.lang.String[]
     * @author cosmo-hhim-open Team
     * @description //用户授权信息过滤
     * @date 16:15 2021/7/28
     * @param scopeType
     * @param dataAuthority
     **/
    private static String[] getScopes(String scopeType, List<SysDataAuthority> dataAuthority) {
        String[] resScopes = new String[3];
        List<SysDataAuthority> factoryAuthorityList;
        List<SysDataAuthority> cimAuthorityList = dataAuthority.stream().filter(s -> "cim".equals(s.getAppType())).collect(Collectors.toList());
        List<SysDataAuthority> dataAuthorityList = cimAuthorityList.stream().filter(s -> scopeType.equals(s.getDataType())).collect(Collectors.toList());
        List<String> scopeList = dataAuthorityList.stream().map(e -> e.getDataCode()).collect(Collectors.toList());
        String scopes = String.join("','", scopeList);
        resScopes[0] = scopes;
        if ("F".equals(scopeType)) {
            resScopes[1] = "FACTORY_CODE";
            factoryAuthorityList = dataAuthorityList;
        } else {
            factoryAuthorityList = cimAuthorityList.stream().filter(s -> "F".equals(s.getDataType())).collect(Collectors.toList());
        }
        if ("W".equals(scopeType)) {
            resScopes[1] = "WSHOP_CODE";
        }
        if ("L".equals(scopeType)) {
            resScopes[1] = "MLINE_CODE";
        }
        if (factoryAuthorityList.size() != 1) {
            resScopes[2] = SecurityUtils.getHeaderFactory();
        } else {
            resScopes[2] = factoryAuthorityList.get(0).getDataCode();
        }
        return resScopes;
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private CimDataScope getAnnotationLog(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        if (method != null) {
            return method.getAnnotation(CimDataScope.class);
        }
        return null;
    }
}
