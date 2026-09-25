/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 数据清理（产品编码相似）
 * @date 2023/1/13 15:58
 */
public interface IMicroCleanService {

    /**
     * 清理重复产品 -> 全流程
     *
     * @return
     */
    boolean cleanDuplicatedProduct();

    /**
     * 初始化工艺链顺序
     *
     * @param customers
     * @return
     */
    boolean initSortChain(List<String> customers);

}
