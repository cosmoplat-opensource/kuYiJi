/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.events;

import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;

import java.util.List;

/**
 * 清理工序主数据事件
 *
 * @author cosmo-hhim-open Team
 */
public class CleanProcessDataEvent extends CleanMasterDataEvent {

    public CleanProcessDataEvent(List<? extends MicroSelectEntity> message) {
        super(message);
    }
}