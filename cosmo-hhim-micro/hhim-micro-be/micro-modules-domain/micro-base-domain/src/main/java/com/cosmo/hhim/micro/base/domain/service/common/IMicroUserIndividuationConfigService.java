/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserIndividuationConfig;

import java.util.List;

/**
 * 用户个性化配置Service接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-06-01
 */
public interface IMicroUserIndividuationConfigService {
    /**
     * 查询用户个性化配置
     *
     * @param id 用户个性化配置ID
     * @return 用户个性化配置
     */
    public MicroUserIndividuationConfig selectMicroUserIndividuationConfigById(Long id);

    /**
     * 查询用户个性化配置列表
     *
     * @param microUserIndividuationConfig 用户个性化配置
     * @return 用户个性化配置集合
     */
    public List<MicroUserIndividuationConfig> selectMicroUserIndividuationConfigList(MicroUserIndividuationConfig microUserIndividuationConfig);

    /**
     * 新增用户个性化配置
     * ⭐️这里的insert into 使用的是 replace into
     * ⭐️因为表中唯一索引设置的是user_id, tenant_code
     *
     * @param microUserIndividuationConfig 用户个性化配置
     * @return 结果
     */
    public int insertMicroUserIndividuationConfig(MicroUserIndividuationConfig microUserIndividuationConfig);

    /**
     * 修改用户个性化配置
     *
     * @param microUserIndividuationConfig 用户个性化配置
     * @return 结果
     */
    public int updateMicroUserIndividuationConfig(MicroUserIndividuationConfig microUserIndividuationConfig);

    /**
     * 批量删除用户个性化配置
     *
     * @param ids 需要删除的用户个性化配置ID
     * @return 结果
     */
    public int deleteMicroUserIndividuationConfigByIds(Long[] ids);

    /**
     * 删除用户个性化配置信息
     *
     * @param id 用户个性化配置ID
     * @return 结果
     */
    public int deleteMicroUserIndividuationConfigById(Long id);

    MicroUserIndividuationConfig selectUserConfigByUserId(Long userId); 

    int flushUserRecommendWindow(MicroUserIndividuationConfig config); 
}
