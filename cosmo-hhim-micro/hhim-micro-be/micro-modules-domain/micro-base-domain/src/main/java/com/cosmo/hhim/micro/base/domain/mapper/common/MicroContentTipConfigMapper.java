/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.common;

import com.cosmo.hhim.micro.base.domain.entity.tips.MicroContentTipConfig;
import com.cosmo.hhim.micro.base.domain.entity.tips.MicroContentTipConfigParam;

import java.util.List;

/**
 * 内容提示配置Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-02-06
 */
public interface MicroContentTipConfigMapper {
    /**
     * 查询内容提示配置
     *
     * @param id 内容提示配置ID
     * @return 内容提示配置
     */
    public MicroContentTipConfig selectMicroContentTipConfigById(Long id);

    /**
     * 查询内容提示配置列表
     *
     * @param param 内容提示配置
     * @return 内容提示配置集合
     */
    List<MicroContentTipConfig> selectMicroContentTipConfigList(MicroContentTipConfigParam param);

    /**
     * 新增内容提示配置
     *
     * @param microContentTipConfig 内容提示配置
     * @return 结果
     */
    public int insertMicroContentTipConfig(MicroContentTipConfig microContentTipConfig);

    /**
     * 修改内容提示配置
     *
     * @param microContentTipConfig 内容提示配置
     * @return 结果
     */
    public int updateMicroContentTipConfig(MicroContentTipConfig microContentTipConfig);

    /**
     * 删除内容提示配置
     *
     * @param id 内容提示配置ID
     * @return 结果
     */
    public int deleteMicroContentTipConfigById(Long id);

    /**
     * 批量删除内容提示配置
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMicroContentTipConfigByIds(Long[] ids);
}
