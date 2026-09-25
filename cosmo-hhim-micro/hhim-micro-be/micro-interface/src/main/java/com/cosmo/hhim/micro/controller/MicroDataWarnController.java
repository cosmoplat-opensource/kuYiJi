/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroDataWarnService;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @description: 工序预警Controller
 * @date 2022/12/9 13:55
 */
@RestController
@RequestMapping("/warn")
public class MicroDataWarnController extends BaseController {

    @Autowired
    private IMicroDataWarnService microDataWarnService;
    @Autowired
    private IMicroProcessStorageService microProcessStorageService;

    /**
     * 产品有多道尾序
     */
    @GetMapping(value = "/product/multiLastProcess")
    public AjaxResult multiLastProcess() {
        return AjaxResult.success(microDataWarnService.selectMultiLastProcessForProduct());
    }

    /**
     * 产品没有尾序, 有标准工艺不展示
     */
    @GetMapping(value = "/product/notHaveLastProcess")
    public AjaxResult notHaveLastProcess() {
        return AjaxResult.success(microDataWarnService.selectNotHaveLastProcessForProduct());
    }

    /**
     * 产品没有首序，有标准工艺不展示
     */
    @GetMapping(value = "/product/notHaveFirstProcess")
    public AjaxResult notHaveFirstProcess() {
        return AjaxResult.success(microDataWarnService.selectNotHaveFirstProcessForProduct());
    }

    /**
     * 工序异常 - 指定工序（首序、尾序）
     *
     * @param productSeq
     * @param processSeq
     * @param processWarnType 异常类型:  1. 无首序 2. 无尾序 3. 多尾序
     * @return
     */
    @GetMapping(value = "/product/assignProcess")
    public AjaxResult assignProcessToProduct(@RequestParam("productSeq") String productSeq,
                                             @RequestParam("processSeq") String processSeq,
                                             @RequestParam("processWarnType") String processWarnType) {
        return AjaxResult.success(microDataWarnService.assignFirstOrLastProcessToProduct(productSeq, processSeq, processWarnType));
    }

    /**
     * 时间范围内报工十次且被纠正过数量的报工人
     * 时间范围设定在两周之内
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/employee/findModifiedRecordOverThreeTimesEmployees")
    public AjaxResult findModifiedRecordOverThreeTimesEmployees(@RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                                @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        return AjaxResult.success(microDataWarnService.findModifiedRecordOverThreeTimesEmployees(startDate, endDate));
    }

    /**
     * 时间范围内连续三天没有报工的工人
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/employee/findConsecutiveThreeDaysNoRecordEmployees")
    public AjaxResult findConsecutiveThreeDaysNoRecordEmployees(@RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                                @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return AjaxResult.success(microDataWarnService.findConsecutiveThreeDaysNoRecordEmployees(startDate, endDate));
    }

    /**
     * 时间范围内连续七天没有报工的工人
     *
     * @return
     */
    @GetMapping("/employee/findConsecutiveSevenDaysNoRecordEmployees")
    public AjaxResult findConsecutiveSevenDaysNoRecordEmployees() {
        return AjaxResult.success(microDataWarnService.findConsecutiveSevenDaysNoRecordEmployees());
    }

    /**
     * 报工数量超过人均日产能的工人
     *
     * @return
     */
    @GetMapping("/employee/findOverDayCapacityEmployees")
    public AjaxResult findOverDayCapacityEmployee() {
        return AjaxResult.success(microDataWarnService.findOverDayCapacityEmployee());
    }

    /**
     * 检查报工数量是否符合安全范围值
     *
     * @param totalSubmitNum
     * @param submitUser
     * @param productSeq
     * @param processSeq
     * @return
     */
    @Deprecated
    @GetMapping("/checkSubmitNum")
    public AjaxResult checkSubmitNum(@RequestParam("totalSubmitNum") BigDecimal totalSubmitNum,
                                     @RequestParam("submitUser") String submitUser,
                                     @RequestParam("productSeq") String productSeq,
                                     @RequestParam("processSeq") String processSeq) {
        return AjaxResult.success(microDataWarnService.checkSubmitNum(totalSubmitNum, submitUser, productSeq, processSeq));
    }

    /**
     * 主数据清理
     *
     * @return AjaxResult
     */
    @GetMapping("/masterData")
    public TableDataInfo checkMasterData(@RequestParam("masterType") String masterType) {
        startPage();
        return getDataTable(microDataWarnService.checkMasterData(masterType));
    }

    /**
     * 主数据清理
     *
     * @return AjaxResult
     */
    @GetMapping("/masterDataCount")
    public AjaxResult checkMasterDataCount(@RequestParam("masterType") String masterType) {
        return AjaxResult.success(microDataWarnService.checkMasterDataCount(masterType));
    }

    /**
     * 库存健康度
     *
     * @return
     */
    @GetMapping("/storage/health")
    public AjaxResult storageHealth() {
        return AjaxResult.success(microProcessStorageService.storageHealth());
    }

    /**
     * 负库存预警
     *
     * @return
     */
    @GetMapping("/negativeStock")
    public TableDataInfo negativeStock() {
        startPage();
        return getDataTable(microProcessStorageService.negativeStockList());
    }

    /**
     * 不良品数量预警
     *
     * @return
     */
    @GetMapping("/ng")
    public TableDataInfo ngList() {
        startPage();
        return getDataTable(microProcessStorageService.ngList());
    }

    /**
     * 报工数据健康度计算
     *
     * @return
     */
    @GetMapping("/dataHealth")
    public AjaxResult calculateDataHealth() {
        return AjaxResult.success(microDataWarnService.calculateDataHealth());
    }
}
