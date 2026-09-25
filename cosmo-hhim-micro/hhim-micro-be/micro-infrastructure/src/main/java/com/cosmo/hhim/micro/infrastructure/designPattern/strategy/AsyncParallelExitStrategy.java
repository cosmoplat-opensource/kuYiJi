/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.designPattern.strategy; 

import java.util.concurrent.Future;

/**
 * @author cosmo-hhim-open Team
 */
public interface AsyncParallelExitStrategy<T> {
    /**
     * 任务退出策略
     *
     * @param future
     * @return
     */
    boolean shouldExit(Future<T> future);
}
