/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.ng;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 员工维度不良品展示实体
 * @date 2023/4/5 10:10
 */
@Data
public class NgProductByUserFromAlreadyCheck {

    private Long submitUser;

    private String submitNickName;

    /**
     * 到人维度的不良品数量
     */
    private BigDecimal totalNgNum;

    /**
     * 人维度下的详细产品工序信息
     */
    private List<NgProductAndProcessByUserFromAlreadyCheck> ngProductAndProcessByUserFromAlreadyCheckList;
}
