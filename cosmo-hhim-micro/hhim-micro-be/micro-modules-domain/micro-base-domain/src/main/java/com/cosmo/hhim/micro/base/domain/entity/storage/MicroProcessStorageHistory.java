/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.storage;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 库存变动历史对象 micro_process_storage_history
 *
 * @date 2022-10-12
 */
@Data
public class MicroProcessStorageHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 产品编码
     */
    private String productCode;
    private String productUnit;

    /**
     * 产品唯一码
     */
    private String productSeq;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 工序唯一码
     */
    private String processSeq;
    /**
     * 工序编码
     */
    private String processCode;

    /**
     * 工序名称
     */
    private String processName;

    /**
     * 操作节点(手动调整、出入库、工序流转)
     */
    private String operateNode;

    /**
     * 良品变化前
     */
    private BigDecimal passFromNum;
    /**
     * 良品变化后
     */
    private BigDecimal passToNum;
    /**
     * 不良品变化前
     */
    private BigDecimal ngFromNum;
    /**
     * 不良品变化后
     */
    private BigDecimal ngToNum;


    private String remark;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdDate;
    private Date startDate;
    private Date endDate;

    /**
     * 最后修改人
     */
    private String lastUpdBy;

    /**
     * 最后修改时间
     */
    private Date lastUpdDate;

}
