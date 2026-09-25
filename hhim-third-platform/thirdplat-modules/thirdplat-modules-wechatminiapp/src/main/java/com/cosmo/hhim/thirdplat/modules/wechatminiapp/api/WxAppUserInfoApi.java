/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatminiapp.api;

import com.cosmo.hhim.thirdplat.modules.wechatminiapp.interceptor.WxAppAccessTokenInjectInterceptor;
import com.cosmo.hhim.thirdplat.modules.wechatminiapp.proxy.annotation.WxAppResponseException;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.UserPhoneNumberParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.UserPhoneInfo;
import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.http.ForestResponse;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-21
 */
@BaseRequest(
        baseURL = "${wxminiappBaseUrl}",
        headers = {
                "Accept-Charset: ${wxminiappEncoding}",
                "Content-Type: ${wxminiContentType}"
        },
        interceptor = WxAppAccessTokenInjectInterceptor.class
)
@WxAppResponseException
public interface WxAppUserInfoApi {

    /**
     * 获取手机号
     *
     * @return
     */
    @Post(url = "/wxa/business/getuserphonenumber")
    ForestResponse<UserPhoneInfo> getPhoneNumber(@JSONBody UserPhoneNumberParam param);

}
