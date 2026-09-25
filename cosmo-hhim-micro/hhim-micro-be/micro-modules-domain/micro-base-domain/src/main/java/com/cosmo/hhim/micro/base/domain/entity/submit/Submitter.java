/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.submit;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author cosmo-hhim-open Team
 * @description: 报工人的多次报工时间
 * @date 2022/11/16 3:33 下午
 */
@Data
public class Submitter {

    private String submitUser;

    private String submitNickUser;

    private LocalDate submitDay;

    /**
     * 未报工的天数
     */
    private Long notSubmitDayNums;

    /**
     * 员工维度的日均产能
     */
    private BigDecimal avgDayProductiveCapacity;

    /**
     * 报工记录被修改的次数
     */
    private int modifiedNums;
}
