/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.log.annotation;

import com.cosmo.hhim.common.log.aspect.AsyncLogSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author cosmo-hhim-open Team
 * @description 开启异步日志注解
 * @createTime 2022-11-22
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AsyncLogSelector.class)
public @interface EnableLogAspect {

}
