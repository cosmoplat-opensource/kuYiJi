/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thridplat.api.wechatminiapp.factory;

import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thridplat.api.wechatminiapp.RemoteWxMiniAppMessageService;
import com.cosmo.hhim.thridplat.api.wechatminiapp.constants.WechatMiniAppConstants;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.BatchSendSameSubscribeMessageParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.SendMessageParam;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/21
 */
@Slf4j
public class RemoteWxMiniAppMessageFallbackFactory implements FallbackFactory<RemoteWxMiniAppMessageService> {
    @Override
    public RemoteWxMiniAppMessageService create(Throwable throwable) {
        return new RemoteWxMiniAppMessageService() {
            @Override
            public APIResponse<Boolean> sendSameSubscribeMessageBatch(BatchSendSameSubscribeMessageParam param) {
                return APIResponse.fail("调用远程服务，调用微信小程序批量发送订阅消息接口失败：" + throwable, 500);
            }

            @Override
            public APIResponse<WechatMiniAppConstants.SendMessageResultEnum> sendSubscribeMessage(SendMessageParam param) {
                return APIResponse.fail("调用远程服务，调用微信小程序发送订阅消息接口失败：" + throwable, 500);
            }
        };
    }
}
