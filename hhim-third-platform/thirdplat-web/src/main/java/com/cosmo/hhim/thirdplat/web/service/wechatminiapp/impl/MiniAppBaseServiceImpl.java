/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.wechatminiapp.impl;

import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.modules.wechatminiapp.api.WxAppCode2SessionApi;
import com.cosmo.hhim.thirdplat.web.service.wechatminiapp.IMiniAppBaseService;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.Code2SessionInfo;
import com.dtflys.forest.http.ForestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.cosmo.hhim.thirdplat.web.utils.ResponseParserUtil.wxMiniAppResponseExceptionHandler;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-20
 */
@Slf4j
@Service
public class MiniAppBaseServiceImpl implements IMiniAppBaseService {

    @Autowired
    private WxAppCode2SessionApi wxAppCode2SessionApi;

    /**
     * 微信小程序登录
     *
     * @param jsCode
     * @return
     */
    @Override
    public APIResponse<Code2SessionInfo> wxMiniAppLoginIn(String jsCode) {
        // 发起请求
        ForestResponse<Code2SessionInfo> response = wxAppCode2SessionApi.code2Session(jsCode);
        APIResponse errorResponse = wxMiniAppResponseExceptionHandler(response);
        if (null != errorResponse) {
            return errorResponse;
        }

        return APIResponse.success(response.getResult());
    }
}
