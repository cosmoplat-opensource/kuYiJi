/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.assembler.base;

import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserIndividuationConfig;
import com.cosmo.hhim.micro.infrastructure.config.MicroStatisticsConfig;
import com.cosmo.hhim.micro.integration.domain.entity.MicroUserEventTrackingEntity;
import com.cosmo.hhim.micro.integration.domain.entity.MicroUserMostUsePageEntity;

import java.util.List;

/**
 * 用户行为埋点转换类
 *
 * @author cosmo-hhim-open Team
 */
public class MicroUserEventTrackingAssembler {

    /**
     * 组装查询最常用的导航页
     *
     * @return
     */
    public static MicroUserMostUsePageEntity assembleRefCondition(MicroStatisticsConfig statisticsConfig, MicroUserIndividuationConfig userConfig) {
        return convertEntity(statisticsConfig.getNavPages(), userConfig);
    }

    /**
     * 组装查询最常用的子页面维度
     *
     * @return
     */
    public static MicroUserMostUsePageEntity assembleCurrentPageCondition(MicroStatisticsConfig statisticsConfig, MicroUserIndividuationConfig userConfig) {
        return convertEntity(statisticsConfig.getCurrentPages(), userConfig);
    }

    private static MicroUserMostUsePageEntity convertEntity(List<String> pageList, MicroUserIndividuationConfig userConfig) {
        long queryBeginTime = 0;
        if (userConfig != null && userConfig.getStatisticsBeginTime() != null) {
            String statisticsBeginTime = DateUtil.format(userConfig.getStatisticsBeginTime(), "yyyyMMdd");
            queryBeginTime = Integer.parseInt(statisticsBeginTime);
        }
        MicroUserMostUsePageEntity mostUsePageEntity = new MicroUserMostUsePageEntity();
        Long userId = SecurityUtils.getUserId();
        MicroUserEventTrackingEntity queryCondition = new MicroUserEventTrackingEntity();
        queryCondition.setEventType("PV");
        queryCondition.setEventSource("PAGE");
        queryCondition.setCreatedDay(queryBeginTime);
        queryCondition.setApplicationSign(SecurityUtils.getApplicationSign());
        mostUsePageEntity.setUserId(userId);
        mostUsePageEntity.setConditionEntity(queryCondition);
        mostUsePageEntity.setPageList(pageList);
        return mostUsePageEntity;
    }

}
