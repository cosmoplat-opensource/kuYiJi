/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp;

import com.cosmo.hhim.thirdplat.api.wechatmp.factory.RemoteWxBaseFallbackFactory;
import com.cosmo.hhim.thirdplat.api.wechatmp.factory.RemoteWxMenuFallbackFactory;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-01
 */
@FeignClient(contextId = "RemoteWxBaseService", url = "${feignDebug.thirdplatUrl}", value = Constant.THIRDPLAT_SERVICE, fallbackFactory = RemoteWxBaseFallbackFactory.class)
public interface RemoteWxBaseService {

    /**
     * 微信服务器接入验签
     * @param timestamp
     * @param nonce
     * @param signature
     * @return
     */
    @GetMapping("/wechatmp/base/checkSignature")
    APIResponse<Boolean> checkSignature(@RequestParam String timestamp, @RequestParam String nonce, @RequestParam String signature);


}
