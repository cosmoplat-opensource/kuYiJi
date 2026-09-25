/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.dto.out.auth;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-22
 */
@Data
public class TokenInfo {

    //token过期时间，ms时间戳
    @JSONField(name = "expire_time")
    private String expireTime;

    //接口调用凭据
    private String token;
}
