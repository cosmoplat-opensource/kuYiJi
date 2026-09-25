/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.proxy.interceptor;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.core.proxy.AbstractInterceptor;
import com.cosmo.hhim.thirdplat.modules.wechatmp.proxy.annotation.WxResponseException;
import com.cosmo.hhim.thirdplat.modules.wechatmp.response.WxResponseResult;
import com.dtflys.forest.http.ForestResponse;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-27
 */
@Component("wxResponseExceptionInterceptor")
public class WxResponseExceptionInterceptor extends AbstractInterceptor {
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
//        WxResponseException responseException = AnnotationUtils.findAnnotation(methodInvocation.getMethod(), WxResponseException.class);
//        if (null == responseException) {
//            return methodInvocation.proceed();
//        }

        if (ForestResponse.class.isAssignableFrom(methodInvocation.getMethod().getReturnType())) {
            ForestResponse proceedResult = (ForestResponse) methodInvocation.proceed();
            if (null != proceedResult && proceedResult.isError()) {
                String content = proceedResult.getContent();
                if (StringUtils.hasText(content)) {
                    WxResponseResult responseResult = JSON.parseObject(content, WxResponseResult.class);
                    proceedResult.setResult(responseResult);
                }
            }
            return proceedResult;
        }

        return methodInvocation.proceed();
    }
}
