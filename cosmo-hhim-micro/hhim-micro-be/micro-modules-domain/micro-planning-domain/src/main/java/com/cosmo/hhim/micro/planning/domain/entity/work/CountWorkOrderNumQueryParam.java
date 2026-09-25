/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.planning.domain.entity.work;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @description: 获取不同状态工单的数量查询参数
 * @date 2023/5/23 17:08
 */
@Data
public class CountWorkOrderNumQueryParam {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    private String workOrderStatus;

    /**
     * 逾期标示
     *
     * 0 - 否
     * 1 - 是
     */
    private String overDateFlag;
}
