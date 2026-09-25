/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.analysis;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @date 2022/10/24 5:09 下午
 * @description 记工排行
 */
@Data
public class SubmitterRank {

    /**
     * 报工人名称
     */
    private String nickName;

    /**
     * 报工人账号
     */
    private String userName;

    /**
     * 报工数量
     */
    private BigDecimal submitNum;

    /**
     * 良品数量
     */
    private BigDecimal passNum;

    /**
     * 良品率
     */
    private BigDecimal passRate;

    /**
     * 单位
     */
    private String unit;

    /**
     * 头像
     */
    private String avatar;
}
