/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.planning;

import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.micro.application.dto.planning.DetailManufactureWorkOrderInfo;
import com.cosmo.hhim.micro.application.dto.planning.MicroManufactureWorkOrderDto;
import com.cosmo.hhim.micro.application.dto.planning.SimpleWorkOrderInfo;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder;

import java.util.Date;
import java.util.List;

/**
 * 生产工单Service接口
 * 
 * @date 2023-03-06
 */
public interface IMicroManufactureWorkOrderFacadeService
{
    /**
     * 查询生产工单
     * 
     * @param id 生产工单ID
     * @return 生产工单
     */
    MicroManufactureWorkOrderDto selectMicroManufactureWorkOrderById(Long id);

    /**
     * 查询生产工单列表
     * 
     * @param condition 筛选条件
     * @return 生产工单集合
     */
    List<MicroManufactureWorkOrderDto> selectMicroManufactureWorkOrderList(MicroManufactureWorkOrderDto condition);

    /**
     * 新增生产工单
     * 
     * @param microManufactureWorkOrder 生产工单
     * @return 结果
     */
    AjaxResult insertMicroManufactureWorkOrder(MicroManufactureWorkOrderDto microManufactureWorkOrder);

    /**
     * 修改生产工单
     * 
     * @param microManufactureWorkOrder 生产工单
     * @return 结果
     */
    int updateMicroManufactureWorkOrder(MicroManufactureWorkOrderDto microManufactureWorkOrder);

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

    /**
     * 获取生产工单开工下发页信息
     * @param id
     * @return
     */
    MicroManufactureWorkOrderDto getIssuedInfo(Long id);

    /**
     * 生产工单开工下发
     */
    void workOrderIssued(MicroManufactureWorkOrderDto microManufactureWorkOrderDto); 

    /**
     * 新增投料单/退料单
     *
     * @param microManufactureWorkOrderDto 工单
     * @return 结果
     */
    void feedingOrReturn(MicroManufactureWorkOrderDto microManufactureWorkOrderDto);

    /**
     * @author cosmo-hhim-open Team
     * @description 操作工单
     * @date 2023/3/13 14:54
     * @param microManufactureWorkOrder
     * @return result > 0 代表操作数据成功
     **/
    int operate(MicroManufactureWorkOrderDto microManufactureWorkOrder);  

    /**
     * @author cosmo-hhim-open Team
     * @description 统计各状态下，工单的数量
     * @date 2023/3/14 16:25
     * @param microManufactureWorkOrder
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.planning.MicroManufactureWorkOrderDto>
     **/
    List<MicroManufactureWorkOrderDto> selectWorkStatusCount(MicroManufactureWorkOrderDto microManufactureWorkOrder);  

    /**
     * 获取生产任务列表-app
     * @param microManufactureWorkOrderDto
     * @return
     */
    List<MicroManufactureWorkOrderDto> getMicroManufactureTaskList(MicroManufactureWorkOrderDto microManufactureWorkOrderDto);

    /**
     * 查询生产任务列表数量-app
     * @param microManufactureWorkOrderDto
     * @return
     */
    List<MicroManufactureWorkOrderDto> getStatusCount(MicroManufactureWorkOrderDto microManufactureWorkOrderDto);

    /**
     * 获取生产报工页面
     */
    MicroManufactureWorkOrderDto getSubmitInfo(Long id);  

    /**
     * @author cosmo-hhim-open Team
     * @description 查询订单下主工单进度列表
     * @date 2023/3/21 10:30
     * @param microManufactureWorkOrder
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.planning.MicroManufactureWorkOrderDto>
     **/
    List<MicroManufactureWorkOrderDto> selectWorkOrderProgressList(MicroManufactureWorkOrderDto microManufactureWorkOrder);  

    /**
     * 获取工单详情信息 - 带有生产任务进度信息
     *
     * @param workOrderNo
     * @return
     */
    DetailManufactureWorkOrderInfo selectDetailManufactureWorkOrderInfo(String workOrderNo);

    /**
     * 获取到人的工单列表信息
     *
     * @param startDate
     * @param endDate
     * @return
     */
    List<SimpleWorkOrderInfo> selectSimpleWorkOrderInfoListByUserAndDay(Date startDate, Date endDate);
}
