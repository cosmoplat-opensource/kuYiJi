/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.tech;

import com.cosmo.hhim.micro.base.domain.entity.tech.MicroProcessChainImportedTemp;

import java.util.List;

/**
 * 工艺导入临时Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-05-16
 */
public interface MicroProcessChainImportedTempMapper {
    /**
     * 查询工艺导入临时
     *
     * @param id 工艺导入临时ID
     * @return 工艺导入临时
     */
    public MicroProcessChainImportedTemp selectMicroProcessChainImportedTempById(Long id);

    /**
     * 查询工艺导入临时列表
     *
     * @param microProcessChainImportedTemp 工艺导入临时
     * @return 工艺导入临时集合
     */
    public List<MicroProcessChainImportedTemp> selectMicroProcessChainImportedTempList(MicroProcessChainImportedTemp microProcessChainImportedTemp);

    /**
     * 新增工艺导入临时
     *
     * @param microProcessChainImportedTemp 工艺导入临时
     * @return 结果
     */
    public int insertMicroProcessChainImportedTemp(MicroProcessChainImportedTemp microProcessChainImportedTemp);

    /**
     * 修改工艺导入临时
     *
     * @param microProcessChainImportedTemp 工艺导入临时
     * @return 结果
     */
    public int updateMicroProcessChainImportedTemp(MicroProcessChainImportedTemp microProcessChainImportedTemp);

    /**
     * 删除工艺导入临时
     *
     * @param id 工艺导入临时ID
     * @return 结果
     */
    public int deleteMicroProcessChainImportedTempById(Long id);

    /**
     * 批量删除工艺导入临时
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMicroProcessChainImportedTempByIds(Long[] ids);
}
