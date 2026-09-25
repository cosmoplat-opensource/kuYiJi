/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.planning;

import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.micro.application.dto.planning.DetailMicroManufactureOrderInfo;
import com.cosmo.hhim.micro.application.dto.planning.MicroManufactureOrderDto;
import com.cosmo.hhim.micro.application.dto.planning.OrderProgressDetailInfo;
import com.cosmo.hhim.micro.application.dto.planning.OrderProgressParam;

import java.util.List;

/**
 * 生产订单Service接口
 * 
 * @date 2023-03-06
 */
public interface IMicroManufactureOrderFacadeService
{
    /**
     * 查询生产订单
     * 
     * @param id 生产订单ID
     * @return 生产订单
     */
    MicroManufactureOrderDto selectMicroManufactureOrderById(Long id);

    /**
     * 查询生产订单列表
     * 
     * @param microManufactureOrder 生产订单
     * @return 生产订单集合
     */
    List<MicroManufactureOrderDto> selectMicroManufactureOrderList(MicroManufactureOrderDto microManufactureOrder);

    /**
     * 新增生产订单
     * 
     * @param microManufactureOrders 生产订单
     * @return 结果
     */
    AjaxResult insertMicroManufactureOrder(List<MicroManufactureOrderDto> microManufactureOrders);

    /**
     * 修改生产订单
     * 
     * @param microManufactureOrder 生产订单
     * @return 结果
     */
    int updateMicroManufactureOrder(MicroManufactureOrderDto microManufactureOrder);

    /**
     * 批量删除生产订单
     * 
     * @param ids 需要删除的生产订单ID
     * @return 结果
     */
    int deleteMicroManufactureOrderByIds(Long[] ids);

    /**
     * 生产订单预排产页详情信息
     * @param microManufactureOrderDto 生产订单
     * @return 结果
     */
    MicroManufactureOrderDto productionSchedulingDetails(MicroManufactureOrderDto microManufactureOrderDto);

    /**
     * 生产订单预排产
     * @param microManufactureOrderDto 生产订单
     */
    void productionScheduling(MicroManufactureOrderDto microManufactureOrderDto);

    int operate(MicroManufactureOrderDto microManufactureOrder); 

    List<MicroManufactureOrderDto> selectOrderStatusCount(MicroManufactureOrderDto microManufactureOrder); 

    AjaxResult adjust(MicroManufactureOrderDto microManufactureOrder); 

    /**
     * 生产订单下发
     *
     * @param orderNo
     * @return
     */
    boolean productiveOrderIssued(String orderNo);

    /**
     * 生产订单详情页面 v2
     *
     * @param orderNo
     * @return
     */
    DetailMicroManufactureOrderInfo getDetailInfo(String orderNo);

    /**
     * 订单进度追踪列表
     *
     * @param orderProgressParam
     * @return
     */
    List<OrderProgressDetailInfo> getOrderProgressList(OrderProgressParam orderProgressParam);
}
