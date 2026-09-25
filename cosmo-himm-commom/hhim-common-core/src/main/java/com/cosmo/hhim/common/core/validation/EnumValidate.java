/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.validation;
/**
 * @description 枚举值校验
 */
public interface EnumValidate <T>{

    /**
     * 校验枚举值是否存在
     */
    boolean existValidate(T value);
}
