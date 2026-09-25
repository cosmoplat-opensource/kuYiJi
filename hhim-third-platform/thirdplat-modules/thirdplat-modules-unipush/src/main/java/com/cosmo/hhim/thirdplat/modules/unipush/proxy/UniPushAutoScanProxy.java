/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.proxy;

import com.cosmo.hhim.common.core.enums.ProxyMode;
import com.cosmo.hhim.common.core.enums.ScanMode;
import com.cosmo.hhim.common.core.proxy.DefaultAutoScanProxy;
import com.cosmo.hhim.thirdplat.modules.unipush.proxy.annotation.UniPushResponseException;
import com.cosmo.hhim.thirdplat.modules.unipush.proxy.interceptor.UniPushResponseExceptionInterceptor;
import org.aopalliance.intercept.MethodInterceptor;

import java.lang.annotation.Annotation;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-27
 */
public class UniPushAutoScanProxy extends DefaultAutoScanProxy {

    private static final String[] SCAN_PACKAGES = {"com.cosmo.hhim.thirdplat.modules.unipush.api"};

    private volatile Class[] classAnnotations;
    private volatile Class[] commonInterceptorClasses;

    public UniPushAutoScanProxy() {
        super(SCAN_PACKAGES, ProxyMode.BY_CLASS_ANNOTATION_ONLY, ScanMode.FOR_CLASS_ANNOTATION_ONLY);
    }

    @Override
    protected Class<? extends MethodInterceptor>[] getCommonInterceptors() {
        if (commonInterceptorClasses == null) {
            commonInterceptorClasses = new Class[]{UniPushResponseExceptionInterceptor.class};
        }
        return commonInterceptorClasses;
    }

    @Override
    protected Class<? extends Annotation>[] getClassAnnotations() {
        if (classAnnotations == null) {
            classAnnotations = new Class[]{UniPushResponseException.class};
        }
        return classAnnotations;
    }
}
