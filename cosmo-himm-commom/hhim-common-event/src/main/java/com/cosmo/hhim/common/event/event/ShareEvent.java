/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.event.event;

/**
 * @author cosmo-hhim-open Team
 * @description 共享一个事件队列的事件类型
 * @createTime 2022/3/18
 */
public abstract class ShareEvent extends Event {
    @Override
    public long sequence() {
        return 0;
    }
}
