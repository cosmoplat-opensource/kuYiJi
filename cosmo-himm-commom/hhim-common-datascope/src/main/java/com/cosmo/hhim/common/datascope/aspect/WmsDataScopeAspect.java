/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datascope.aspect;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.CheckObjectUtils;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.cosmo.hhim.common.datascope.annotation.DataScopes;
import com.cosmo.hhim.common.datascope.annotation.WmsDataScope;
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
public class WmsDataScopeAspect {

    /**
     * 全部数据权限
     */
    public static final String DATA_SCOPE_ALL = "1";

    /**
     * 自定数据权限
     */
    public static final String DATA_SCOPE_CUSTOM = "2";

    /**
     * 部门数据权限
     */
    public static final String DATA_SCOPE_DEPT = "3";

    /**
     * 部门及以下数据权限
     */
    public static final String DATA_SCOPE_DEPT_AND_CHILD = "4";

    /**
     * 仅本人数据权限
     */
    public static final String DATA_SCOPE_SELF = "5";


    /**
     * 数据权限过滤关键字
     */
    public static final String DATA_SCOPE = "dataScope";

    @Autowired
    private TokenService tokenService;

    // 配置织入点
    @Pointcut("@annotation(com.cosmo.hhim.common.datascope.annotation.WmsDataScope) || " +
            "@annotation(com.cosmo.hhim.common.datascope.annotation.DataScopes)")
    public void dataScopePointCut() {
    }

    @Before("dataScopePointCut()")
    public void doBefore(JoinPoint point) throws Throwable {
        handleDataScope(point);
    }

    protected void handleDataScope(final JoinPoint joinPoint) {
        List<WmsDataScope> list = new ArrayList<>();
        String keyword = DataScopes.Type.AND.name();
        // 获得注解
        WmsDataScope controllerDataScope = getAnnotationLog(joinPoint);
        if (controllerDataScope != null) {
            list.add(controllerDataScope);
        } else {
            DataScopes controllerDataScopes = getAnnotationLog2(joinPoint);
            if (controllerDataScopes == null) {
                return;
            }
            keyword = controllerDataScopes.type().name();
            list.addAll(Arrays.asList(controllerDataScopes.value()));
        }
        // 如果是mainAccountFlag！=1，则不过滤数据
        if ((CheckObjectUtils.isEmpty(ThreadContext.get(Constants.MAIN_ACCOUNT_FLAG)) || !"1".equals(ThreadContext.get(Constants.MAIN_ACCOUNT_FLAG))) && !"System".equals(SecurityUtils.getUsername())) {
            dataScopeFilter(joinPoint,  list, keyword);
        }

    }

    /**
     * 数据范围过滤
     *
     * @param list      数据权限注解
     * @param joinPoint 切点
     * @param keyword   sql连接关键字
     */
    public static void dataScopeFilter(JoinPoint joinPoint,  List<WmsDataScope> list, String keyword) {
        List<SysDataAuthority> dataAuthority = SecurityUtils.getAuthority();
        StringBuilder sqlString = new StringBuilder();
        for (WmsDataScope controllerDataScope : list) {
            String type = controllerDataScope.wmsType().toString();
            String wmsAlias = controllerDataScope.wmsAlias();
            String customizeAlias = controllerDataScope.wmsCustomizeAlias();
            List<SysDataAuthority> wmsAuthorityList = dataAuthority.stream().filter(s -> "wms".equals(s.getAppType())).collect(Collectors.toList());
            List<SysDataAuthority> dataAuthorityList = wmsAuthorityList.stream().filter(s -> type.equals(s.getDataType())).collect(Collectors.toList());
            List<String> scopeList = dataAuthorityList.stream().map(e -> e.getDataCode()).collect(Collectors.toList());
            String scopes = String.join("','", scopeList);



            // wms专用数据权限
            // 自定义列
            if (StringUtils.isNotBlank(customizeAlias)) {
                sqlString.append(StringUtils.format(
                        keyword + " {} in ('{}') ", customizeAlias, scopes));
            } else {
                // 库区级
                if (WmsDataScope.Type.AREA.toString().equals(type) && StringUtils.isNotBlank(wmsAlias)) {
                    sqlString.append(StringUtils.format(
                            keyword + " {}.area_id in ('{}') ", wmsAlias, scopes));
                } else if (WmsDataScope.Type.WH.toString().equals(type) && StringUtils.isNotBlank(wmsAlias)) {
                    // 仓库级
                    sqlString.append(StringUtils.format(
                            keyword + " {}.wh_code in ('{}') ", wmsAlias, scopes));
                } else if (WmsDataScope.Type.FAC.toString().equals(type) && StringUtils.isNotBlank(wmsAlias)) {
                    // 工厂级
                    sqlString.append(StringUtils.format(
                            keyword + " {}.factory_code in ('{}') ", wmsAlias, scopes));
                } else {
                    sqlString.append(StringUtils.format(
                            keyword + " {} in ('{}') ", wmsAlias, scopes));
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
    private WmsDataScope getAnnotationLog(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        if (method != null) {
            return method.getAnnotation(WmsDataScope.class);
        }
        return null;
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
