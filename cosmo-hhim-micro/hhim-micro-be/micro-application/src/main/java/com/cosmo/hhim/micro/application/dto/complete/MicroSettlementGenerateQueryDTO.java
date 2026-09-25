/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.complete;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroSettlementGenerateQueryDTO implements Serializable {
    @NotNull(message = "无法获取时间范围")
    private Date startDate;
    @NotNull(message = "无法获取时间范围")
    private Date endDate;
}
