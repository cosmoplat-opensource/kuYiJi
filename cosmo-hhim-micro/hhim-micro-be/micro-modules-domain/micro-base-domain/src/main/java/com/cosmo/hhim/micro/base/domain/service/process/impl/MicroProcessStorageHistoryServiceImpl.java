/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.process.impl;

import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageHistory;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessStorageHistoryMapper;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessStorageHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 库存变动历史Service业务层处理
 *
 * @date 2022-10-11
 */
@Service
public class MicroProcessStorageHistoryServiceImpl implements IMicroProcessStorageHistoryService {
    @Autowired
    private MicroProcessStorageHistoryMapper historyMapper;


    /**
     * 查询库存变动历史列表
     *
     * @param microProcessStorageHistory 库存变动历史
     * @return 库存变动历史
     */
    @Override
    public List<MicroProcessStorageHistory> selectMicroProcessStorageHistoryList(MicroProcessStorageHistory microProcessStorageHistory) {
        return historyMapper.selectMicroProcessStorageHistoryList(microProcessStorageHistory);
    }
}
