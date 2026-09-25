/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.tips;

import lombok.Data;

import java.util.List;

@Data
public class MicroContentTipExecuteParam { 

    /**
 * @author cosmo-hhim-open Team
     * 触发动作
     */
    private List<String> triggerActions;

    /**
     * 动作类型
     */
    private String actionType;

}
