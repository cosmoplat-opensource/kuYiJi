/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.planning.work;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.common.redis.distributedlock.annotation.SubmitLock;
import com.cosmo.hhim.excel.ExcelUtil;
import com.cosmo.hhim.micro.application.dto.planning.MicroManufactureOrderDto;
import com.cosmo.hhim.micro.application.dto.planning.MicroManufactureWorkOrderDto;
import com.cosmo.hhim.micro.application.dto.planning.SimpleWorkOrderInfo;
import com.cosmo.hhim.micro.application.service.planning.IMicroManufactureWorkOrderFacadeService;
import com.cosmo.hhim.micro.infrastructure.enums.BaseEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.WorkOrderStatusEnum;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 生产工单Controller
 * 
 * @date 2023-03-06
 */
@RestController
@RequestMapping("/workOrder")
public class MicroManufactureWorkOrderController extends BaseController
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

    /**
     * 查询生产工单列表
     */
    @GetMapping("/list")
    public TableDataInfo list(MicroManufactureWorkOrderDto microManufactureWorkOrder)
    {
        startPage();
        List<MicroManufactureWorkOrderDto> list = microManufactureWorkOrderFacadeService.selectMicroManufactureWorkOrderList(microManufactureWorkOrder);
        return getDataTable(list);
    }

    /**
     * 查询生产工单列表
     */
    @GetMapping("/listWorkStatusCount")
    public AjaxResult listWorkStatusCount(MicroManufactureWorkOrderDto microManufactureWorkOrder)
    {
        return AjaxResult.success(microManufactureWorkOrderFacadeService.selectWorkStatusCount(microManufactureWorkOrder));
    }

    /**
     * 获取生产工单详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id")  Long id)
    {
        return AjaxResult.success(microManufactureWorkOrderFacadeService.selectMicroManufactureWorkOrderById(id));
    }

    /**
     * 获取工单详细信息 - 带着生产任务相关信息
     *
     * @param workOrderNo
     * @return
     */
    @GetMapping("/detailInfo")
    public AjaxResult getDetailInfoByWorkOrder(@RequestParam String workOrderNo) {
        return AjaxResult.success(microManufactureWorkOrderFacadeService.selectDetailManufactureWorkOrderInfo(workOrderNo));
    }

    /**
     * 新增生产工单
     */
    @PostMapping
//    @SubmitLock(expire = 10) 
    public AjaxResult add(@RequestBody MicroManufactureWorkOrderDto microManufactureWorkOrder)
    {
        // 两种场景 1.表单重复提交（单人多次） 2.同时提交相同工单编号（多人同时）
        // SubmitLock的提示语只针对第一种场景
        // todo 第二种场景提示语
        return microManufactureWorkOrderFacadeService.insertMicroManufactureWorkOrder(microManufactureWorkOrder);
    }

    /**
     * 修改生产工单
     */
    @PutMapping
    public AjaxResult edit(@RequestBody MicroManufactureWorkOrderDto microManufactureWorkOrder)
    {
        return toAjax(microManufactureWorkOrderFacadeService.updateMicroManufactureWorkOrder(microManufactureWorkOrder));
    }

    /**
     * 删除生产工单
     */
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(microManufactureWorkOrderFacadeService.deleteMicroManufactureWorkOrderByIds(ids));
    }

    /**
     * 获取生产工单开工下发页信息
     */
    @GetMapping(value = "/getIssuedInfo")
    public AjaxResult getIssuedInfo(@RequestParam("id") Long id) {
        return AjaxResult.success(microManufactureWorkOrderFacadeService.getIssuedInfo(id));
    }

    /**
     * 生产工单开工下发
     */
    @PostMapping(value = "/workOrderIssued")
    @SubmitLock
    public AjaxResult workOrderIssued(@RequestBody MicroManufactureWorkOrderDto microManufactureWorkOrderDto) {
        microManufactureWorkOrderFacadeService.workOrderIssued(microManufactureWorkOrderDto);
        return AjaxResult.success();
    }

    /**
     * 生产投料单/退料单
     */
    @PostMapping("/feedingOrReturn")
    public AjaxResult feedingOrReturn(@RequestBody MicroManufactureWorkOrderDto microManufactureWorkOrderDto) {
        microManufactureWorkOrderFacadeService.feedingOrReturn(microManufactureWorkOrderDto);
        return AjaxResult.success();
    }

    /**
     * 生产订单操作
     */
    @PutMapping(value = "/operate")
    public AjaxResult operate(@RequestBody MicroManufactureWorkOrderDto microManufactureWorkOrder)
    {
        return toAjax(microManufactureWorkOrderFacadeService.operate(microManufactureWorkOrder));
    }

    /**
     * 订单进度跟踪列表
     */
    @GetMapping("/listOrderProgress")
    public TableDataInfo listOrderProgress(MicroManufactureWorkOrderDto microManufactureWorkOrder)
    {
        startPage();
        List<MicroManufactureWorkOrderDto> list = microManufactureWorkOrderFacadeService.selectWorkOrderProgressList(microManufactureWorkOrder);
        return getDataTable(list);
    }


    /**
     * 获取到人的简单信息工单列表
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/simpleListByUser")
    public TableDataInfo simpleListByUser(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        startPage();
        List<SimpleWorkOrderInfo> list = microManufactureWorkOrderFacadeService.selectSimpleWorkOrderInfoListByUserAndDay(startDate, endDate);
        return getDataTable(list);
    }
}
