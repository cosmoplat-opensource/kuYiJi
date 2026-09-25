/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.integration;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroFileDomain;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroCustomerSuggestionDTO implements Serializable { 
    private String content;
    /**
 * @author cosmo-hhim-open Team
     * 未处理	10
     * 处理中	20
     * 已处理	30
     */
    private String status;
    private List<MicroFileDomain> attachFiles;
    private String contactNumber;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private String dealResult;
}
