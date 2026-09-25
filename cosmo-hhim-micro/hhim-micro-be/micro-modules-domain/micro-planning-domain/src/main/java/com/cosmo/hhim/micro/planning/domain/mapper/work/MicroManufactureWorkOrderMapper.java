/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.planning.domain.mapper.work;

import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import com.cosmo.hhim.micro.planning.domain.entity.work.CountWorkOrderNumQueryParam;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrderMaterialEntity;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrderParam;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 生产工单Mapper接口
 * 
 * @date 2023-03-06
 */
public interface MicroManufactureWorkOrderMapper 
{
    /**
     * 查询生产工单
     * 
     * @param id 生产工单ID
     * @return 生产工单
     */
    MicroManufactureWorkOrder selectMicroManufactureWorkOrderById(Long id);

    /**
     * 查询生产工单
     *
     * @param workOrderNo 工单号
     * @return 生产工单
     */
    MicroManufactureWorkOrder selectMicroManufactureWorkOrderByWorkOrderNo(String workOrderNo);

    /**
     * 查询生产工单
     *
     * @param workOrderNoList 生产工单号
     * @return 生产工单
     */
    List<MicroManufactureWorkOrder> getOrderNoByNo(List<String> workOrderNoList);

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
     * 删除生产工单
     * 
     * @param id 生产工单ID
     * @return 结果
     */
    int deleteMicroManufactureWorkOrderById(Long id);

    /**
     * 批量删除生产工单
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteMicroManufactureWorkOrderByIds(Long[] ids);

    /**
     * @author cosmo-hhim-open Team
     * @description 批量修改工单
     * @date 2023/3/13 13:35
     * @param workOrderList
     * @return int
     **/
    int batchUpdateManufactureWorkOrder(List<MicroManufactureWorkOrder> workOrderList); 

    /**
     * @author cosmo-hhim-open Team
     * @date 2023/3/28 15:01
     * @param workOrder
     * @return int
     **/
    int updateMicroManufactureWorkOrder4Status(MicroManufactureWorkOrder workOrder); 

    /**
     * @author cosmo-hhim-open Team
     * @description 统计工单状态的工单数量
     * @date 2023/3/14 16:40
     * @param condition
     * @return int
     **/
    int countWorkOrderByCondition(MicroManufactureWorkOrder condition); 

    /**
     * @description 统计工单状态的工单数量-任务单列表
     **/
    int getStatusCountByTask(MicroManufactureWorkOrder condition); 

    /**
     * @author cosmo-hhim-open Team
     * @description 条件查询工单列表
     * @date 2023/3/16 9:37
     * @param condition
     * @return java.util.List<com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder>
     **/
    List<MicroManufactureWorkOrder> selectMicroManufactureWorkOrderListByCondition(MicroManufactureWorkOrder condition); 

    /**
     * 获取生产任务列表-app
     * @param microManufactureWorkOrder
     * @return
     */
    List<MicroManufactureWorkOrder> getMicroManufactureTaskList(MicroManufactureWorkOrder microManufactureWorkOrder);

    /**
     * 增加工单完成数与不良数
     * @param userId
     * @param workOrderList
     */
    void addWorkOrderFinishNum(@Param("userId") String userId, @Param("workOrderList") List<MicroManufactureWorkOrder> workOrderList);

    /**
     * @author cosmo-hhim-open Team
     * @description
     * @date 2023/3/21 11:03
     * @param condition
     * @return java.util.List<com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder>
     **/
    List<MicroManufactureWorkOrder> selectWorkOrderListWithCustomerAndProduct(MicroManufactureWorkOrder condition); 

    /**
     * 根据工单号查询工单相关信息-报工记录审产
     * @param workOrderNoList
     * @return
     */
    List<MicroManufactureWorkOrder> getWorkOrderInfoBySubmit(List<String> workOrderNoList);

    /**
     * @author cosmo-hhim-open Team
     * @description 查询未完成（已完工、已关单除外）的工单
     * @date 2023/3/29 10:35
     * @param param
     * @return java.util.List<com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder>
     **/
    List<MicroManufactureWorkOrder> selectMicroManufactureWorkOrderList4NotComplete(MicroManufactureWorkOrder param); 

    /**
     * 查询已完成+已关单但是未完工状态的工单信息列表
     * @param param
     * @return
     */
    List<MicroManufactureWorkOrder> selectNotCompleteMicroManufactureWorkOrderList(MicroManufactureWorkOrderParam param);

    /**
     * 更新完工标识
     * @param ids
     * @param workOrder
     */
    void updateCompleteFlag(@Param("ids") List<Long> ids, @Param("workOrder") MicroManufactureWorkOrder workOrder);

    /**
     * 根据工单ID查询工单信息
     * @param workOrderId
     * @return
     */
    MicroManufactureWorkOrder selectNormalMicroManufactureWorkOrderById(Long workOrderId);

    /**
     * 多状态的工单信息查询
     *
     * @param microManufactureWorkOrder
     * @return
     */
    List<MicroManufactureWorkOrder> selectMicroManufactureWorkOrderListByWorkOrderStatusList(MicroManufactureWorkOrder microManufactureWorkOrder);

    /**
     * 获取不同状态的工单数量
     *
     * @param countWorkOrderNumQueryParam
     * @return
     */
    Long countWorkOrderNumByCondition(CountWorkOrderNumQueryParam countWorkOrderNumQueryParam);

    /**
     * 取工单所需数量+工单下BOM的产品信息
     *
     * @param condition
     * @return
     */
    List<MicroManufactureWorkOrderMaterialEntity> selectMaterialDemandInfoByStandardBom(@Param("condition") MicroManufactureWorkOrder condition);

    /**
     * 取工单所需数量+工单的产品信息
     *
     * @param condition
     * @return
     */
    List<MicroManufactureWorkOrderMaterialEntity> selectMaterialShortageDetailByStandardBom(@Param("condition") MicroManufactureWorkOrder condition);

    /**
     * 获取所有已经存在的工单号信息
     *
     * @return
     */
    List<String> getAllExistWorkOrderNoListInCurrentDate();

    /**
     * 根据产品ids查询关联的产品数据
     *
     * @param ids
     * @return
     */
    List<MicroSelectEntity> selectReProductByProductIds(Long[] ids);

    /**
     * 查询生产订单的主工单信息
     *
     * @param orderNoList
     * @return
     */
    List<MicroManufactureWorkOrder> selectMicroMainManufactureWorkOrderByOrderNoList(@Param("orderNoList") List<String> orderNoList);

    /**
     * 获取到人的工单列表信息
     *
     * @param workOrderList
     * @return
     */
    List<MicroManufactureWorkOrder> selectSimpleWorkOrderInfoListByWorkOrderList(@Param("workOrderList") List<String> workOrderList);
}
