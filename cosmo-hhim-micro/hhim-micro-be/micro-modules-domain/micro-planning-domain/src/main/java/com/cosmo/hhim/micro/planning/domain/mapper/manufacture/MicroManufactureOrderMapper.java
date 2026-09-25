/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.planning.domain.mapper.manufacture;

import com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroManufactureOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 生产订单Mapper接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-06
 */
public interface MicroManufactureOrderMapper 
{
    /**
     * 查询生产订单
     * 
     * @param id 生产订单ID
     * @return 生产订单
     */
    MicroManufactureOrder selectMicroManufactureOrderById(Long id);

    /**
     * 查询生产订单
     *
     * @param orderNo
     * @return
     */
    MicroManufactureOrder selectMicroManufactureOrderByOrderNo(String orderNo);

    /**
     * 查询生产订单
     *
     * @param id 生产订单ID
     * @return 生产订单
     */
    MicroManufactureOrder productionSchedulingDetails(Long id);

    /**
     * 查询生产订单列表
     * 
     * @param microManufactureOrder 生产订单
     * @return 生产订单集合
     */
    List<MicroManufactureOrder> selectMicroManufactureOrderList(MicroManufactureOrder microManufactureOrder);

    /**
     * 新增生产订单
     * 
     * @param microManufactureOrder 生产订单
     * @return 结果
     */
    int insertMicroManufactureOrder(MicroManufactureOrder microManufactureOrder);

    /**
     * 修改生产订单
     * 
     * @param microManufactureOrder 生产订单
     * @return 结果
     */
    int updateMicroManufactureOrder(MicroManufactureOrder microManufactureOrder);


    /**
     * 修改生产订单-根据订单号
     *
     * @param microManufactureOrder 生产订单
     * @return 结果
     */
    int updateOrderByNo(MicroManufactureOrder microManufactureOrder);

    /**
     * 删除生产订单
     * 
     * @param id 生产订单ID
     * @return 结果
     */
    int deleteMicroManufactureOrderById(Long id);

    /**
     * 批量删除生产订单
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteMicroManufactureOrderByIds(Long[] ids);

    /**
     * @author cosmo-hhim-open Team
     * @description 更新订单状态
     * @date 2023/3/29 10:59
     * @param manufactureOrder
     * @return int
     **/
    int updateMicroManufactureOrder4Status(MicroManufactureOrder manufactureOrder); 

    /**
     * @author cosmo-hhim-open Team
     * @description 查询生产订单 (关联查询base领域数据)
     * @date 2023/3/15 15:37
     * @param condition
     * @return java.util.List<com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroManufactureOrder>
     **/
    List<MicroManufactureOrder> selectMicroManufactureOrderListByCondition(MicroManufactureOrder condition); 

    int countOrderByCondition(MicroManufactureOrder condition); 

    /**
     * 增加订单完成数与不良数
     * @param userId
     * @param orderList
     */
    void addOrderFinishNum(@Param("userId") String userId, @Param("orderList") List<MicroManufactureOrder> orderList);

    /**
     * @author cosmo-hhim-open Team
     * @description 更新
     * @date 2023/3/24 9:50
     * @param manufactureOrder
     * @return int
     **/
    int updateWithFiledNull(MicroManufactureOrder manufactureOrder); 

    /**
     * 获取所有已经存在的订单号
     * @return
     */
    List<String> getAllExistOrderNoListInCurrentDate();
}
