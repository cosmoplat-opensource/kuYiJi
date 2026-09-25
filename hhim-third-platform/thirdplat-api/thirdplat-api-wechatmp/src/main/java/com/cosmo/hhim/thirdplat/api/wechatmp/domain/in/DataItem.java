/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp.domain.in;

import lombok.Builder;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-08
 */
@Data
@Builder
public class DataItem {
    // 内容值
    private String value;

    // 模板内容字体颜色，不填默认为黑色（N）
    private String color;
}