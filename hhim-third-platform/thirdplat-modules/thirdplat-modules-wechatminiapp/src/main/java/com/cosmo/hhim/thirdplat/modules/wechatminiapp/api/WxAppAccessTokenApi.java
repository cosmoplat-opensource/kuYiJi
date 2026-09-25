/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatminiapp.api;

import com.cosmo.hhim.thirdplat.modules.wechatminiapp.interceptor.GainAppAccessTokenInterceptor;
import com.cosmo.hhim.thirdplat.modules.wechatminiapp.proxy.annotation.WxAppResponseException;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.AppAccessTokenInfo;
import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.http.ForestResponse;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-19
 */
@BaseRequest(
        baseURL = "${wxminiappBaseUrl}",
        headers = {
                "Accept-Charset: ${wxminiappEncoding}",
                "Content-Type: ${wxminiContentType}"
        }
)
@WxAppResponseException
public interface WxAppAccessTokenApi {

    /**
     * 获取稳定版 access token（无并发刷新导致旧token失效问题）
     *
     * @return
     */
    @Post(
            url = "/cgi-bin/stable_token",
            interceptor = GainAppAccessTokenInterceptor.class
    )
    ForestResponse<AppAccessTokenInfo> getStableAccessToken();

}
