/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.proxy.interceptor;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.core.proxy.AbstractInterceptor;
import com.cosmo.hhim.thirdplat.modules.unipush.response.UniPushResponseResult;
import com.cosmo.hhim.thirdplat.modules.unipush.proxy.annotation.UniPushResponseException;
import com.dtflys.forest.http.ForestResponse;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-27
 */
@Component("uniPushResponseExceptionInterceptor")
public class UniPushResponseExceptionInterceptor extends AbstractInterceptor {
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
//        UniPushResponseException uniPushResponseException = AnnotationUtils.findAnnotation(methodInvocation.getMethod(), UniPushResponseException.class);
//        if (null == uniPushResponseException) {
//            return methodInvocation.proceed();
//        }

        if (ForestResponse.class.isAssignableFrom(methodInvocation.getMethod().getReturnType())) {
            ForestResponse proceedResult = (ForestResponse) methodInvocation.proceed();
            if (null != proceedResult && proceedResult.isError()) {
                String content = proceedResult.getContent();
                if (StringUtils.hasText(content)) {
                    UniPushResponseResult uniPushResponseResult = JSON.parseObject(content, UniPushResponseResult.class);
                    proceedResult.setResult(uniPushResponseResult);
                }
            }
            return proceedResult;
        }

        return methodInvocation.proceed();
    }
}
