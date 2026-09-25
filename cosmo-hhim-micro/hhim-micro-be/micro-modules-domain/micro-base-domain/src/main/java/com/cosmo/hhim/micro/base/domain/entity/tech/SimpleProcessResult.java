/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.tech;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description: 存储简单的工序信息结果
 * @date 2023/3/5 17:26
 */
@Data
public class SimpleProcessResult {

    private String processName;

    private String processSeq;

    private String processCode;

    /**
     * 首序标示
     */
    private String isFirstProcess;

    /**
     * 尾序标示
     */
    private String isLastProcess;
}
