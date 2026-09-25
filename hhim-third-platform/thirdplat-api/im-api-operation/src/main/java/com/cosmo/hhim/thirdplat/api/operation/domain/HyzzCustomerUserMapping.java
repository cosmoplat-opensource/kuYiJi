/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 用户租户映射对象 hyzz_customer_user_mapping
 *
 * @author cosmo-hhim-open Team
 * @date 2022-12-08
 */
@Data
public class HyzzCustomerUserMapping extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 租户ID
     */
    private Long customerId;

    /**
     * 租户编码
     */
    private String customerCode;

    /**
     * 账号类型（1-企业管理员；2普通账号）
     */
    private String accountType;

    /**
     * 用户有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date validDate;

    /**
     * 扮演用户
     */
    private String playUser;

    /**
     * 客户产品类型(0:MOM,1:微应用)
     */
    private Integer customerProductType;
}
