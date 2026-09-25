/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.base.factory;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.common.security.annotation.PreAuthorize;
import com.cosmo.hhim.micro.application.dto.base.MicroWorkShopDTO;
import com.cosmo.hhim.micro.application.service.base.IMicroWorkShopFacadeService;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

import java.util.List;

/**
 * 生产车间Controller
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
@RestController
@RequestMapping("/workshop")
public class MicroWorkShopController extends BaseController
{

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroWorkShopFacadeService microWorkShopFacadeService;

    /**
     * 查询生产车间列表
     */
    @GetMapping("/list")
    public TableDataInfo list(MicroWorkShopDTO condition)
    {
        startPage();
        List<MicroWorkShopDTO> list = microWorkShopFacadeService.selectMicroWorkshopList(condition);
        return getDataTable(list);
    }


    /**
     * 获取生产车间详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@ApiParam(name="id",value="生产车间ID") @PathVariable("id")  Long id)
    {
        return AjaxResult.success(microWorkShopFacadeService.selectMicroWorkshopById(id));
    }

    /**
     * 新增生产车间
     */
    @PostMapping
    public AjaxResult add(@RequestBody MicroWorkShopDTO microWorkshop)
    {
        return toAjax(microWorkShopFacadeService.insertMicroWorkshop(microWorkshop));
    }

    /**
     * 修改生产车间
     */
    @PutMapping
    public AjaxResult edit(@RequestBody MicroWorkShopDTO microWorkshop)
    {
        return toAjax(microWorkShopFacadeService.updateMicroWorkshop(microWorkshop));
    }

    /**
     * 删除生产车间
     */
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@ApiParam(name="id",value="生产车间ID")  @PathVariable Long[] ids)
    {
        return toAjax(microWorkShopFacadeService.deleteMicroWorkshopByIds(ids));
    }
}
