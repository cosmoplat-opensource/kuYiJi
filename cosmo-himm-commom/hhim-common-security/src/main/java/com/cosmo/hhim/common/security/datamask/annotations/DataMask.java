/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.datamask.annotations;

import com.cosmo.hhim.common.core.constant.enums.DataMaskType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author cosmo-hhim-open Team
 * @description 数据脱敏自定义注解
 * @createTime 2022-01-11
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface DataMask {

    // 填充字符
    String padChar() default "**";

    // 脱敏字段所属业务类型
    DataMaskType dataMaskType() default DataMaskType.COST_PRICE;
}
