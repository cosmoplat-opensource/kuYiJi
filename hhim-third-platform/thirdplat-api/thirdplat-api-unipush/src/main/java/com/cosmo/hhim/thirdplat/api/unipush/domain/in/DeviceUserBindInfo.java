/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-30
 */
@Data
public class DeviceUserBindInfo {

    /**
     * 设备ID
     */
    @NotBlank(message = "设备ID不允许为空")
    private String deviceId;

    /**
     * 用户账号
     */
    @NotBlank(message = "用户账号不允许为空")
    private String userName;

}
