/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.material;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 物料需求分析查询传输实体
 *
 * @author cosmo-hhim-open Team
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroMaterialAnalysisQueryDTO implements Serializable {
    /**
     * 需求开始日期
     */
    @NotNull(message = "无法获取查询时间范围")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date planStartDate;
    /**
     * 需求结束日期
     */
    @NotNull(message = "无法获取查询时间范围")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date planEndDate;
    /**
     * 详情时使用
     */
    private String productSeq;

}

