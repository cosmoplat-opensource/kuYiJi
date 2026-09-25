/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datascope.aspect;

import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.cosmo.hhim.common.datascope.annotation.DataScopes;
import com.cosmo.hhim.common.datascope.annotation.DeviceDataScope;
import com.cosmo.hhim.common.security.pojo.LoginUser;
import com.cosmo.hhim.common.security.pojo.SysRole;
import com.cosmo.hhim.common.security.pojo.SysUser;
import com.cosmo.hhim.common.security.service.TokenService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.cosmo.hhim.common.datascope.constant.DataScopeConstants.*;

/**
 * @description: 设备数据权限切面
 * @classname: DeviceDataScopeAspect
 * @date: 2021/12/6 11:17
 * @author cosmo-hhim-open Team
 * @version 1.0
 */
@Aspect
@Component
public class DeviceDataScopeAspect {

    @Resource
    private TokenService tokenService;

    // 配置织入点
    @Pointcut("@annotation(com.cosmo.hhim.common.datascope.annotation.DeviceDataScope) || " +
            "@annotation(com.cosmo.hhim.common.datascope.annotation.DataScopes)")
    public void dataScopePointCut() {
    }

    @Before("dataScopePointCut()")
    public void doBefore(JoinPoint point) throws Throwable {
        handleDataScope(point);
    }

    protected void handleDataScope(final JoinPoint joinPoint) {
        List<DeviceDataScope> list = new ArrayList<>();
        String keyword = DataScopes.Type.OR.name();
        // 获得注解
        DeviceDataScope controllerDataScope = getAnnotationLog(joinPoint);
        if (controllerDataScope != null) {
            list.add(controllerDataScope);
        } else {
            DataScopes controllerDataScopes = getAnnotationLog2(joinPoint);
            if (controllerDataScopes == null) {
                return;
            }
            keyword = controllerDataScopes.type().name();
            list.addAll(Arrays.asList(controllerDataScopes.deviceValue()));
        }
        // 获取当前的用户
        LoginUser loginUser = tokenService.getLoginUser();
        if (StringUtils.isNotNull(loginUser)) {
            SysUser currentUser = loginUser.getSysUser();
            // 如果是超级管理员，则不过滤数据
            if (StringUtils.isNotNull(currentUser) && !currentUser.isAdmin()) {
                dataScopeFilter(joinPoint, currentUser, list, keyword);
            }
        }
    }

    /**
     * 数据范围过滤
     *
     * @param list      数据权限注解
     *                  //     * @param deptAlias 部门别名
     *                  //     * @param userAlias 用户别名
     * @param joinPoint 切点
     * @param user      用户
     * @param keyword   sql连接关键字
     */
    public static void dataScopeFilter(JoinPoint joinPoint, SysUser user, List<DeviceDataScope> list, String keyword) {
        StringBuilder sqlString = new StringBuilder();
        for (DeviceDataScope controllerDataScope : list) {
            String deptAlias = controllerDataScope.deptAlias();
            String userAlias = controllerDataScope.userAlias();
            DeviceDataScope.Type type = controllerDataScope.deviceType();
            String deviceAlias = controllerDataScope.deviceAlias();
            String customizeAlias = controllerDataScope.deviceCustomizeAlias();

            for (SysRole role : user.getRoles()) {
                String dataScope = role.getDataScope();
                if (DATA_SCOPE_ALL.equals(dataScope)) {
                    break;
                } else if (DATA_SCOPE_CUSTOM.equals(dataScope) && StringUtils.isNotBlank(deptAlias)) {
                    sqlString.append(StringUtils.format(
                            " OR {}.dept_id IN ( SELECT dept_id FROM sys_role_dept WHERE role_id = {} ) ", deptAlias,
                            role.getRoleId()));
                } else if (DATA_SCOPE_DEPT.equals(dataScope) && StringUtils.isNotBlank(deptAlias)) {
                    sqlString.append(StringUtils.format(" OR {}.dept_id = {} ", deptAlias, user.getDeptId()));
                } else if (DATA_SCOPE_DEPT_AND_CHILD.equals(dataScope) && StringUtils.isNotBlank(deptAlias)) {
                    sqlString.append(StringUtils.format(
                            " OR {}.dept_id IN ( SELECT dept_id FROM sys_dept WHERE dept_id = {} or find_in_set( {} , ancestors ) )",
                            deptAlias, user.getDeptId(), user.getDeptId()));
                } else if (DATA_SCOPE_SELF.equals(dataScope)) {
                    if (StringUtils.isNotBlank(userAlias)) {
                        sqlString.append(StringUtils.format(" OR {}.user_id = {} ", userAlias, user.getUserId()));
                    } else {
                        // 数据权限为仅本人且没有userAlias别名不查询任何数据
                        sqlString.append(" OR 1=0 ");
                    }
                }
            }

            // 自定义列
            if (StringUtils.isNotBlank(customizeAlias)) {
                sqlString.append(StringUtils.format(
                        " " + keyword + " {} in ( select data_code from sys_data_authority sds where sds.user_id = {} and sds.data_type = '{}' and sds.app_type='itpm') OR {} IS NULL",
                        customizeAlias, user.getUserId(), type.toString(),customizeAlias));
            } else {
                //设备级
                if (DeviceDataScope.Type.DEVICE.equals(type) && StringUtils.isNotBlank(deviceAlias)) {
                    sqlString.append(StringUtils.format(
                            " " + keyword + " {}.dept_id in ( select data_code from sys_data_authority sds where sds.user_id = {} and sds.data_type = '{}' and sds.app_type='itpm') OR {}.dept_id IS NULL",
                            deviceAlias, user.getUserId(), type.toString(),deviceAlias));
                }
            }
        }

        if (StringUtils.isNotBlank(sqlString.toString())) {
            Object params = joinPoint.getArgs()[0];
            if (StringUtils.isNotNull(params) && params instanceof BaseEntity) {
                BaseEntity baseEntity = (BaseEntity) params;
                baseEntity.getParams().put(DATA_SCOPE, " AND (" + sqlString.substring(4) + ")");
            }
        }
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private DeviceDataScope getAnnotationLog(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        if (method != null) {
            return method.getAnnotation(DeviceDataScope.class);
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
