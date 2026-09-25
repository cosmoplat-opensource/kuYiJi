/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.wechatminiapp;

import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thridplat.api.wechatminiapp.constants.WechatMiniAppConstants;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.BatchSendSameSubscribeMessageParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.SendMessageParam;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/21
 */
public interface IMiniAppMessageService {

    /**
     * 批量发送相同微信小程序订阅消息
     * @param param
     * @return
     */
    APIResponse<Boolean> batchSendSameSubscribeMessage(BatchSendSameSubscribeMessageParam param);

    /**
     * 发送微信小程序订阅消息
     * @param param
     * @return
     */
    APIResponse<WechatMiniAppConstants.SendMessageResultEnum> sendSubscribeMessage(SendMessageParam param);

}
