/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.process;

import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageHistory;

import java.util.List;

/**
 * 库存变动历史Service接口
 *
 * @date 2022-10-11
 */
public interface IMicroProcessStorageHistoryService {

    /**
     * 查询库存变动历史列表
     *
     * @param microProcessStorageHistory 库存变动历史
     * @return 库存变动历史集合
     */
    List<MicroProcessStorageHistory> selectMicroProcessStorageHistoryList(MicroProcessStorageHistory microProcessStorageHistory);

}
