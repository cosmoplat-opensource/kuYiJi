/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户建议对象 micro_suggestion
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroSuggestion implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 提交时间
     */
    private Date createdAt;
}
