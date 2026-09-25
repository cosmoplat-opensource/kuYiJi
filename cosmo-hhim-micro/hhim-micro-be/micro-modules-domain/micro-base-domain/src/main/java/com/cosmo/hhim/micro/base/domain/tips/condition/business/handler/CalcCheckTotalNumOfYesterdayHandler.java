/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.business.handler;

import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.redis.service.RedisCacheCustomer;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitDto;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.AbstractBusinessHandler;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerParam;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerResult;
import com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @desc 计算当前员工昨天的审核总数
 * @createTime 2023/2/10
 */
@Slf4j
@Component
public class CalcCheckTotalNumOfYesterdayHandler extends AbstractBusinessHandler {

    @Autowired
    private MicroWorkSubmitMapper microWorkSubmitMapper;
    @Autowired
    private RedisCacheCustomer redisCacheCustomer;

    @Override
    protected BusinessHandlerResult executeHandler(BusinessHandlerParam param) {
        BusinessHandlerResult result = new BusinessHandlerResult();

        Date nowDate = DateUtils.getNowDate();
        Date yesterday = com.cosmo.hhim.micro.infrastructure.util.DateUtil.plus(nowDate, -1, ChronoUnit.DAYS);

        // 1.审核员角色判断是否是当天首次进入小程序
        if (!super.isFirstEnterProgramForWorkerRole(param, nowDate, MicroBusinessConstants.AUDITOR_ROLE)) {
            return result;
        }

        // 2.查询当前员工昨天的审查总数
        MicroWorkSubmitDto queryParam = new MicroWorkSubmitDto();
        queryParam.setCheckUser(param.getUserId().toString());
        queryParam.setStartDate(DateUtil.beginOfDay(yesterday).toJdkDate());
        queryParam.setEndDate(DateUtil.endOfDay(yesterday).toJdkDate());
        queryParam.setSubmitStatus(SubmitStatusEnum.APPROVED.getCode()); // 审核通过 
        List<MicroWorkSubmit> microWorkSubmits = microWorkSubmitMapper.selectWorkSubmitInfos(queryParam);
        if (CollectionUtils.isEmpty(microWorkSubmits)) {
            return result;
        }

        result.setMatched(true);
        List<String> placeHolderInfos = Lists.newArrayList();
        placeHolderInfos.add(String.valueOf(microWorkSubmits.size()));
        result.setPlaceHolderInfos(placeHolderInfos);

        return result;
    }
}
