/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.business.handler;

import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.AbstractBusinessHandler;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerParam;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 当天第一个记工人的判断handler
 * @date 2023/2/10 09:43
 */
@Slf4j
@Component
public class FirstSubmitWorkerInTodayHandler extends AbstractBusinessHandler {

    @Autowired
    private MicroWorkSubmitMapper microWorkSubmitMapper;

    @Override
    protected BusinessHandlerResult executeHandler(BusinessHandlerParam param) {
        BusinessHandlerResult result = new BusinessHandlerResult();

        // 获取当前时间
        MicroWorkSubmit findParam = new MicroWorkSubmit();
        Date submitDay = DateUtil.beginOfDay(DateUtils.getNowDate());
        findParam.setSubmitDay(submitDay);
        List<MicroWorkSubmit> workSubmits = microWorkSubmitMapper.selectMicroWorkSubmitList(findParam);
        // 判断今天是否只有一条报工记录
        if (workSubmits.size() == 1) {
            result.setMatched(true);
        }

        return result;
    }
}
