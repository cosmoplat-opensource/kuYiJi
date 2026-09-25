/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatminiapp.api;

import com.cosmo.hhim.thirdplat.modules.wechatminiapp.interceptor.Code2SessionInterceptor;
import com.cosmo.hhim.thirdplat.modules.wechatminiapp.proxy.annotation.WxAppResponseException;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.Code2SessionInfo;
import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Var;
import com.dtflys.forest.http.ForestResponse;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-20
 */
@BaseRequest(
        baseURL = "${wxminiappBaseUrl}",
        headers = {
                "Accept-Charset: ${wxminiappEncoding}",
                "Content-Type: ${wxminiContentType}"
        }
)
@WxAppResponseException
public interface WxAppCode2SessionApi {

    /**
     * 小程序登录code换session
     *
     * @param jsCode 登录时获取的 code，可通过wx.login获取
     * @return
     */
    @Get(
            url = "/sns/jscode2session?js_code=${js_code}",
            interceptor = Code2SessionInterceptor.class
    )
    ForestResponse<Code2SessionInfo> code2Session(@Var("js_code") String jsCode);

}
