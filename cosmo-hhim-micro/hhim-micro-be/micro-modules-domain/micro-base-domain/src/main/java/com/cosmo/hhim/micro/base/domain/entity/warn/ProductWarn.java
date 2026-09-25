/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.warn;

import lombok.Data;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 产品预警
 * @date 2022/12/9 14:45
 */
@Data
public class ProductWarn {

    /**
     * 产品序列码
     */
    private String warnProductSeq;

    /**
     * 产品编码
     */
    private String warnProductCode;

    /**
     * 产品名称
     */
    private String warnProductName;

    /**
     * 工序预警List
     */
    private List<ProcessWarn> processWarnList;
}
