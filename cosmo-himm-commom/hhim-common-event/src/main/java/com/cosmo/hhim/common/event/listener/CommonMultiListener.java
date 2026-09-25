/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.event.listener;


import com.cosmo.hhim.common.event.event.Event;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description 通用多事件监听器抽象
 * @createTime 2022/3/18
 */
public abstract class CommonMultiListener extends CommonListener {

    /**
     * 关注多个事件类型
     *
     * @return
     */
    public abstract List<Class<? extends Event>> subscribeTypes();

    @Override
    public final Class<? extends Event> subscribeType() {
        return null;
    }

    @Override
    public final boolean ignoreExpireEvent() {
        return false;
    }

}
