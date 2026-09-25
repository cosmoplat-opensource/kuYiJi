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
public @interface WmsDataScope {

    enum Type {FAC, WH, AREA}

    /**
     * 部门表的别名
     */
    public String deptAlias() default "";

    /**
     * 用户表的别名
     */
    public String userAlias() default "";

    /**
     * wms数据权限类型
     */
    public Type wmsType() default Type.AREA;

    /**
     * wms业务表别名
     */
    public String wmsAlias() default "";

    /**
     * wms业务表自定义列
     */
    public String  wmsCustomizeAlias() default "";

}
