/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.tech;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 工艺链绑定值对象
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroProcessChainBindEntity implements Serializable {
    private Long id;
    private Long techId;
    private Long productId;
    private String productSeq;
    private String productName;
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
    private Date createdDate;
    private Integer sort;
    /**
     * 存放工艺类型
     */
    private String techType;
    private Integer techPattern;
    private String techName;
}