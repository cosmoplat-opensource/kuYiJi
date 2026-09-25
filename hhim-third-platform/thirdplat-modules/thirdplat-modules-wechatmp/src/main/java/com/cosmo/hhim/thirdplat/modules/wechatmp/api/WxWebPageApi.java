/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.api;

import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.in.GetUserInfoInDto;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out.GetWebUserInfoOutDto;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out.WebAccessTokenInfo;
import com.cosmo.hhim.thirdplat.modules.wechatmp.interceptor.GainWebTokenInterceptor;
import com.cosmo.hhim.thirdplat.modules.wechatmp.interceptor.RefreshWebTokenInterceptor;
import com.cosmo.hhim.thirdplat.modules.wechatmp.proxy.annotation.WxResponseException;
import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Query;
import com.dtflys.forest.annotation.Var;
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
public interface WxWebPageApi {

    /**
     * 通过code换取网页授权access_token
     *
     * @param code
     * @return
     */
    @Get(url = "/sns/oauth2/access_token?code=${code}",
            interceptor = GainWebTokenInterceptor.class
    )
    ForestResponse<WebAccessTokenInfo> getWebAccessToken(@Var("code") String code);

    /**
     * 刷新web access_token
     *
     * @param refreshToken
     * @return
     */
    @Get(url = "/sns/oauth2/refresh_token?refresh_token=${refreshToken}",
            interceptor = RefreshWebTokenInterceptor.class
    )
    ForestResponse<WebAccessTokenInfo> refreshWebAccessToken(@Var("refreshToken") String refreshToken);

    /**
     * 拉取用户信息(需scope为 snsapi_userinfo)
     *
     * @param inDto
     * @return
     */
    @Get(url = "/sns/userinfo?access_token=${accessToken}")
    ForestResponse<GetWebUserInfoOutDto> getUserInfo(@Var("accessToken") String accessToken, @Query GetUserInfoInDto inDto);

}
