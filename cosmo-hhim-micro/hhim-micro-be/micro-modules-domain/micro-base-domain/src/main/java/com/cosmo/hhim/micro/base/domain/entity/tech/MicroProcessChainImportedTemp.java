/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.tech;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 工艺导入临时对象 micro_process_chain_imported_temp
 *
 * @author cosmo-hhim-open Team
 * @date 2023-05-16
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroProcessChainImportedTemp implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键ID
     */
    private Long id;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 生产模式（SEQUENCE:顺序，PARALLEL:并序）
     */
    private String produceMode;

    /**
     * 工序名称
     */
    private String processName;

    /**
     * 生产顺序
     */
    private Long sort;

    /**
     * 首尾序标识（FIRST_PROCESS:首序，LAST_PROCESS:尾序）
     */
    private String firstOrLastProcess;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;

    /**
     * 更新人
     */
    private String lastUpdBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdDate;

    /**
     * 备注
     */
    private String remark;
}
