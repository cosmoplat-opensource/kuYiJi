/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.handler.importexport.confirm;

import com.cosmo.hhim.micro.application.handler.importexport.MicroImportedHandler;
import com.cosmo.hhim.micro.application.service.tech.IMicroTechImportedFacadeService;
import com.cosmo.hhim.micro.infrastructure.annotation.HandlerType;
import com.cosmo.hhim.micro.infrastructure.enums.importexport.HandlerTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/16
 */
@Slf4j
@Component
@RequiredArgsConstructor
@HandlerType(type = HandlerTypeEnum.IMPORT_TASK_TECHNOLOGY_CONFIRM)
public class MicroTechnologyImportedConfirmHandler implements MicroImportedHandler<String> {

    private final IMicroTechImportedFacadeService microTechImportedFacadeService;

    @Override
    public boolean doHandler(String flag) {
        microTechImportedFacadeService.importConfirm(flag);
        return true;
    }
}
