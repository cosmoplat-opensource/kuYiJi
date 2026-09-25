/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datascope.annotation;

import java.lang.annotation.*;

/**
 * @description: 制造运维数据权限注解
 * @classname: DeviceDataScope
 * @date: 2021/12/6 13:12
 * @author cosmo-hhim-open Team
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DeviceDataScope {

    enum Type {DEVICE}

    /**
     * 部门表的别名
     */
    public String deptAlias() default "";

    /**
     * 用户表的别名
     */
    public String userAlias() default "";

    /**
     * itpm数据权限类型
     */
    public DeviceDataScope.Type deviceType() default Type.DEVICE;

    /**
     * itpm业务表别名
     */
    public String deviceAlias() default "";

    /**
     * itpm业务表自定义列
     */
    public String  deviceCustomizeAlias() default "";
}
