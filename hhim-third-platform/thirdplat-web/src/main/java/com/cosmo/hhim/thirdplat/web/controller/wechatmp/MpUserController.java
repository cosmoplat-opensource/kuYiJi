/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.controller.wechatmp;

import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.WxMpUserInfoResult;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.web.service.wechatmp.IMpUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-02
 */
@Slf4j
@RestController
@RequestMapping("/wechatmp/user")
public class MpUserController {

    @Autowired
    private IMpUserService mpUserService;

    /**
     * 通过openID查询微信粉丝基本用户信息
     * @param openid
     * @return
     */
    @GetMapping("/info")
    public APIResponse<WxMpUserInfoResult> getMpUserInfo(String openid){
        return mpUserService.getMpUserInfo(openid);
    }

}
