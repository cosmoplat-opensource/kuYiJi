/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.wechatminiapp.impl;

import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.modules.wechatminiapp.api.WxAppUserInfoApi;
import com.cosmo.hhim.thirdplat.web.service.wechatminiapp.IMiniAppUserInfoService;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.UserPhoneNumberParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.UserPhoneInfo;
import com.dtflys.forest.http.ForestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.cosmo.hhim.thirdplat.web.utils.ResponseParserUtil.wxMiniAppResponseExceptionHandler;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-21
 */
@Slf4j
@Service
public class MiniAppUserInfoServiceImpl implements IMiniAppUserInfoService {

    @Autowired
    private WxAppUserInfoApi wxAppUserInfoApi;

    /**
     * 获取手机号
     *
     * @param param
     * @return
     */
    @Override
    public APIResponse<UserPhoneInfo> getPhoneNumber(UserPhoneNumberParam param) {
        // 发起请求
        ForestResponse<UserPhoneInfo> response = wxAppUserInfoApi.getPhoneNumber(param);
        APIResponse errorResponse = wxMiniAppResponseExceptionHandler(response);
        if (null != errorResponse) {
            return errorResponse;
        }

        return APIResponse.success(response.getResult());
    }
}
