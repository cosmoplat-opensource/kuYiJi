/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.distributedlock.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author cosmo-hhim-open Team
 * @description key生成器
 * @date 2021-04-19
 */
public interface LockKeyGenerator {
    /**
     * 获取AOP参数,生成指定缓存Key
     *
     * @param pjp PJP
     * @return 缓存KEY
     */
    String getLockKey(ProceedingJoinPoint pjp);
}
