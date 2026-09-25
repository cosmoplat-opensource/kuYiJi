/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.service.impl;

import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.infrastructure.enums.NotifyEnums;
import com.cosmo.hhim.micro.integration.domain.entity.MicroNoticeConfig;
import com.cosmo.hhim.micro.integration.domain.mapper.MicroNoticeConfigMapper;
import com.cosmo.hhim.micro.integration.domain.service.IMicroNoticeConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知配置Service业务层处理
 *
 * @author cosmo-hhim-open Team
 * @date 2023-06-06
 */
@Service
public class MicroNoticeConfigServiceImpl implements IMicroNoticeConfigService {
    @Autowired
    private MicroNoticeConfigMapper microNoticeConfigMapper;

    /**
     * 查询通知配置
     * @param businessSign
     * @return
     */
    @Override
    public MicroNoticeConfig selectMicroNoticeConfigByBusinessSign(NotifyEnums.NoticeBusinessSignEnum businessSign) {
        String appSign = SecurityUtils.getApplicationSign();
        return microNoticeConfigMapper.selectMicroNoticeConfigInfo(businessSign.getCode(), appSign);
    }

    /**
     * 查询通知配置列表
     *
     * @param microNoticeConfig 通知配置
     * @return 通知配置
     */
    @Override
    public List<MicroNoticeConfig> selectMicroNoticeConfigList(MicroNoticeConfig microNoticeConfig) {
        return microNoticeConfigMapper.selectMicroNoticeConfigList(microNoticeConfig);
    }

    /**
     * 新增通知配置
     *
     * @param microNoticeConfig 通知配置
     * @return 结果
     */
    @Override
    public int insertMicroNoticeConfig(MicroNoticeConfig microNoticeConfig) {
        microNoticeConfig.setCreateTime(DateUtils.getNowDate());
        return microNoticeConfigMapper.insertMicroNoticeConfig(microNoticeConfig);
    }

    /**
     * 修改通知配置
     *
     * @param microNoticeConfig 通知配置
     * @return 结果
     */
    @Override
    public int updateMicroNoticeConfig(MicroNoticeConfig microNoticeConfig) {
        return microNoticeConfigMapper.updateMicroNoticeConfigByAppSignAndBusSign(microNoticeConfig);
    }

    /**
     * 批量删除通知配置
     *
     * @param ids 需要删除的通知配置ID
     * @return 结果
     */
    @Override
    public int deleteMicroNoticeConfigByIds(Long[] ids) {
        return microNoticeConfigMapper.deleteMicroNoticeConfigByIds(ids);
    }

    /**
     * 删除通知配置信息
     *
     * @param id 通知配置ID
     * @return 结果
     */
    @Override
    public int deleteMicroNoticeConfigById(Long id) {
        return microNoticeConfigMapper.deleteMicroNoticeConfigById(id);
    }
}
