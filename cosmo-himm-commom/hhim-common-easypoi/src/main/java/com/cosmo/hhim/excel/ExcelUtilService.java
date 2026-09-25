
/**
 * Copyright 2013-2015 JueYue (qrb.jueyue@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * Modifications by 海尔卡奥斯物联科技有限公司:
 *   2026 - Added @Slf4j, replaced e.printStackTrace() with log.error(), fixed Javadoc
 *
 * Original: https://github.com/jueyue/easypoi
 */
package com.cosmo.hhim.excel;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
import cn.afterturn.easypoi.handler.impl.ExcelDataHandlerDefaultImpl;
import cn.afterturn.easypoi.handler.inter.IExcelExportServer;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.domain.DictData;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.domain.ImportResutl;
import com.cosmo.hhim.local.service.MultiLangCache;
import com.cosmo.hhim.minio.ISysFileService;
import com.github.pagehelper.PageHelper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.security.SecureRandom;

/**
 * @author cosmo-hhim-open Team
 * excel工具栏
 */
@Component
public class ExcelUtilService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelUtilService.class);

    /** 安全的随机数源（用于生成隐藏 Sheet 名称） */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private ISysFileService iSysFileService;


    /**
     * 导入
     *
     * @param importParam
     * @return
     */
    public ImportResutl importExcel(ImportParam importParam) {
        int successCount = 0;
        int failCount = 0;
        String url = "";
        List<?> failList = new ArrayList<>();
        List<?> successList = new ArrayList<>();
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        long start = System.currentTimeMillis() / 1000;
        try {
            ImportParams params = new ImportParams();
            params.setVerifyFileSplit(false);
            params.setSheetNum(importParam.getSheetNum());
            params.setHeadRows(importParam.getHeaderNum());
            params.setTitleRows(importParam.getTitleNum());
            params.setNeedVerify(true);
            params.setVerifyGroup(importParam.getVerifyGroup());
            if (importParam.getVerifyHandler() != null) {
                params.setVerifyHandler(importParam.getVerifyHandler());
            }
            ExcelImportResult<?> result = ExcelImportUtil.importExcelMore(
                    importParam.getMultipartFile().getInputStream(),
                    importParam.getPojoClass(), params, importParam.getImportRowNum());
            successList = result.getList();
            successCount = successList.size();
            long end = System.currentTimeMillis() / 1000;
            LOGGER.info("解析导入excel处理时间：{}秒", end - start);

            //判断是否有错误
            failList = result.getFailList();
            List<Object> removeList = new ArrayList<>();
            /**
             * 处理空白行
             */
            for (int a = 0; a < failList.size(); a++) {
                boolean b = true;
                for (Field f : failList.get(a).getClass().getDeclaredFields()) {
                    if ("rowNum".equals(f.getName()) || "errorMsg".equals(f.getName()) || "serialVersionUID".equals(f.getName())) {
                        continue;
                    }
                    if (getFieldValueByGetter(failList.get(a), f.getName()) != null) {
                        b = false;
                    }
                }
                if (b) {
                    removeList.add(failList.get(a));
                }
                b = true;
            }
            failList.removeAll(removeList);
            failCount = failList.size();
            if (failCount != 0 && importParam.isUploadFlag()) {
                Map<String, Object> map = new HashMap<>();
                map.put(importParam.getKey(), failList);
                TemplateExportParams templateExportParams = new TemplateExportParams(importParam.getFilePath(), importParam.getSheetNums());
                Workbook failWorkbook = ExcelExportUtil.exportExcel(templateExportParams, map);
                bos = new ByteArrayOutputStream();
                failWorkbook.write(bos);
                byte[] barray = bos.toByteArray();
                is = new ByteArrayInputStream(barray);
                MultipartFile multipartFilemini = new MockMultipartFile("导入失败的数据", ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", is);
                url = iSysFileService.uploadFile(multipartFilemini, "importExcel");
            }
        } catch (Exception e) {
            LOGGER.error("excel导入异常", e);
            if (e.getMessage().indexOf("不是合法的Excel模板") != -1) {
                return ImportResutl.error("不是合法的Excel模板");
            } else {
                return ImportResutl.error("导入失败" + e.getMessage());
            }
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
            } catch (IOException e) {
                LOGGER.error("is.close()", e);
            }
            try {
                if (null != bos) {
                    bos.close();
                }
            } catch (IOException e) {
                LOGGER.error("bos.close()", e);
            }
        }
        if (failCount == 0) {
            return ImportResutl.success("全部导入成功！共" + successCount + "条数据", url, successList, failList, new ArrayList<>());
        } else if (successCount == 0) {
            return ImportResutl.success("全部导入失败！共" + failCount + "条数据", url, successList, failList, new ArrayList<>());
        } else {
            return ImportResutl.success("部分导入成功！成功" + successCount + "条数据" + ",失败" + failCount + "条数据", url, successList, failList, new ArrayList<>());
        }
    }


    /**
     * 导出(带下落选)
     *
     * @return
     */
    public AjaxResult ExportExcel(String exportUrl, Map<String, Object> map, List<ExportParam> list, Integer... sheetNum) {
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        String url = "";
        long start = System.currentTimeMillis() / 1000;
        try {
            TemplateExportParams templateExportParams = new TemplateExportParams(exportUrl, sheetNum);
            long end = System.currentTimeMillis() / 1000;
            LOGGER.info("解析导入excel处理时间1：{}秒", end - start);
            start = System.currentTimeMillis() / 1000;
            Workbook failWorkbook = ExcelExportUtil.exportExcel(templateExportParams, map);
            LOGGER.info("excel export data: {}", JSONObject.toJSONString(map));
            end = System.currentTimeMillis() / 1000;
            LOGGER.info("解析导入excel处理时间2：{}秒", end - start);
            start = System.currentTimeMillis() / 1000;
            for (ExportParam exportParam : list) {
                selectList(failWorkbook, exportParam.getFirstRow(), exportParam.getLastRow(), exportParam.getFirstCol(), exportParam.getLastCol(), exportParam.getDataArray());
            }

            bos = new ByteArrayOutputStream();
            failWorkbook.write(bos);
            end = System.currentTimeMillis() / 1000;
            LOGGER.info("解析导入excel处理时间3：{}秒", end - start);
            byte[] barray = bos.toByteArray();
            is = new ByteArrayInputStream(barray);
            MultipartFile multipartFilemini = new MockMultipartFile("导入失败的数据", ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", is);
            start = System.currentTimeMillis() / 1000;
            url = iSysFileService.uploadFile(multipartFilemini, "importExcel");
            end = System.currentTimeMillis() / 1000;
            LOGGER.info("解析导入excel处理时间4：{}秒", end - start);
        } catch (Exception e) {
            LOGGER.error("excel导入异常", e);
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
            } catch (IOException e) {
                LOGGER.error("is.close()", e);
            }
            try {
                if (null != bos) {
                    bos.close();
                }
            } catch (IOException e) {
                LOGGER.error("bos.close()", e);
            }
        }
        return AjaxResult.success(url);
    }


    /**
     * 导出
     *
     * @return
     */
    public AjaxResult ExportExcel(String exportUrl, Map<String, Object> map, Integer... sheetNum) {
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        String url = "";
        try {
            TemplateExportParams templateExportParams = new TemplateExportParams(exportUrl, sheetNum);
            Workbook failWorkbook = ExcelExportUtil.exportExcel(templateExportParams, map);
            bos = new ByteArrayOutputStream();
            failWorkbook.write(bos);
            byte[] barray = bos.toByteArray();
            is = new ByteArrayInputStream(barray);
            MultipartFile multipartFilemini = new MockMultipartFile("导入失败的数据", ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", is);
            url = iSysFileService.uploadFile(multipartFilemini, "importExcel");
        } catch (Exception e) {
            LOGGER.error("excel导入异常", e);
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
            } catch (IOException e) {
                LOGGER.error("is.close()", e);
            }
            try {
                if (null != bos) {
                    bos.close();
                }
            } catch (IOException e) {
                LOGGER.error("bos.close()", e);
            }
        }
        return AjaxResult.success(url);
    }


    /**
     * 自定义字段导出
     */
    public AjaxResult customExport(Class<?> pojo, List<?> list, Map<String, String> map, String fileName, String downloadPath) {
        long start = System.currentTimeMillis() / 1000;
        List listTemp = new ArrayList<>();
        listTemp.addAll(list);
        if (CollectionUtils.isEmpty(list)) {
            return AjaxResult.error("数据为空");
        }
        try {
            int sheetRow = 0;
            int sheetCell = 0;
            ExportParams exportParams = new ExportParams();
            Field[] fields = pojo.getDeclaredFields();
            boolean flag = false;
            Map<String, List<DictData>> dicts = new HashMap<>();
            for (Field f : fields) {
                if (!Objects.isNull(f.getAnnotation(Excel.class)) && StringUtils.isNotEmpty(f.getAnnotation(Excel.class).dict())) {
                    dicts.put(f.getAnnotation(Excel.class).dict(), ExcelUtil.getDictCache(f.getAnnotation(Excel.class).dict()));
                    flag = true;
                }
            }
            if (flag) {
                exportParams.setDictHandler(new IExcelDictHandlerImpl(dicts));
            }
            exportParams.setType(ExcelType.XSSF);
            Workbook workbook = ExcelExportUtil.exportExcel(exportParams,
                    pojo, list);
            Sheet sheet = workbook.getSheetAt(0);
            for (int a = 0; a < 100; a++) {
                if (sheet.getRow(sheetRow).getCell(a) == null) {
                    sheetCell = a;
                    break;
                }
            }
            //先渲染表头
            int roundOne = 0;
            LinkedHashMap<Object, Object> lmp = new LinkedHashMap<>();
            for (Map.Entry entry : map.entrySet()) {
                sheet.getRow(sheetRow).createCell(sheetCell + roundOne).setCellValue(entry.getValue().toString());
                roundOne++;
                lmp.put(entry.getKey(), entry.getValue());
            }
            int roundTwo = 0;
            sheetRow = 1;
            //渲染数据
            Method m;
            JSONObject obj;
            for (Object s : listTemp) {
                m = s.getClass().getMethod("getExtendContent");
                obj = JSONObject.parseObject((String) m.invoke(s));
                for (Map.Entry<Object, Object> entry : lmp.entrySet()) {

                    if (obj != null && entry != null && obj.containsKey(entry.getKey()) && !Objects.isNull(obj.get(entry.getKey()))) {
                        sheet.getRow(sheetRow).createCell(sheetCell + roundTwo).setCellValue(obj.get(entry.getKey()).toString());
                    }
                    roundTwo++;
                }
                roundTwo = 0;
                sheetRow++;
            }

            if (StringUtils.isEmpty(downloadPath)) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                workbook.write(bos);
                byte[] barray = bos.toByteArray();
                InputStream is = new ByteArrayInputStream(barray);
                MultipartFile multipartFilemini = new MockMultipartFile("导入失败的数据", ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", is);
                String url = iSysFileService.uploadFileByName(multipartFilemini, "ExportExcel", fileName + ".xlsx");
                long end = System.currentTimeMillis() / 1000;
                LOGGER.info("解析导出excel处理时间：{}秒", end - start);
                return AjaxResult.success(url);
            } else {
                downloadPath = downloadPath + fileName + ".xlsx";
                File desc = new File(downloadPath);
                if (!desc.getParentFile().exists()) {
                    desc.getParentFile().mkdirs();
                }
                OutputStream out = new FileOutputStream(downloadPath);
                workbook.write(out);
                long end = System.currentTimeMillis() / 1000;
                LOGGER.info("解析导出excel处理时间：{}秒", end - start);
                return AjaxResult.success(fileName + ".xlsx");
            }
        } catch (Exception e) {
            LOGGER.error("导出异常", e);
        }
        return AjaxResult.error("导出异常");
    }


    /**
     * 自定义字段导入
     *
     * @param file                    文件
     * @param map                     自定义导出规则
     * @param excelDataHandlerDefault 字段转换拦截器
     * @return
     */
    public List<Map<String, Object>> customImport(File file, Map<String, Object> map, ExcelDataHandlerDefaultImpl excelDataHandlerDefault) {

        JSONObject jsonObject = new JSONObject();
        ImportParams params = new ImportParams();
        params.setDataHandler(excelDataHandlerDefault);
        //ExtendContent
        List<Map<String, Object>> list = ExcelImportUtil.importExcel(
                file, Map.class, params, 20000);
        for (Map<String, Object> maps : list) {
            try {
                for (Map.Entry<String, Object> entry : maps.entrySet()) {
                    for (Map.Entry<String, Object> entry1 : map.entrySet()) {
                        if (entry.getKey().equals(entry1.getValue())) {
                            jsonObject.put(entry1.getKey(), entry.getValue());
                        }
                    }
                }
            } catch (Exception e) {
                continue;
            }
            maps.put("extendContent", jsonObject);
            jsonObject = new JSONObject();
        }

        return list;
    }

    /**
     * 导出execl
     *
     * @param response
     * @param fileName
     * @param workbook
     * @throws IOException
     */
    public void exportExcel(HttpServletResponse response, String fileName, Workbook workbook) throws IOException {
        Iterator<Sheet> sheetIterator = workbook.sheetIterator();
        int a = 0;
        while (sheetIterator.hasNext()) {


            Row row = sheetIterator.next().getRow(a);
            if (row == null) {
                continue;
            }
            Iterator<Cell> it = row.cellIterator();
            while (it.hasNext()) {
                boolean boo = false;
                Cell cell = it.next();
                if (cell == null) {
                    continue;
                }
                String str = cell.getStringCellValue();
                if (str.startsWith("*")) {
                    str = str.replace("*", "");
                    boo = true;
                }
                try {
                    if (boo) {
                        cell.setCellValue("*" + (String) MultiLangCache.getInstance().getCacheObject(str));
                    } else {
                        cell.setCellValue((String) MultiLangCache.getInstance().getCacheObject(str));
                    }
                } catch (Exception e) {
                    LOGGER.error("获取缓存失败", e);
                }

            }
            a++;
        }

        response.setContentType("application/vnd.ms-excel");
        response.addHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(StandardCharsets.UTF_8),"ISO8859-1") + ".xlsx");
        response.setContentType("application/vnd.ms-excel;charset=gb2312");
        response.setCharacterEncoding("utf-8");
        this.exportExcel(response.getOutputStream(), workbook);
    }

    public void exportExcel(OutputStream outputStream, Workbook workbook) {
        try {
            workbook.write(outputStream);
        } catch (Exception e) {
            LOGGER.error("导出Excel异常{}", e.getMessage());
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e1) {
                    LOGGER.error("ExcelUtilServiceError", e1);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e1) {
                    LOGGER.error("ExcelUtilServiceError", e1);
                }
            }
        }
    }

    /**
     * 下拉选
     * firstRow 开始行号(下标0开始)
     * lastRow  结束行号，最大65535
     * firstCol 区域中第一个单元格的列号 (下标0开始)
     * lastCol 区域中最后一个单元格的列号
     * dataArray 下拉内容
     * sheetHidden 影藏的sheet编号（例如1,2,3），多个下拉数据不能使用同一个
     */
    public void selectList(Workbook workbook, int firstRow, int lastRow, int firstCol, int lastCol, String[] dataArray) {
        String hiddenName = "hidden_" + (int) ((SECURE_RANDOM.nextDouble() * 9 + 1) * 10000);
        Sheet sheet = workbook.getSheetAt(0);
        Sheet hidden = workbook.createSheet(hiddenName);
        Cell cell = null;
        for (int i = 0, length = dataArray.length; i < length; i++) {
            String name = dataArray[i];
            Row row = hidden.createRow(i);
            cell = row.createCell(0);
            cell.setCellValue(name);
        }

        Name namedCell = workbook.createName();
        namedCell.setNameName(hiddenName);
        namedCell.setRefersToFormula(hiddenName + "!$A$1:$A$" + dataArray.length);

        // 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);

        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper((XSSFSheet) sheet);
        XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(dataArray);
        XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        // 将sheet设置为隐藏
        workbook.setSheetHidden(workbook.getSheetIndex(hiddenName), true);
        sheet.addValidationData(validation);


    }

    /**
     * 导出excel(数据字典)
     *
     * @param list      数据集合
     * @param pojoClass 数据类型
     * @param fileName  文件名称
     * @param title     表明
     */
    public AjaxResult exportExcel(List<?> list, Class<?> pojoClass, String fileName, String title) {
        try {
            ExportParams exportParams = new ExportParams(title, null, ExcelType.XSSF);
            // 自定义字典查询规则
            Field[] fields = pojoClass.getDeclaredFields();
            boolean flag = false;
            Map<String, List<DictData>> dicts = new HashMap<>();
            for (Field f : fields) {
                if (!Objects.isNull(f.getAnnotation(Excel.class)) && StringUtils.isNotEmpty(f.getAnnotation(Excel.class).dict())) {
                    dicts.put(f.getAnnotation(Excel.class).dict(), ExcelUtil.getDictCache(f.getAnnotation(Excel.class).dict()));
                    flag = true;
                }
            }
            if (flag) {
                exportParams.setDictHandler(new IExcelDictHandlerImpl(dicts));
            }
            Workbook workbook = ExcelExportUtil.exportExcel(exportParams, pojoClass, list);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            byte[] barray = bos.toByteArray();
            InputStream is = new ByteArrayInputStream(barray);
            MultipartFile multipartFilemini = new MockMultipartFile("导入失败的数据", ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", is);
            String url = iSysFileService.uploadFileByName(multipartFilemini, "ExportExcel", fileName + ".xlsx");
            return AjaxResult.success(url);
        } catch (Exception e) {
            LOGGER.error("导出异常", e);
        }
        return AjaxResult.error("导出异常");

    }

    /**
     * 导出excel(数据字典)
     *
     * @param list
     * @param pojoClass
     * @param title
     * @return
     */
    public ByteArrayOutputStream exportExcel(List<?> list, Class<?> pojoClass, String title) {
        try {
            ExportParams exportParams = new ExportParams(title, null, ExcelType.XSSF);
            // 自定义字典查询规则
            Field[] fields = pojoClass.getDeclaredFields();
            boolean flag = false;
            Map<String, List<DictData>> dicts = new HashMap<>();
            for (Field f : fields) {
                if (!Objects.isNull(f.getAnnotation(Excel.class)) && StringUtils.isNotEmpty(f.getAnnotation(Excel.class).dict())) {
                    dicts.put(f.getAnnotation(Excel.class).dict(), ExcelUtil.getDictCache(f.getAnnotation(Excel.class).dict()));
                    flag = true;
                }
            }
            if (flag) {
                exportParams.setDictHandler(new IExcelDictHandlerImpl(dicts));
            }
            Workbook workbook = ExcelExportUtil.exportExcel(exportParams, pojoClass, list);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos;
        } catch (Exception e) {
            LOGGER.error("导出异常", e);
        }
        return null;
    }

    /**
     * 大数据量导出excel
     *
     * @param pojoClass 数据类型
     * @param fileName  文件名称
     * @param title     表明
     */
    public AjaxResult exportBigExcel(Class<?> pojoClass, String fileName, String title, String sheetName, ExportBigExcelService exportBigExcelService, Object param, int pageSize) {
        ExportParams exportParam = new ExportParams(title, sheetName);
        Field[] fields = pojoClass.getDeclaredFields();
        boolean flag = false;
        Map<String, List<DictData>> dicts = new HashMap<>();
        for (Field f : fields) {
            if (!Objects.isNull(f.getAnnotation(Excel.class)) && StringUtils.isNotEmpty(f.getAnnotation(Excel.class).dict())) {
                dicts.put(f.getAnnotation(Excel.class).dict(), ExcelUtil.getDictCache(f.getAnnotation(Excel.class).dict()));
                flag = true;
            }
        }
        if (flag) {
            exportParam.setDictHandler(new IExcelDictHandlerImpl(dicts));
        }
        String url = null;
        try {
            Workbook workbook = ExcelExportUtil.exportBigExcel(exportParam, pojoClass, new IExcelExportServer() {
                @Override
                public List<Object> selectListForExcelExport(Object o, int i) {
                    PageHelper.startPage(i, pageSize, "");

                    List<?> list = exportBigExcelService.exportQuery(param);
                    return (List<Object>) list;
                }
            }, 0);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            byte[] barray = bos.toByteArray();
            InputStream is = new ByteArrayInputStream(barray);
            MultipartFile multipartFilemini = new MockMultipartFile("工时统计", ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", is);
            url = iSysFileService.uploadFileByName(multipartFilemini, "importExcel", fileName + ".xlsx");
        } catch (Exception e) {

            LOGGER.error("ExcelUtilServiceError", e);
        }
        return AjaxResult.success(url);

    }


    /**
     * 导出多个sheet的excel
     *
     * @param mapList  参数集合
     * @param fileName 文件名称
     */
    public AjaxResult exportExcelByManySheet(List<Map<String, Object>> mapList, String fileName) {
        for (Map<String, Object> map : mapList) {
            ExportParams exportParam = (ExportParams) map.get("title");
            Field[] fields = ((Class<?>) map.get("entity")).getDeclaredFields();
            boolean flag = false;
            Map<String, List<DictData>> dicts = new HashMap<>();
            for (Field f : fields) {
                if (!Objects.isNull(f.getAnnotation(Excel.class)) && StringUtils.isNotEmpty(f.getAnnotation(Excel.class).dict())) {
                    dicts.put(f.getAnnotation(Excel.class).dict(), ExcelUtil.getDictCache(f.getAnnotation(Excel.class).dict()));
                    flag = true;
                }
            }
            if (flag) {
                exportParam.setDictHandler(new IExcelDictHandlerImpl(dicts));
            }
        }
        String url = null;
        try {
            Workbook workbook = ExcelExportUtil.exportExcel(mapList, ExcelType.XSSF);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            byte[] barray = bos.toByteArray();
            InputStream is = new ByteArrayInputStream(barray);
            MultipartFile multipartFilemini = new MockMultipartFile("库存齐套分析", ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", is);
            url = iSysFileService.uploadFileByName(multipartFilemini, "ExportExcel", fileName + ".xlsx");
        } catch (Exception e) {
            LOGGER.error("ExcelUtilServiceError", e);
        }
        return AjaxResult.success(url);

    }

    /**
     * 导出多个sheet的excel
     *
     * @param mapList  参数集合
     * @param fileName 文件名称
     */
    public Workbook exportExcelByMultiSheet(ByteArrayOutputStream out, List<Map<String, Object>> mapList, String fileName) {
        for (Map<String, Object> map : mapList) {
            ExportParams exportParam = (ExportParams) map.get("title");
            Field[] fields = ((Class<?>) map.get("entity")).getDeclaredFields();
            boolean flag = false;
            Map<String, List<DictData>> dicts = new HashMap<>();
            for (Field f : fields) {
                if (!Objects.isNull(f.getAnnotation(Excel.class)) && StringUtils.isNotEmpty(f.getAnnotation(Excel.class).dict())) {
                    dicts.put(f.getAnnotation(Excel.class).dict(), ExcelUtil.getDictCache(f.getAnnotation(Excel.class).dict()));
                    flag = true;
                }
            }
            if (flag) {
                exportParam.setDictHandler(new IExcelDictHandlerImpl(dicts));
            }
        }
        try {
            Workbook workbook = ExcelExportUtil.exportExcel(mapList, ExcelType.XSSF);
            workbook.write(out);
            return workbook;
        } catch (Exception e) {
            LOGGER.error("ExcelUtilServiceError", e);
        }
        return null;

    }

    /**
     * 导出自定义表头
     *
     * @param fileName 文件名称
     */
    public AjaxResult exportExcelByHeadRows(List<ExcelExportEntity> titles, List<Map<String, Object>> values, String fileName) {
        String url = null;
        try {
            ExportParams exportParams = new ExportParams(fileName, null, ExcelType.XSSF);
            exportParams.setType(ExcelType.XSSF);
            //生成
            Workbook workbook = ExcelExportUtil.exportExcel(exportParams, titles, values);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            byte[] barray = bos.toByteArray();
            InputStream is = new ByteArrayInputStream(barray);
            MultipartFile multipartFilemini = new MockMultipartFile("导入失败的数据", ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", is);
            url = iSysFileService.uploadFileByName(multipartFilemini, "ExportExcel", fileName + ".xlsx");

        } catch (Exception e) {
            LOGGER.error("ExcelUtilServiceError", e);
        }
        return AjaxResult.success(url);

    }


    /**
     * 基于模板文件自定义下载模板
     *
     * @return
     */
    public AjaxResult customDownloadExcel(String exportUrl, Map<String, Object> map, LinkedHashMap<String, Object> tempMap, int rowStart, int cellStart, Integer... sheetNum) {
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        String url = "";
        try {
            TemplateExportParams templateExportParams = new TemplateExportParams(exportUrl, sheetNum);
            Workbook workbook = ExcelExportUtil.exportExcel(templateExportParams, map);
            Sheet sheet = workbook.getSheetAt(0);
            int roundOne = 0;
            for (Map.Entry entry : tempMap.entrySet()) {
                Cell cell = sheet.getRow(rowStart).getCell(cellStart + roundOne);
                if (cell == null) {
                    cell = sheet.getRow(rowStart).createCell(cellStart + roundOne);
                }
                cell.setCellValue(entry.getValue().toString());
                roundOne++;
            }
            bos = new ByteArrayOutputStream();
            workbook.write(bos);
            byte[] barray = bos.toByteArray();
            is = new ByteArrayInputStream(barray);
            MultipartFile multipartFilemini = new MockMultipartFile("自定义导入模板", ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", is);
            url = iSysFileService.uploadFile(multipartFilemini, "importExcel");
        } catch (Exception e) {
            LOGGER.error("excel导入异常", e);
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
            } catch (IOException e) {
                LOGGER.error("is.close()", e);
            }
            try {
                if (null != bos) {
                    bos.close();
                }
            } catch (IOException e) {
                LOGGER.error("bos.close()", e);
            }
        }
        return AjaxResult.success(url);
    }

    /**
     * 基于模板文件自定义导出
     *
     * @return
     */
    public AjaxResult customExportExcel(String exportUrl, Map<String, Object> map, LinkedHashMap<String, Object> tempMap, int rowStart, int cellStart, Integer... sheetNum) {
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        String url = "";

        try {
            TemplateExportParams templateExportParams = new TemplateExportParams(exportUrl, sheetNum);
            Workbook failWorkbook = ExcelExportUtil.exportExcel(templateExportParams, map);
            Sheet sheet = failWorkbook.getSheetAt(0);

            List listTemp = new ArrayList<>();
            listTemp.addAll((List) map.get("list"));
            //先渲染表头
            int roundOne = 0;
            for (Map.Entry entry : tempMap.entrySet()) {
                Cell cell = sheet.getRow(rowStart).getCell(cellStart + roundOne);
                if (cell == null) {
                    cell = sheet.getRow(rowStart).createCell(cellStart + roundOne);
                }
                cell.setCellValue(entry.getValue().toString());
                roundOne++;
            }
            int i = 1;
            Method m;
            for (Object s : listTemp) {
                int roundTwo = 0;
                for (Map.Entry entry : tempMap.entrySet()) {
                    String key = entry.getKey().toString().substring(0, 1).toUpperCase() + entry.getKey().toString().substring(1);
                    m = s.getClass().getMethod("get" + key);
                    Cell cell = sheet.getRow(rowStart + i).getCell(cellStart + roundTwo);
                    if (cell == null) {
                        cell = sheet.getRow(rowStart + i).createCell(cellStart + roundTwo);
                    }
                    if (null == m.invoke(s)) {
                        cell.setCellValue("");
                    } else {
                        cell.setCellValue(m.invoke(s).toString());
                    }

                    roundTwo++;
                }
                i++;
            }
            bos = new ByteArrayOutputStream();
            failWorkbook.write(bos);
            byte[] barray = bos.toByteArray();
            is = new ByteArrayInputStream(barray);
            MultipartFile multipartFilemini = new MockMultipartFile("导入失败的数据", ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", is);
            url = this.iSysFileService.uploadFile(multipartFilemini, "importExcel");
        } catch (Exception var23) {
            LOGGER.error("excel导入异常", var23);
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
            } catch (IOException var22) {
                LOGGER.error("is.close()", var22);
            }
            try {
                if (null != bos) {
                    bos.close();
                }
            } catch (IOException var21) {
                LOGGER.error("bos.close()", var21);
            }

        }
        return AjaxResult.success(url);
    }

    /**
     * 导出并直接下载
     *
     * @param response
     * @param fileName
     * @throws IOException
     */
    public void exportExcelAndDownload(HttpServletResponse response, List<?> list, Class<?> pojoClass, String fileName, String title) throws IOException {
        ExportParams exportParams = new ExportParams(title, null, ExcelType.XSSF);
        // 自定义字典查询规则
        Field[] fields = pojoClass.getDeclaredFields();
        boolean flag = false;
        Map<String, List<DictData>> dicts = new HashMap<>();
        for (Field f : fields) {
            if (!Objects.isNull(f.getAnnotation(Excel.class)) && StringUtils.isNotEmpty(f.getAnnotation(Excel.class).dict())) {
                dicts.put(f.getAnnotation(Excel.class).dict(), ExcelUtil.getDictCache(f.getAnnotation(Excel.class).dict()));
                flag = true;
            }
        }
        if (flag) {
            exportParams.setDictHandler(new IExcelDictHandlerImpl(dicts));
        }
        Workbook workbook = ExcelExportUtil.exportExcel(exportParams, pojoClass, list);

        Iterator<Sheet> sheetIterator = workbook.sheetIterator();
        int a = 0;
        while (sheetIterator.hasNext()) {

            Row row = sheetIterator.next().getRow(a);
            if (row == null) {
                continue;
            }
            Iterator<Cell> it = row.cellIterator();
            while (it.hasNext()) {
                boolean boo = false;
                Cell cell = it.next();
                if (cell == null) {
                    continue;
                }
                String str = cell.getStringCellValue();
                if (str.startsWith("*")) {
                    str = str.replace("*", "");
                    boo = true;
                }
                try {
                    if (boo) {
                        cell.setCellValue("*" + (String) MultiLangCache.getInstance().getCacheObject(str));
                    } else {
                        cell.setCellValue((String) MultiLangCache.getInstance().getCacheObject(str));
                    }
                } catch (Exception e) {
                    LOGGER.error("获取缓存失败", e);
                }

            }
            a++;
        }

        response.setContentType("application/vnd.ms-excel");
//        response.addHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        response.addHeader("Content-disposition", "attachment;filename=" + new String(fileName.getBytes(StandardCharsets.UTF_8),"ISO8859-1") + ".xlsx");
        response.setContentType("application/vnd.ms-excel;charset=gb2312");
        response.setCharacterEncoding("utf-8");
        this.exportExcel(response.getOutputStream(), workbook);
    }

    /**
     * 通过公共 getter 反射读取字段值（避免使用 setAccessible 修改访问权限修饰符）
     */
    private Object getFieldValueByGetter(Object obj, String fieldName) {
        if (obj == null || fieldName == null) {
            return null;
        }
        String suffix = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        try {
            return obj.getClass().getMethod("get" + suffix).invoke(obj);
        } catch (Exception e) {
            try {
                return obj.getClass().getMethod("is" + suffix).invoke(obj);
            } catch (Exception e2) {
                return null;
            }
        }
    }

}
