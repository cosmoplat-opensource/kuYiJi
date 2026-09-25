/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.eventbus;



import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author cosmo-hhim-open Team
 * @description 订阅者注解
 * @createTime 2021-06-01
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

public @interface Subscribe {

}
