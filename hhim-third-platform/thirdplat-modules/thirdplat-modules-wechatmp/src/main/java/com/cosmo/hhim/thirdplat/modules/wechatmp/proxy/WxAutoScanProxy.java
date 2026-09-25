/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.proxy;

import com.cosmo.hhim.common.core.enums.ProxyMode;
import com.cosmo.hhim.common.core.enums.ScanMode;
import com.cosmo.hhim.common.core.proxy.DefaultAutoScanProxy;
import com.cosmo.hhim.thirdplat.modules.wechatmp.proxy.annotation.WxResponseException;
import com.cosmo.hhim.thirdplat.modules.wechatmp.proxy.interceptor.WxResponseExceptionInterceptor;
import org.aopalliance.intercept.MethodInterceptor;

import java.lang.annotation.Annotation;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-27
 */
public class WxAutoScanProxy extends DefaultAutoScanProxy {

    private static final String[] SCAN_PACKAGES = {"com.cosmo.hhim.thirdplat.modules.wechatmp.api"};

    private volatile Class[] classAnnotations;
    private volatile Class[] commonInterceptorClasses;

    public WxAutoScanProxy() {
        super(SCAN_PACKAGES, ProxyMode.BY_CLASS_ANNOTATION_ONLY, ScanMode.FOR_CLASS_ANNOTATION_ONLY);
    }

    @Override
    protected Class<? extends MethodInterceptor>[] getCommonInterceptors() {
        if (commonInterceptorClasses == null) {
            commonInterceptorClasses = new Class[]{WxResponseExceptionInterceptor.class};
        }
        return commonInterceptorClasses;
    }

    @Override
    protected Class<? extends Annotation>[] getClassAnnotations() {
        if (classAnnotations == null) {
            classAnnotations = new Class[]{WxResponseException.class};
        }
        return classAnnotations;
    }
}
