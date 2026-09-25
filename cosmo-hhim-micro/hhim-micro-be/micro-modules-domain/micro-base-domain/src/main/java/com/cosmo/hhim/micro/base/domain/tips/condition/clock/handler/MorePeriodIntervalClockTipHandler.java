/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.clock.handler;

import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.micro.base.domain.entity.tips.MicroContentTipRecord;
import com.cosmo.hhim.micro.base.domain.entity.tips.TimeTriggerInfo;
import com.cosmo.hhim.micro.infrastructure.annotation.HandlerType;
import com.cosmo.hhim.micro.infrastructure.enums.importexport.HandlerTypeEnum;
import com.cosmo.hhim.micro.infrastructure.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @desc 时间周期内，至少间隔指定时间，可多次提示（eg:一天内可多次提示，间隔时间至少3小时）
 * @createTime 2023-01-31
 */
@Slf4j
@Component
@HandlerType(type = HandlerTypeEnum.CLOCK_TIP_MORE_PERIOD_INTERVAL)
public class MorePeriodIntervalClockTipHandler extends AbstractClockTipHandler {

    @Override
    public boolean isMatchTipCondition(TimeTriggerInfo timeTriggerInfo) {
        Date nowDate = DateUtils.getNowDate();

        // 1.查询限定时间范围上次提示触发时间
        List<MicroContentTipRecord> contentTipRecords = super.getContentTipRecordListByDateRange(timeTriggerInfo);
        if (CollectionUtils.isEmpty(contentTipRecords)) {
            return true;
        }

        Date lastTriggerTipContentRecord = contentTipRecords.stream()
                .max(Comparator.comparing(MicroContentTipRecord::getTipDate))
                .map(MicroContentTipRecord::getTipDate)
                .orElse(null);

        // 2.判断是否满足提示间隔时间要求
        if (null == lastTriggerTipContentRecord) { return true; }
        Date endDate = DateUtil.plus(lastTriggerTipContentRecord, timeTriggerInfo.getIntervalTime(), timeTriggerInfo.getIntervalTimeUnit());
        return nowDate.after(endDate);
    }
}
