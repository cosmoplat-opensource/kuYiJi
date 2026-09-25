/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.api;

import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.in.GetMsgTemplateIdInDto;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.in.SendMsgTemplateInDto;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out.GetMsgTemplateIdOutDto;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out.GetMsgTemplateListOutDto;
import com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out.SendMsgTemplateOutDto;
import com.cosmo.hhim.thirdplat.modules.wechatmp.interceptor.AccessTokenUrlInjectInterceptor;
import com.cosmo.hhim.thirdplat.modules.wechatmp.proxy.annotation.WxResponseException;
import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.http.ForestResponse;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-01
 */
@BaseRequest(
        baseURL = "${wxmpBaseUrl}",
        headers = {
                "Accept-Charset: ${wxmpEncoding}",
                "Content-Type: ${wxmpContentType}"
        },
        interceptor = AccessTokenUrlInjectInterceptor.class
)
@WxResponseException
public interface WxMsgTemplateApi {

    /**
     * 获得模板ID
     * @param inDto
     * @return
     */
    @Post(url = "/cgi-bin/template/api_add_template")
    ForestResponse<GetMsgTemplateIdOutDto> getMsgTemplateId(@JSONBody GetMsgTemplateIdInDto inDto);

    /**
     * 获取模板列表
     * @return
     */
    @Get(url = "/cgi-bin/template/get_all_private_template")
    ForestResponse<GetMsgTemplateListOutDto> getMsgTemplateList();

    /**
     * 发送模板消息
     * @return
     */
    @Post(url = "/cgi-bin/message/template/send")
    ForestResponse<SendMsgTemplateOutDto> sendMsgTemplate(@JSONBody SendMsgTemplateInDto inDto);


}
