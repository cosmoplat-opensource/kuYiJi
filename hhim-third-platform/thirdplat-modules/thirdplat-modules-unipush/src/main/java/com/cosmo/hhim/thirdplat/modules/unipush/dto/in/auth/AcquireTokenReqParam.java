/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.dto.in.auth;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-22
 */
@Data
public class AcquireTokenReqParam {
    // 签名，加密算法: SHA256，格式: sha256(appkey+timestamp+mastersecret)
    private String sign;

    // 毫秒时间戳，请使用当前毫秒时间戳，误差太大可能出错
    private String timestamp;

    // 创建应用时生成的appkey
    private String appkey;
}
