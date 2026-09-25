/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.cosmosupport.sms;

import com.cosmo.hhim.thirdplat.api.cosmosupport.domain.in.CheckStandardCodeSmsParam;
import com.cosmo.hhim.thirdplat.api.cosmosupport.domain.in.SendSmsParam;
import com.cosmo.hhim.thirdplat.api.cosmosupport.domain.in.SendStandardCodeSmsParam;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;

import javax.validation.Valid;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-04
 */
public interface ICosmoSmsService {

    /**
     * 发送sms
     * @param param
     * @return
     */
    APIResponse<Boolean> sendSms(SendSmsParam param);

    /**
     * 发送标准短信验证码
     * @param param
     * @return
     */
    APIResponse<Boolean> sendStandardCodeSms(SendStandardCodeSmsParam param);

    /**
     * 校验短信验证码的合法性
     * @param param
     * @return
     */
    APIResponse<Boolean> checkStandardCodeSms(@Valid CheckStandardCodeSmsParam param);

}
