/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.planning.task;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.micro.application.dto.planning.MicroManufactureTaskDto;
import com.cosmo.hhim.micro.application.dto.planning.MicroManufactureWorkOrderDto;
import com.cosmo.hhim.micro.application.service.planning.IMicroManufactureTaskFacadeService;
import com.cosmo.hhim.micro.application.service.planning.IMicroManufactureWorkOrderFacadeService;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

import java.util.List;

/**
 * 生产任务Controller
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-09
 */
@RestController
@RequestMapping("/task")
public class MicroManufactureTaskController extends BaseController
{

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroManufactureWorkOrderFacadeService microManufactureWorkOrderFacadeService;

    @Autowired
    private IMicroManufactureTaskFacadeService microManufactureTaskFacadeService;

    /**
     * 查询生产任务列表-app
     */
    @GetMapping("/list")
    public TableDataInfo list(MicroManufactureWorkOrderDto microManufactureWorkOrderDto) {
        startPage();
        List<MicroManufactureWorkOrderDto> list = microManufactureWorkOrderFacadeService.getMicroManufactureTaskList(microManufactureWorkOrderDto);
        return getDataTable(list);
    }


    /**
     * 查询生产任务列表数量-app
     */
    @GetMapping("/getStatusCount")
    public AjaxResult getStatusCount(MicroManufactureWorkOrderDto microManufactureWorkOrderDto)
    {
        return AjaxResult.success(microManufactureWorkOrderFacadeService.getStatusCount(microManufactureWorkOrderDto));
    }

    /**
     * 获取生产报工页面
     */
    @GetMapping("/getSubmitInfo")
    public AjaxResult getSubmitInfo(@RequestParam("id") Long id) {
        return AjaxResult.success(microManufactureWorkOrderFacadeService.getSubmitInfo(id));
    }

    /**
     * 查询工单任务进度
     */
    @GetMapping("/listByCondition")
    public AjaxResult listByCondition(MicroManufactureTaskDto condition) {
        return AjaxResult.success(microManufactureTaskFacadeService.selectMicroManufactureTaskList(condition));
    }


    /**
     * 生产报工
     */
    @PostMapping("/productionReporting")
    public AjaxResult productionReporting(@RequestBody MicroManufactureTaskDto microManufactureTaskDto) {
        microManufactureTaskFacadeService.productionReporting(microManufactureTaskDto);
        return AjaxResult.success();
    }

}
