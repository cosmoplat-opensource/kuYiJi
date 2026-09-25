/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.storage.domain.entity;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/4/24
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroFinishProductStorageBoundBatchParam {

    // 出入库标识(入库：true，出库：false) 
    @NotNull(message = "出入库标识不允许为空！")
    private Boolean inOrOutFlag;

    // 备注 
    private String reason;

    // 批量信息列表 
    @Valid
    List<MicroFinishProductStorageBoundParam> boundParamList;

}
