/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.distributedlock.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author cosmo-hhim-open Team
 * @description 分布式锁注解
 * @date 2021-04-19
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface SubmitLock {

    /**
     * redis 锁key的前缀
     *
     * @return redis 锁key的前缀
     */
    String prefix() default "";

    /**
     * 过期秒数,默认为5秒
     *
     * @return 轮询锁的时间
     */
    int expire() default 5;

    /**
     * 超时时间单位
     *
     * @return 秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * Key的分隔符（默认 :）
     * 生成的Key：N:SO1008:500
     *
     * @return String
     */
    String delimiter() default ":";
}
