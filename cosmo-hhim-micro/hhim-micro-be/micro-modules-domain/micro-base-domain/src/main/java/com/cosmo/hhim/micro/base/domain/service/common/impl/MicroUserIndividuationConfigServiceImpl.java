/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserIndividuationConfig;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroUserIndividuationConfigMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroUserIndividuationConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户个性化配置Service业务层处理
 *
 * @author cosmo-hhim-open Team
 * @date 2023-06-01
 */
@Service
public class MicroUserIndividuationConfigServiceImpl implements IMicroUserIndividuationConfigService {
    @Autowired
    private MicroUserIndividuationConfigMapper microUserIndividuationConfigMapper;

    /**
     * 查询用户个性化配置
     *
     * @param id 用户个性化配置ID
     * @return 用户个性化配置
     */
    @Override
    public MicroUserIndividuationConfig selectMicroUserIndividuationConfigById(Long id) {
        return microUserIndividuationConfigMapper.selectMicroUserIndividuationConfigById(id);
    }

    /**
     * 查询用户个性化配置列表
     *
     * @param microUserIndividuationConfig 用户个性化配置
     * @return 用户个性化配置
     */
    @Override
    public List<MicroUserIndividuationConfig> selectMicroUserIndividuationConfigList(MicroUserIndividuationConfig microUserIndividuationConfig) {
        return microUserIndividuationConfigMapper.selectMicroUserIndividuationConfigList(microUserIndividuationConfig);
    }

    /**
     * 新增用户个性化配置
     * ⭐️这里的insert into 使用的是 replace into
     * ⭐️因为表中唯一索引设置的是user_id, tenant_code
     *
     * @param microUserIndividuationConfig 用户个性化配置
     * @return 结果
     */
    @Override
    public int insertMicroUserIndividuationConfig(MicroUserIndividuationConfig microUserIndividuationConfig) {
        return microUserIndividuationConfigMapper.insertMicroUserIndividuationConfig(microUserIndividuationConfig);
    }

    /**
     * 修改用户个性化配置
     *
     * @param microUserIndividuationConfig 用户个性化配置
     * @return 结果
     */
    @Override
    public int updateMicroUserIndividuationConfig(MicroUserIndividuationConfig microUserIndividuationConfig) {
        return microUserIndividuationConfigMapper.updateMicroUserIndividuationConfig(microUserIndividuationConfig);
    }

    /**
     * 批量删除用户个性化配置
     *
     * @param ids 需要删除的用户个性化配置ID
     * @return 结果
     */
    @Override
    public int deleteMicroUserIndividuationConfigByIds(Long[] ids) {
        return microUserIndividuationConfigMapper.deleteMicroUserIndividuationConfigByIds(ids);
    }

    /**
     * 删除用户个性化配置信息
     *
     * @param id 用户个性化配置ID
     * @return 结果
     */
    @Override
    public int deleteMicroUserIndividuationConfigById(Long id) {
        return microUserIndividuationConfigMapper.deleteMicroUserIndividuationConfigById(id);
    }

    @Override
    public MicroUserIndividuationConfig selectUserConfigByUserId(Long userId) {
        return microUserIndividuationConfigMapper.selectOneByUserId(userId);
    }

    @Override
    public int flushUserRecommendWindow(MicroUserIndividuationConfig config) {
        return microUserIndividuationConfigMapper.updateMicroUserIndividuationConfigByUser(config);
    }
}
