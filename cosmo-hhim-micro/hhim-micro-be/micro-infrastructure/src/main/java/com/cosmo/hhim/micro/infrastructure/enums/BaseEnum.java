/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.enums;

/**
 * @author cosmo-hhim-open Team
 * 枚举定义接口，实现了统一接口的枚举方便做统一处理
 */
public interface BaseEnum<T> {

    /**
     * 获取枚举编码
     *
     * @return 枚举编码
     */
    T getCode();

    /**
     * 获取枚举显示说明
     *
     * @return 枚举说明
     */
    String getDesc();

}
