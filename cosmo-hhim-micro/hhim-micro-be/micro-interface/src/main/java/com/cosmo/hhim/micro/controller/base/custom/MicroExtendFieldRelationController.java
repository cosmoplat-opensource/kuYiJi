/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.base.custom;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.micro.application.dto.base.MicroExtendFieldRelationDTO;
import com.cosmo.hhim.micro.application.service.base.IMicroExtendFieldRelationFacadeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

import java.util.List;

/**
 * 扩展字段关系Controller
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-27
 */
@Api
@RestController
@RequestMapping("/field/relation")
public class MicroExtendFieldRelationController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroExtendFieldRelationFacadeService microExtendFieldRelationFacadeService;

    /**
     * 查询扩展字段关系列表
     */
    @ApiOperation(value = "查询扩展字段关系列表")
    @GetMapping("/list")
    public TableDataInfo list(MicroExtendFieldRelationDTO microExtendFieldRelation) {
        startPage();
        List<MicroExtendFieldRelationDTO> list = microExtendFieldRelationFacadeService.selectMicroExtendFieldRelationList(microExtendFieldRelation);
        return getDataTable(list);
    }

    /**
     * 查询扩展字段关系列表
     */
    @ApiOperation(value = "查询扩展字段关系列表")
    @GetMapping("/listByCondition")
    public AjaxResult listByCondition(MicroExtendFieldRelationDTO microExtendFieldRelation) {
        return AjaxResult.success(microExtendFieldRelationFacadeService.selectMicroExtendFieldRelationListByCondition(microExtendFieldRelation));
    }

    /**
     * 获取扩展字段关系详细信息
     */
    @ApiOperation(value = "获取扩展字段关系详细信息")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@ApiParam(name = "id", value = "扩展字段关系ID") @PathVariable("id") Long id) {
        return AjaxResult.success(microExtendFieldRelationFacadeService.selectMicroExtendFieldRelationById(id));
    }

    /**
     * 新增扩展字段关系
     */
    @ApiOperation(value = "新增扩展字段关系")
    @PostMapping
    public AjaxResult add(@RequestBody MicroExtendFieldRelationDTO microExtendFieldRelation) {
        return toAjax(microExtendFieldRelationFacadeService.insertMicroExtendFieldRelation(microExtendFieldRelation));
    }

    /**
     * 修改扩展字段关系
     */
    @ApiOperation(value = "修改扩展字段关系")
    @PutMapping
    public AjaxResult edit(@RequestBody MicroExtendFieldRelationDTO microExtendFieldRelation) {
        return toAjax(microExtendFieldRelationFacadeService.updateMicroExtendFieldRelation(microExtendFieldRelation));
    }

    /**
     * 删除扩展字段关系
     */
    @ApiOperation(value = "删除扩展字段关系")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@ApiParam(name = "id", value = "扩展字段关系ID") @PathVariable Long[] ids) {
        return toAjax(microExtendFieldRelationFacadeService.deleteMicroExtendFieldRelationByIds(ids));
    }
}
