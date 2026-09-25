/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.enums;

/**
 * @author cosmo-hhim-open Team
 * @description 类扫描方式枚举项
 * @createTime 2021-09-07
 */
public enum ScanMode {
    // 只执行扫描到接口名或者类名上的注解后的处理
    FOR_CLASS_ANNOTATION_ONLY,
    // 只执行扫描到接口或者类方法上的注解后的处理
    FOR_METHOD_ANNOTATION_ONLY,
    // 上述两者都执行
    FOR_CLASS_OR_METHOD_ANNOTATION
}