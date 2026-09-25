/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.controller.wechatminiapp;

import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.web.service.wechatminiapp.IMiniAppMessageService;
import com.cosmo.hhim.thridplat.api.wechatminiapp.constants.WechatMiniAppConstants;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.BatchSendSameSubscribeMessageParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.SendMessageParam;
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
 * @createTime 2023/2/21
 */
@Slf4j
@RestController
@RequestMapping("/wechat/miniapp/message")
public class MiniAppMessageController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMiniAppMessageService miniAppMessageService;

    /**
     * 批量发送相同订阅消息
     *
     * @param param
     * @return
     */
    @PostMapping("/batch/sendSameSubscribeMessage")
    public APIResponse<Boolean> sendSameSubscribeMessageBatch(@Valid BatchSendSameSubscribeMessageParam param) {
        return miniAppMessageService.batchSendSameSubscribeMessage(param);
    }


    /**
     * 发送订阅消息
     *
     * @param param
     * @return
     */
    @PostMapping("/sendSubscribeMessage")
    public APIResponse<WechatMiniAppConstants.SendMessageResultEnum> sendSubscribeMessage(@Valid @RequestBody SendMessageParam param) {
        return miniAppMessageService.sendSubscribeMessage(param);
    }

}
