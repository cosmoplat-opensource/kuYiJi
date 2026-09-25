/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/25
 */
@Data
public class MicroCompleteReportStatisticsParam {

    // 查询开始时间
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    // 查询结束时间
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    // 来源渠道 
    private String sourceChannel;
    /**
     * 仅获取正常的完工报告数据,不包括已结算的
     */
    private boolean onlyNormal;
    private String productSeq;
}
