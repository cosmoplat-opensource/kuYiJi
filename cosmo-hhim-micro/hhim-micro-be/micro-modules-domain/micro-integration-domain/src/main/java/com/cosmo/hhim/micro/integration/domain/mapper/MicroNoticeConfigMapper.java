/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.mapper;

import com.cosmo.hhim.micro.integration.domain.entity.MicroNoticeConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知配置Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-06-06
 */
public interface MicroNoticeConfigMapper {
    /**
     * 查询通知配置
     *
     * @param id 通知配置ID
     * @return 通知配置
     */
    public MicroNoticeConfig selectMicroNoticeConfigById(Long id);

    /**
     * 通过业务标识查询通知配置
     * @param businessSign
     * @return
     */
    MicroNoticeConfig selectMicroNoticeConfigInfo(@Param("businessSign") String businessSign, @Param("appSign") String appSign); 

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
     * 根据应用标识+业务标识更新通知配置
     * @param microNoticeConfig
     * @return
     */
    int updateMicroNoticeConfigByAppSignAndBusSign(MicroNoticeConfig microNoticeConfig);

    /**
     * 删除通知配置
     *
     * @param id 通知配置ID
     * @return 结果
     */
    public int deleteMicroNoticeConfigById(Long id);

    /**
     * 批量删除通知配置
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMicroNoticeConfigByIds(Long[] ids);
}
