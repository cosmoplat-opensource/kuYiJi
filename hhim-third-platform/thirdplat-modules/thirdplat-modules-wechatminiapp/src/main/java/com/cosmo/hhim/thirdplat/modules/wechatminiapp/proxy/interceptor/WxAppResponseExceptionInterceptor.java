/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatminiapp.proxy.interceptor;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.core.proxy.AbstractInterceptor;
import com.cosmo.hhim.thridplat.api.wechatminiapp.response.WxAppResponseResult;
import com.dtflys.forest.http.ForestResponse;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-27
 */
@Component("wxAppResponseExceptionInterceptor")
public class WxAppResponseExceptionInterceptor extends AbstractInterceptor {
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        if (ForestResponse.class.isAssignableFrom(methodInvocation.getMethod().getReturnType())) {
            ForestResponse proceedResult = (ForestResponse) methodInvocation.proceed();
            if (null != proceedResult && proceedResult.isError()) {
                String content = proceedResult.getContent();
                if (StringUtils.hasText(content)) {
                    WxAppResponseResult responseResult = JSON.parseObject(content, WxAppResponseResult.class);
                    proceedResult.setResult(responseResult);
                }
            }
            return proceedResult;
        }

        return methodInvocation.proceed();
    }
}
