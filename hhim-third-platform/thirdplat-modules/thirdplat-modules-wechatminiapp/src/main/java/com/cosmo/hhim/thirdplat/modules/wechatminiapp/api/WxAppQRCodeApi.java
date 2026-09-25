/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatminiapp.api;

import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.SchemeCodeParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.UnlimitedQRCodeParam;
import com.cosmo.hhim.thirdplat.modules.wechatminiapp.interceptor.WxAppAccessTokenInjectInterceptor;
import com.cosmo.hhim.thirdplat.modules.wechatminiapp.proxy.annotation.WxAppResponseException;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.QRCodeParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.UrlLinkParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.QRCodeInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.SchemeCodeInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.UrlLinkInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.response.WxAppResponseResult;
import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.Post;
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
        },
        interceptor = WxAppAccessTokenInjectInterceptor.class
)
@WxAppResponseException
public interface WxAppQRCodeApi {

    /**
     * 获取小程序码
     *
     * @return
     */
    @Post(url = "/wxa/getwxacode")
    ForestResponse<WxAppResponseResult> getQRCode(@JSONBody QRCodeParam param);

    /**
     * 获取小程序码(无限制)
     *
     * @return
     */
    @Post(url = "/wxa/getwxacodeunlimit")
    ForestResponse<WxAppResponseResult> getUnlimitedQRCode(@JSONBody UnlimitedQRCodeParam param);


    /**
     * 获取 URL Link
     * @param param
     * @return
     */
    @Post(url = "/wxa/generate_urllink")
    ForestResponse<UrlLinkInfo> generateUrlLink(@JSONBody UrlLinkParam param);

    /**
     * 获取 scheme 码
     * @param param
     * @return
     */
    @Post(url = "/wxa/generatescheme")
    ForestResponse<SchemeCodeInfo> generateScheme(@JSONBody SchemeCodeParam param);

}
