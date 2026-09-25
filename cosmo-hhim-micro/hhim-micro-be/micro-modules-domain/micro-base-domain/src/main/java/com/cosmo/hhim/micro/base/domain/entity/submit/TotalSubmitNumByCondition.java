/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.submit;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @date 2023/3/8 16:52
 */
@Data
public class TotalSubmitNumByCondition extends TotalSubmitNumByUser {

    private String conditionKey;
}
