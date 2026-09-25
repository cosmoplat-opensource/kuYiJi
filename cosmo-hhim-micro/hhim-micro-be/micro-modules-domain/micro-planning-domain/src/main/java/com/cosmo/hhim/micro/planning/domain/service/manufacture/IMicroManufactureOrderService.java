/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.planning.domain.service.manufacture;

import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroManufactureOrder;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder;

import java.util.List;

/**
 * 生产订单Service业务层处理
 *
 * @date 2023-03-06
 */
public interface IMicroManufactureOrderService {

    MicroManufactureOrder selectMicroManufactureOrderById(Long id); 

    List<MicroManufactureOrder> selectMicroManufactureOrderList(MicroManufactureOrder microManufactureOrder); 

    int insertMicroManufactureOrder(MicroManufactureOrder manufactureOrder); 

    int updateMicroManufactureOrder(MicroManufactureOrder microManufactureOrder); 

    int deleteMicroManufactureOrderByIds(Long[] ids); 

    int operate(MicroManufactureOrder manufactureOrder,String operateFlag); 

    List<MicroManufactureOrder> selectMicroManufactureOrderListByCondition(MicroManufactureOrder condition); 

    List<MicroManufactureOrder> selectWorkStatusCount(MicroManufactureOrder condition); 

    int updateWithFiledNull(MicroManufactureOrder manufactureOrder); 

    boolean isUniqueOrder(String orderNo); 

    /**
     * 获取生产订单信息
     *
     * @param orderNo
     * @return
     */
    MicroManufactureOrder selectMicroManufactureOrderByOrderNo(String orderNo);
}
