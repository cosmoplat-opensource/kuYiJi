/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.tips;

import lombok.Data;

import java.time.temporal.ChronoUnit;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-01-31
 */
@Data
public class TimeTriggerInfo {

    /**
     * 提示配置ID
     */
    private Long tipConfigId;

    /**
     * 周期类型
     * 具体类型参考：@see ClockTipHandlerTypeEnum
     */
    private String periodType;

    /**
     * 时间周期
     */
    private Long period;

    /**
     * 时间周期单位
     */
    private ChronoUnit periodTimeUnit;

    /**
     * 间隔时间
     */
    private Long intervalTime;

    /**
     * 间隔时间单位
     */
    private ChronoUnit intervalTimeUnit;

    /**
     * 最高触发次数
     */
    private Long triggerMaxNum;

}
