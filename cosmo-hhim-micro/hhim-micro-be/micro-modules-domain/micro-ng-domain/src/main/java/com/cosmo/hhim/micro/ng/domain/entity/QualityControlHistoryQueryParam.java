/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.ng.domain.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @description: 质检历史记录查询参数
 * @date 2023/4/10 17:03
 */
@Data
public class QualityControlHistoryQueryParam {

    /**
     * 查询key
     *
     * 产品编码、名称
     * 工序编码、名称
     */
    private String queryKey;

    private Date startDate;

    private Date endDate;
}
