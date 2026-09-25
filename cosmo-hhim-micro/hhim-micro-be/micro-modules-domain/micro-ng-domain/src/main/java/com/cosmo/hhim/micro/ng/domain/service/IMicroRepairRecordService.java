/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.ng.domain.service;


import com.cosmo.hhim.micro.ng.domain.entity.MicroRepairRecord;

import java.util.List;

/**
 * 质检记录Service接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-31
 */
public interface IMicroRepairRecordService 
{
    /**
     * 查询质检记录
     * 
     * @param id 质检记录ID
     * @return 质检记录
     */
    public MicroRepairRecord selectMicroRepairRecordById(Long id);

    /**
     * 查询质检记录列表
     * 
     * @param microRepairRecord 质检记录
     * @return 质检记录集合
     */
    public List<MicroRepairRecord> selectMicroRepairRecordList(MicroRepairRecord microRepairRecord);

    /**
     * 新增质检记录
     * 
     * @param microRepairRecord 质检记录
     * @return 结果
     */
    public int insertMicroRepairRecord(MicroRepairRecord microRepairRecord);

    /**
     * 修改质检记录
     * 
     * @param microRepairRecord 质检记录
     * @return 结果
     */
    public int updateMicroRepairRecord(MicroRepairRecord microRepairRecord);

    /**
     * 批量删除质检记录
     * 
     * @param ids 需要删除的质检记录ID
     * @return 结果
     */
    public int deleteMicroRepairRecordByIds(Long[] ids);

    /**
     * 删除质检记录信息
     * 
     * @param id 质检记录ID
     * @return 结果
     */
    public int deleteMicroRepairRecordById(Long id);
}
