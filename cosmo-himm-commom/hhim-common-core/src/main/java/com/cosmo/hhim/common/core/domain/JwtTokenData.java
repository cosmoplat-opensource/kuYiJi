/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.domain;

import com.cosmo.hhim.common.core.constant.enums.CommonConstant;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description JWT token数据信息
 * @createTime 2022-03-08
 */
@Data
public class JwtTokenData {

    // 登录设备类型
    private CommonConstant.DeviceType deviceType;

    // 账号ID
    private String loginId;

    // token有效期（单位：s）
    private long timeout;

    // 所属租户编码
    private String tenantCode;

    // 所属数据源
    private String dataSource;

    // 所属schema
    private String dataSchema;

    // 微信用户OpenId
    private String openid;


}
