/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotBlank;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-11-01
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GuideNodeChangeStatusParam {

    // 节点编码 
    @NotBlank(message = "节点编码不允许为空！")
    private String nodeCode;

    // 节点状态（0：未完成，1：已完成） 
    @NotBlank(message = "节点状态不允许为空！")
    private String nodeStatus;

    // 节点操作耗时 
    private String elapsedTime;

}
