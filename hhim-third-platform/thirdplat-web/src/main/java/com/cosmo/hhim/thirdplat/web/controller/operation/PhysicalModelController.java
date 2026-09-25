/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.controller.operation;

import java.util.List;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import com.cosmo.hhim.thirdplat.api.operation.domain.PhysicalModel;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.web.service.physicaModel.IPhysicalModelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;
import com.cosmo.hhim.common.log.annotation.Log;
import com.cosmo.hhim.common.log.enums.BusinessType;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;

/**
 * iot 物模型Controller
 *
 * @author cosmo-hhim-open Team
 * @date 2024-01-29
 */
@Api
@RestController
@RequestMapping("/model")
public class PhysicalModelController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IPhysicalModelService physicalModelService;

    /**
     * 查询iot 物模型列表
     */
    @ApiOperation(value = "查询iot 物模型列表")
    @GetMapping("/list")
    public APIResponse<List<PhysicalModel>> list(PhysicalModel physicalModel) {
        return APIResponse.success(physicalModelService.selectPhysicalModelList(physicalModel));
    }


    /**
     * 获取iot 物模型详细信息
     */
    @ApiOperation(value = "获取iot 物模型详细信息")
    @GetMapping(value = "/{deviceCode}")
    public AjaxResult getInfo(@ApiParam(name = "deviceCode", value = "iot 物模型ID") @PathVariable("deviceCode") String deviceCode) {
        return AjaxResult.success(physicalModelService.selectPhysicalModelById(deviceCode));
    }

    /**
     * 新增iot 物模型
     */
    @ApiOperation(value = "新增iot 物模型")
    @Log(title = "iot 物模型", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PhysicalModel physicalModel) {
        return toAjax(physicalModelService.insertPhysicalModel(physicalModel));
    }

    /**
     * 修改iot 物模型
     */
    @ApiOperation(value = "修改iot 物模型")
    @Log(title = "iot 物模型", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PhysicalModel physicalModel) {
        return toAjax(physicalModelService.updatePhysicalModel(physicalModel));
    }

    /**
     * 删除iot 物模型
     */
    @ApiOperation(value = "删除iot 物模型")
    @Log(title = "iot 物模型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deviceCodes}")
    public AjaxResult remove(@ApiParam(name = "deviceCode", value = "iot 物模型ID") @PathVariable String[] deviceCodes) {
        return toAjax(physicalModelService.deletePhysicalModelByIds(deviceCodes));
    }
}
