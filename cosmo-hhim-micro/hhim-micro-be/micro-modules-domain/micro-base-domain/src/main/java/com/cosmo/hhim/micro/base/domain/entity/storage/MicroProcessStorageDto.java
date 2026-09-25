/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.storage;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description:
 * @date 2022/10/27 6:36 下午
 */
@Data
public class MicroProcessStorageDto extends MicroProcessStorage {

    /**
     * 前端入参
     */
    private String processNameOrCode;
}
