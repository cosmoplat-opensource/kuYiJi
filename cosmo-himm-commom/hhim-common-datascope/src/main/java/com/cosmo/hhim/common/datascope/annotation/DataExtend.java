/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datascope.annotation;

import java.lang.annotation.*;

/**
 * 数据扩展过滤注解
 * 
 * @author cosmo-hhim-open Team
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataExtend
{
    String value() default "";
    String extendAlias() default "";
}
