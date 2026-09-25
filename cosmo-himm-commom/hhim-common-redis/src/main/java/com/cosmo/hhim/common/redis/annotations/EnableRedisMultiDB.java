/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.redis.annotations;

import com.cosmo.hhim.common.redis.configure.RedisMultiDBAutoConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author cosmo-hhim-open Team
 * @description 开启可进行Redis多数据库切换的注解
 * @createTime 2021-09-10
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Import(RedisMultiDBAutoConfig.class)
public @interface EnableRedisMultiDB {

}
