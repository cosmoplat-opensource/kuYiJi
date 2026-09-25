/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base.impl;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.assembler.base.MicroCustomerAssembler;
import com.cosmo.hhim.micro.application.dto.base.MicroCustomerDTO;
import com.cosmo.hhim.micro.application.service.base.IMicroCustomerFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.customer.MicroCustomer;
import com.cosmo.hhim.micro.base.domain.service.customer.IMicroCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 客户基础Service业务层处理
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
@Service
public class MicroCustomerFacadeServiceImpl implements IMicroCustomerFacadeService
{
    @Autowired
    private IMicroCustomerService microCustomerService;

    @Override
    public List<MicroCustomerDTO> selectMicroCustomerList(MicroCustomerDTO condition) {
        MicroCustomer params = MicroCustomerAssembler.convert2Condition(condition);
        List<MicroCustomer> customers = microCustomerService.selectMicroCustomerList(params);
        return MicroCustomerAssembler.convertDomains2DTOs4Page(customers);
    }

    @Override
    public int insertMicroCustomer(MicroCustomerDTO microCustomer) {
        if (!microCustomerService.isUniqueCustomerName(microCustomer.getCustomerName())){
            throw new CustomException("客户名称已存在");
        }
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        return microCustomerService.insertMicroCustomer(MicroCustomerAssembler.convertDto2Domain4Insert(microCustomer,SecurityUtils.getUserId(),tenantCode));
    }

    @Override
    public int updateMicroCustomer(MicroCustomerDTO microCustomer) {
        return microCustomerService.updateMicroCustomer(MicroCustomerAssembler.convertDto2Domain4Update(microCustomer,SecurityUtils.getUserId()));
    }

    @Override
    public int deleteMicroCustomerByIds(Long[] ids) {
        return microCustomerService.deleteMicroCustomerByIds(ids);
    }
}
