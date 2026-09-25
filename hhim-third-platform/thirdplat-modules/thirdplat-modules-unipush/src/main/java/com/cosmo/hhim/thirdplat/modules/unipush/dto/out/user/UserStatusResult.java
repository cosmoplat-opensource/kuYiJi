/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.dto.out.user;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description 用户状态信息
 * @createTime 2021-09-23
 */
@Data
public class UserStatusResult {

    // 毫秒时间戳
    @JSONField(name = "last_login_time")
    private String lastLoginTime;

    // 状态，online在线 offline离线
    private String status;
}
