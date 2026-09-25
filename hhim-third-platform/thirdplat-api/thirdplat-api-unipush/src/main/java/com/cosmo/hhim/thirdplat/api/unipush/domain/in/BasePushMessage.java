/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in;

import lombok.Data;

@Data
public class BasePushMessage {
    // 请求唯一标识号，10-32位之间；如果request_id重复，会导致消息丢失
    private String requestId;
}
