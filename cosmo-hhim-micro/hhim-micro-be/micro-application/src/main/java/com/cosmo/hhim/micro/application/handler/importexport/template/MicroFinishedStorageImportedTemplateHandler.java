/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.handler.importexport.template;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import com.cosmo.hhim.excel.ExcelUtilService;
import com.cosmo.hhim.excel.ExportParam;
import com.cosmo.hhim.micro.application.dto.storage.MicroPlanFinishProductStorage;
import com.cosmo.hhim.micro.application.dto.storage.MicroPlanFinishProductStorageExportTemplate;
import com.cosmo.hhim.micro.application.handler.importexport.MicroImportedHandler;
import com.cosmo.hhim.micro.application.service.storage.IMicroFinishedStorageImportedTempFacadeService;
import com.cosmo.hhim.micro.infrastructure.annotation.HandlerType;
import com.cosmo.hhim.micro.infrastructure.enums.ProductTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.importexport.HandlerTypeEnum;
import com.google.common.base.Throwables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants.FINISH_STORAGE_IMPORT_TEMPLATE_URL;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/6
 */
@Slf4j
@Component
@RequiredArgsConstructor
@HandlerType(type = HandlerTypeEnum.IMPORT_TASK_FINISHED_STORAGE_TEMPLATE)
public class MicroFinishedStorageImportedTemplateHandler implements MicroImportedHandler<HttpServletResponse> {

    @Value("${minio.url}")
    private String minioUrl;

    private final ExcelUtilService excelUtilService;
    private final IMicroFinishedStorageImportedTempFacadeService microFinishedStorageImportedTempFacadeService;

    @Override
    public boolean doHandler(HttpServletResponse response) {
        try {
            // 导出模板参数设置
            TemplateExportParams templateExportParams = new TemplateExportParams(minioUrl + FINISH_STORAGE_IMPORT_TEMPLATE_URL, 0);
            Map<String, Object> map = new HashMap<>(2);

            List<MicroPlanFinishProductStorage> list = microFinishedStorageImportedTempFacadeService.selectFinishedStorageImportTemplateInfo();
            List<MicroPlanFinishProductStorageExportTemplate> resultList = list.stream().map(e -> {
                MicroPlanFinishProductStorageExportTemplate result = new MicroPlanFinishProductStorageExportTemplate();
                BeanUtils.copyProperties(e, result);

                if(StringUtils.hasText(result.getProductType())) {
                    result.setProductType(ProductTypeEnum.getEnumDesc(result.getProductType()));
                }

                return result;
            }).collect(Collectors.toList());

            map.put("list", resultList);

            Workbook workbook = ExcelExportUtil.exportExcel(templateExportParams, map);

            // 做下拉选
            List<ExportParam> exportParams = getExportParam();
            for (ExportParam exportParam : exportParams) {
                excelUtilService.selectList(workbook, exportParam.getFirstRow(), exportParam.getLastRow(), exportParam.getFirstCol(), exportParam.getLastCol(), exportParam.getDataArray());
            }
            excelUtilService.exportExcel(response, "期初产成品库存导入模版", workbook);
        } catch (IOException e) {
            log.error("下载期初产成品库存导入模版失败！errorMsg:{}", Throwables.getStackTraceAsString(e));
            return false;
        }
        return true;
    }

    /**
     * 获取下拉选参数
     *
     * @return
     */
    public List<ExportParam> getExportParam() {
        List<ExportParam> exportParamList = new ArrayList<>();

        // 物料类别
        ExportParam exportParam = new ExportParam();
        exportParam.setFirstCol(2);
        exportParam.setLastCol(2);
        exportParam.setFirstRow(3);
        exportParam.setLastRow(100);
        exportParam.setDataArray(Arrays.stream(ProductTypeEnum.values()).map(ProductTypeEnum::getDesc).toArray(String[]::new));
        exportParamList.add(exportParam);

        return exportParamList;
    }
}
