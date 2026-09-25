/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.bom;

import com.cosmo.hhim.micro.base.domain.entity.bom.MicroProductBomImportedTemp;

import java.util.List;

/**
 * BOM导入临时Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-05-10
 */
public interface MicroProductBomImportedTempMapper {
    /**
     * 查询BOM导入临时
     *
     * @param id BOM导入临时ID
     * @return BOM导入临时
     */
    public MicroProductBomImportedTemp selectMicroProductBomImportedTempById(Long id);

    /**
     * 查询BOM导入临时列表
     *
     * @param microProductBomImportedTemp BOM导入临时
     * @return BOM导入临时集合
     */
    public List<MicroProductBomImportedTemp> selectMicroProductBomImportedTempList(MicroProductBomImportedTemp microProductBomImportedTemp);

    /**
     * 新增BOM导入临时
     *
     * @param microProductBomImportedTemp BOM导入临时
     * @return 结果
     */
    public int insertMicroProductBomImportedTemp(MicroProductBomImportedTemp microProductBomImportedTemp);

    /**
     * 修改BOM导入临时
     *
     * @param microProductBomImportedTemp BOM导入临时
     * @return 结果
     */
    public int updateMicroProductBomImportedTemp(MicroProductBomImportedTemp microProductBomImportedTemp);

    /**
     * 删除BOM导入临时
     *
     * @param id BOM导入临时ID
     * @return 结果
     */
    public int deleteMicroProductBomImportedTempById(Long id);

    /**
     * 批量删除BOM导入临时
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMicroProductBomImportedTempByIds(Long[] ids);
}
