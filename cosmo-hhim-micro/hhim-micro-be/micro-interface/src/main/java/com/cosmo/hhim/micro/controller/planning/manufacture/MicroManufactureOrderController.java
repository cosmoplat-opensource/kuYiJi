/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.planning.manufacture;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.micro.application.dto.planning.MicroManufactureOrderDto;
import com.cosmo.hhim.micro.application.dto.planning.OrderProgressDetailInfo;
import com.cosmo.hhim.micro.application.dto.planning.OrderProgressParam;
import com.cosmo.hhim.micro.application.service.planning.IMicroManufactureOrderFacadeService;
import com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroManufactureOrder;
import org.aspectj.weaver.loadtime.Aj;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

import java.util.List;

/**
 * 生产订单Controller
 * 
 * @date 2023-03-06
 */
@RestController
@RequestMapping("/order")
public class MicroManufactureOrderController extends BaseController
{

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroManufactureOrderFacadeService iMicroManufactureOrderFacadeService;

    /**
     * 查询生产订单列表
     */
    @GetMapping("/list")
    public TableDataInfo list(MicroManufactureOrderDto microManufactureOrder)
    {
        startPage();
        List<MicroManufactureOrderDto> list = iMicroManufactureOrderFacadeService.selectMicroManufactureOrderList(microManufactureOrder);
        return getDataTable(list);
    }

    /**
     * 获取生产订单详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id")  Long id)
    {
        return AjaxResult.success(iMicroManufactureOrderFacadeService.selectMicroManufactureOrderById(id));
    }

    /**
     * 获取生产订单详细信息 - v2
     *
     * 1.自定义字段信息
     * 2.工单信息
     */
    @GetMapping("/detailInfo")
    public AjaxResult getDetailInfo(@RequestParam String orderNo) {
        return AjaxResult.success(iMicroManufactureOrderFacadeService.getDetailInfo(orderNo));
    }

    /**
     * 查询生产工单列表
     */
    @GetMapping("/listWorkStatusCount")
    public AjaxResult listWorkStatusCount(MicroManufactureOrderDto microManufactureOrder)
    {
        return AjaxResult.success(iMicroManufactureOrderFacadeService.selectOrderStatusCount(microManufactureOrder));
    }

    /**
     * 新增生产订单
     */
    @PostMapping
    public AjaxResult add(@Validated @RequestBody List<MicroManufactureOrderDto> microManufactureOrders)
    {
        return iMicroManufactureOrderFacadeService.insertMicroManufactureOrder(microManufactureOrders);
    }

    /**
     * 修改生产订单
     */
    @PutMapping
    public AjaxResult edit(@RequestBody MicroManufactureOrderDto microManufactureOrder)
    {
        return toAjax(iMicroManufactureOrderFacadeService.updateMicroManufactureOrder(microManufactureOrder));
    }

    /**
     * 修改生产订单
     */
    @PutMapping(value = "/operate")
    public AjaxResult operate(@RequestBody MicroManufactureOrderDto microManufactureOrder)
    {
        return toAjax(iMicroManufactureOrderFacadeService.operate(microManufactureOrder));
    }

    /**
     * 调整生产订单
     */
    @PutMapping(value = "/adjust")
    public AjaxResult adjust(@RequestBody MicroManufactureOrderDto microManufactureOrder)
    {
        return iMicroManufactureOrderFacadeService.adjust(microManufactureOrder);
    }

    /**
     * 删除生产订单
     */
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(iMicroManufactureOrderFacadeService.deleteMicroManufactureOrderByIds(ids));
    }

    /**
     * 生产订单预排产页详情信息
     */
    @PostMapping("/productionSchedulingDetails")
    public AjaxResult productionSchedulingDetails(@RequestBody MicroManufactureOrderDto microManufactureOrderDto) {
        if(null==microManufactureOrderDto.getId()){
            return AjaxResult.error("生产订单不能为空!");
        }
        MicroManufactureOrderDto result= iMicroManufactureOrderFacadeService.productionSchedulingDetails(microManufactureOrderDto);
        return AjaxResult.success(result);
    }

    /**
     * 生产订单排产
     */
    @PostMapping("/productionScheduling")
    public AjaxResult productionScheduling(@RequestBody MicroManufactureOrderDto microManufactureOrderDto) {
        iMicroManufactureOrderFacadeService.productionScheduling(microManufactureOrderDto);
        return AjaxResult.success();
    }

    /**
     * 生产订单下发
     *
     * 1. 只有一个工单
     * 2. 该工单有工艺信息
     * 3. 否则，返回false
     *
     * @param orderNo
     * @return
     */
    @GetMapping("/productiveOrderIssued")
    public AjaxResult productiveOrderIssued(@RequestParam String orderNo) {
        return AjaxResult.success(iMicroManufactureOrderFacadeService.productiveOrderIssued(orderNo));
    }

    /**
     * 订单进度追踪
     *
     * @param orderProgressParam
     * @return
     */
    @GetMapping("/orderProgressList")
    public TableDataInfo orderProgressList(OrderProgressParam orderProgressParam) {
        startPage();
        List<OrderProgressDetailInfo> orderProgressList = iMicroManufactureOrderFacadeService.getOrderProgressList(orderProgressParam);
        return getDataTable(orderProgressList);
    }
}
