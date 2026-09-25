/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.planning.domain.service.work;

import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrderMaterialEntity;

import java.util.Date;
import java.util.List;

/**
 * 生产工单Service接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-06
 */
public interface IMicroManufactureWorkOrderService 
{
    /**
     * 查询生产工单
     * 
     * @param id 生产工单ID
     * @return 生产工单
     */
    MicroManufactureWorkOrder selectMicroManufactureWorkOrderById(Long id);

    /**
     * 查询生产工单列表
     * 
     * @param microManufactureWorkOrder 生产工单
     * @return 生产工单集合
     */
    List<MicroManufactureWorkOrder> selectMicroManufactureWorkOrderList(MicroManufactureWorkOrder microManufactureWorkOrder);

    /**
     * 新增生产工单
     * 
     * @param microManufactureWorkOrder 生产工单
     * @return 结果
     */
    int insertMicroManufactureWorkOrder(MicroManufactureWorkOrder microManufactureWorkOrder);

    /**
     * 修改生产工单
     * 
     * @param microManufactureWorkOrder 生产工单
     * @return 结果
     */
    int updateMicroManufactureWorkOrder(MicroManufactureWorkOrder microManufactureWorkOrder);

    /**
     * 批量删除生产工单
     * 
     * @param ids 需要删除的生产工单ID
     * @return 结果
     */
    int deleteMicroManufactureWorkOrderByIds(Long[] ids);

    /**
     * 删除生产工单信息
     * 
     * @param id 生产工单ID
     * @return 结果
     */
    int deleteMicroManufactureWorkOrderById(Long id);

    int operate(MicroManufactureWorkOrder workOrder, String operateFlag); 

    boolean isUniqueWorkOrderNo(String workOrderNo); 

    List<MicroManufactureWorkOrder> selectWorkStatusCount(MicroManufactureWorkOrder condition); 

    List<MicroManufactureWorkOrder> selectMicroManufactureWorkOrderListByCondition(MicroManufactureWorkOrder condition); 

    List<MicroManufactureWorkOrder> getStatusCount(MicroManufactureWorkOrder condition); 

    int saveWorkOrders(List<MicroManufactureWorkOrder> workOrderList); 

    int updateWorkOrders(List<MicroManufactureWorkOrder> workOrderList); 

    List<MicroManufactureWorkOrder> selectWorkOrderProgressList(MicroManufactureWorkOrder condition); 

    /**
     * 根据工单号查询工单相关信息-报工记录审产
     *
     * @param workOrderNoList
     * @return
     */
    List<MicroManufactureWorkOrder> getWorkOrderInfoBySubmit(List<String> workOrderNoList);

    /**
     * 根据条件获取范围内物料缺料信息
     *
     * @param condition
     * @return
     */
    List<MicroManufactureWorkOrderMaterialEntity> selectWorkOrderShortageMaterialByStandardBom(MicroManufactureWorkOrder condition);

    /**
     * 根据条件范围获取物料缺料工单信息
     *
     * @param condition
     * @return
     */
    List<MicroManufactureWorkOrderMaterialEntity> selectShortageDetailList(MicroManufactureWorkOrder condition);
}
