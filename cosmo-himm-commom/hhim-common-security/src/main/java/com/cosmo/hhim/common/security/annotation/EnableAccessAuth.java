/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.annotation;

import com.cosmo.hhim.common.security.accessauth.AccessAuthSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author cosmo-hhim-open Team
 * @description 开启访问权限控制的注解
 * @createTime 2022-11-06
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AccessAuthSelector.class)
public @interface EnableAccessAuth {

    String[] basePackages() default {};

}
