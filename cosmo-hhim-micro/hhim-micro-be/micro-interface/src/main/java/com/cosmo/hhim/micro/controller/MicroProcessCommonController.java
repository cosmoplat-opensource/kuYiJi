/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.common.log.annotation.Log;
import com.cosmo.hhim.common.log.enums.BusinessType;
import com.cosmo.hhim.common.security.annotation.AccessAuth;
import com.cosmo.hhim.micro.application.dto.base.MicroProcessMixedSelectDTO;
import com.cosmo.hhim.micro.application.dto.base.MicroProcessStockQueryDTO;
import com.cosmo.hhim.micro.application.service.base.IMicroProcessFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessCommon;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessCommonService;
import com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

import java.util.List;

/**
 * processController
 *
 * @date 2022-10-11
 */
@RestController
@RequestMapping("/process")
public class MicroProcessCommonController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroProcessCommonService microProcessCommonService;
    @Autowired
    private IMicroProcessFacadeService processFacadeService;

    /**
     * 查询process列表
     */
    @GetMapping("/list")
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    public TableDataInfo list(MicroProcessCommon microProcessCommon) {
        startPage();
        List<MicroProcessCommon> list = microProcessCommonService.selectMicroProcessCommonList(microProcessCommon);
        return getDataTable(list);
    }

    /**
     * 获取process详细信息
     */
    @GetMapping(value = "/{id}")
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(microProcessCommonService.selectMicroProcessCommonById(id));
    }

    /**
     * 获取工序下拉
     */
    @GetMapping(value = "/select")
    public AjaxResult select(@RequestParam String key,
                             @RequestParam(required = false) String productSeq,
                             @RequestParam(required = false) boolean standard) {
        return AjaxResult.success(microProcessCommonService.selectMicroProcessByName(key, productSeq, standard));
    }

    /**
     * 在批量报工流程中获取工序下拉
     */
    @PostMapping(value = "/selectMixed")
    public AjaxResult selectMixed(@RequestBody MicroProcessMixedSelectDTO mixedSelectDTO) {
        return AjaxResult.success(processFacadeService.selectMixedByName(mixedSelectDTO));
    }

    /**
     * 多产品+多现工序推荐第一个前工序+是否首尾序
     */
    @PostMapping(value = "/selectPre")
    public AjaxResult selectMultiMixed(@RequestBody List<MicroProcessMixedSelectDTO> multiList) {
        return AjaxResult.success(processFacadeService.selectMultiMixed(multiList));
    }

    /**
     * 根据产品获取产品工序
     */
    @PostMapping(value = "/selectStock")
    public AjaxResult select(@RequestBody MicroProcessStockQueryDTO queryDTO) {
        return AjaxResult.success(processFacadeService.selectProcessStock(queryDTO));
    }

    /**
     * 新增process
     */
    @PostMapping("/add")
    @Log(title = "工序管理模块", businessType = BusinessType.INSERT)
    public AjaxResult add(@Validated @RequestBody MicroProcessCommon microProcessCommon) {
        return AjaxResult.success(microProcessCommonService.insertMicroProcessCommon(microProcessCommon));
    }

    /**
     * 新增process
     */
    @PostMapping("/experienceAdd")
    @Log(title = "体验引导模块-新增工序", businessType = BusinessType.INSERT)
    public AjaxResult experienceAdd(@RequestBody List<String> processList) {
        return toAjax(microProcessCommonService.insertExperienceProcess(processList));
    }

    /**
     * 修改process
     */
    @PostMapping("/edit")
    @Log(title = "工序管理模块", businessType = BusinessType.UPDATE)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    public AjaxResult edit(@Validated @RequestBody MicroProcessCommon microProcessCommon) {
        return toAjax(microProcessCommonService.updateMicroProcessCommon(microProcessCommon));
    }

    /**
     * 删除process
     */
    @DeleteMapping("/{ids}")
    @Log(title = "工序管理模块", businessType = BusinessType.DELETE)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(processFacadeService.removeProcessByIds(ids));
    }

    @GetMapping("/isOrNotHave")
    public AjaxResult isOrNotHaveProcess(String processName) {
        return AjaxResult.success(microProcessCommonService.isOrNotHaveProcess(processName));
    }

    /**
     * 根据产品seq获取工序列表
     *
     * @param productSeq
     * @return
     */
    @GetMapping("/selectByChain")
    public AjaxResult selectByProduct(@RequestParam String key, @RequestParam(required = false) String productSeq) {
        return AjaxResult.success(microProcessCommonService.selectByChain(key, productSeq));
    }

    /**
     * 根据标准工艺进行默认工序推荐
     *
     * @param productSeq
     * @param operateProcessSeq
     * @return
     */
    @GetMapping("/defaultRecommend")
    public AjaxResult defaultRecommendByStandardTech(@RequestParam String productSeq,
                                                     @RequestParam String operateProcessSeq) {
        return AjaxResult.success(processFacadeService.defaultRecommendByStandardTech(productSeq, operateProcessSeq));
    }
}
