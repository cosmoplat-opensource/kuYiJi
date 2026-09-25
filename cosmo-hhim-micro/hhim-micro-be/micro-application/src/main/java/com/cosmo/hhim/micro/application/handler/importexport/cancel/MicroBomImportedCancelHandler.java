/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.handler.importexport.cancel;

import com.cosmo.hhim.micro.application.handler.importexport.MicroImportedHandler;
import com.cosmo.hhim.micro.application.service.base.IMicroProductBomImportedFacadeService;
import com.cosmo.hhim.micro.infrastructure.annotation.HandlerType;
import com.cosmo.hhim.micro.infrastructure.enums.importexport.HandlerTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/10
 */
@Slf4j
@Component
@RequiredArgsConstructor
@HandlerType(type = HandlerTypeEnum.IMPORT_TASK_BOM_CANCEL)
public class MicroBomImportedCancelHandler implements MicroImportedHandler {

    private final IMicroProductBomImportedFacadeService microProductBomImportedFacadeService;

    @Override
    public boolean doHandler(Object o) {
        microProductBomImportedFacadeService.importCancel();
        return true;
    }
}
