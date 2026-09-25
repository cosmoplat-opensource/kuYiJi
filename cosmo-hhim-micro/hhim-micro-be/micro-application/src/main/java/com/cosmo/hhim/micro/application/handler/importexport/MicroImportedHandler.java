/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.handler.importexport;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-01-31
 */
public interface MicroImportedHandler<T> {

    /**
     * 执行任务
     *
     * @return
     */
    boolean doHandler(T t); 

}
