/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.api;

import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.in.GetUserInfoInDto;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out.GetUserInfoOutDto;
import com.cosmo.hhim.thirdplat.modules.wechatmp.interceptor.AccessTokenUrlInjectInterceptor;
import com.cosmo.hhim.thirdplat.modules.wechatmp.proxy.annotation.WxResponseException;
import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Query;
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
        },
        interceptor = AccessTokenUrlInjectInterceptor.class
)
@WxResponseException
public interface WxUserApi {

    /**
     * 获取用户基本信息(UnionID机制)
     *
     * @param inDto
     * @return
     */
    @Get(url = "/cgi-bin/user/info")
    ForestResponse<GetUserInfoOutDto> getUserInfo(@Query GetUserInfoInDto inDto);

}
