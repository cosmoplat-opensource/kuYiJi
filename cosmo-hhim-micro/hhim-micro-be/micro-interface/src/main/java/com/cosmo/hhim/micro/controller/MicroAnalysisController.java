/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller;

import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.common.security.annotation.AccessAuth;
import com.cosmo.hhim.micro.application.service.analysis.IMicroAnalysisFacadeService;
import com.cosmo.hhim.micro.application.service.base.IMicroLoadDisplayDataFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.analysis.*;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitDto;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitProductCount;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroAnalysisService;
import com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 分析模块的接口
 * @date 2022/10/24 5:15 下午
 */
@RestController
@RequestMapping("/analysis")
public class MicroAnalysisController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroAnalysisService microAnalysisService;
    @Autowired
    private IMicroLoadDisplayDataFacadeService microLoadDisplayDataFacadeService;
    @Autowired
    private IMicroAnalysisFacadeService microAnalysisFacadeService;

    /**
     * 获取分析首页的信息
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/index")
    public AjaxResult obtainedAnalysisIndexInformation(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        return AjaxResult.success(microAnalysisService.obtainedAnalysisIndexInformation(productionQualityAnalysisParam));
    }

    /**
     * 记工排名
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/submitRecordRank")
    public AjaxResult getRecordRankForSubmitter(@RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return AjaxResult.success(microAnalysisService.getRecordRankForSubmitter(startDate, endDate));
    }

    /**
     * 完工统计
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/finishedProductStatistics")
    public AjaxResult getFinishedProductStatistics(@RequestParam(value = "startDate",required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                   @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return AjaxResult.success(microAnalysisService.getFinishedProductStatistics(startDate, endDate));
    }

    /**
     * 在制品库存排名
     *
     * @return
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/storageRank")
    public AjaxResult getStorageRank(@RequestParam(value = "productSeq", required = false) String productSeq) {
        return AjaxResult.success(microAnalysisService.getStorageRank(productSeq));
    }

    /**
     * 质量分析 - 数量统计
     *
     * @param productNameOrCode
     * @param startDate
     * @param endDate
     * @return
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/showTotalCount")
    public AjaxResult showTotalCount(@RequestParam(value = "productNameOrCode", required = false) String productNameOrCode,
                                     @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                     @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        return AjaxResult.success(microAnalysisService.showTotalCount(productNameOrCode, startDate, endDate));
    }

    /**
     * 生产质量趋势
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/productionQualityTrend")
    public AjaxResult showProductionQualityTrend(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        return AjaxResult.success(microAnalysisService.productionQualityTrend(productionQualityAnalysisParam));
    }

    /**
     * 良品率分析（产品维度）
     *
     * @param microWorkSubmitDto
     * @return
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/passRateAnalysisByProduct")
    public AjaxResult passRateAnalysisByProduct(MicroWorkSubmitDto microWorkSubmitDto) {
        startPage();
        List<MicroWorkSubmitProductCount> microWorkSubmitProductCounts = microAnalysisService.passRateAnalysisByProduct(microWorkSubmitDto);
        return AjaxResult.success(microWorkSubmitProductCounts);
    }

    /**
     * 良品率分析 (工序维度)
     *
     * @param microWorkSubmitDto
     * @return
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/passRateAnalysisByProcess")
    public AjaxResult passRateAnalysisByProcess(MicroWorkSubmitDto microWorkSubmitDto) {
        startPage();
        List<MicroWorkSubmitProductCount> microWorkSubmitProductCounts = microAnalysisService.passRateAnalysisByProcess(microWorkSubmitDto);
        return AjaxResult.success(microWorkSubmitProductCounts);
    }

    /**
     * 良品率分析 (员工维度)
     *
     * @param microWorkSubmitDto
     * @return
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/passRateAnalysisByEmployee")
    public AjaxResult passRateAnalysisByEmployee(MicroWorkSubmitDto microWorkSubmitDto) {
        startPage();
        List<SubmitterRank> submitterRanks = microAnalysisService.passRateAnalysisByEmployee(microWorkSubmitDto);
        return AjaxResult.success(submitterRanks);
    }

    /**
     * 导入演示数据
     *
     * @param apiUrl
     * @return
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("loadDisplayData")
    public AjaxResult loadDisplayData(@RequestParam("apiUrl") String apiUrl) {
        return AjaxResult.success(microLoadDisplayDataFacadeService.loadDisplayData(apiUrl));
    }

    /**
     * 展示不良品列表 (报工表)
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    @GetMapping("/showNgProductList")
    public TableDataInfo showNgProductList(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        startPage();
        List<NgProduct> ngProducts = microAnalysisService.showNgProductList(productionQualityAnalysisParam);
        return getDataTable(ngProducts);
    }

    /**
     * 展示不良品列表 (库存表)
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    @GetMapping("/showNgProductListByStock")
    public TableDataInfo showNgProductListByStock(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        startPage();
        List<NgProduct> ngProducts = microAnalysisService.showNgProductListByStock(productionQualityAnalysisParam);
        return getDataTable(ngProducts);
    }

    /**
     * 展示良品列表 (报工表)
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    @GetMapping("/showPassProductList")
    public TableDataInfo showPassProductList(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        startPage();
        List<PassProduct> passProducts = microAnalysisService.showPassProductList(productionQualityAnalysisParam);
        return getDataTable(passProducts);
    }

    /**
     * 展示良品列表  (库存表)
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    @GetMapping("/showPassProductListByStock")
    public TableDataInfo showPassProductListByStock(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        startPage();
        List<PassProduct> passProducts = microAnalysisService.showPassProductListByStock(productionQualityAnalysisParam);
        return getDataTable(passProducts);
    }

    /**
     * 分析页面 - 产品数字 跳转列表
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    @GetMapping("/showProductList")
    public TableDataInfo showProductList(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        startPage();
        List<RecordForProduct> products = microAnalysisService.showProductList(productionQualityAnalysisParam);
        return getDataTable(products);
    }

    /**
     * 分析页面 - 记工人数跳转
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    @GetMapping("/showWorkerList")
    public TableDataInfo showWorkerList(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        startPage();
        List<RecordForWorker> recordForWorkerList = microAnalysisService.showWorkerList(productionQualityAnalysisParam);
        return getDataTable(recordForWorkerList);
    }

    /**
     * 获取总的数量（良品和不良品）
     *
     * @return
     */
    @GetMapping("/obtainedTotalStock")
    public AjaxResult obtainedTotalStock() {
        return AjaxResult.success(microAnalysisService.obtainedTotalStock());
    }

    /**
     * 工易派首页信息
     *
     * @param queryDate
     * @return
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE})
    @GetMapping("/index/plan")
    public AjaxResult calculateIndexStatisticInfoFromPlan(@RequestParam("queryDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date queryDate) {
        return AjaxResult.success(microAnalysisFacadeService.calculateIndexStatisticInfoFromPlan(queryDate));
    }

    /**
     * 工易派首页 - 不同工单状态下的数量 - 饼状图
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/calcWorkOrderInDifferentStatus")
    public AjaxResult calculateWorkOrderNumInDifferentStatus(@RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                             @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return AjaxResult.success(microAnalysisFacadeService.calculateWorkOrderNumInDifferentStatus(startDate, endDate));
    }

    /**
     * 工易派首页 - 预警信息
     *
     * @return
     */
    @GetMapping("/calcWarnInfo")
    public AjaxResult calculateWarnInfo() {
        return AjaxResult.success(microAnalysisFacadeService.calculateWarnStatisticInfo());
    }

    /**
     * 工易派 - 生产管理模块的统计信息
     *
     * @return
     */
    @GetMapping("/calcProductionManageInfo")
    public AjaxResult calculateProductionManageInfo() {
        return AjaxResult.success(microAnalysisFacadeService.calculateProductionManageStatisticInfo());
    }

    /**
     * 工易派 - 基础数据信息统计
     *
     * @return
     */
    @GetMapping("/calcBaseInfo")
    public AjaxResult calculateBaseInfo() {
        return AjaxResult.success(microAnalysisFacadeService.calculateBaseStatisticInfo());
    }

    /**
     * 工易派 - 产品维度生产报工汇总分析
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    @GetMapping("/submitSummaryAnalysisByProduct")
    public TableDataInfo productiveSubmitSummaryAnalysisByProduct(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        startPage();
        List<ProductiveSubmitSummaryInfoByProduct> productiveSubmitSummaryAnalysisByProduct = microAnalysisFacadeService.getProductiveSubmitSummaryAnalysisByProduct(productionQualityAnalysisParam);
        return getDataTable(productiveSubmitSummaryAnalysisByProduct);
    }

    /**
     * 工易派 - 产品维度生产报工汇总分析
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    @GetMapping("/submitSummaryAnalysisByProcessBaseProduct")
    AjaxResult productiveSubmitSummaryAnalysisByProcessBaseProduct(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        return AjaxResult.success(microAnalysisFacadeService.getProductiveSubmitSummaryAnalysisByProcessBaseProduct(productionQualityAnalysisParam));
    }

    /**
     * 工易派 - 员工维度生产报工数据
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    @GetMapping("/submitSummaryAnalysisByUser")
    public TableDataInfo getProductiveSubmitSummaryAnalysisByUser(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        startPage();
        List<ProductiveSubmitAnalysisByUser> productiveSubmitSummaryAnalysisByUser = microAnalysisFacadeService.getProductiveSubmitSummaryAnalysisByUser(productionQualityAnalysisParam);
        return getDataTable(productiveSubmitSummaryAnalysisByUser);
    }

    /**
     * 工易派 - 员工维度生产报工数据
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    @GetMapping("/submitSummaryAnalysisByProductBaseUser")
    public AjaxResult getProductiveSubmitSummaryInfoBaseUser(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        return AjaxResult.success(microAnalysisFacadeService.getProductiveSubmitSummaryInfoBaseUser(productionQualityAnalysisParam));
    }

    /**
         * 工易派 - 产品入库列表
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/inBoundProductStatisticsInfo")
    public AjaxResult InBoundProductStatisticsInfo(@RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate, 
                                                   @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return AjaxResult.success(microAnalysisFacadeService.InBoundProductStatisticsInfo(startDate, endDate));
    }
}
