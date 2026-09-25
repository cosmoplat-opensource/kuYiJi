/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.constant;

/**
 * @author cosmo-hhim-open Team
 * @description 代理方式常量
 * @createTime 2021-09-07
 */
public class ProxyConstant {
    public static final String CGLIB = "Cglib";

    // JDK Proxy 类型
    public static final String PROXY_TYPE_REFLECTIVE = "Reflective Aop Proxy";

    // CGLIB Proxy 类型
    public static final String PROXY_TYPE_CGLIB = "Cglib Aop Proxy";

    // JDK Proxy 名称关键字
    public static final String JDK_PROXY_NAME_KEY = "com.sun.proxy";

    // CGLIB Proxy 名称关键字
    public static final String CGLIB_PROXY_NAME_KEY = "ByCGLIB";

    public static final String SEPARATOR = ";";
}