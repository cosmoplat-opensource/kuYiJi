/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.event.dispatcher;

/**
 * @author cosmo-hhim-open Team
 * @description 资源关闭处理接口
 * @createTime 2022/3/18
 */
public interface Closeable {
    /**
     * 关闭资源
     */
    void shutdown();
}
