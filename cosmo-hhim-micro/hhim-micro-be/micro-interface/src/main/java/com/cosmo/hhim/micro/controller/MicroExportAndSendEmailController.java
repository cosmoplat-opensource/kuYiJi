/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller;

import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.log.annotation.Log;
import com.cosmo.hhim.common.log.enums.BusinessType;
import com.cosmo.hhim.common.security.annotation.AccessAuth;
import com.cosmo.hhim.micro.application.dto.complete.MicroSettlementQueryDTO;
import com.cosmo.hhim.micro.application.service.complete.IMicroSettlementFacadeService;
import com.cosmo.hhim.micro.application.service.submit.IMicroSubmitFacadeService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroAnalysisService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroExportAndSendEmailService;
import com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @description: 导出并发送邮件服务
 * @date 2023/1/6 09:45
 */
@RestController
@RequestMapping("/export")
public class MicroExportAndSendEmailController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    IMicroAnalysisService microAnalysisService;
    @Autowired
    IMicroExportAndSendEmailService microExportAndSendEmailService;
    @Autowired
    IMicroSettlementFacadeService settlementFacadeService;
    @Autowired
    IMicroSubmitFacadeService microSubmitFacadeService;

    /**
     * 报工记录导出发送邮件
     *
     * @param startDate
     * @param endDate
     * @param receivedBy
     * @return
     */
    @Log(title = "导出模块", businessType = BusinessType.EXPORT)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/submitRecord")
    public AjaxResult exportEmployeeSubmitRecord(@RequestParam(name = "startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                 @RequestParam(name = "endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                                 @RequestParam(name = "receivedBy") String receivedBy) {
        return AjaxResult.success(microSubmitFacadeService.exportEmployeeSubmitRecord(startDate, endDate, receivedBy));
    }

    /**
     * 完工产品导出发送邮件
     *
     * @param startDate
     * @param endDate
     * @param receivedBy
     * @return
     */
    @Log(title = "导出模块", businessType = BusinessType.EXPORT)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/finishedProduct")
    public AjaxResult exportFinishedProduct(@RequestParam(name = "startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                            @RequestParam(name = "endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                            @RequestParam(name = "receivedBy") String receivedBy) {
        return AjaxResult.success(microExportAndSendEmailService.exportFinishedProduct(startDate, endDate, receivedBy));
    }

    /**
     * 车间库存导出发送邮件
     *
     * @param receivedBy
     * @return
     */
    @Log(title = "导出模块", businessType = BusinessType.EXPORT)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/allProcessStorage")
    public AjaxResult exportAllProcessStorage(@RequestParam(name = "receivedBy") String receivedBy) {
        return AjaxResult.success(microExportAndSendEmailService.exportAllProcessStorage(receivedBy));
    }

    /**
     * 质量趋势导出发送邮件
     *
     * @param startDate
     * @param endDate
     * @param receivedBy
     * @return
     */
    @Log(title = "导出模块", businessType = BusinessType.EXPORT)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/qualityTrends")
    public AjaxResult exportQualityTrends(@RequestParam(name = "startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                          @RequestParam(name = "endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                          @RequestParam(name = "receivedBy") String receivedBy) {
        return AjaxResult.success(microExportAndSendEmailService.exportQualityTrends(startDate, endDate, receivedBy));
    }

    /**
     * 库存变动记录 - 导出excel并发送邮件
     *
     * @param productSeq
     * @param processSeq
     * @param startDate
     * @param endDate
     * @param receivedBy
     * @return
     */
    @Log(title = "导出模块", businessType = BusinessType.EXPORT)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/stockChangeRecord")
    public AjaxResult exportQualityTrends(@RequestParam(name = "productSeq") String productSeq,
                                          @RequestParam(name = "processSeq") String processSeq,
                                          @RequestParam(name = "startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                          @RequestParam(name = "endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                          @RequestParam(name = "receivedBy") String receivedBy) {
        return AjaxResult.success(microExportAndSendEmailService.exportStockChangeRecords(productSeq, processSeq, startDate, endDate, receivedBy));
    }

    /**
     * 计件结算已结算导出 - 导出excel并发送邮件
     *
     * @param queryDTO 结算查询DTO
     * @return
     */
    @Log(title = "计件已结算导出模块", businessType = BusinessType.EXPORT)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/settledReport")
    public AjaxResult exportSettledReport(@Validated MicroSettlementQueryDTO queryDTO) {
        return AjaxResult.success(settlementFacadeService.exportSettledReport(queryDTO));
    }

    /**
     * 计件结算已结算导出 - 导出excel并发送邮件
     *
     * @return
     */
    @Log(title = "计件待结算导出模块", businessType = BusinessType.EXPORT)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/openSettlementReport")
    public AjaxResult exportOpenSettlementReport(@Validated MicroSettlementQueryDTO queryDTO) {
        return AjaxResult.success(settlementFacadeService.exportOpenSettlementReport(queryDTO));
    }

    /**
     * 期初工序库存导入模版导出 - 导出excel并发送邮件
     *
     * @return
     */
    @Log(title = "期初工序库存导入模版导出模块", businessType = BusinessType.EXPORT)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/processStorageImportTemplate")
    public AjaxResult exportProcessStorageImportTemplate(@RequestParam(name = "receivedBy") String receivedBy) {
        return AjaxResult.success(microExportAndSendEmailService.exportProcessStorageTemplate(receivedBy));
    }

}
