/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.wechatmp;

import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.MsgTemplateParam;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.MsgTemplateInfoResult;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-02
 */
public interface IMpMessageService {

    /**
     * 根据模板库中模板的编号获取模版ID
     * @param templateIdShort
     * @return
     */
    APIResponse<String> getTemplateId(String templateIdShort);

    /**
     * 获取已添加至帐号下所有模板列表信息
     * @return
     */
    APIResponse<List<MsgTemplateInfoResult>> getTempList();

    /**
     * 发送模版消息（通用）
     * @param param
     * @return
     */
    APIResponse<String> sendTemplateMessage(MsgTemplateParam param);

}
