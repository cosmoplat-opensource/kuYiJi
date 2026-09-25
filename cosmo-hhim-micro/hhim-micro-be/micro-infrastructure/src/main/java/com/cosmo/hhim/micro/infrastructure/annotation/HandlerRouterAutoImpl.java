/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.annotation;

import java.lang.annotation.*;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-01-31
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HandlerRouterAutoImpl {

    /**
     * 在spring容器中对应的名称
     *
     * @return
     */
    String value();

}
