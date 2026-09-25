/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.base.factory;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.common.security.annotation.PreAuthorize;
import com.cosmo.hhim.micro.application.dto.base.MicroFactoryDTO;
import com.cosmo.hhim.micro.application.service.base.IMicroFactoryFacadeService;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

import java.util.List;

/**
 * 工厂基础信息Controller
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
@RestController
@RequestMapping("/factory")
public class MicroFactoryController extends BaseController
{

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroFactoryFacadeService microFactoryFacadeService;

    /**
     * 查询工厂基础信息列表
     */
    @GetMapping("/list")
    public TableDataInfo list(MicroFactoryDTO condition)
    {
        startPage();
        List<MicroFactoryDTO> list = microFactoryFacadeService.selectMicroFactoryList(condition,condition.isLazy());
        return getDataTable(list);
    }


    /**
     * 获取工厂基础信息详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@ApiParam(name="id",value="工厂基础信息ID") @PathVariable("id")  Long id)
    {
        return AjaxResult.success(microFactoryFacadeService.selectMicroFactoryById(id));
    }

    /**
     * 新增工厂基础信息
     */
    @PreAuthorize(hasPermi = "bsea:factory:add")
    @PostMapping
    public AjaxResult add(@RequestBody MicroFactoryDTO microFactory)
    {
        return toAjax(microFactoryFacadeService.insertMicroFactory(microFactory));
    }

    /**
     * 修改工厂基础信息
     */
    @PreAuthorize(hasPermi = "bsea:factory:edit")
    @PutMapping
    public AjaxResult edit(@RequestBody MicroFactoryDTO microFactory)
    {
        return toAjax(microFactoryFacadeService.updateMicroFactory(microFactory));
    }

    /**
     * 删除工厂基础信息
     */
    @PreAuthorize(hasPermi = "bsea:factory:remove")
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@ApiParam(name="id",value="工厂基础信息ID")  @PathVariable Long[] ids)
    {
        return toAjax(microFactoryFacadeService.deleteMicroFactoryByIds(ids));
    }
}
