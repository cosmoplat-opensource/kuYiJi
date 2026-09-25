/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.tech.impl;

import com.cosmo.hhim.micro.base.domain.entity.tech.MicroProcessChainImportedTemp;
import com.cosmo.hhim.micro.base.domain.mapper.tech.MicroProcessChainImportedTempMapper;
import com.cosmo.hhim.micro.base.domain.service.tech.IMicroProcessChainImportedTempService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 工艺导入临时Service业务层处理
 *
 * @author cosmo-hhim-open Team
 * @date 2023-05-16
 */
@Slf4j
@Service
public class MicroProcessChainImportedTempServiceImpl implements IMicroProcessChainImportedTempService {
    @Autowired
    private MicroProcessChainImportedTempMapper microProcessChainImportedTempMapper;

    /**
     * 查询工艺导入临时
     *
     * @param id 工艺导入临时ID
     * @return 工艺导入临时
     */
    @Override
    public MicroProcessChainImportedTemp selectMicroProcessChainImportedTempById(Long id) {
        return microProcessChainImportedTempMapper.selectMicroProcessChainImportedTempById(id);
    }

    /**
     * 查询工艺导入临时列表
     *
     * @param microProcessChainImportedTemp 工艺导入临时
     * @return 工艺导入临时
     */
    @Override
    public List<MicroProcessChainImportedTemp> selectMicroProcessChainImportedTempList(MicroProcessChainImportedTemp microProcessChainImportedTemp) {
        return microProcessChainImportedTempMapper.selectMicroProcessChainImportedTempList(microProcessChainImportedTemp);
    }

    /**
     * 新增工艺导入临时
     *
     * @param microProcessChainImportedTemp 工艺导入临时
     * @return 结果
     */
    @Override
    public int insertMicroProcessChainImportedTemp(MicroProcessChainImportedTemp microProcessChainImportedTemp) {
        return microProcessChainImportedTempMapper.insertMicroProcessChainImportedTemp(microProcessChainImportedTemp);
    }

    /**
     * 修改工艺导入临时
     *
     * @param microProcessChainImportedTemp 工艺导入临时
     * @return 结果
     */
    @Override
    public int updateMicroProcessChainImportedTemp(MicroProcessChainImportedTemp microProcessChainImportedTemp) {
        return microProcessChainImportedTempMapper.updateMicroProcessChainImportedTemp(microProcessChainImportedTemp);
    }

    /**
     * 批量删除工艺导入临时
     *
     * @param ids 需要删除的工艺导入临时ID
     * @return 结果
     */
    @Override
    public int deleteMicroProcessChainImportedTempByIds(Long[] ids) {
        return microProcessChainImportedTempMapper.deleteMicroProcessChainImportedTempByIds(ids);
    }

    /**
     * 删除工艺导入临时信息
     *
     * @param id 工艺导入临时ID
     * @return 结果
     */
    @Override
    public int deleteMicroProcessChainImportedTempById(Long id) {
        return microProcessChainImportedTempMapper.deleteMicroProcessChainImportedTempById(id);
    }
}
