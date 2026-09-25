/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.api;

import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.in.CreateMenuInfoInDto;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out.CreateMenuInfoOutDto;
import com.cosmo.hhim.thirdplat.modules.wechatmp.interceptor.AccessTokenUrlInjectInterceptor;
import com.cosmo.hhim.thirdplat.modules.wechatmp.proxy.annotation.WxResponseException;
import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.Post;
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
public interface WxMenuApi {
    /**
     * 创建微信自定义菜单
     *
     * @param inDto
     * @return
     */
    @Post(url = "/cgi-bin/menu/create")
    ForestResponse<CreateMenuInfoOutDto> createMenu(@JSONBody CreateMenuInfoInDto inDto);
}
