/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.business.handler;

import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.core.constant.enums.CommonConstant;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.redis.service.RedisCacheCustomer;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitDto;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.AbstractBusinessHandler;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerParam;
import com.cosmo.hhim.micro.base.domain.tips.condition.business.BusinessHandlerResult;
import com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitStatusEnum;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitTypeEnum;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.cosmo.hhim.micro.infrastructure.constant.ContentTipConstants.KEY_A;
import static com.cosmo.hhim.micro.infrastructure.constant.ContentTipConstants.KEY_B;

/**
 * @author cosmo-hhim-open Team
 * @desc 检查当前员工昨天记工总数是否最多
 * @createTime 2023/2/8
 */
@Slf4j
@Component
public class MaxNumWorkSubmitOfYesterdayHandler extends AbstractBusinessHandler {

    @Autowired
    private MicroWorkSubmitMapper microWorkSubmitMapper;
    @Autowired
    private RedisCacheCustomer redisCacheCustomer;

    @Override
    protected BusinessHandlerResult executeHandler(BusinessHandlerParam param) {
        BusinessHandlerResult result = new BusinessHandlerResult();
        Date nowDate = DateUtils.getNowDate();
        Date yesterday = com.cosmo.hhim.micro.infrastructure.util.DateUtil.plus(nowDate, -1, ChronoUnit.DAYS);

        // 1.普通员工角色判断是否是当天首次进入小程序
        if (!super.isFirstEnterProgramForWorkerRole(param, nowDate, MicroBusinessConstants.WORKER_ROLE)) {
            return result;
        }


        // 2.判断当前用户昨天的记工总数是否最多
        // 按照用户分组统计昨天已审核的记工总数
        MicroWorkSubmitDto queryParam = new MicroWorkSubmitDto();
        queryParam.setStartDate(DateUtil.beginOfDay(yesterday).toJdkDate());
        queryParam.setEndDate(DateUtil.endOfDay(yesterday).toJdkDate());
        queryParam.setSubmitStatus(SubmitStatusEnum.APPROVED.getCode()); // 审核通过 
        String appSign = SecurityUtils.getApplicationSign();
        if (CommonConstant.ApplicationSignEnum.MICRO_PROCESS.getKey().equals(appSign)) {
            queryParam.setSubmitType(SubmitTypeEnum.KU_YI_JI_SUBMIT.getCode());
        } else if (CommonConstant.ApplicationSignEnum.MICRO_PLAN.getKey().equals(appSign)) {
            queryParam.setSubmitType(SubmitTypeEnum.GONG_YI_PAI_SUBMIT.getCode());
        }
        List<Map<String, Object>> groupSumResult = microWorkSubmitMapper.selectWorkSubmitSumGroupByUser(queryParam);
        if (!CollectionUtils.isEmpty(groupSumResult)) {
            // 计算最大的记工数
            BigDecimal maxWorkSubmitNum = groupSumResult.stream().map(e -> (BigDecimal) e.get(KEY_B)).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

            // 将List<Map>转为Map
            Map<String, BigDecimal> map = Maps.newHashMap();
            for (Map<String, Object> stringLongMap : groupSumResult) {
                String key = null;
                BigDecimal value = BigDecimal.ZERO;
                for (Map.Entry<String, Object> entry : stringLongMap.entrySet()) {
                    if (KEY_A.equals(entry.getKey())) {
                        key = (String) entry.getValue();
                    } else if (KEY_B.equals(entry.getKey())) {
                        value = (BigDecimal) entry.getValue();
                    }
                }
                map.put(key, value);
            }

            // 判断当前用户的记工总数是否为最大的记工数
            BigDecimal currentUserYesterdaySubmitTotalNum = map.get(param.getUserId().toString());
            if (null != currentUserYesterdaySubmitTotalNum && currentUserYesterdaySubmitTotalNum.compareTo(maxWorkSubmitNum) == 0) {
                result.setMatched(true);
            }
        }

        return result;
    }
}
