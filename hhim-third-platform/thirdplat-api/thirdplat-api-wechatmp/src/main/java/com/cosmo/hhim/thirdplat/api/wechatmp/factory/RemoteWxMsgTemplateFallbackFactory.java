/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp.factory;

import com.cosmo.hhim.thirdplat.api.wechatmp.RemoteWxMsgTemplateService;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.MsgTemplateParam;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.MsgTemplateInfoResult;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-01
 */
@Slf4j
public class RemoteWxMsgTemplateFallbackFactory implements FallbackFactory<RemoteWxMsgTemplateService> {
    @Override
    public RemoteWxMsgTemplateService create(Throwable throwable) {

        return new RemoteWxMsgTemplateService() {
            @Override
            public APIResponse<String> getTemplateId(String templateIdShort) {
                return APIResponse.fail("调用远程服务，根据模板库中模板的编号获取模版ID失败：" + throwable, 500);
            }

            @Override
            public APIResponse<List<MsgTemplateInfoResult>> getTempList() {
                return APIResponse.fail("调用远程服务，获取已添加至帐号下所有模板列表信息失败：" + throwable, 500);
            }

            @Override
            public APIResponse<String> sendTemplateMessage(MsgTemplateParam param) {
                return APIResponse.fail("调用远程服务，发送模版消息（通用）失败：" + throwable, 500);
            }
        };
    }
}
