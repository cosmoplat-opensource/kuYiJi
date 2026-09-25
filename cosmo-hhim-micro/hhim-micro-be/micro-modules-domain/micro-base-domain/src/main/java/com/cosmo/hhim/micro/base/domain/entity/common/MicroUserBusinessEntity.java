/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

import java.io.Serializable;

/**
 */
@Data
public class MicroUserBusinessEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 产品数量
     */
    private Long productNum;
    /**
     * 员工数量
     */
    private Long staffNum;
    /**
     * 工序数量
     */
    private Long processNum;
}
