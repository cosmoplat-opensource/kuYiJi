/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.storage.impl;

import com.cosmo.hhim.micro.application.service.storage.IMicroFinishedStorageImportedFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroProductService;
import com.cosmo.hhim.micro.infrastructure.util.MicroPageUtils;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedProductStorage;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedStorageImportedTemp;
import com.cosmo.hhim.micro.storage.domain.service.IMicroFinishedProductStorageService;
import com.cosmo.hhim.micro.storage.domain.service.IMicroFinishedStorageImportedTempService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/9
 */
@Slf4j
@Service
public class MicroFinishedStorageImportedFacadeServiceImpl implements IMicroFinishedStorageImportedFacadeService {

    @Autowired
    private IMicroFinishedStorageImportedTempService microFinishedStorageImportedTempService;
    @Autowired
    private IMicroFinishedProductStorageService microFinishedProductStorageService;
    @Autowired
    private IMicroProductService microProductService;

    /**
     * 查询产成品库存导入临时数据列表
     *
     * @param microFinishedStorageImportedTemp
     * @return
     */
    @Override
    public List<MicroFinishedStorageImportedTemp> selectMicroFinishedStorageImportedTempList(MicroFinishedStorageImportedTemp microFinishedStorageImportedTemp) {
        // 1.查询产成品库存导入临时表数据
        List<MicroFinishedStorageImportedTemp> resultList = microFinishedStorageImportedTempService.selectMicroFinishedStorageImportedTempList(microFinishedStorageImportedTemp);
        if (CollectionUtils.isEmpty(resultList)) {
            return resultList;
        }

        // 2.填充当前库存数量
        List<MicroFinishedStorageImportedTemp> assembledList = resultList.stream().peek(e -> {
            MicroProduct microProduct = microProductService.selectMicroProductByProductCode(e.getProductCode());
            if (null != microProduct) {
                MicroFinishedProductStorage microFinishedProductStorage = microFinishedProductStorageService.selectMicroFinishedProductStorageByProductSeq(microProduct.getProductSeq());
                if (null != microFinishedProductStorage && null != microFinishedProductStorage.getNum()) {
                    e.setNum(microFinishedProductStorage.getNum());
                }
            }
        }).collect(Collectors.toList());

        return MicroPageUtils.listToPage(resultList, assembledList);
    }
}
