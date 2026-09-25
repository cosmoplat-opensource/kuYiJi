/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.storage;

import java.util.List;

/**
 * 工序库存
 *
 * @author cosmo-hhim-open Team
 */
public interface IMicroProcessStorageFacadeService {
    /**
     * 获取所有产品+工序目前的良品率及日产能指标
     */
    void flushWarningMetrics(String tenantCode);

    int removeZeroProcessStorage(List<Long> productIds); 
}
