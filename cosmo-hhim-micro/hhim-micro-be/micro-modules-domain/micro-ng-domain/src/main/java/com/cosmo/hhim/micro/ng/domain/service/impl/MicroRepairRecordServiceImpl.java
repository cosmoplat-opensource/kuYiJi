/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.ng.domain.service.impl;

import com.cosmo.hhim.micro.ng.domain.entity.MicroRepairRecord;
import com.cosmo.hhim.micro.ng.domain.mapper.MicroRepairRecordMapper;
import com.cosmo.hhim.micro.ng.domain.service.IMicroRepairRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 质检记录Service业务层处理
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-31
 */
@Slf4j
@Service
public class MicroRepairRecordServiceImpl implements IMicroRepairRecordService
{
    @Autowired
    private MicroRepairRecordMapper microRepairRecordMapper;

    /**
     * 查询质检记录
     * 
     * @param id 质检记录ID
     * @return 质检记录
     */
    @Override
    public MicroRepairRecord selectMicroRepairRecordById(Long id)
    {
        return microRepairRecordMapper.selectMicroRepairRecordById(id);
    }

    /**
     * 查询质检记录列表
     * 
     * @param microRepairRecord 质检记录
     * @return 质检记录
     */
    @Override
    public List<MicroRepairRecord> selectMicroRepairRecordList(MicroRepairRecord microRepairRecord)
    {
        return microRepairRecordMapper.selectMicroRepairRecordList(microRepairRecord);
    }

    /**
     * 新增质检记录
     * 
     * @param microRepairRecord 质检记录
     * @return 结果
     */
    @Override
    public int insertMicroRepairRecord(MicroRepairRecord microRepairRecord)
    {
        return microRepairRecordMapper.insertMicroRepairRecord(microRepairRecord);
    }

    /**
     * 修改质检记录
     * 
     * @param microRepairRecord 质检记录
     * @return 结果
     */
    @Override
    public int updateMicroRepairRecord(MicroRepairRecord microRepairRecord)
    {
        return microRepairRecordMapper.updateMicroRepairRecord(microRepairRecord);
    }

    /**
     * 批量删除质检记录
     * 
     * @param ids 需要删除的质检记录ID
     * @return 结果
     */
    @Override
    public int deleteMicroRepairRecordByIds(Long[] ids)
    {
        return microRepairRecordMapper.deleteMicroRepairRecordByIds(ids);
    }

    /**
     * 删除质检记录信息
     * 
     * @param id 质检记录ID
     * @return 结果
     */
    @Override
    public int deleteMicroRepairRecordById(Long id)
    {
        return microRepairRecordMapper.deleteMicroRepairRecordById(id);
    }
}
