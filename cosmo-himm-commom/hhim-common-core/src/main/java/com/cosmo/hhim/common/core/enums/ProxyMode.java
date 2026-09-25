/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.enums;

/**
 * @author cosmo-hhim-open Team
 * @description 代理类型枚举项
 * @createTime 2021-09-07
 */
public enum ProxyMode {
    // 只通过扫描到接口名或者类名上的注解后，来确定是否要代理
    BY_CLASS_ANNOTATION_ONLY,
    // 只通过扫描到接口或者类方法上的注解后，来确定是否要代理
    BY_METHOD_ANNOTATION_ONLY,
    // 上述两者都可以
    BY_CLASS_OR_METHOD_ANNOTATION
}