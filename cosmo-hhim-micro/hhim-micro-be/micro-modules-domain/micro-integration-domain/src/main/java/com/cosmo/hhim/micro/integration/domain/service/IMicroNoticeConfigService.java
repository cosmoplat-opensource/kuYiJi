/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.service;

import com.cosmo.hhim.micro.infrastructure.enums.NotifyEnums;
import com.cosmo.hhim.micro.integration.domain.entity.MicroNoticeConfig;

import java.util.List;

/**
 * 通知配置Service接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-06-06
 */
public interface IMicroNoticeConfigService {
    /**
     * 查询通知配置
     *
     * @return 通知配置
     */
    MicroNoticeConfig selectMicroNoticeConfigByBusinessSign(NotifyEnums.NoticeBusinessSignEnum businessSign); 

    /**
     * 查询通知配置列表
     *
     * @param microNoticeConfig 通知配置
     * @return 通知配置集合
     */
    public List<MicroNoticeConfig> selectMicroNoticeConfigList(MicroNoticeConfig microNoticeConfig);

    /**
     * 新增通知配置
     *
     * @param microNoticeConfig 通知配置
     * @return 结果
     */
    public int insertMicroNoticeConfig(MicroNoticeConfig microNoticeConfig);

    /**
     * 修改通知配置
     *
     * @param microNoticeConfig 通知配置
     * @return 结果
     */
    public int updateMicroNoticeConfig(MicroNoticeConfig microNoticeConfig);

    /**
     * 批量删除通知配置
     *
     * @param ids 需要删除的通知配置ID
     * @return 结果
     */
    public int deleteMicroNoticeConfigByIds(Long[] ids);

    /**
     * 删除通知配置信息
     *
     * @param id 通知配置ID
     * @return 结果
     */
    public int deleteMicroNoticeConfigById(Long id);
}
