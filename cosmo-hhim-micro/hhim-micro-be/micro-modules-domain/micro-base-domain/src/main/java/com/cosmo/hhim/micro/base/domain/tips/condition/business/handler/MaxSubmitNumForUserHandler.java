/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.business.handler;

import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.submit.ProductiveCapacityByDay;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.AbstractBusinessHandler;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerParam;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 针对员工最大报工数量的handler
 * @date 2023/2/10 14:49
 */
@Component
@Slf4j
public class MaxSubmitNumForUserHandler extends AbstractBusinessHandler {

    private static final int MIN_HISTORY_DAYS = 2;

    @Autowired
    private MicroWorkSubmitMapper microWorkSubmitMapper;

    @Override
    protected BusinessHandlerResult executeHandler(BusinessHandlerParam param) {
        BusinessHandlerResult result = new BusinessHandlerResult();

        // 1. 找到该人每天的报工数(今天之前)并获取最大值
        List<ProductiveCapacityByDay> productiveCapacityByDays = microWorkSubmitMapper.selectUserSubmitNumGroupBySubmitDay(SecurityUtils.getUserId());
        if (productiveCapacityByDays.size() < MIN_HISTORY_DAYS) {
            return result;
        }
        productiveCapacityByDays.sort(Comparator.comparing(ProductiveCapacityByDay::getTotalNum).reversed());

        // 最大值出现在今天的话则设置为true
        if (productiveCapacityByDays.get(0).getSubmitDay().equals(LocalDate.now())) {
            result.setMatched(true);
        }

        return result;
    }
}
