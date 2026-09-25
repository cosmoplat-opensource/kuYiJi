/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thridplat.api.wechatminiapp;

import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thridplat.api.wechatminiapp.constants.WechatMiniAppConstants;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.BatchSendSameSubscribeMessageParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.SendMessageParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.factory.RemoteWxMiniAppMessageFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/21
 */
@FeignClient(contextId = "RemoteWxMiniAppMessageService", url = "${feignDebug.thirdplatUrl}",
        value = Constant.THIRDPLAT_SERVICE, fallbackFactory = RemoteWxMiniAppMessageFallbackFactory.class)
public interface RemoteWxMiniAppMessageService {

    /**
     * 批量发送相同订阅消息
     *
     * @param param
     * @return
     */
    @PostMapping("/wechat/miniapp/message/batch/sendSameSubscribeMessage")
    APIResponse<Boolean> sendSameSubscribeMessageBatch(BatchSendSameSubscribeMessageParam param);


    /**
     * 发送订阅消息
     *
     * @param param
     * @return
     */
    @PostMapping("/wechat/miniapp/message/sendSubscribeMessage")
    APIResponse<WechatMiniAppConstants.SendMessageResultEnum> sendSubscribeMessage(SendMessageParam param);

}
