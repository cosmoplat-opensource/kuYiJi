/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp;

import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.MsgTemplateParam;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.MsgTemplateInfoResult;
import com.cosmo.hhim.thirdplat.api.wechatmp.factory.RemoteWxMsgTemplateFallbackFactory;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-01
 */
@FeignClient(contextId = "RemoteWxMsgTemplateService", url = "${feignDebug.thirdplatUrl}", value = Constant.THIRDPLAT_SERVICE, fallbackFactory = RemoteWxMsgTemplateFallbackFactory.class)
public interface RemoteWxMsgTemplateService {
    /**
     * 根据模板库中模板的编号获取模版ID
     *
     * @param templateIdShort
     * @return
     */
    @GetMapping("/wechatmp/message/getTempId")
    APIResponse<String> getTemplateId(String templateIdShort);


    /**
     * 获取已添加至帐号下所有模板列表信息
     *
     * @return
     */
    @GetMapping("/wechatmp/message/getTempList")
    APIResponse<List<MsgTemplateInfoResult>> getTempList();

    /**
     * 发送模版消息（通用）
     *
     * @param param
     * @return
     */
    @PostMapping("/wechatmp/message/sendTempMessage")
    APIResponse<String> sendTemplateMessage(MsgTemplateParam param);
}
