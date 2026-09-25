/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.ng.domain.mapper;

import java.util.List;
import com.cosmo.hhim.micro.ng.domain.entity.MicroRepairRecord;
import com.cosmo.hhim.micro.ng.domain.entity.RepairNumInfoByProductAndProcessAndUser;
import com.cosmo.hhim.micro.ng.domain.entity.RepairRecordInfo;
import org.apache.ibatis.annotations.Param;

/**
 * 质检记录Mapper接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-31
 */
public interface MicroRepairRecordMapper 
{
    /**
     * 查询质检记录
     * 
     * @param id 质检记录ID
     * @return 质检记录
     */
    MicroRepairRecord selectMicroRepairRecordById(Long id);

    /**
     * 查询质检记录列表
     * 
     * @param microRepairRecord 质检记录
     * @return 质检记录集合
     */
    List<MicroRepairRecord> selectMicroRepairRecordList(MicroRepairRecord microRepairRecord);

    /**
     * 新增质检记录
     * 
     * @param microRepairRecord 质检记录
     * @return 结果
     */
    int insertMicroRepairRecord(MicroRepairRecord microRepairRecord);

    /**
     * 批量新增质检记录
     *
     * @param microRepairRecords
     * @return
     */
    int insertMicroRepairRecordBatch(@Param("list") List<MicroRepairRecord> microRepairRecords);

    /**
     * 修改质检记录
     * 
     * @param microRepairRecord 质检记录
     * @return 结果
     */
    int updateMicroRepairRecord(MicroRepairRecord microRepairRecord);

    /**
     * 删除质检记录
     * 
     * @param id 质检记录ID
     * @return 结果
     */
    int deleteMicroRepairRecordById(Long id);

    /**
     * 批量删除质检记录
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteMicroRepairRecordByIds(Long[] ids);

    /**
     * 返修复核列表
     *
     * @param queryKey
     * @return
     */
    List<RepairRecordInfo> selectMicroRepairRecordListByQueryKey(String queryKey);

    /**
     * 获取到产品、工序、人的返修数量信息
     *
     * @return
     */
    List<RepairNumInfoByProductAndProcessAndUser> selectNumInfoByProductAndProcessAndUser();
}
