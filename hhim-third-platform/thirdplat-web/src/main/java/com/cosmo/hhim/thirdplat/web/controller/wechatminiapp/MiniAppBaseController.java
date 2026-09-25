/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.controller.wechatminiapp;

import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.web.service.wechatminiapp.IMiniAppBaseService;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.Code2SessionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-20
 */
@Slf4j
@RestController
@RequestMapping("/wechat/miniapp/base")
public class MiniAppBaseController {

    @Autowired
    private IMiniAppBaseService miniAppBaseService;

    /**
     * 微信小程序登录
     * @param jsCode
     * @return
     */
    @GetMapping("/login")
    public APIResponse<Code2SessionInfo> wxMiniAppLoginIn(String jsCode){
        return miniAppBaseService.wxMiniAppLoginIn(jsCode);
    }


}
