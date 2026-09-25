/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.base.custom;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.common.log.annotation.Log;
import com.cosmo.hhim.common.log.enums.BusinessType;
import com.cosmo.hhim.micro.application.dto.base.MicroExtendFieldDTO;
import com.cosmo.hhim.micro.application.service.base.IMicroExtendFieldFacadeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

import java.util.List;

/**
 * 扩展字段Controller
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-22
 */
@Api
@RestController
@RequestMapping("/field")
public class MicroExtendFieldController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroExtendFieldFacadeService microExtendFieldFacadeService;

    /**
     * 分页查询扩展字段列表
     */
    @ApiOperation(value = "分页查询扩展字段列表")
    @GetMapping("/list")
    public TableDataInfo list(MicroExtendFieldDTO microExtendField) {
        startPage();
        List<MicroExtendFieldDTO> list = microExtendFieldFacadeService.selectMicroExtendFieldList(microExtendField);
        return getDataTable(list);
    }

    /**
     * 查询扩展字段列表
     */
    @ApiOperation(value = "查询扩展字段列表")
    @GetMapping("/listByCondition")
    public AjaxResult listByCondition(MicroExtendFieldDTO microExtendField) {
        return AjaxResult.success(microExtendFieldFacadeService.selectMicroExtendFieldListByCondition(microExtendField));
    }

    /**
     * 获取扩展字段详细信息
     */
    @ApiOperation(value = "获取扩展字段详细信息")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@ApiParam(name = "id", value = "扩展字段ID") @PathVariable("id") Long id) {
        return AjaxResult.success(microExtendFieldFacadeService.selectMicroExtendFieldById(id));
    }

    /**
     * 新增扩展字段
     */
    @ApiOperation(value = "新增扩展字段")
    @Log(title = "扩展字段", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MicroExtendFieldDTO microExtendField) {
        return AjaxResult.success(microExtendFieldFacadeService.insertMicroExtendField(microExtendField));
    }

    /**
     * 修改扩展字段
     */
    @ApiOperation(value = "修改扩展字段")
    @Log(title = "扩展字段", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MicroExtendFieldDTO microExtendField) {
        return toAjax(microExtendFieldFacadeService.updateMicroExtendField(microExtendField));
    }

    /**
     * 删除扩展字段
     */
    @ApiOperation(value = "删除扩展字段")
    @Log(title = "扩展字段", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@ApiParam(name = "id", value = "扩展字段ID") @PathVariable Long[] ids) {
        return toAjax(microExtendFieldFacadeService.deleteMicroExtendFieldByIds(ids));
    }
}
