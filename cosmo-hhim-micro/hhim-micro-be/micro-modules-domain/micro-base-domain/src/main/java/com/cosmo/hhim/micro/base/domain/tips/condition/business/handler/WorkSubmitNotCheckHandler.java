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
import com.cosmo.hhim.micro.infrastructure.enums.SubmitStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @desc 检查是否存在未审核记工数据
 * @createTime 2023/2/8
 */
@Slf4j
@Component
public class WorkSubmitNotCheckHandler extends AbstractBusinessHandler {

    @Autowired
    private MicroWorkSubmitMapper microWorkSubmitMapper;

    @Override
    protected BusinessHandlerResult executeHandler(BusinessHandlerParam param) {
        BusinessHandlerResult businessHandlerResult = new BusinessHandlerResult();
        Date nowDate = DateUtils.getNowDate();

        // 查询当前员工今天之前报工未审核的报工记录
        MicroWorkSubmitDto queryParam = new MicroWorkSubmitDto();
        queryParam.setSubmitUser(param.getUserId().toString());
        queryParam.setEndDate(DateUtil.beginOfDay(nowDate));
        queryParam.setSubmitStatus(SubmitStatusEnum.UN_APPROVE.getCode());
        List<MicroWorkSubmit> microWorkSubmits = microWorkSubmitMapper.selectWorkSubmitInfos(queryParam);
        if (microWorkSubmits.size() > 0) {
            businessHandlerResult.setMatched(true);
        }

        return businessHandlerResult;
    }
}
