/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.wechatminiapp.impl;

import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.modules.wechatminiapp.api.WxAppMessageApi;
import com.cosmo.hhim.thirdplat.modules.wechatminiapp.enums.WechatErrorEnums;
import com.cosmo.hhim.thirdplat.web.service.wechatminiapp.IMiniAppMessageService;
import com.cosmo.hhim.thridplat.api.wechatminiapp.constants.WechatMiniAppConstants;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.BatchSendSameSubscribeMessageParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.SendMessageParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.response.WxAppResponseResult;
import com.dtflys.forest.http.ForestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.cosmo.hhim.thirdplat.web.utils.ResponseParserUtil.wxMiniAppResponseExceptionHandler;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/21
 */
@Slf4j
@Service
public class MiniAppMessageServiceImpl implements IMiniAppMessageService {


    @Autowired
    private WxAppMessageApi wxAppMessageApi;


    /**
     * 批量发送相同微信小程序订阅消息
     *
     * @param param
     * @return
     */
    @Override
    public APIResponse<Boolean> batchSendSameSubscribeMessage(BatchSendSameSubscribeMessageParam param) {

        List<String> openIds = param.getOpenIds();
        for (String openId : openIds) {
            // 发起请求
            SendMessageParam requestParam = new SendMessageParam();
            BeanUtils.copyProperties(param, requestParam);
            requestParam.setTouser(openId);
            if (StringUtils.hasText(param.getMiniprogramState())) {
                requestParam.setMiniprogram_state(param.getMiniprogramState());
            }
            ForestResponse<WxAppResponseResult> response = wxAppMessageApi.sendSubscribeMessage(requestParam);
            APIResponse errorResponse = wxMiniAppResponseExceptionHandler(response);
            if (null != errorResponse) {
                return APIResponse.success(false);
            }

        }

        return APIResponse.success(true);
    }

    /**
     * 发送微信小程序订阅消息
     * @param param
     * @return
     */
    @Override
    public APIResponse<WechatMiniAppConstants.SendMessageResultEnum> sendSubscribeMessage(SendMessageParam param) {

        // 发起请求
        ForestResponse<WxAppResponseResult> response = wxAppMessageApi.sendSubscribeMessage(param);
        APIResponse errorResponse = wxMiniAppResponseExceptionHandler(response);
        if (null != errorResponse) {
            if(errorResponse.getCode() == WechatErrorEnums.REFUSE_TO_ACCEPT.getErrorCode()){
                return APIResponse.success(WechatMiniAppConstants.SendMessageResultEnum.REFUSE_TO_ACCEPT);
            }
            return APIResponse.success(WechatMiniAppConstants.SendMessageResultEnum.FAIL);
        }

        return APIResponse.success(WechatMiniAppConstants.SendMessageResultEnum.SUCCESS);
    }


}
