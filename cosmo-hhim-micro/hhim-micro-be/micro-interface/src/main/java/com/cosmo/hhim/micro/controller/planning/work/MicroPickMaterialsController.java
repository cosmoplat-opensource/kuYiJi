/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.planning.work;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.micro.application.dto.material.MicroPickingMaterialDetailInfoDTO;
import com.cosmo.hhim.micro.application.dto.planning.MicroPickMaterialsDto;
import com.cosmo.hhim.micro.application.service.planning.IMicroPickMaterialsFacadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

import java.util.List;

/**
 * 投料单/退料单Controller
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-10
 */
@RestController
@RequestMapping("/materials")
public class MicroPickMaterialsController extends BaseController
{

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroPickMaterialsFacadeService microPickMaterialsFacadeService;

    /**
     * 查询工单下投料单/退料单列表
     *
     * bom + 物料单的bom
     */
    @GetMapping("/getListByWorkOrder")
    public AjaxResult getListByWorkOrder(MicroPickMaterialsDto microPickMaterialsDto) {
        MicroPickingMaterialDetailInfoDTO microPickingMaterialDetailInfoDTO = microPickMaterialsFacadeService.getListByWorkOrder(microPickMaterialsDto);
        return AjaxResult.success(microPickingMaterialDetailInfoDTO);
    }

    /**
     * 查询工单下投料单/退料单明细列表
     */
    @GetMapping("/getList")
    public TableDataInfo getList(MicroPickMaterialsDto microPickMaterialsDto) {
        startPage();
        List<MicroPickMaterialsDto> list = microPickMaterialsFacadeService.getList(microPickMaterialsDto);
        return getDataTable(list);
    }

}
