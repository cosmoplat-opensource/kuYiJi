/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import com.cosmo.hhim.micro.base.domain.entity.analysis.*;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorage;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitDto;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitProductCount;

import java.time.LocalDate;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 各分析服务接口
 * @date 2022/10/24 5:02 下午
 */
public interface IMicroAnalysisService {

    /**
     * 记功排名
     *
     * @param startDate
     * @param endDate
     * @return
     */
    List<SubmitterRank> getRecordRankForSubmitter(LocalDate startDate, LocalDate endDate);

    /**
     * 完工产品统计
     *
     * @param startDate
     * @param endDate
     * @return
     */
    List<FinishedProduct> getFinishedProductStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 库存排名（产品 + 工序）
     *
     * @param productSeq
     * @return
     */
    List<MicroProcessStorage> getStorageRank(String productSeq);

    /**
     * 质量分析 - 总计展示
     *
     * @param productNameOrCode
     * @param startDate
     * @param endDate
     * @return
     */
    MicroWorkSubmitProductCount showTotalCount(String productNameOrCode, LocalDate startDate, LocalDate endDate);

    /**
     * 生产质量趋势
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<MicroWorkSubmitProductCount> productionQualityTrend(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 良品率分析 - 产品维度
     *
     * @param microWorkSubmitDto
     * @return
     */
    List<MicroWorkSubmitProductCount> passRateAnalysisByProduct(MicroWorkSubmitDto microWorkSubmitDto);

    /**
     * 良品率分析 - 工序维度
     *
     * @param microWorkSubmitDto
     * @return
     */
    List<MicroWorkSubmitProductCount> passRateAnalysisByProcess(MicroWorkSubmitDto microWorkSubmitDto);

    /**
     * 良品率分析 - 员工维度
     *
     * @param microWorkSubmitDto
     * @return
     */
    List<SubmitterRank> passRateAnalysisByEmployee(MicroWorkSubmitDto microWorkSubmitDto);

    /**
     * 获取分析首页信息
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    AnalysisIndex obtainedAnalysisIndexInformation(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 展示不良品列表 （报工表）
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<NgProduct> showNgProductList(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 展示不良品列表（库存表）
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<NgProduct> showNgProductListByStock(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 根据良品seq获取各工序的数量 （报工记录表）
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<PassProduct> showPassProductList(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 根据良品seq获取各工序的数量 （库存表）
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<PassProduct> showPassProductListByStock(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 分析页面 - 产品数字 跳转列表
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<RecordForProduct> showProductList(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 分析页面 - 记工人数量跳转
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<RecordForWorker> showWorkerList(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 获取总的良品和不良品数
     *
     * @return
     */
    TotalStock obtainedTotalStock();
}
