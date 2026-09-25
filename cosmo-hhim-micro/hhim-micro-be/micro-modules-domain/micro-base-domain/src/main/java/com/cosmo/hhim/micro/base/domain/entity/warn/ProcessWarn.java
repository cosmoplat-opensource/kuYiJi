/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.warn;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description: 工序异常
 * @date 2022/12/9 14:04
 */
@Data
public class ProcessWarn {

    private Long id;

    /**
     * 工序编码
     */
    private String warnProcessCode;

    /**
     * 工序名称
     */
    private String warnProcessName;

    /**
     *
     */
    private String warnProcessSeq;
}
