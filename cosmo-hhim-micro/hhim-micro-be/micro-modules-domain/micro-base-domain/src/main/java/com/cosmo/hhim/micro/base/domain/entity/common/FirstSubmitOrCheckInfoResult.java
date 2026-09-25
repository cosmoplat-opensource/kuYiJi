/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-11-02
 */
@Data
public class FirstSubmitOrCheckInfoResult {

    // 操作人 
    private String operator;

    // 操作时间 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date operateDate;

    // 产品名称 
    private String productName;

    // 产品唯一码 
    private String productSeq;

    // 工序名称 
    private String processName;

    // 工序唯一码 
    private String processSeq;

}
