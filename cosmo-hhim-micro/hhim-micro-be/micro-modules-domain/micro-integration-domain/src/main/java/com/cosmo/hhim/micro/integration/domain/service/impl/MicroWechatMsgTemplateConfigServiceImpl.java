/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.service.impl;

import com.cosmo.hhim.micro.integration.domain.entity.MicroWechatMsgTemplateConfig;
import com.cosmo.hhim.micro.integration.domain.mapper.MicroWechatMsgTemplateConfigMapper;
import com.cosmo.hhim.micro.integration.domain.service.IMicroWechatMsgTemplateConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 微信模版消息配置Service业务层处理
 *
 * @author cosmo-hhim-open Team
 * @date 2023-02-22
 */
@Service
public class MicroWechatMsgTemplateConfigServiceImpl implements IMicroWechatMsgTemplateConfigService {
    @Autowired
    private MicroWechatMsgTemplateConfigMapper microWechatMsgTemplateConfigMapper;

    /**
     * 查询微信模版消息配置列表
     *
     * @param microWechatMsgTemplateConfig 微信模版消息配置
     * @return 微信模版消息配置
     */
    @Override
    public List<MicroWechatMsgTemplateConfig> selectMicroWechatMsgTemplateConfigList(MicroWechatMsgTemplateConfig microWechatMsgTemplateConfig) {
        return microWechatMsgTemplateConfigMapper.selectMicroWechatMsgTemplateConfigList(microWechatMsgTemplateConfig);
    }

}
