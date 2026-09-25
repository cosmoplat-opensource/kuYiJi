/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datascope.annotation;

import java.lang.annotation.*;

/**
 * 数据权限过滤注解
 * 
 * @author cosmo-hhim-open Team
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CimDataScope
{
    /**
     * 部门表的别名
     */
    String deptAlias() default "";

    /**
     * 用户表的别名
     */
    String userAlias() default "";

//    权限类别
    String scopeType() default "";
//    权限表别名
    String scopeAlias() default "";
}
