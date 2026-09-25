/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.ng.domain.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @description: 不良类型统计查询参数
 * @date 2023/4/11 18:12
 */
@Data
public class NgTypeStatisticQueryParam {

    private String productNameOrCode;

    private Date startDate;

    private Date endDate;
}
