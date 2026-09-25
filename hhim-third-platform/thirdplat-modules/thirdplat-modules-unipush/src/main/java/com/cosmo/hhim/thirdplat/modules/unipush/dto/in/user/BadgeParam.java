/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.dto.in.user;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-23
 */
@Data
public class BadgeParam {

    /**
     * 用户应用icon上显示的数字
     * +N: 在原有badge上+N
     * -N: 在原有badge上-N
     * N: 直接设置badge(数字，会覆盖原有的badge值)
     */
    private String badge;
}
