/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.listener.event;

import com.cosmo.hhim.micro.application.service.tech.IMicroTechFacadeService;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import com.cosmo.hhim.micro.infrastructure.events.StorageImport2TechEvent;
import com.google.common.collect.HashMultimap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 期初库存导入的产品工序对应关系转换成工艺链(草稿-初始化状态)
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
public class StorageImport2TechListener {

    @Autowired
    private IMicroTechFacadeService facadeService;

    @Async("asyncEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, value = {StorageImport2TechEvent.class})
    public void onApplicationEvent(StorageImport2TechEvent<MicroSelectEntity, MicroSelectEntity> event) {
        HashMultimap<MicroSelectEntity, MicroSelectEntity> map = event.getMap();
        try {
            facadeService.saveSingleChainFromStorageImport(map);
        } catch (Exception e) {
            log.error("Failed to save technology chain from storage_import:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        }
    }
}
