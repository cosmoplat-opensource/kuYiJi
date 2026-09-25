/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.storage;

import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorage;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageDto;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @date 2023/3/13 13:59
 */
public interface IMicroStorageFacadeService {

    /**
     * 工序库存列表
     *
     * @param seqKey
     * @param productSeq
     * @param processSeq
     * @return
     */
    List<MicroProcessStorage> selectMicroProcessStorageListBySeq(String seqKey, String productSeq, String processSeq);

    /**
     * 获取产品下面各工序的数量情况，如果有工艺则按照工艺链进行展示
     *
     * @param microProcessStorageDto
     * @return
     */
    List<MicroProcessStorage> selectMicroProcessStorageListByCondition(MicroProcessStorageDto microProcessStorageDto);
}
