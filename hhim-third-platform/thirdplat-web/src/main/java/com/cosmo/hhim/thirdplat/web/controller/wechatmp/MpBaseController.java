/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.controller.wechatmp;

import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.web.service.wechatmp.IMpBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-06-29
 */
@Slf4j
@RestController
@RequestMapping("/wechatmp/base")
public class MpBaseController {


    @Autowired
    private IMpBaseService mpBaseService;

    /**
     * 微信服务器接入验签
     * @param timestamp
     * @param nonce
     * @param signature
     * @return
     */
    @GetMapping("/checkSignature")
    public APIResponse<Boolean> checkSignature(@RequestParam String timestamp, @RequestParam String nonce, @RequestParam String signature) {
        return mpBaseService.checkSignature(timestamp, nonce, signature);
    }


}
