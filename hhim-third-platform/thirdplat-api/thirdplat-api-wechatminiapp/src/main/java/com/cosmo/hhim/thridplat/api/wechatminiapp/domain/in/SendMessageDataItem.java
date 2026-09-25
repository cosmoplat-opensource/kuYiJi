/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in;

import lombok.Builder;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/21
 */
@Data
@Builder
public class SendMessageDataItem {
    private String value;
    private String color;
}