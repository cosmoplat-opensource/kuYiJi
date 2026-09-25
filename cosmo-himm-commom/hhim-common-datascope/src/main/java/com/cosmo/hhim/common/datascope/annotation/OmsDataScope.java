/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datascope.annotation;

import java.lang.annotation.*;

/**
 * oms数据权限过滤注解
 * 
 * @author cosmo-hhim-open Team
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OmsDataScope
{
    enum Type {SALE}
    /**
     * 部门表的别名
     */
    public String deptAlias() default "";

    /**
     * 用户表的别名
     */
    public String userAlias() default "";

    public OmsDataScope.Type omsType() default Type.SALE;

    String omsAlias() default "";

    String omsCustomizeAlias() default "";
}
