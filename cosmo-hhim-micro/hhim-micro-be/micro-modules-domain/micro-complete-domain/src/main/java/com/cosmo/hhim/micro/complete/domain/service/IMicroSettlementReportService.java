/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.service;

import com.cosmo.hhim.micro.complete.domain.entity.*;

import java.util.Date;
import java.util.List;

/**
 * 计件结算报告Service接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-22
 */
public interface IMicroSettlementReportService {
    /**
     * 报工记录审产后生成待结算报告
     *
     * @param generateList
     * @return
     */
    boolean generateReportAfterCheck(List<MicroSettlementGenerateDomain> generateList);

    /**
     * 审产撤销后清理,如果未结算清理待结算数据,如果已结算,不允许撤销
     *
     * @param submitIds
     * @return
     */
    boolean undoSettlementReport(List<Long> submitIds);

    /**
     * 生成结算报告
     */
    List<MicroSettlementReportEntity> generateReport(List<MicroSettledDomain> toSettleList, Date settlementDate); 

    /**
     * 查询人员的待结算信息
     *
     * @param entity
     * @return
     */
    List<MicroSettlementReportDomain> getOpenSettlementUser(MicroSettlementReportEntity entity); 

    /**
     * 查询人员的已结算信息
     *
     * @param entity
     * @return
     */
    List<MicroSettlementReportDomain> getSettledUser(MicroSettlementReportEntity entity); 

    /**
     * 查询产品的已结算信息
     *
     * @param entity
     * @return
     */
    List<MicroSettledProductDomain> getSettledProduct(MicroSettlementReportEntity entity); 

    List<MicroSettledProductDetailDomain> getSettledProductDetail(String productSeq, Date searchDate); 

    /**
     * 调整员工的待结算数量
     *
     * @param reportEntity
     * @return
     */
    int editEmployeeSettlement(MicroSettlementReportEntity reportEntity); 

    /**
     * 查询结算报告的调整历史
     *
     * @param reportEntity
     * @param searchKey
     * @return
     */
    List<MicroSettlementHistoryQueryEntity> getReportEditHistory(MicroSettlementReportEntity reportEntity, String searchKey); 

    List<MicroSettlementHistoryEntity> getReportEditHistoryDetail(MicroSettlementHistoryEntity history); 
}
