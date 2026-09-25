/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.analysis;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 分析页面 - 记工数量跳转的实体对象
 * @date 2023/2/21 15:16
 */
@Data
public class RecordForWorker {

    /**
     * 昵称
     */
    private String nickName;

    private String userName;

    /**
     * 总的报工数量
     */
    private BigDecimal totalSubmitNum;
}
