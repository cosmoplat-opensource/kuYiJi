/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.tech;

import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 产品+工序混合查询时结果集
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroProcessMixedResultEntity implements Serializable {

    private boolean standard;
    private boolean draft;
    private boolean serial;
    private boolean finalReturn;
    private List<MicroSelectEntity> resultList;
}