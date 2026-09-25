/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base;


import com.cosmo.hhim.micro.application.dto.base.MicroCustomerDTO;

import java.util.List;

/**
 * 客户基础Service接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
public interface IMicroCustomerFacadeService
{

    /**
     * @author cosmo-hhim-open Team
     * @description 根据条件查询客户信息
     * @date 2023/3/8 10:35
     * @param condition
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.base.MicroCustomerDTO>
     **/
    List<MicroCustomerDTO> selectMicroCustomerList(MicroCustomerDTO condition); 

    /**
     * @author cosmo-hhim-open Team
     * @description 新增客户信息
     * @date 2023/3/8 10:36
     * @param microCustomer
     * @return int
     **/
    int insertMicroCustomer(MicroCustomerDTO microCustomer); 

    /**
     * @author cosmo-hhim-open Team
     * @description 编辑客户信息
     * @date 2023/3/8 10:38
     * @param microCustomer
     * @return int
     **/
    int updateMicroCustomer(MicroCustomerDTO microCustomer); 

    /**
     * @author cosmo-hhim-open Team
     * @description 逻辑删除客户信息
     * @date 2023/3/8 10:39
     * @param ids
     * @return int
     **/
    int deleteMicroCustomerByIds(Long[] ids); 
}
