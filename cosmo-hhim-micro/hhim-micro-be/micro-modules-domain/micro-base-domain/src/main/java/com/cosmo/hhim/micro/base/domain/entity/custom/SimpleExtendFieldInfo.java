/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.custom;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description: 自定义字段基础信息
 * @date 2023/5/10 11:15
 */
@Data
public class SimpleExtendFieldInfo {

    private String fieldName;

    private String fieldValue;
}
