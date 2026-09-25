/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.out;

import lombok.Data;

import java.io.Serializable;

/**
 * 设备与用户绑定关系对象 hyzz_device_user_bind
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class DeviceUserBind implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 自增ID
     */
    private Long id;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 可用状态（0：不可用，1：可用）
     */
    private String status;
}
