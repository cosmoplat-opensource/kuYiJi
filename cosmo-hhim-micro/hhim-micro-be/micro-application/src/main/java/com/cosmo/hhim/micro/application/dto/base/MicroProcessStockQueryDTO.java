/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.base;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 下拉工序附带库存查询实体
 *
 * @author cosmo-hhim-open Team
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroProcessStockQueryDTO implements Serializable {
    @NotNull(message = "无法获取产品信息")
    private String productSeq;
    private List<String> processList;
}
