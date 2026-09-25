/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import lombok.Data;

/**
 * 微信与企业用户绑定信息对象 hyzz_wechat_user_bind
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class HyzzWechatUserBind extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 自增ID
     */
    private Long id;

    /**
     * 用户的标识，对当前公众号唯一
     */
    private String openid;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 司机名
     */
    private String driverName;

    /**
     * 用户身份(0：司机，1：供应商，2：仓管员，3：TE货代)
     */
    private String userIdentity;

    /**
     * 所属租户编码
     */
    private String tenantCode;
}
