/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.customer;

import com.cosmo.hhim.micro.base.domain.entity.customer.MicroCustomer;

import java.util.List;

/**
 * 客户基础Service接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
public interface IMicroCustomerService 
{
    /**
     * 查询客户基础
     * 
     * @param id 客户基础ID
     * @return 客户基础
     */
    MicroCustomer selectMicroCustomerById(Long id);

    /**
     * 查询客户基础列表
     * 
     * @param microCustomer 客户基础
     * @return 客户基础集合
     */
    List<MicroCustomer> selectMicroCustomerList(MicroCustomer microCustomer);

    /**
     * 新增客户基础
     * 
     * @param microCustomer 客户基础
     * @return 结果
     */
    int insertMicroCustomer(MicroCustomer microCustomer);

    /**
     * 修改客户基础
     * 
     * @param microCustomer 客户基础
     * @return 结果
     */
    int updateMicroCustomer(MicroCustomer microCustomer);

    /**
     * 批量删除客户基础
     * 
     * @param ids 需要删除的客户基础ID
     * @return 结果
     */
    int deleteMicroCustomerByIds(Long[] ids);

    /**
     * 删除客户基础信息
     * 
     * @param id 客户基础ID
     * @return 结果
     */
    int deleteMicroCustomerById(Long id);

    /**
     * @author cosmo-hhim-open Team
     * @description 校验客户名称唯一性
     * @date 2023/3/24 13:24
     * @param customerName
     * @return boolean
     **/
    boolean isUniqueCustomerName(String customerName); 
}
