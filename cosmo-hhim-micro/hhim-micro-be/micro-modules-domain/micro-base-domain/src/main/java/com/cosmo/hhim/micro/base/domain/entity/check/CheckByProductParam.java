/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.check;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description: 产品维度审核请求入参类
 * @date 2023/2/8 15:05
 */
@Data
public class CheckByProductParam {

    /**
     * 审核状态
     */
    private Long submitStatus;

    /**
     * 产品模糊搜索参数
     */
    private String productNameOrCode;

    /**
     * 产品序列码
     */
    private String productSeq;

    /**
     * 备注
     */
    private String remark;
}