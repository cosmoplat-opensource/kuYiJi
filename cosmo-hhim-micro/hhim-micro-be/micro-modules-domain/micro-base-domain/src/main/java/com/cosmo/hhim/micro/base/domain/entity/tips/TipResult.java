/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.tips;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/23
 */
@Data
public class TipResult {

    // 提示内容 
    private String tipContent;

    // 提示方式-供前端显示标识（10:状态栏显示，20:弹窗显示） 
    private String tipWay;
}
