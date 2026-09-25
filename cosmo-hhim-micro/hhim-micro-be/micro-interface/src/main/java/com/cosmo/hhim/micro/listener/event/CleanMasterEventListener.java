/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.listener.event;

import com.cosmo.hhim.micro.base.domain.service.bom.IMicroProductBomService;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessStorageService;
import com.cosmo.hhim.micro.base.domain.service.tech.IMicroTechnologyService;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import com.cosmo.hhim.micro.infrastructure.events.CleanProcessDataEvent;
import com.cosmo.hhim.micro.infrastructure.events.CleanProductDataEvent;
import com.cosmo.hhim.micro.infrastructure.util.MicroSupportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 清理主数据事件监听
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
public class CleanMasterEventListener {

    @Autowired
    private IMicroProcessStorageService storageService;
    @Autowired
    private IMicroTechnologyService technologyService;

    @Autowired
    private IMicroProductBomService bomService;

    @Autowired
    private MicroSupportUtil supportUtil;

    /**
     * 清理产品事件监听
     *
     * @param event
     */
    @Async("asyncEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, value = {CleanProductDataEvent.class})
    public void cleanProductDataEvent(CleanProductDataEvent event) {
        List<MicroSelectEntity> message = (List<MicroSelectEntity>) event.getMessage();
        if (CollectionUtils.isEmpty(message)) {
            return;
        }
        List<String> seqList = new ArrayList<>();
        List<Long> productIds = new ArrayList<>();
        for (MicroSelectEntity select : message) {
            seqList.add(select.getItemSeq());
            productIds.add(select.getItemId());
        }
        //清理库存为0的数据
        storageService.removeZeroProcessStorageByProduct(productIds);
        // 清理bom
        bomService.removeBomByProduct(seqList);
        // 清理工艺
        technologyService.removeTechByProduct(productIds);
        supportUtil.removeProductHotData(message);
    }

    /**
     * 清理工序事件监听
     * 清理库存为0的数据
     *
     * @param event
     */
    @Async("asyncEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, value = {CleanProcessDataEvent.class})
    public void cleanProcessDataEvent(CleanProcessDataEvent event) {
        List<MicroSelectEntity> message = (List<MicroSelectEntity>) event.getMessage();
        if (CollectionUtils.isEmpty(message)) {
            return;
        }
        List<Long> processIds = message.stream().map(MicroSelectEntity::getItemId).collect(Collectors.toList());
        //清理库存为0的数据
        storageService.removeZeroProcessStorageByProcess(processIds);
    }
}
