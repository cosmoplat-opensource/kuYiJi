/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.storage.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/25
 */
@Data
public class MicroFinishStorageChangeHistoryParam {

    // 产品唯一码 
    private String productSeq;

    // 查询开始时间 
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    // 查询结束时间 
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    // 变动类型集合 
    private List<String> changeTypeList;
}
