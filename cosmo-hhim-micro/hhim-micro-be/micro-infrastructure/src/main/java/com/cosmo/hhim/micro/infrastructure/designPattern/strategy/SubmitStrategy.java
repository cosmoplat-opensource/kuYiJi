/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.designPattern.strategy; 

/**
 * @author cosmo-hhim-open Team
 * @description: 报工模块策略
 * @date 2023/3/24 13:36
 */
public interface SubmitStrategy<T, R> {

    /**
     * 报工行为
     *
     * @param t
     * @return
     */
    R submit(T t);
}
