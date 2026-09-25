/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatminiapp.api;

import com.cosmo.hhim.thirdplat.modules.wechatminiapp.interceptor.WxAppAccessTokenInjectInterceptor;
import com.cosmo.hhim.thirdplat.modules.wechatminiapp.proxy.annotation.WxAppResponseException;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.SendMessageParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.response.WxAppResponseResult;
import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.http.ForestResponse;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/21
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
public interface WxAppMessageApi {

    /**
     * 发送订阅消息
     *
     * @return
     */
    @Post(url = "/cgi-bin/message/subscribe/send")
    ForestResponse<WxAppResponseResult> sendSubscribeMessage(@JSONBody SendMessageParam param);

}
