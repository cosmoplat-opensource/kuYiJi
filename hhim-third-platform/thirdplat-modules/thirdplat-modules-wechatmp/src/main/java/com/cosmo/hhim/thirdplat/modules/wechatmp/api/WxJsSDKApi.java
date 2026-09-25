/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.api;

import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out.GetJsSdkTicketOutDto;
import com.cosmo.hhim.thirdplat.modules.wechatmp.interceptor.AccessTokenUrlInjectInterceptor;
import com.cosmo.hhim.thirdplat.modules.wechatmp.proxy.annotation.WxResponseException;
import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.http.ForestResponse;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-05-12
 */
@BaseRequest(
        baseURL = "${wxmpBaseUrl}",
        headers = {
                "Accept-Charset: ${wxmpEncoding}",
                "Content-Type: ${wxmpContentType}"
        },
        interceptor = AccessTokenUrlInjectInterceptor.class
)
@WxResponseException
public interface WxJsSDKApi {
    /**
     * 获取微信JS-SDK票据
     *
     * @return
     */
    @Get(url = "/cgi-bin/ticket/getticket?type=jsapi")
    ForestResponse<GetJsSdkTicketOutDto> getJsSdkTicket();
}
