/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.controller.wechatminiapp;

import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.web.service.wechatminiapp.IMiniAppUserInfoService;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.UserPhoneNumberParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.UserPhoneInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

import javax.validation.Valid;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-21
 */
@Slf4j
@RestController
@RequestMapping("/wechat/miniapp/userinfo")
public class MiniAppUserInfoController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMiniAppUserInfoService miniAppUserInfoService;

    /**
     * 获取手机号
     *
     * @param param
     * @return
     */
    @GetMapping("/getPhoneNumber")
    public APIResponse<UserPhoneInfo> getPhoneNumber(@Valid UserPhoneNumberParam param) {
        return miniAppUserInfoService.getPhoneNumber(param);
    }

}
