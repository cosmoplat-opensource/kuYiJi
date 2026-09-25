/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.base.factory;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.common.security.annotation.PreAuthorize;
import com.cosmo.hhim.micro.application.dto.base.MicroManufactureLineDTO;
import com.cosmo.hhim.micro.application.service.base.IMicroManufactureLineFacadeService;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

import java.util.List;

/**
 * 生产线基础信息Controller
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
@RestController
@RequestMapping("/line")
public class MicroManufactureLineController extends BaseController
{

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroManufactureLineFacadeService microManufactureLineFacadeService;

    /**
     * 查询生产线基础信息列表
     */
    @GetMapping("/list")
    public TableDataInfo list(MicroManufactureLineDTO condition)
    {
        startPage();
        List<MicroManufactureLineDTO> list = microManufactureLineFacadeService.selectMicroManufactureLineList(condition);
        return getDataTable(list);
    }


    /**
     * 获取生产线基础信息详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@ApiParam(name="id",value="生产线基础信息ID") @PathVariable("id")  Long id)
    {
        return AjaxResult.success(microManufactureLineFacadeService.selectMicroManufactureLineById(id));
    }

    /**
     * 新增生产线基础信息
     */
    @PostMapping
    public AjaxResult add(@RequestBody MicroManufactureLineDTO microManufactureLine)
    {
        return toAjax(microManufactureLineFacadeService.insertMicroManufactureLine(microManufactureLine));
    }

    /**
     * 修改生产线基础信息
     */
    @PutMapping
    public AjaxResult edit(@RequestBody MicroManufactureLineDTO microManufactureLine)
    {
        return toAjax(microManufactureLineFacadeService.updateMicroManufactureLine(microManufactureLine));
    }

    /**
     * 删除生产线基础信息
     */
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@ApiParam(name="id",value="生产线基础信息ID")  @PathVariable Long[] ids)
    {
        return toAjax(microManufactureLineFacadeService.deleteMicroManufactureLineByIds(ids));
    }
}
