/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.designPattern.strategy; 

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-01-31
 */
public interface HandlerRouter<T> {

    /**
     * 获取处理器
     *
     * @param type
     * @param args
     * @return
     */
    T getHandler(String type, Object... args);

}
