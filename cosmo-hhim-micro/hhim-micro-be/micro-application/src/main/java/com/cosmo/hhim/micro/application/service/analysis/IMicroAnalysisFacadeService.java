/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.analysis;

import com.cosmo.hhim.micro.application.dto.analysis.*;
import com.cosmo.hhim.micro.base.domain.entity.analysis.*;

import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 不同角色首页展示接口 Facade 层
 * @date 2023/5/4 13:46
 */
public interface IMicroAnalysisFacadeService {

    /**
     * 计算工易派首页信息（企业主、审产员）
     *
     * @param queryDate
     * @return
     */
    IndexStatisticInfoFromPlan calculateIndexStatisticInfoFromPlan(Date queryDate);

    /**
     * 统计时间范围内不同状态下工单数量
     *
     * @param startDate
     * @param endDate
     * @return
     */
    WorkOrderNumInDifferentStatus calculateWorkOrderNumInDifferentStatus(Date startDate, Date endDate);

    /**
     * 统计工易派的预警信息
     *
     * @return
     */
    WarnStatisticInfo calculateWarnStatisticInfo();

    /**
     * 统计工易派生产管理模块的信息
     *
     * @return
     */
    ProductionManageStatisticInfo calculateProductionManageStatisticInfo();

    /**
     * 统计工易派基础数据信息
     *
     * @return
     */
    BaseStatisticInfo calculateBaseStatisticInfo();

    /**
     * 工易派 - 产品维度生产报工汇总分析
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<ProductiveSubmitSummaryInfoByProduct> getProductiveSubmitSummaryAnalysisByProduct(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 工易派 - 基于产品的工序维度生产报工汇总分析
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<ProductiveSubmitSummaryInfoByProcessBaseProduct> getProductiveSubmitSummaryAnalysisByProcessBaseProduct(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 工易派 - 员工维度生产报工数据
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<ProductiveSubmitAnalysisByUser> getProductiveSubmitSummaryAnalysisByUser(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 工易派 - 员工维度下各产品的报工数据
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<ProductSubmitSummaryInfoBaseUser> getProductiveSubmitSummaryInfoBaseUser(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 统计时间范围内入库产品信息
     *
     * @param startDate
     * @param endDate
     * @return
     */
    List<InBoundProduct> InBoundProductStatisticsInfo(Date startDate, Date endDate); 
}
