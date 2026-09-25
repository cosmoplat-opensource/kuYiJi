/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.tips.condition.business;

import lombok.Data;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/7
 */
@Data
public class BusinessHandlerParam {

    // 用户ID 
    private Long userId;

    // 用户角色列表 
    private List<String> roleCodes;
}
