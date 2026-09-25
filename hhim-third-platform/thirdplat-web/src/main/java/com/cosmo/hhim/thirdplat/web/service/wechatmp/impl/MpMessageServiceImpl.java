/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.wechatmp.impl;

import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.MsgTemplateParam;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.MsgTemplateInfoResult;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.modules.wechatmp.api.WxMsgTemplateApi;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.in.GetMsgTemplateIdInDto;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.in.SendMsgTemplateInDto;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out.GetMsgTemplateIdOutDto;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out.GetMsgTemplateListOutDto;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out.SendMsgTemplateOutDto;
import com.cosmo.hhim.thirdplat.web.service.wechatmp.IMpMessageService;
import com.dtflys.forest.http.ForestResponse;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

import static com.cosmo.hhim.thirdplat.web.utils.ResponseParserUtil.wxResponseExceptionHandler;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-02
 */
@Slf4j
@Service
public class MpMessageServiceImpl implements IMpMessageService {

    @Autowired
    private WxMsgTemplateApi wxMsgTemplateApi;

    /**
     * 根据模板库中模板的编号获取模版ID
     *
     * @param templateIdShort
     * @return
     */
    @Override
    public APIResponse<String> getTemplateId(String templateIdShort) {
        // 组装请求参数
        GetMsgTemplateIdInDto requestParam = new GetMsgTemplateIdInDto();
        requestParam.setTemplate_id_short(templateIdShort);

        // 发起请求
        ForestResponse<GetMsgTemplateIdOutDto> response = wxMsgTemplateApi.getMsgTemplateId(requestParam);
        APIResponse errorResponse = wxResponseExceptionHandler(response);
        if (null != errorResponse) {
            return errorResponse;
        }
        return APIResponse.success(response.getResult().getTemplate_id());
    }

    /**
     * 获取已添加至帐号下所有模板列表信息
     *
     * @return
     */
    @Override
    public APIResponse<List<MsgTemplateInfoResult>> getTempList() {
        // 发起请求
        ForestResponse<GetMsgTemplateListOutDto> response = wxMsgTemplateApi.getMsgTemplateList();
        APIResponse errorResponse = wxResponseExceptionHandler(response);
        if (null != errorResponse) {
            return errorResponse;
        }

        // 解析响应数据
        List<MsgTemplateInfoResult> result = Lists.newArrayList();
        GetMsgTemplateListOutDto resultData = response.getResult();
        if (null != resultData) {
            List<GetMsgTemplateListOutDto.TemplateInfo> templateList = resultData.getTemplate_list();
            if (!CollectionUtils.isEmpty(templateList)) {
                for (GetMsgTemplateListOutDto.TemplateInfo templateInfo : templateList) {
                    MsgTemplateInfoResult msgTemplateInfoResult = new MsgTemplateInfoResult();
                    msgTemplateInfoResult.setTemplateId(templateInfo.getTemplate_id());
                    msgTemplateInfoResult.setContent(templateInfo.getContent());
                    msgTemplateInfoResult.setDeputyIndustry(templateInfo.getDeputy_industry());
                    msgTemplateInfoResult.setExample(templateInfo.getExample());
                    msgTemplateInfoResult.setTitle(templateInfo.getTitle());
                    msgTemplateInfoResult.setPrimaryIndustry(templateInfo.getPrimary_industry());
                    result.add(msgTemplateInfoResult);
                }
            }
        }

        return APIResponse.success(result);
    }

    /**
     * 发送模版消息（通用）
     *
     * @param param
     * @return
     */
    @Override
    public APIResponse<String> sendTemplateMessage(MsgTemplateParam param) {
        // 组装请求参数
        SendMsgTemplateInDto requestParam = new SendMsgTemplateInDto();
        requestParam.setAppid(param.getAppId());
        requestParam.setTemplate_id(param.getTemplateId());
        requestParam.setData(param.getData());
        requestParam.setUrl(param.getUrl());
        requestParam.setPagepath(param.getPagePath());
        requestParam.setMiniprogram(param.getMiniProgram());
        requestParam.setTouser(param.getToUser());

        // 发起请求
        ForestResponse<SendMsgTemplateOutDto> response = wxMsgTemplateApi.sendMsgTemplate(requestParam);
        APIResponse errorResponse = wxResponseExceptionHandler(response);
        if (null != errorResponse) {
            return errorResponse;
        }
        return APIResponse.success(response.getResult().getMsgid());
    }
}
