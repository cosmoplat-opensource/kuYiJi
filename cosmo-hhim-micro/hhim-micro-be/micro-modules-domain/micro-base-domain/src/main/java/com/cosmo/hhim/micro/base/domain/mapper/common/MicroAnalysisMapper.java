/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.common;

import com.cosmo.hhim.micro.base.domain.entity.analysis.*;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorage;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitProductCount;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @date 2022/10/24 5:17 下午
 */
public interface MicroAnalysisMapper {

    /**
     * 记工排名
     *
     * @param startDate
     * @param endDate
     * @param submitType
     * @return
     */
    List<SubmitterRank> getRecordRankForSubmitter(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("submitType") Long submitType);

    /**
     * 完工产品统计
     *
     * @param startDate
     * @param endDate
     * @return
     */
    List<FinishedProduct> getFinishedProductStatistics(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 库存排名（产品 + 工序）
     *
     * @param productSeq
     * @return
     */
    List<MicroProcessStorage> getStorageRank(@Param("productSeq") String productSeq);

    /**
     * 质量分析 - 总计展示（审核之后）
     *
     * @param productNameOrCode
     * @param startDate
     * @param endDate
     * @return
     */
    MicroWorkSubmitProductCount showTotalCount(@Param("productNameOrCode") String productNameOrCode, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 生产质量趋势
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<MicroWorkSubmitProductCount> productionQualityTrend(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 获取时间范围内的报工人数和产品数
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    AnalysisIndex obtainedTotalCountsForProductAndSubmitUser(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 获取时间范围内的报工产品总数
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    BigDecimal obtainedTotalProductCountsForSubmit(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 获取总审核数、良品审核数、不良品审核数
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    MicroWorkSubmitProductCount obtainedTotalProductCountsForCheck(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 获取不良品的种类 (报工记录中 已审核)
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<NgProduct> selectNgProductList(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 获取不良品的种类 (库存表)
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<NgProduct> selectNgProductListByStock(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 根据产品序列码获取到工序的数量 （已审核）
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<NgProductAndProcess> selectNgProductAndProcessList(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 根据产品序列码获取到工序的数量 （库存表）
     *
     * @return
     */
    List<NgProductAndProcess> selectNgProductAndProcessListByStock();

    /**
     * 获取良品的种类列表 （已审核）
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<PassProduct> selectPassProductList(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 根据产品序列码获取到工序的数量 （已审核）
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<PassProductAndProcess> selectPassProductAndProcessList(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 获取良品的种类列表 （库存表）
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<PassProduct> selectPassProductListByStock(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 根据产品序列码获取到工序的数量 （库存表）
     *
     * @return
     */
    List<PassProductAndProcess> selectPassProductAndProcessListByStock();

    /**
     * 获取时间范围内记工的产品种类
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<RecordForProduct> selectProductList(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 查询产品的记工总数 （待审核 + 审核）
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<RecordForProductAndProcess> selectProductAndProcessList(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 查询产品的记工总数 （待审核 + 审核）
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<RecordForWorker> selectWorkerList(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 获取总库存 良品和不良品数量
     *
     * @param tenantCode 租户编码（库存表无动态注入，需显式传参隔离）
     * @return
     */
    TotalStock obtainedTotalStock(@org.apache.ibatis.annotations.Param("tenantCode") String tenantCode);
}
