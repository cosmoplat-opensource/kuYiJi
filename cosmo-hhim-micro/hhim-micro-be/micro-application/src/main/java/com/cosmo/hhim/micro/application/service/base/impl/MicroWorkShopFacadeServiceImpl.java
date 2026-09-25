/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base.impl;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.assembler.base.MicroWorkShopAssembler;
import com.cosmo.hhim.micro.application.dto.base.MicroWorkShopDTO;
import com.cosmo.hhim.micro.application.service.base.IMicroWorkShopFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.factory.MicroWorkShop;
import com.cosmo.hhim.micro.base.domain.service.factory.IMicroWorkShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 生产车间Service业务层处理
 * 
 * @author cosmo-hhim-open Team
 */
@Service
public class MicroWorkShopFacadeServiceImpl implements IMicroWorkShopFacadeService
{

    @Autowired
    private IMicroWorkShopService microWorkshopService;

    @Override
    public MicroWorkShopDTO selectMicroWorkshopById(Long id) {
        MicroWorkShop workShop =  microWorkshopService.selectMicroWorkshopById(id);
        return Objects.nonNull(workShop)? MicroWorkShopAssembler.convertDomain2Dto(workShop):null;
    }

    /**
     * @author cosmo-hhim-open Team
     * @param condition
     * @return java.util.List<com.cosmo.hhim.micro.application.dto.base.MicroWorkShopDTO>
     **/
    @Override
    public List<MicroWorkShopDTO> selectMicroWorkshopList(MicroWorkShopDTO condition) {
        MicroWorkShop queryParams = MicroWorkShopAssembler.convert2Condition(condition);
        List<MicroWorkShop> workShops = microWorkshopService.selectMicroWorkshopList(queryParams,condition.isLazy());
        return MicroWorkShopAssembler.convertDomains2DTOs4Page(workShops,condition.isLazy());
    }

    /**
     * @author cosmo-hhim-open Team
     * @param microWorkshop
     * @return int
     **/
    @Override
    public int insertMicroWorkshop(MicroWorkShopDTO microWorkshop) {
        Long userId = SecurityUtils.getUserId();
        String tenantCode = ThreadContext.get(Constants.TARGET_CUSTOMER).toString();
        MicroWorkShop workShop = MicroWorkShopAssembler.convertDto2Domain(microWorkshop,userId,tenantCode);
        return microWorkshopService.insertMicroWorkshop(workShop);
    }

    /**
     * @author cosmo-hhim-open Team
     * @param microWorkshop
     * @return int
     **/
    @Override
    public int updateMicroWorkshop(MicroWorkShopDTO microWorkshop) {
        Long userId = SecurityUtils.getUserId();
        MicroWorkShop workShop = MicroWorkShopAssembler.convertDto2Domain4Update(microWorkshop,userId);
        workShop.setLastUpdBy(String.valueOf(userId));
        return microWorkshopService.updateMicroWorkshop(workShop);
    }

    /**
     * @author cosmo-hhim-open Team
     * @param ids
     * @return int
     **/
    @Override
    public int deleteMicroWorkshopByIds(Long[] ids) {
        return microWorkshopService.deleteMicroWorkshopByIds(ids);
    }
}
