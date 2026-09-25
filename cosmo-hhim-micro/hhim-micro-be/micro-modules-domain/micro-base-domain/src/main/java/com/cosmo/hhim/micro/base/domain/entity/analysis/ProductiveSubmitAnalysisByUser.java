/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.analysis;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 员工维度生产报工分析
 * @date 2023/6/5 13:52
 */
@Data
public class ProductiveSubmitAnalysisByUser {

    /**
     * 员工id
     */
    private Long submitUser;

    /**
     * 员工昵称
     */
    private String submitNickName;

    private String userName;

    /**
     * 报工总数
     */
    private BigDecimal totalNum;
}
