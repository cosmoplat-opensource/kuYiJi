/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.storage;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 期初工序库存导入临时对象 micro_process_storage_imported_temp
 *
 * @author cosmo-hhim-open Team
 * @date 2023-04-12
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroProcessStorageImportedTemp implements Serializable {
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
     * 工序编码
     */
    private String processCode;

    /**
     * 工序名称
     */
    private String processName;

    /**
     * 调整后良品数量
     */
    private BigDecimal passNumAdjusted;

    /**
     * 调整后不良品数量
     */
    private BigDecimal ngNumAdjusted;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;

    /**
     * 更新人
     */
    private String lastUpdBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdDate;

    /**
     * 当前良品数量
     */
    private BigDecimal passNum;

    /**
     * 当前不良品数量
     */
    private BigDecimal ngNum;


}
