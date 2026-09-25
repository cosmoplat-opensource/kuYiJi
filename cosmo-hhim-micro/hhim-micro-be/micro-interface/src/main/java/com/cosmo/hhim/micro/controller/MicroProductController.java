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
import com.cosmo.hhim.micro.application.service.base.IMicroProductFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroProductService;
import com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

import java.util.List;

/**
 * 产品Controller
 *
 * @date 2022-10-11
 */
@RestController
@RequestMapping("/product")
public class MicroProductController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroProductService microProductService;
    @Autowired
    private IMicroProductFacadeService productFacadeService;

    /**
     * 查询产品列表
     */
    @GetMapping("/list")
    public TableDataInfo list(MicroProduct microProduct) {
        startPage();
        List<MicroProduct> list = microProductService.selectMicroProductList(microProduct);
        return getDataTable(list);
    }

    /**
     * 获取产品详细信息
     */
    @GetMapping(value = "/{id}")
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(microProductService.selectMicroProductById(id));
    }

    /**
     * 获取产品详细信息
     */
    @GetMapping(value = "/info/{productSeq}")
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    public AjaxResult getInfo(@PathVariable("productSeq") String productSeq) {
        MicroProduct product = new MicroProduct();
        product.setProductSeq(productSeq);
        List<MicroProduct> data = microProductService.selectMicroProductList(product);
        return AjaxResult.success(CollectionUtils.isEmpty(data) ? new MicroProduct() : data.get(0));
    }

    /**
     * 获取产品下拉
     */
    @GetMapping(value = "/select")
    public AjaxResult select(@RequestParam String key, @RequestParam(required = false) boolean mixed) {
        return AjaxResult.success(productFacadeService.selectMicroProductByName(key, mixed));
    }

    /**
     * 新增产品
     */
    @PostMapping("/add")
    @Log(title = "产品管理模块", businessType = BusinessType.INSERT)
    public AjaxResult add(@Validated @RequestBody MicroProduct microProduct) {
        return AjaxResult.success(microProductService.insertMicroProduct(microProduct));
    }

    /**
     * 修改产品
     */
    @PostMapping("/edit")
    @Log(title = "产品管理模块", businessType = BusinessType.UPDATE)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    public AjaxResult edit(@Validated @RequestBody MicroProduct microProduct) {
        return toAjax(microProductService.updateMicroProduct(microProduct));
    }

    /**
     * 删除产品
     */
    @DeleteMapping("/{ids}")
    @Log(title = "产品管理模块", businessType = BusinessType.DELETE)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(productFacadeService.removeProductByIds(ids));
    }

    @GetMapping("/isOrNotHave")
    public AjaxResult isOrNotHaveProduct(String productName) {
        return AjaxResult.success(microProductService.isOrNotHaveProduct(productName));
    }

}
