/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.handler.importexport.template;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import com.cosmo.hhim.excel.ExcelUtilService;
import com.cosmo.hhim.excel.ExportParam;
import com.cosmo.hhim.micro.application.handler.importexport.MicroImportedHandler;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessCommon;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroProductService;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessCommonService;
import com.cosmo.hhim.micro.infrastructure.annotation.HandlerType;
import com.cosmo.hhim.micro.infrastructure.enums.ProductTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.importexport.HandlerTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.importexport.ProcessTypesEnum;
import com.cosmo.hhim.micro.infrastructure.enums.importexport.ProduceModeEnum;
import com.google.common.base.Throwables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants.TECHNOLOGY_IMPORT_TEMPLATE_URL;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/16
 */
@Slf4j
@Component
@RequiredArgsConstructor
@HandlerType(type = HandlerTypeEnum.IMPORT_TASK_TECHNOLOGY_TEMPLATE)
public class MicroTechnologyImportedTemplateHandler implements MicroImportedHandler<HttpServletResponse> {

    @Value("${minio.url}")
    private String minioUrl;

    private final ExcelUtilService excelUtilService;
    private final IMicroProductService microProductService;
    private final IMicroProcessCommonService microProcessCommonService;

    @Override
    public boolean doHandler(HttpServletResponse response) {
        try {
            // 导出模板参数设置
            TemplateExportParams templateExportParams = new TemplateExportParams(minioUrl + TECHNOLOGY_IMPORT_TEMPLATE_URL, 0, 1, 2);

            Map<String, Object> map = new HashMap<>(2);

            // 查询产品基础信息
            List<MicroProduct> microProducts = microProductService.selectMicroAllProductList();
            if (!CollectionUtils.isEmpty(microProducts)) {
                microProducts = microProducts.stream().peek(e -> {
                    if (StringUtils.hasText(e.getProductType())) {
                        e.setProductType(ProductTypeEnum.getEnumDesc(e.getProductType()));
                    }
                }).collect(Collectors.toList());
                map.put("list2", microProducts);
            }

            // 查询工序基础信息
            List<MicroProcessCommon> microProcessCommons = microProcessCommonService.selectMicroAllProcessCommonList();
            if (!CollectionUtils.isEmpty(microProcessCommons)) {
                map.put("list3", microProcessCommons);
            }

            Workbook workbook = ExcelExportUtil.exportExcel(templateExportParams, map);

            //做下拉选
            List<ExportParam> exportParams = getExportParam();
            for (ExportParam exportParam : exportParams) {
                excelUtilService.selectList(workbook, exportParam.getFirstRow(), exportParam.getLastRow(), exportParam.getFirstCol(), exportParam.getLastCol(), exportParam.getDataArray());
            }
            excelUtilService.exportExcel(response, "工艺导入模板", workbook);
        } catch (IOException e) {
            log.error("下载工艺导入模版失败！errorMsg:{}", Throwables.getStackTraceAsString(e));
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

        //生产模式
        ExportParam exportParam = new ExportParam();
        exportParam.setFirstCol(2);
        exportParam.setLastCol(2);
        exportParam.setFirstRow(3);
        exportParam.setLastRow(100);
        exportParam.setDataArray(Arrays.stream(ProduceModeEnum.values()).map(ProduceModeEnum::getDesc).toArray(String[]::new));
        exportParamList.add(exportParam);

        // 工序类型
        ExportParam exportParamA = new ExportParam();
        exportParamA.setFirstCol(5);
        exportParamA.setLastCol(5);
        exportParamA.setFirstRow(3);
        exportParamA.setLastRow(100);
        exportParamA.setDataArray(Arrays.stream(ProcessTypesEnum.values()).map(ProcessTypesEnum::getDesc).toArray(String[]::new));
        exportParamList.add(exportParamA);

        return exportParamList;
    }


}
