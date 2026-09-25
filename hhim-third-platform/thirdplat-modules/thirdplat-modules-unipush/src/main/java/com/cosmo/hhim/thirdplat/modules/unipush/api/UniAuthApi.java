/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.api;

import com.cosmo.hhim.thirdplat.modules.unipush.response.UniPushResponseResult;
import com.cosmo.hhim.thirdplat.modules.unipush.dto.out.auth.TokenInfo;
import com.cosmo.hhim.thirdplat.modules.unipush.interceptor.AcquireTokenInterceptor;
import com.cosmo.hhim.thirdplat.modules.unipush.interceptor.DelTokenInterceptor;
import com.cosmo.hhim.thirdplat.modules.unipush.proxy.annotation.UniPushResponseException;
import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.Delete;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.annotation.Var;
import com.dtflys.forest.http.ForestResponse;

/**
 * @author cosmo-hhim-open Team
 * @description 鉴权API
 * @createTime 2021-09-22
 */
@BaseRequest(
        baseURL = "${uniBaseUrl}${uniAppId}",
        headers = {
                "Accept-Charset: ${uniEncoding}",
                "Content-Type: ${uniContentType}"
        }
)
@UniPushResponseException
public interface UniAuthApi {

    /**
     * 获取鉴权token
     * @return
     */
    @Post(
            url = "/auth",
            interceptor = AcquireTokenInterceptor.class
    )
    ForestResponse<UniPushResponseResult<TokenInfo>> getToken();

    /**
     * 删除鉴权token
     * @param token
     * @return
     */
    @Delete(
            url = "/auth/${token}",
            dataType = "json",
            interceptor = DelTokenInterceptor.class
    )
    ForestResponse<UniPushResponseResult<String>> deleteToken(@Var("token") String token);
}
