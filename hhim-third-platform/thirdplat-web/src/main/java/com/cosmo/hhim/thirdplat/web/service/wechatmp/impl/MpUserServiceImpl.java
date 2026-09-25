/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.wechatmp.impl;

import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.WxMpUserInfoResult;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.modules.wechatmp.api.WxUserApi;
import com.cosmo.hhim.thirdplat.modules.wechatmp.constants.WechatMpConstant;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.in.GetUserInfoInDto;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out.GetUserInfoOutDto;
import com.cosmo.hhim.thirdplat.web.service.wechatmp.IMpUserService;
import com.dtflys.forest.http.ForestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.cosmo.hhim.thirdplat.web.utils.ResponseParserUtil.wxResponseExceptionHandler;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-02
 */
@Slf4j
@Service
public class MpUserServiceImpl implements IMpUserService {

    @Autowired
    private WxUserApi wxUserApi;

    /**
     * 查询微信公众号粉丝基本信息
     *
     * @param openid
     * @return
     */
    @Override
    public APIResponse<WxMpUserInfoResult> getMpUserInfo(String openid) {

        // 组装请求信息
        GetUserInfoInDto inDto = new GetUserInfoInDto();
        inDto.setOpenid(openid);
        inDto.setLang(WechatMpConstant.Lang.ZH_CN.getKey());

        // 发起请求
        ForestResponse<GetUserInfoOutDto> response = wxUserApi.getUserInfo(inDto);
        APIResponse errorResponse = wxResponseExceptionHandler(response);
        if (null != errorResponse) {
            return errorResponse;
        }

        // 解析响应信息
        WxMpUserInfoResult result = new WxMpUserInfoResult();
        BeanUtils.copyProperties(response.getResult(), result);
        return APIResponse.success(result);
    }
}
