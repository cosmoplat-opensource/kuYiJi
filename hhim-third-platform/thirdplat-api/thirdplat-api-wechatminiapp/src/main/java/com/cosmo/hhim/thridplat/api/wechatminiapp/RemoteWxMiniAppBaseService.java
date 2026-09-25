/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thridplat.api.wechatminiapp;

import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.Code2SessionInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.factory.RemoteWxMiniAppBaseFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022/10/20
 */
@FeignClient(contextId = "RemoteWxMiniAppBaseService", url = "${feignDebug.thirdplatUrl}", value = Constant.THIRDPLAT_SERVICE, fallbackFactory = RemoteWxMiniAppBaseFallbackFactory.class)
public interface RemoteWxMiniAppBaseService {

    /**
     * 微信小程序登录
     * @param jsCode
     * @return
     */
    @GetMapping("/wechat/miniapp/base/login")
    APIResponse<Code2SessionInfo> wxMiniAppLoginIn(@RequestParam String jsCode);

}
