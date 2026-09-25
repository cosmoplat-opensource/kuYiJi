/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.business.handler;

import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitDto;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.AbstractBusinessHandler;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerParam;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @desc 查询昨天是否未记工
 * @createTime 2023/2/8
 */
@Slf4j
@Component
public class YestNotWorkSubmitHandler extends AbstractBusinessHandler {

    @Autowired
    private MicroWorkSubmitMapper microWorkSubmitMapper;

    @Override
    protected BusinessHandlerResult executeHandler(BusinessHandlerParam param) {
        BusinessHandlerResult result = new BusinessHandlerResult();

        Date nowDate = DateUtils.getNowDate();
        Date yesterday = DateUtils.addDays(nowDate, -1);

        // 查询当前员工昨天的记工数据
        MicroWorkSubmitDto queryParam = new MicroWorkSubmitDto();
        queryParam.setSubmitUser(param.getUserId().toString());
        queryParam.setStartDate(DateUtil.beginOfDay(yesterday));
        queryParam.setEndDate(DateUtil.endOfDay(yesterday));
        List<MicroWorkSubmit> microWorkSubmits = microWorkSubmitMapper.selectWorkSubmitInfos(queryParam);
        if (CollectionUtils.isEmpty(microWorkSubmits)) {
            result.setMatched(true);
        }

        return result;
    }
}
