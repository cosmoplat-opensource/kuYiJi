/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.api;

import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out.AccessTokenInfo;
import com.cosmo.hhim.thirdplat.modules.wechatmp.interceptor.GainTokenInterceptor;
import com.cosmo.hhim.thirdplat.modules.wechatmp.proxy.annotation.WxResponseException;
import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.http.ForestResponse;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-01
 */

@BaseRequest(
        baseURL = "${wxmpBaseUrl}",
        headers = {
                "Accept-Charset: ${wxmpEncoding}",
                "Content-Type: ${wxmpContentType}"
        }
)
@WxResponseException
public interface WxAccessTokenApi {

    /**
     * 获取access token
     *
     * @return
     */
    @Get(
            url = "/cgi-bin/token",
            interceptor = GainTokenInterceptor.class
    )
    ForestResponse<AccessTokenInfo> getAccessToken();
}
