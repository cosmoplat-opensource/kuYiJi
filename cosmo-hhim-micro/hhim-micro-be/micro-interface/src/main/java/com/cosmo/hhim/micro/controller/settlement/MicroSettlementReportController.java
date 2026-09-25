/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.settlement;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.common.log.annotation.Log;
import com.cosmo.hhim.common.log.enums.BusinessType;
import com.cosmo.hhim.common.security.annotation.AccessAuth;
import com.cosmo.hhim.micro.application.dto.complete.MicroSettlementGenerateQueryDTO;
import com.cosmo.hhim.micro.application.dto.complete.MicroSettlementModifyDTO;
import com.cosmo.hhim.micro.application.dto.complete.MicroSettlementQueryDTO;
import com.cosmo.hhim.micro.application.service.complete.IMicroSettlementFacadeService;
import com.cosmo.hhim.micro.complete.domain.entity.MicroSettledProductDetailDomain;
import com.cosmo.hhim.micro.complete.domain.entity.MicroSettledProductDomain;
import com.cosmo.hhim.micro.complete.domain.entity.MicroSettlementReportDomain;
import com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

import java.util.Date;
import java.util.List;

/**
 * 计件结算报告Controller
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-22
 */
@RestController
@RequestMapping("/settlement")
public class MicroSettlementReportController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroSettlementFacadeService facadeService;

    /**
     * 生成结算报告
     */
    @GetMapping("/generate")
    @Log(title = "生成结算报告", businessType = BusinessType.INSERT)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    public AjaxResult generate(MicroSettlementGenerateQueryDTO queryDTO) {
        return toAjax(facadeService.generate(queryDTO));
    }

    /**
     * 查询人员的待结算信息
     */
    @GetMapping("/open/user")
    public TableDataInfo openUser(MicroSettlementQueryDTO queryDTO) {
        startPage();
        List<MicroSettlementReportDomain> list = facadeService.getOpenSettlementUser(queryDTO);
        return getDataTable(list);
    }

    /**
     * 查询人员的已结算信息
     */
    @GetMapping("/settled/user")
    public TableDataInfo settledUser(@Validated MicroSettlementQueryDTO queryDTO) {
        startPage();
        List<MicroSettlementReportDomain> list = facadeService.getSettledUser(queryDTO);
        return getDataTable(list);
    }

    /**
     * 查询产品的已结算信息
     */
    @GetMapping("/settled/product")
    public TableDataInfo settledProduct(@Validated MicroSettlementQueryDTO queryDTO) {
        startPage();
        List<MicroSettledProductDomain> list = facadeService.getSettledProduct(queryDTO);
        return getDataTable(list);
    }


    /**
     * 查询产品的已结算信息详情
     */
    @GetMapping("/settled/product/detail")
    public TableDataInfo settledProductDetail(@Validated MicroSettlementQueryDTO queryDTO) {
        startPage();
        List<MicroSettledProductDetailDomain> list = facadeService.getSettledProductDetail(queryDTO);
        return getDataTable(list);
    }

    /**
     * 调整员工的待结算数量
     */
    @PostMapping("/edit")
    @Log(title = "调整结算数量", businessType = BusinessType.UPDATE)
    public AjaxResult editReport(@RequestBody @Validated MicroSettlementModifyDTO modifyDTO) {
        return toAjax(facadeService.editEmployeeSettlement(modifyDTO));
    }

    /**
     * 查询结算报告的调整历史
     */
    @GetMapping("/history")
    public TableDataInfo reportEditHistory(MicroSettlementQueryDTO queryDTO) {
        startPage();
        return getDataTable(facadeService.getReportEditHistory(queryDTO));
    }

    /**
     * 查询结算报告的调整历史
     */
    @GetMapping("/history/detail")
    public TableDataInfo reportEditHistoryDetail(@RequestParam Long userId,
                                                 @RequestParam(required = false) String productSeq,
                                                 @RequestParam(required = false) String operateProcessSeq,
                                                 @RequestParam(required = false) Date searchDate) {
        startPage();
        return getDataTable(facadeService.getReportEditHistoryDetail(userId, productSeq, operateProcessSeq, searchDate));
    }

    /**
     * 计件结算报告的工作台展示数量
     */
    @GetMapping("/home/index")
    public AjaxResult homeIndex() {
        return AjaxResult.success(facadeService.homeIndex());
    }

}
