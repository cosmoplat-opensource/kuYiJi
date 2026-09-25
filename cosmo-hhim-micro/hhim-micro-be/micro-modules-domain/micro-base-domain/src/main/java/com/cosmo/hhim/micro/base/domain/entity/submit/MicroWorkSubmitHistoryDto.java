/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.submit;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @date 2022/12/1 10:42 上午
 */
@Data
public class MicroWorkSubmitHistoryDto extends MicroWorkSubmitHistory {

    /**
     * 操作员名称
     */
    private String operatorName;
}
