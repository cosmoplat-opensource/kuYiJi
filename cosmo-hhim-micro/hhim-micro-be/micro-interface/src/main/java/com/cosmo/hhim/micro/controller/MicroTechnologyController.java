/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.log.annotation.Log;
import com.cosmo.hhim.common.log.enums.BusinessType;
import com.cosmo.hhim.common.security.annotation.AccessAuth;
import com.cosmo.hhim.micro.application.dto.tech.MicroTechnologyBindDTO;
import com.cosmo.hhim.micro.application.dto.tech.ValidSubmitRecordTechInfoParam;
import com.cosmo.hhim.micro.application.service.tech.IMicroTechFacadeService;
import com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

/**
 * @author cosmo-hhim-open Team
 */
@RestController
@RequestMapping("/tech")
public class MicroTechnologyController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroTechFacadeService facadeService;

    @GetMapping("/self/{productId}")
    public AjaxResult getSelfProductTechnology(@PathVariable("productId") Long productId) {
        return AjaxResult.success(facadeService.getSelfProductTechnology(productId));
    }

    /**
     * 根据产品ID获取工艺链信息
     *
     * @param productId
     * @return
     */
    @GetMapping("/{productId}")
    public AjaxResult getProductTechnology(@PathVariable("productId") Long productId) {
        return AjaxResult.success(facadeService.getTechnologyChain(productId));
    }

    /**
     * 工艺绑定模块
     */
    @PostMapping("/bind")
    @Log(title = "工艺绑定模块", businessType = BusinessType.INSERT)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    public AjaxResult bind(@Validated @RequestBody MicroTechnologyBindDTO bindDTO) {
        return AjaxResult.success(facadeService.bindTech(bindDTO));
    }

    /**
     * 工艺绑定二次覆盖或相似模块
     */
    @PostMapping("/coverOrSimilar")
    @Log(title = "工艺绑定二次覆盖或相似模块", businessType = BusinessType.INSERT)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    public AjaxResult coverOrSimilar(@Validated @RequestBody MicroTechnologyBindDTO bindDTO) {
        return AjaxResult.success(facadeService.coverOrSimilar(bindDTO));
    }

    /**
     * 工艺链获取带标准工艺标记的产品列表
     */
    @GetMapping("/select/standardPro")
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    public AjaxResult getStandardPro(@RequestParam(required = false) String key) {
        return AjaxResult.success("SUCCESS", facadeService.selectStandardPro(key));
    }

    /**
     * 查询该产品是否已经有标准工艺
     *
     * @param productSeq
     * @return
     */
    @GetMapping("/isOrNotHaveStandardTech")
    public AjaxResult isOrNotHaveStandardTech(@RequestParam String productSeq) {
        return AjaxResult.success(facadeService.isOrNotHaveStandardTech(productSeq));
    }

    @PostMapping("/validSubmitRecordTechInfo")
    public AjaxResult validSubmitRecordTechInfo(@RequestBody ValidSubmitRecordTechInfoParam validSubmitRecordTechInfoParam  )  {
        return AjaxResult.success(facadeService.validSubmitRecordTechInfo(validSubmitRecordTechInfoParam));
    }
}
