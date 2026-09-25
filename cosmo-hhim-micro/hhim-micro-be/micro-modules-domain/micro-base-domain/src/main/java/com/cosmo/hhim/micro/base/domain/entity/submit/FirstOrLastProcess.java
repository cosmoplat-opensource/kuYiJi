/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.submit;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description: 获取产品 + 工序的 isLastProcess 和 isFirstProcess, 原则上不能同时为0
 * @date 2022/12/20 16:28
 */
@Data
public class FirstOrLastProcess {

    /**
     * 是否为首序
     */
    private String isFirstProcess;

    /**
     * 是否为尾序
     */
    private String isLastProcess;
}
