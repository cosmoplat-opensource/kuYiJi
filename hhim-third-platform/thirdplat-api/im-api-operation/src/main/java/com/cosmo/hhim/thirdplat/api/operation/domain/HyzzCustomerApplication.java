/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class HyzzCustomerApplication extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * 租户编码
     */
    private String customerCode;

    /**
     * 应用id
     */
    private String appId;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date customerValidDate;

    /**
     * 客户产品类型(0:MOM,1:微应用)
     */
    private Integer customerProductType;

    /**
     * 应用使用状态（0：试用，1：正式使用）
     */
    private String useStatus;
}