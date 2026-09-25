/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.storage.domain.service.impl;

import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedStorageImportedTemp;
import com.cosmo.hhim.micro.storage.domain.mapper.MicroFinishedProductStorageMapper;
import com.cosmo.hhim.micro.storage.domain.mapper.MicroFinishedStorageImportedTempMapper;
import com.cosmo.hhim.micro.storage.domain.service.IMicroFinishedStorageImportedTempService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 产成品库存导入临时Service业务层处理
 *
 * @author cosmo-hhim-open Team
 * @date 2023-05-05
 */
@Slf4j
@Service
public class MicroFinishedStorageImportedTempServiceImpl implements IMicroFinishedStorageImportedTempService {
    @Autowired
    private MicroFinishedStorageImportedTempMapper microFinishedStorageImportedTempMapper;
    @Autowired
    private MicroFinishedProductStorageMapper microFinishedProductStorageMapper;

    /**
     * 查询产成品库存导入临时
     *
     * @param id 产成品库存导入临时ID
     * @return 产成品库存导入临时
     */
    @Override
    public MicroFinishedStorageImportedTemp selectMicroFinishedStorageImportedTempById(Long id) {
        return microFinishedStorageImportedTempMapper.selectMicroFinishedStorageImportedTempById(id);
    }

    /**
     * 查询产成品库存导入临时列表
     *
     * @param microFinishedStorageImportedTemp 产成品库存导入临时
     * @return 产成品库存导入临时
     */
    @Override
    public List<MicroFinishedStorageImportedTemp> selectMicroFinishedStorageImportedTempList(MicroFinishedStorageImportedTemp microFinishedStorageImportedTemp) {
        return microFinishedStorageImportedTempMapper.selectMicroFinishedStorageImportedTempList(microFinishedStorageImportedTemp);
    }

    /**
     * 新增产成品库存导入临时
     *
     * @param microFinishedStorageImportedTemp 产成品库存导入临时
     * @return 结果
     */
    @Override
    public int insertMicroFinishedStorageImportedTemp(MicroFinishedStorageImportedTemp microFinishedStorageImportedTemp) {
        return microFinishedStorageImportedTempMapper.insertMicroFinishedStorageImportedTemp(microFinishedStorageImportedTemp);
    }

    /**
     * 修改产成品库存导入临时
     *
     * @param microFinishedStorageImportedTemp 产成品库存导入临时
     * @return 结果
     */
    @Override
    public int updateMicroFinishedStorageImportedTemp(MicroFinishedStorageImportedTemp microFinishedStorageImportedTemp) {
        return microFinishedStorageImportedTempMapper.updateMicroFinishedStorageImportedTemp(microFinishedStorageImportedTemp);
    }

    /**
     * 批量删除产成品库存导入临时
     *
     * @param ids 需要删除的产成品库存导入临时ID
     * @return 结果
     */
    @Override
    public int deleteMicroFinishedStorageImportedTempByIds(Long[] ids) {
        return microFinishedStorageImportedTempMapper.deleteMicroFinishedStorageImportedTempByIds(ids);
    }

    /**
     * 删除产成品库存导入临时信息
     *
     * @param id 产成品库存导入临时ID
     * @return 结果
     */
    @Override
    public int deleteMicroFinishedStorageImportedTempById(Long id) {
        return microFinishedStorageImportedTempMapper.deleteMicroFinishedStorageImportedTempById(id);
    }
}
