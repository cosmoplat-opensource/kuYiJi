/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.cosmosupport;

import com.cosmo.hhim.thirdplat.api.cosmosupport.domain.in.CheckStandardCodeSmsParam;
import com.cosmo.hhim.thirdplat.api.cosmosupport.domain.in.SendSmsParam;
import com.cosmo.hhim.thirdplat.api.cosmosupport.domain.in.SendStandardCodeSmsParam;
import com.cosmo.hhim.thirdplat.api.cosmosupport.factory.RemoteSmsFallbackFactory;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-03
 */
@FeignClient(contextId = "RemoteSmsService", url = "${feignDebug.thirdplatUrl}", value = Constant.THIRDPLAT_SERVICE, fallbackFactory = RemoteSmsFallbackFactory.class)
public interface RemoteSmsService {
    /**
     * 发送sms
     *
     * @param param
     * @return
     */
    @PostMapping("/cosmosupport/sms/send")
    APIResponse<Boolean> sendSms(SendSmsParam param);

    /**
     * 发送短信验证码
     *
     * @param param
     * @return
     */
    @PostMapping("/cosmosupport/sms/send/code")
    APIResponse<Boolean> sendStandardCodeSms(SendStandardCodeSmsParam param);

    /**
     * 校验短信验证码合法性
     *
     * @return
     */
    @PostMapping("/cosmosupport/sms/send/checkcode")
    APIResponse<Boolean> checkStandardCodeSms(CheckStandardCodeSmsParam param);
}
