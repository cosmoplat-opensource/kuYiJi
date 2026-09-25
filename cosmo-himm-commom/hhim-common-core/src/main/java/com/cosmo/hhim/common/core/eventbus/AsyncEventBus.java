/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.eventbus;

import java.util.concurrent.Executor;

/**
 * @author cosmo-hhim-open Team
 * @description 异步事件执行总线
 * @createTime 2021-06-01
 */
public class AsyncEventBus extends EventBus{
    public AsyncEventBus(Executor executor){
        super(executor);
    }
}
