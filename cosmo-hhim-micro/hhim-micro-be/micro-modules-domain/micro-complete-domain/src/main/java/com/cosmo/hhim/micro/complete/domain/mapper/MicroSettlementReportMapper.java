/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.mapper;

import com.cosmo.hhim.micro.complete.domain.entity.*;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 计件结算报告Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-22
 */
public interface MicroSettlementReportMapper {

    /**
     * 查询计件结算报告列表
     *
     * @param microSettlementReport 计件结算报告
     * @return 计件结算报告集合
     */
    List<MicroSettlementReportEntity> selectMicroSettlementReportList(MicroSettlementReportEntity microSettlementReport);

    /**
     * 新增计件结算报告
     *
     * @param microSettlementReport 计件结算报告
     * @return 结果
     */
    int insertMicroSettlementReport(MicroSettlementReportEntity microSettlementReport);

    /**
     * 修改计件结算报告
     *
     * @param microSettlementReport 计件结算报告
     * @return 结果
     */
    int updateMicroSettlementReport(MicroSettlementReportEntity microSettlementReport);

    /**
     * 删除计件结算报告
     *
     * @param id 计件结算报告ID
     * @return 结果
     */
    int deleteMicroSettlementReportById(Long id);

    /**
     * 批量删除计件结算报告
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteMicroSettlementReportByIds(Long[] ids);

    /**
     * 获取人员的已结算或未结算信息
     *
     * @return
     */
    List<MicroSettlementReportResultDomain> selectMicroSettlementReportUserDetail(@Param("entity") MicroSettlementReportEntity entity, @Param("employeeIds") Set<Long> employeeIds); 

    /**
     * 查询产品的已结算信息
     *
     * @param entity 结算报告实体
     * @return
     */
    List<MicroSettledProductDomain> getSettledByProduct(@Param("entity") MicroSettlementReportEntity entity); 

    /**
     * 查询产品的已结算明细信息
     *
     * @param productSeq
     * @param searchDate
     * @return
     */
    List<MicroSettledProductDetailDomain> selectSettledByProductDetail(@Param("productSeq") String productSeq, @Param("searchDate") Date searchDate);

    List<MicroSettlementReportEntity> selectReportListBySubmitIds(@Param("submitIds") List<Long> submitIds); 

    int deleteMicroSettlementReportBySubmitIds(Long[] ids); 

    int insertMicroSettlementReportBatch(@Param("dataList") List<MicroSettlementReportEntity> reportList); 

    List<MicroSettlementReportEntity> selectToGenerateReportList(@Param("productSeqs") List<String> productSeqs, @Param("submitDeadline") Date submitDeadline); 

    @MapKey("employeeId")
    Map<Long, MicroSettlementReportEmployeeDomain> selectMicroSettlementReportByUser(@Param("entity") MicroSettlementReportEntity entity); 

    BigDecimal selectEmployeeSettledNum(@Param("settlementDay") Date settlementDay, @Param("employeeId") Long employeeId); 

    BigDecimal selectEmployeeOpenNum(@Param("employeeId") Long employeeId); 
}
