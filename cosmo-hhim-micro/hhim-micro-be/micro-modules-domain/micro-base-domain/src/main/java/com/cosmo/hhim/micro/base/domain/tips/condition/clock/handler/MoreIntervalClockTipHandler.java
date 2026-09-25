/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.clock.handler;

import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.micro.base.domain.entity.tips.TimeTriggerInfo;
import com.cosmo.hhim.micro.infrastructure.annotation.HandlerType;
import com.cosmo.hhim.micro.infrastructure.enums.importexport.HandlerTypeEnum;
import com.cosmo.hhim.micro.infrastructure.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @desc 至少间隔指定时间可多次提示（eg:可多次提示，提示间隔时间至少3小时）
 * @createTime 2023-01-31
 */
@Slf4j
@Component
@HandlerType(type = HandlerTypeEnum.CLOCK_TIP_MORE_INTERVAL)
public class MoreIntervalClockTipHandler extends AbstractClockTipHandler {

    @Autowired
    private RedisCache redisCache;

    @Override
    public boolean isMatchTipCondition(TimeTriggerInfo timeTriggerInfo) {
        Date nowDate = DateUtils.getNowDate();

        // 1.获取当前用户上次触发时间
        Date lastTriggerDate = super.getLastTriggerDate(timeTriggerInfo);
        if (null == lastTriggerDate) {
            return true;
        }

        // 2.判断是否满足提示间隔时间要求
        Date endDate = DateUtil.plus(lastTriggerDate, timeTriggerInfo.getIntervalTime(), timeTriggerInfo.getIntervalTimeUnit());
        return nowDate.after(endDate);
    }
}
