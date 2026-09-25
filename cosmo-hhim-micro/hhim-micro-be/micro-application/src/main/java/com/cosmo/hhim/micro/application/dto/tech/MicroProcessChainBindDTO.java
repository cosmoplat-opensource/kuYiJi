/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.tech;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 工艺链绑定值对象
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroProcessChainBindDTO implements Serializable {
    private Long id;
    private Long techId;
    private Long processId;
    /**
     * 父工序ID
     */
    private Long parentProcessId;
    private String processSeq;
    private String processCode;
    private String parentProcessSeq;
    private String processName;
    private String isLastProcess;
    private Integer sort;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdDate;
}