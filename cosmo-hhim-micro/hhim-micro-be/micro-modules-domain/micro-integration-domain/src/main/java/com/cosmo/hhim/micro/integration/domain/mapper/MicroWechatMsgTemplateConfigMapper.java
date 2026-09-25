/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.mapper;

import com.cosmo.hhim.micro.integration.domain.entity.MicroWechatMsgTemplateConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 微信模版消息配置Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-02-22
 */
public interface MicroWechatMsgTemplateConfigMapper {
    /**
     * 查询微信模版消息配置
     *
     * @return 微信模版消息配置
     */
    MicroWechatMsgTemplateConfig selectMicroWechatMsgTemplateConfig(@Param("serviceSign") String serviceSign, 
                                                                    @Param("templateType") String templateType,
                                                                    @Param("messageType") String messageType);

    /**
     * 查询微信模版消息配置列表
     *
     * @param microWechatMsgTemplateConfig 微信模版消息配置
     * @return 微信模版消息配置集合
     */
    public List<MicroWechatMsgTemplateConfig> selectMicroWechatMsgTemplateConfigList(MicroWechatMsgTemplateConfig microWechatMsgTemplateConfig);

    /**
     * 新增微信模版消息配置
     *
     * @param microWechatMsgTemplateConfig 微信模版消息配置
     * @return 结果
     */
    public int insertMicroWechatMsgTemplateConfig(MicroWechatMsgTemplateConfig microWechatMsgTemplateConfig);

    /**
     * 修改微信模版消息配置
     *
     * @param microWechatMsgTemplateConfig 微信模版消息配置
     * @return 结果
     */
    public int updateMicroWechatMsgTemplateConfig(MicroWechatMsgTemplateConfig microWechatMsgTemplateConfig);

    /**
     * 删除微信模版消息配置
     *
     * @param id 微信模版消息配置ID
     * @return 结果
     */
    public int deleteMicroWechatMsgTemplateConfigById(Long id);

    /**
     * 批量删除微信模版消息配置
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMicroWechatMsgTemplateConfigByIds(Long[] ids);
}
