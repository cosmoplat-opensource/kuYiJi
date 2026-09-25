/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.ng.domain.entity;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description: 获取不良品类型的参数
 * @date 2023/4/4 10:48
 */
@Data
public class NgTypeQueryParam {

    /**
     * 查询的key值
     */
    private String key;

    /**
     * 为了获取一类产品的不良品类型做扩展
     */
    private String productSeq;
}
