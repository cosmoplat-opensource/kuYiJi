/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.cosmosupport.factory;

import com.cosmo.hhim.thirdplat.api.cosmosupport.RemoteSmsService;
import com.cosmo.hhim.thirdplat.api.cosmosupport.domain.in.CheckStandardCodeSmsParam;
import com.cosmo.hhim.thirdplat.api.cosmosupport.domain.in.SendSmsParam;
import com.cosmo.hhim.thirdplat.api.cosmosupport.domain.in.SendStandardCodeSmsParam;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-03
 */
@Slf4j
public class RemoteSmsFallbackFactory implements FallbackFactory<RemoteSmsService> {
    @Override
    public RemoteSmsService create(Throwable throwable) {
        return new RemoteSmsService() {
            @Override
            public APIResponse<Boolean> sendSms(SendSmsParam param) {
                return APIResponse.fail("调用远程服务，发送sms失败：" + throwable, 500);
            }

            @Override
            public APIResponse<Boolean> sendStandardCodeSms(SendStandardCodeSmsParam param) {
                return APIResponse.fail("调用远程服务，发送短信验证码失败：" + throwable, 500);
            }

            @Override
            public APIResponse<Boolean> checkStandardCodeSms(CheckStandardCodeSmsParam param) {
                return APIResponse.fail("调用远程服务，校验短信验证码合法性失败：" + throwable, 500);
            }
        };
    }
}
