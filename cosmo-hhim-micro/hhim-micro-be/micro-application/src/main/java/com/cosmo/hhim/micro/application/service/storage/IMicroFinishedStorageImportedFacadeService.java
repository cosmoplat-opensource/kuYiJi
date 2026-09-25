/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.storage;

import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedStorageImportedTemp;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/9
 */
public interface IMicroFinishedStorageImportedFacadeService {

    /**
     * 查询产成品库存导入临时数据列表
     * @param microFinishedStorageImportedTemp
     * @return
     */
    List<MicroFinishedStorageImportedTemp> selectMicroFinishedStorageImportedTempList(MicroFinishedStorageImportedTemp microFinishedStorageImportedTemp);

}
