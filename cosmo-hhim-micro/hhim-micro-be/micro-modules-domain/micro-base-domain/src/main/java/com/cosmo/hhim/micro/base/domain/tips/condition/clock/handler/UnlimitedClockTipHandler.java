/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.clock.handler;

import com.cosmo.hhim.micro.base.domain.entity.tips.TimeTriggerInfo;
import com.cosmo.hhim.micro.infrastructure.annotation.HandlerType;
import com.cosmo.hhim.micro.infrastructure.enums.importexport.HandlerTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author cosmo-hhim-open Team
 * @desc 无限制（eg：可多次提示）
 * @createTime 2023-01-31
 */
@Slf4j
@Component
@HandlerType(type = HandlerTypeEnum.CLOCK_TIP_UNLIMITED)
public class UnlimitedClockTipHandler extends AbstractClockTipHandler {

    @Override
    public boolean isMatchTipCondition(TimeTriggerInfo timeTriggerInfo) {
        return true;
    }
}
