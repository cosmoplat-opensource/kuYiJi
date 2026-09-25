/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-04-24
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WechatUserBindUpdateParam extends HyzzWechatUserBind {
    /**
     * 用户的标识（更新条件）
     */
    private String openidCondition;

    /**
     * 用户名(更新条件)
     */
    private String userNameCondition;

    /**
     * 用户身份(0：司机，1：供应商，2：仓管员，3：TE货代)(更新条件)
     */
    private String userIdentityCondition;

    /**
     * 所属租户编码(更新条件)
     */
    private String tenantCodeCondition;
}
