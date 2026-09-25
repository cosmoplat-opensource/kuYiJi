/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import lombok.Data;

import java.util.List;

/**
 * 应用帮助对象 hyzz_apps_help
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class HyzzUserVerification extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Integer rowId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 手机号
     */
    private String phoneNumber;

    /**
     * 新增或修改标识  0: 新增  1:修改
     */
    private Integer flag;

    /**
     * 用户名集合
     */
    private List<String> userNameList;

    /**
     * 手机号集合
     */
    private List<String> phoneNumberList;

    /**
     * 租户编码
     */
    private String customerCode;
}
