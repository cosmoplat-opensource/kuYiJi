/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.handler.importexport.template;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import com.cosmo.hhim.excel.ExcelUtilService;
import com.cosmo.hhim.micro.application.handler.importexport.MicroImportedHandler;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroProductService;
import com.cosmo.hhim.micro.infrastructure.annotation.HandlerType;
import com.cosmo.hhim.micro.infrastructure.enums.ProductTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.importexport.HandlerTypeEnum;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants.BOM_IMPORT_TEMPLATE_URL;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/10
 */
@Slf4j
@Component
@RequiredArgsConstructor
@HandlerType(type = HandlerTypeEnum.IMPORT_TASK_BOM_TEMPLATE)
public class MicroBomImportedTemplateHandler implements MicroImportedHandler<HttpServletResponse> {

    @Value("${minio.url}")
    private String minioUrl;

    private final ExcelUtilService excelUtilService;
    private final IMicroProductService microProductService;

    @Override
    public boolean doHandler(HttpServletResponse response) {
        try {
            // 导出模板参数设置
            TemplateExportParams templateExportParams = new TemplateExportParams(minioUrl + BOM_IMPORT_TEMPLATE_URL, 0, 1);

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
            Workbook workbook = ExcelExportUtil.exportExcel(templateExportParams, map);
            excelUtilService.exportExcel(response, "BOM导入模板", workbook);
        } catch (IOException e) {
            log.error("下载BOM导入模版失败！errorMsg:{}", Throwables.getStackTraceAsString(e));
            return false;
        }

        return true;
    }
}
