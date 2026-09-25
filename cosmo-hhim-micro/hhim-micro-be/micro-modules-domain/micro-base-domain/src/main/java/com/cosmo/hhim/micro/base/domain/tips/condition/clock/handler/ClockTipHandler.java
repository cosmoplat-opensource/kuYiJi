/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.clock.handler;

import com.cosmo.hhim.micro.base.domain.entity.tips.TimeTriggerInfo;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-01-31
 */
public interface ClockTipHandler {

    /**
     * 判定是否满足时间触发条件
     *
     * @param timeTriggerInfo
     * @return
     */
    boolean isMatchTipCondition(TimeTriggerInfo timeTriggerInfo);

}
