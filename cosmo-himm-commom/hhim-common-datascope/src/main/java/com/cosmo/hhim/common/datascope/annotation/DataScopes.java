/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datascope.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScopes {
    WmsDataScope[] value() default {};

    SrmDataScope[] srmValue() default {};

    DeviceDataScope[] deviceValue() default {};

    enum Type {AND, OR}

    /**
     * 连接类型
     *
     * @return
     */
    Type type() default Type.OR;
}
