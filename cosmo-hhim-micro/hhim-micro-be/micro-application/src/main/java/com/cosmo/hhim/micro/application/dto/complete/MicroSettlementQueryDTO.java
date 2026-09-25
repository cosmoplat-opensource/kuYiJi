/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.complete;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroSettlementQueryDTO implements Serializable {
    @NotNull(message = "无法获取时间范围")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date searchDate;

    private String productSeq;
    private Long userId;
    private String operateProcessSeq;
    private String receivedBy;
    private String searchKey;
}
