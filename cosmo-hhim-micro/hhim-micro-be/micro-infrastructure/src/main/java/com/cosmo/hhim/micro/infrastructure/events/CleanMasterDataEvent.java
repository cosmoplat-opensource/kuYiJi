/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.events;

import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 清理产品主数据事件
 *
 * @author cosmo-hhim-open Team
 */
public class CleanMasterDataEvent extends ApplicationEvent {
    /**
     * 接收主数据ID集合
     */
    private final List<? extends MicroSelectEntity> message;

    public CleanMasterDataEvent(List<? extends MicroSelectEntity> message) {
        super(message);
        this.message = message;
    }

    public List<? extends MicroSelectEntity> getMessage() {
        return message;
    }
}