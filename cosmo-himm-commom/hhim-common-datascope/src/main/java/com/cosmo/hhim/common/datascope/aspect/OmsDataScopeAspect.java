/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datascope.aspect;

import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.cosmo.hhim.common.datascope.annotation.*;
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

import static com.cosmo.hhim.common.datascope.constant.DataScopeConstants.*;
import static com.cosmo.hhim.common.datascope.constant.DataScopeConstants.DATA_SCOPE;

/**
 * 数据过滤处理
 * 
 * @author cosmo-hhim-open Team
 */
@Aspect
@Component
public class OmsDataScopeAspect
{

    @Autowired
    private TokenService tokenService;

    // 配置织入点
    @Pointcut("@annotation(com.cosmo.hhim.common.datascope.annotation.OmsDataScope)")
    public void dataScopePointCut()
    {
    }

    @Before("dataScopePointCut()")
    public void doBefore(JoinPoint point) throws Throwable
    {
        handleDataScope(point);
    }

    protected void handleDataScope(final JoinPoint joinPoint)
    {
        List<OmsDataScope> list = new ArrayList<>();
        String keyword = DataScopes.Type.AND.name();
        // 获得注解
        OmsDataScope controllerDataScope = getAnnotationLog(joinPoint);
        if (controllerDataScope != null) {
            list.add(controllerDataScope);
        }
        // 获取当前的用户
        LoginUser loginUser = tokenService.getLoginUser();
        if (StringUtils.isNotNull(loginUser))
        {
            SysUser currentUser = loginUser.getSysUser();
            // 如果是超级管理员，则不过滤数据
            if (StringUtils.isNotNull(currentUser) && !currentUser.isAdmin())
            {
                dataScopeFilter(joinPoint, currentUser, list,keyword);
            }
        }
    }

    /**
     * 数据范围过滤
     * 
     */
    public static void dataScopeFilter(JoinPoint joinPoint, SysUser user, List<OmsDataScope> list, String keyword)
    {
        List<SysDataAuthority> dataAuthority = SecurityUtils.getAuthority();
        StringBuilder sqlString = new StringBuilder();
        for (OmsDataScope controllerDataScope : list) {
            OmsDataScope.Type type = controllerDataScope.omsType();
            String omsAlias = controllerDataScope.omsAlias();
            String customizeAlias = controllerDataScope.omsCustomizeAlias();


            List<SysDataAuthority> omsAuthorityList = dataAuthority.stream().filter(s -> "oms".equals(s.getAppType())).collect(Collectors.toList());
            List<String> scopeList = omsAuthorityList.stream().map(SysDataAuthority::getDataCode).collect(Collectors.toList());
            String scopes = String.join("','", scopeList);

            // oms专用数据权限
            // 自定义列
            if (StringUtils.isNotBlank(customizeAlias)) {
                sqlString.append(StringUtils.format(
                        keyword + " {} in ('{}') ",
                        customizeAlias, scopes));
            } else {
                if (OmsDataScope.Type.SALE.equals(type)) {
                    if(StringUtils.isNotBlank(omsAlias)){
                        sqlString.append(StringUtils.format(
                                keyword + " {}.CLIENT_CODE in ('{}') ",omsAlias, scopes));
                    }else{
                        sqlString.append(StringUtils.format(
                                keyword + " CLIENT_CODE in ('{}') ",scopes));
                    }
                }
            }
        }

        if (StringUtils.isNotBlank(sqlString.toString())) {
            BaseEntity baseEntity = (BaseEntity) joinPoint.getArgs()[0];
            baseEntity.setDataScope(sqlString.toString());
        }
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private OmsDataScope getAnnotationLog(JoinPoint joinPoint)
    {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        if (method != null)
        {
            return method.getAnnotation(OmsDataScope.class);
        }
        return null;
    }
}
