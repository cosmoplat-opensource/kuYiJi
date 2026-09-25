/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.controller.cosmosupport.sms;

import com.cosmo.hhim.thirdplat.api.cosmosupport.domain.in.CheckStandardCodeSmsParam;
import com.cosmo.hhim.thirdplat.api.cosmosupport.domain.in.SendSmsParam;
import com.cosmo.hhim.thirdplat.api.cosmosupport.domain.in.SendStandardCodeSmsParam;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.web.service.cosmosupport.sms.ICosmoSmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

import javax.validation.Valid;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-04
 */
@Slf4j
@RestController
@RequestMapping("/cosmosupport/sms")
public class CosmoSmsController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private ICosmoSmsService cosmoSmsService;

    /**
     * 发送sms
     * @param param
     * @return
     */
    @PostMapping("/send")
    public APIResponse<Boolean> sendSms(@Valid @RequestBody SendSmsParam param){
        return cosmoSmsService.sendSms(param);
    }

    /**
     * 发送短信验证码
     * @param param
     * @return
     */
    @PostMapping("/send/code")
    public APIResponse<Boolean> sendStandardCodeSms(@Valid @RequestBody SendStandardCodeSmsParam param){
        return cosmoSmsService.sendStandardCodeSms(param);
    }

    /**
     * 校验短信验证码合法性
     * @return
     */
    @PostMapping("/send/checkcode")
    public APIResponse<Boolean> checkStandardCodeSms(@Valid @RequestBody CheckStandardCodeSmsParam param){
        return cosmoSmsService.checkStandardCodeSms(param);
    }

}
