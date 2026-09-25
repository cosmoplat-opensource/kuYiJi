/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.service;


import com.cosmo.hhim.micro.integration.domain.entity.MicroWechatMsgTemplateConfig;

import java.util.List;

/**
 * 微信模版消息配置Service接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-02-22
 */
public interface IMicroWechatMsgTemplateConfigService {

    /**
     * 查询微信模版消息配置列表
     *
     * @param microWechatMsgTemplateConfig 微信模版消息配置
     * @return 微信模版消息配置
     */
    List<MicroWechatMsgTemplateConfig> selectMicroWechatMsgTemplateConfigList(MicroWechatMsgTemplateConfig microWechatMsgTemplateConfig);

}
