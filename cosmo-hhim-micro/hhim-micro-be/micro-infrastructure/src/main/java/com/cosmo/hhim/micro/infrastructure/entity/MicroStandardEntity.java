/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工艺链复制产品,搜索时带标准工艺标记实体
 *
 * @author cosmo-hhim-open Team
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MicroStandardEntity extends MicroSelectEntity {
    private Long id;
    private Boolean standard;
}
