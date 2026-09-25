/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import lombok.Data;

/**
 * 开票信息对象 hyzz_invoice_info
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class HyzzInvoiceInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long rowId;

    /**
     * 客户编码
     */
    private String cmpCode;

    /**
     * 发票抬头
     */
    private String invoiceTitle;

    /**
     * 税号
     */
    private String taxNumber;

    /**
     * 开户银行
     */
    private String bankName;

    /**
     * 银行账户
     */
    private String bankAccount;

    /**
     * 开户地址
     */
    private String openAddress;

    /**
     * 开户银行
     */
    private String openPhone;

}
