/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.datamask.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author cosmo-hhim-open Team
 * @description 数据脱敏Excel注解
 * @createTime 2022-01-12
 */
@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD, ElementType.TYPE})
@Inherited
public @interface DataMaskExcelApi {

}
