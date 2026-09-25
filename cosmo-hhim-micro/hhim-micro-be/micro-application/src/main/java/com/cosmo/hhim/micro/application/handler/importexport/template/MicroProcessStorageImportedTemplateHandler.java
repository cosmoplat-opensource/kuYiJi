/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.handler.importexport.template;

import com.cosmo.hhim.excel.ExcelUtilService;
import com.cosmo.hhim.micro.application.handler.importexport.MicroImportedHandler;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageExportModelResult;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessStorageService;
import com.cosmo.hhim.micro.infrastructure.annotation.HandlerType;
import com.cosmo.hhim.micro.infrastructure.enums.importexport.HandlerTypeEnum;
import com.google.common.base.Throwables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/6
 */
@Slf4j
@Component
@RequiredArgsConstructor
@HandlerType(type = HandlerTypeEnum.IMPORT_TASK_PROCESS_STORAGE_TEMPLATE)
public class MicroProcessStorageImportedTemplateHandler implements MicroImportedHandler<HttpServletResponse> {

    private final ExcelUtilService excelUtilService;
    private final IMicroProcessStorageService processStorageService;

    @Override
    public boolean doHandler(HttpServletResponse response) {
        try {
            List<MicroProcessStorageExportModelResult> list = processStorageService.selectAllMicroProcessStorageList();
            excelUtilService.exportExcelAndDownload(response, list, MicroProcessStorageExportModelResult.class, "期初工序库存导入模版", "期初工序库存导入模版");
        } catch (IOException e) {
            log.error("下载期初工序库存导入模版失败！errorMsg:{}", Throwables.getStackTraceAsString(e));
            return false;
        }
        return true;
    }
}
