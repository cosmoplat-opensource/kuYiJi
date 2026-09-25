/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thridplat.api.wechatminiapp;

import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.UserPhoneNumberParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.UserPhoneInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.factory.RemoteWxMiniAppUserInfoFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-21
 */
@FeignClient(contextId = "RemoteWxMiniAppUserInfoService", url = "${feignDebug.thirdplatUrl}", value = Constant.THIRDPLAT_SERVICE, fallbackFactory = RemoteWxMiniAppUserInfoFallbackFactory.class)
public interface RemoteWxMiniAppUserInfoService {

    /**
     * 获取手机号
     *
     * @param param
     * @return
     */
    @GetMapping("/wechat/miniapp/userinfo/getPhoneNumber")
    APIResponse<UserPhoneInfo> getPhoneNumber(@SpringQueryMap UserPhoneNumberParam param);

}
