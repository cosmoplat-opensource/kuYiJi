/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp;

import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.WxMpUserInfoResult;
import com.cosmo.hhim.thirdplat.api.wechatmp.factory.RemoteWxUserFallbackFactory;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-01
 */
@FeignClient(contextId = "RemoteWxUserService", url = "${feignDebug.thirdplatUrl}", value = Constant.THIRDPLAT_SERVICE, fallbackFactory = RemoteWxUserFallbackFactory.class)
public interface RemoteWxUserService {
    /**
     * 通过openID查询微信粉丝基本用户信息
     *
     * @param openid
     * @return
     */
    @GetMapping("/wechatmp/user/info")
    APIResponse<WxMpUserInfoResult> getMpUserInfo(String openid);
}
