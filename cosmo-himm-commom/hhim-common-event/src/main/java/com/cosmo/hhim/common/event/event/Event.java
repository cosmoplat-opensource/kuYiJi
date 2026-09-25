/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.event.event;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author cosmo-hhim-open Team
 * @description 事件抽象
 * @createTime 2022/3/18
 */
public abstract class Event implements Serializable {
    
    private static final AtomicLong SEQUENCE = new AtomicLong(0);
    
    private final long sequence = SEQUENCE.getAndIncrement();
    
    /**
     * 事件序列号
     *
     * @return
     */
    public long sequence() {
        return sequence;
    }
    
}