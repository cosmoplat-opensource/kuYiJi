/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.annotations;

import java.lang.annotation.*;

/**
 * @author cosmo-hhim-open Team
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RedisLockByUserName {
    String methodName() default "";
    long  expireTime() default 50L;
}

