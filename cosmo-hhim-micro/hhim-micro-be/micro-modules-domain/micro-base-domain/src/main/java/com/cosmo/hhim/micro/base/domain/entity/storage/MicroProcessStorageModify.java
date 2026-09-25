/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.storage;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 库存对象 micro_process_storage
 *
 * @date 2022-10-12
 */
@Data
public class MicroProcessStorageModify implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 产品唯一码
     */
    private String productSeq;

    /**
     * 产品名称
     */
    @NotEmpty(message = "产品名称不能为空")
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
    @NotEmpty(message = "工序名称不能为空")
    private String processName;

    /**
     * 操作节点(手动调整、出入库、工序流转)
     */
    private String operateNode;
    /**
     * 备注
     */
    private String remark;

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

}
