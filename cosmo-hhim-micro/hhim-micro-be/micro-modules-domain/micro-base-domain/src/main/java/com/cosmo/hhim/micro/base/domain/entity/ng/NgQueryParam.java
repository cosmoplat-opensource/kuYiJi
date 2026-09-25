/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.ng;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description: 不良品模块中要查询报工表的统一参数实体
 * @date 2023/4/6 14:58
 */
@Data
public class NgQueryParam {

    private String productSeq;

    private String operateProcessSeq;

    private Long submitUser;

    private String ids;
}
