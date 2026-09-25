/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datascope.aspect;

import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.cosmo.hhim.common.datascope.annotation.DataScopes;
import com.cosmo.hhim.common.datascope.annotation.SrmDataScope;
import com.cosmo.hhim.common.security.pojo.LoginUser;
import com.cosmo.hhim.common.security.pojo.SysDataAuthority;
import com.cosmo.hhim.common.security.pojo.SysRole;
import com.cosmo.hhim.common.security.pojo.SysUser;
import com.cosmo.hhim.common.security.service.TokenService;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据过滤处理
 *
 * @author cosmo-hhim-open Team
 */
@Aspect
@Component
public class SrmDataScopeAspect {


    /**
     * 数据权限过滤关键字
     */
    public static final String DATA_SCOPE = "dataScope";


    @Autowired
    private TokenService tokenService;

    // 配置织入点
    @Pointcut("@annotation(com.cosmo.hhim.common.datascope.annotation.SrmDataScope)")
    public void dataScopePointCut() {
    }

    @Before("dataScopePointCut()")
    public void doBefore(JoinPoint point) throws Throwable {
        clearDataScope(point);
        handleDataScope(point);
    }

    protected void handleDataScope(final JoinPoint joinPoint) {
        List<SrmDataScope> list = new ArrayList<>();
        String keyword = DataScopes.Type.AND.name();
        // 获得注解
        SrmDataScope controllerDataScope = getAnnotationLog(joinPoint);
        if (controllerDataScope != null) {
            list.add(controllerDataScope);
        } else {
            DataScopes controllerDataScopes = getAnnotationLog2(joinPoint);
            if (controllerDataScopes == null) {
                return;
            }
            keyword = controllerDataScopes.type().name();
            list.addAll(Arrays.asList(controllerDataScopes.srmValue()));
        }
        // 如果是超级管理员，则不过滤数据
        dataScopeFilter(joinPoint, list, keyword);
    }

    /**
     * 数据范围过滤
     *
     * @param list      数据权限注解
     *                  //     * @param deptAlias 部门别名
     *                  //     * @param userAlias 用户别名
     * @param joinPoint 切点
     * @param keyword   sql连接关键字
     */
    public static void dataScopeFilter(JoinPoint joinPoint, List<SrmDataScope> list, String keyword) {
        List<SysDataAuthority> dataAuthority = SecurityUtils.getAuthority();
        StringBuilder sqlString = new StringBuilder();
        for (SrmDataScope controllerDataScope : list) {
            SrmDataScope.Type type = controllerDataScope.srmType();
            String srmAlias = controllerDataScope.srmAlias();
            String customizeAlias = controllerDataScope.srmCustomizeAlias();


            List<SysDataAuthority> srmAuthorityList = dataAuthority.stream().filter(s -> "srm".equals(s.getAppType())).collect(Collectors.toList());
            List<String> scopeList = srmAuthorityList.stream().map(e -> e.getDataCode()).collect(Collectors.toList());
            String scopes = String.join("','", scopeList);

            // srm专用数据权限
            // 自定义列
            if (StringUtils.isNotBlank(customizeAlias)) {
                sqlString.append(StringUtils.format(
                        keyword + " {} in ('{}') ",
                        customizeAlias, scopes));
            } else {
                // 供应商级
                if (SrmDataScope.Type.SUPPLIER.equals(type) && StringUtils.isNotBlank(srmAlias)) {
                    sqlString.append(StringUtils.format(
                            keyword + " {}.supplier_code in ('{}') ",
                            srmAlias,  scopes));
                }
            }
        }
        if (StringUtils.isNotBlank(sqlString.toString())) {
            Object params = joinPoint.getArgs()[0];
            if (StringUtils.isNotNull(params) && params instanceof BaseEntity) {
                BaseEntity baseEntity = (BaseEntity) params;
                if (list.size() > 1 && DataScopes.Type.OR.name().equals(keyword)) {
                    sqlString = new StringBuilder("AND (" + sqlString.substring(sqlString.indexOf(DataScopes.Type.OR.name()) + 2) + ")");
                }
                baseEntity.getParams().put(DATA_SCOPE, sqlString);
            }
        }
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private SrmDataScope getAnnotationLog(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        if (method != null) {
            return method.getAnnotation(SrmDataScope.class);
        }
        return null;
    }

    /**
     * 拼接权限sql前先清空params.dataScope参数防止注入
     */
    private void clearDataScope(final JoinPoint joinPoint) {
        Object params = joinPoint.getArgs()[0];
        if (StringUtils.isNotNull(params) && params instanceof BaseEntity) {
            BaseEntity baseEntity = (BaseEntity) params;
            baseEntity.getParams().put(DATA_SCOPE, "");
        }
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private DataScopes getAnnotationLog2(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        if (method != null) {
            return method.getAnnotation(DataScopes.class);
        }
        return null;
    }
}
