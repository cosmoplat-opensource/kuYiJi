/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.storage.domain.entity;

import com.cosmo.hhim.common.core.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 成品库存变更历史对象 micro_finished_product_storage_history
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-23
 */
@Data
public class MicroFinishedProductStorageHistory implements Serializable {
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
     * 产品唯一码
     */
    private String productSeq;

    // 产品编码 
    @Excel(name = "产品编码", sort = 3)
    private String productCode;

    /**
     * 产品名称
     */
    @Excel(name = "产品名称", sort = 2)
    private String productName;

    /**
     * 变更类型（10:完工入库，20:完工撤销，30:库存变动，40:手动出库，50:库存导入，60:手动入库，70:生产投料，80:生产退料）
     */
    @Excel(name = "变动类型", sort = 4, readConverterExp = "10=完工入库,20=完工撤销,30=库存变动,40=手动出库,50=库存导入,60=手动入库,70=生产投料,80=生产退料")
    private String changeType;

    /**
     * 变更数量
     */
    @Excel(name = "变动数量", sort = 6)
    private BigDecimal changeNum;

    /**
     * 变更后库存数量
     */
    @Excel(name = "变更后数量", sort = 7)
    private BigDecimal finishChangeNum;

    /**
     * 变更时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "操作时间", sort = 1, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date changeTime;

    /**
     * 变更人ID
     */
    private Long changeUser;

    @Excel(name = "操作人", sort = 5)
    private String changeUserNickName;

    /**
     * 变更原因
     */
    @Excel(name = "变更原因", sort = 8)
    private String changeReason;

    private String remark;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdDate;

    /**
     * 更新人
     */
    private String lastUpdBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastUpdDate;

    // 变更日期（格式：yyyy-MM-dd） 
    private String changeDate;
}

