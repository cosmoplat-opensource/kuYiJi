/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.distributedlock.annotation;

import java.lang.annotation.*;

/**
 * @author cosmo-hhim-open Team
 * @description 分布式锁的参数
 * @date 2021-04-19
 */
@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface SubmitParam {

    /**
     * 字段名称
     *
     * @return String
     */
    String name() default "";
}
