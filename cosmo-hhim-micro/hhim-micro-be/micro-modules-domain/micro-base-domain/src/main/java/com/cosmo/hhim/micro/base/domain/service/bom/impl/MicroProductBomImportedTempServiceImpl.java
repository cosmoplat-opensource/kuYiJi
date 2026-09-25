/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.bom.impl;

import com.cosmo.hhim.micro.base.domain.entity.bom.MicroProductBomImportedTemp;
import com.cosmo.hhim.micro.base.domain.mapper.bom.MicroProductBomImportedTempMapper;
import com.cosmo.hhim.micro.base.domain.service.bom.IMicroProductBomImportedTempService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * BOM导入临时Service业务层处理
 *
 * @author cosmo-hhim-open Team
 * @date 2023-05-10
 */
@Service
public class MicroProductBomImportedTempServiceImpl implements IMicroProductBomImportedTempService {
    @Autowired
    private MicroProductBomImportedTempMapper microProductBomImportedTempMapper;

    /**
     * 查询BOM导入临时
     *
     * @param id BOM导入临时ID
     * @return BOM导入临时
     */
    @Override
    public MicroProductBomImportedTemp selectMicroProductBomImportedTempById(Long id) {
        return microProductBomImportedTempMapper.selectMicroProductBomImportedTempById(id);
    }

    /**
     * 查询BOM导入临时列表
     *
     * @param microProductBomImportedTemp BOM导入临时
     * @return BOM导入临时
     */
    @Override
    public List<MicroProductBomImportedTemp> selectMicroProductBomImportedTempList(MicroProductBomImportedTemp microProductBomImportedTemp) {
        return microProductBomImportedTempMapper.selectMicroProductBomImportedTempList(microProductBomImportedTemp);
    }

    /**
     * 新增BOM导入临时
     *
     * @param microProductBomImportedTemp BOM导入临时
     * @return 结果
     */
    @Override
    public int insertMicroProductBomImportedTemp(MicroProductBomImportedTemp microProductBomImportedTemp) {
        return microProductBomImportedTempMapper.insertMicroProductBomImportedTemp(microProductBomImportedTemp);
    }

    /**
     * 修改BOM导入临时
     *
     * @param microProductBomImportedTemp BOM导入临时
     * @return 结果
     */
    @Override
    public int updateMicroProductBomImportedTemp(MicroProductBomImportedTemp microProductBomImportedTemp) {
        return microProductBomImportedTempMapper.updateMicroProductBomImportedTemp(microProductBomImportedTemp);
    }

    /**
     * 批量删除BOM导入临时
     *
     * @param ids 需要删除的BOM导入临时ID
     * @return 结果
     */
    @Override
    public int deleteMicroProductBomImportedTempByIds(Long[] ids) {
        return microProductBomImportedTempMapper.deleteMicroProductBomImportedTempByIds(ids);
    }

    /**
     * 删除BOM导入临时信息
     *
     * @param id BOM导入临时ID
     * @return 结果
     */
    @Override
    public int deleteMicroProductBomImportedTempById(Long id) {
        return microProductBomImportedTempMapper.deleteMicroProductBomImportedTempById(id);
    }
}
