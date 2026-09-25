/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.storage.domain.mapper;

import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedStorageImportedTemp;

import java.util.List;

/**
 * 产成品库存导入临时Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-05-05
 */
public interface MicroFinishedStorageImportedTempMapper {
    /**
     * 查询产成品库存导入临时
     *
     * @param id 产成品库存导入临时ID
     * @return 产成品库存导入临时
     */
    public MicroFinishedStorageImportedTemp selectMicroFinishedStorageImportedTempById(Long id);

    /**
     * 查询产成品库存导入临时列表
     *
     * @param microFinishedStorageImportedTemp 产成品库存导入临时
     * @return 产成品库存导入临时集合
     */
    public List<MicroFinishedStorageImportedTemp> selectMicroFinishedStorageImportedTempList(MicroFinishedStorageImportedTemp microFinishedStorageImportedTemp);

    /**
     * 新增产成品库存导入临时
     *
     * @param microFinishedStorageImportedTemp 产成品库存导入临时
     * @return 结果
     */
    public int insertMicroFinishedStorageImportedTemp(MicroFinishedStorageImportedTemp microFinishedStorageImportedTemp);

    /**
     * 修改产成品库存导入临时
     *
     * @param microFinishedStorageImportedTemp 产成品库存导入临时
     * @return 结果
     */
    public int updateMicroFinishedStorageImportedTemp(MicroFinishedStorageImportedTemp microFinishedStorageImportedTemp);

    /**
     * 删除产成品库存导入临时
     *
     * @param id 产成品库存导入临时ID
     * @return 结果
     */
    public int deleteMicroFinishedStorageImportedTempById(Long id);

    /**
     * 批量删除产成品库存导入临时
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMicroFinishedStorageImportedTempByIds(Long[] ids);
}
