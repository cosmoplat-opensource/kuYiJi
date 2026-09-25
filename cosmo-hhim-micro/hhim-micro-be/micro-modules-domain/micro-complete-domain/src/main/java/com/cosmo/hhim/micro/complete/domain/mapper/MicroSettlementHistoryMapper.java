/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.mapper;

import com.cosmo.hhim.micro.complete.domain.entity.MicroSettlementHistoryEntity;
import com.cosmo.hhim.micro.complete.domain.entity.MicroSettlementHistoryQueryEntity;
import com.cosmo.hhim.micro.complete.domain.entity.MicroSettlementHistoryQuerySubEntity;
import com.cosmo.hhim.micro.complete.domain.entity.MicroSettlementReportEntity;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 计件结算调整历史Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-22
 */
public interface MicroSettlementHistoryMapper {


    /**
     * 查询计件结算调整历史列表
     *
     * @param historyEntity 计件结算调整历史
     * @return 计件结算调整历史集合
     */
    List<MicroSettlementHistoryEntity> selectMicroSettlementHistoryList(@Param("entity") MicroSettlementHistoryEntity historyEntity);

    /**
     * 调整历史明细列表
     *
     * @param historyEntity
     * @return
     */
    List<MicroSettlementHistoryEntity> selectMicroSettlementHistoryDetailList(@Param("entity") MicroSettlementHistoryEntity historyEntity);

    /**
     * 删除计件结算调整历史
     *
     * @param id 计件结算调整历史ID
     * @return 结果
     */
    int deleteMicroSettlementHistoryById(Long id);


    int insertMicroSettlementHistoryBatch(@Param("dataList") List<MicroSettlementHistoryEntity> historyList); 

    int deleteMicroSettlementHistoryBySubmitIds(Long[] ids); 

    @MapKey("employeeId")
    Map<Long, MicroSettlementHistoryQueryEntity> selectHistoryListGroupByUser(@Param("entity") MicroSettlementReportEntity entity, @Param("searchKey") String searchKey); 

    List<MicroSettlementHistoryQuerySubEntity> selectHistorySubListGroupByUser(@Param("entity") MicroSettlementReportEntity entity, @Param("employeeIds") Set<Long> employeeIds); 
}
