/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.storage.domain.entity;

import lombok.Data;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/22
 */
@Data
public class ChangeFinishedStorageParam {

    // 新增库存列表 
    private List<MicroFinishedProductStorage> storageAddList;

    // 修改库存列表 
    private List<MicroFinishedProductStorage> storageUpdateList;

    // 新增库存变动历史列表 
    private List<MicroFinishedProductStorageHistory> storageHistoryList;
}
