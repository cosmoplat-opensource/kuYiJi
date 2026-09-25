/**
 * Copyright 2013-2015 JueYue (qrb.jueyue@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * Modifications by cosmo-hhim-open Team: 2026 - 适配 Ku易记 项目需求
 */
package com.cosmo.hhim.excel;

import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
import cn.afterturn.easypoi.excel.imports.sax.SaxReadExcel;
import cn.afterturn.easypoi.exception.excel.ExcelImportException;
import cn.afterturn.easypoi.handler.inter.IReadHandler;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Excel 导入工具
 * 
 * @author cosmo-hhim-open Team
 *  2013-9-24
 * @version 1.0
 */
@SuppressWarnings({ "unchecked" })
public class ExcelImportUtil {

    private ExcelImportUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelImportUtil.class);

    /**
     * Excel 导入 数据源本地文件,不返回校验结果 导入 字 段类型 Integer,Long,Double,Date,String,Boolean
     * 
     * @param file
     * @param pojoClass
     * @param params
     * @return
     */
    public static <T> List<T> importExcel(File file, Class<?> pojoClass, ImportParams params,int importRowNum) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            return new ExcelImportService().importExcelByIs(in, pojoClass, params, false,importRowNum).getList();
        } catch (ExcelImportException e) {
            throw new ExcelImportException(e.getType(), e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new ExcelImportException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * Excel 导入 数据源IO流,不返回校验结果 导入 字段类型 Integer,Long,Double,Date,String,Boolean
     * 
     * @param inputstream
     * @param pojoClass
     * @param params
     * @return
     * @throws Exception
     */
    public static <T> List<T> importExcel(InputStream inputstream, Class<?> pojoClass,
                                          ImportParams params,int importRowNum) throws Exception {
        return new ExcelImportService().importExcelByIs(inputstream, pojoClass, params, false,importRowNum).getList();
    }

    /**
     * Excel 导入 数据源IO流 字段类型 Integer,Long,Double,Date,String,Boolean
     * 支持校验,支持Key-Value
     * 
     * @param inputstream
     * @param pojoClass
     * @param params
     * @return
     * @throws Exception
     */
    public static <T> ExcelImportResult<T> importExcelMore(InputStream inputstream,
                                                             Class<?> pojoClass,
                                                             ImportParams params,int importRowNum) throws Exception {
        return new ExcelImportService().importExcelByIs(inputstream, pojoClass, params, true,importRowNum);
    }

    /**
     * Excel 导入 数据源本地文件 字段类型 Integer,Long,Double,Date,String,Boolean
     * 支持校验,支持Key-Value
     * @param file
     * @param pojoClass
     * @param params
     * @return
     */
    public static <T> ExcelImportResult<T> importExcelMore(File file, Class<?> pojoClass,
                                                             ImportParams params,int importRowNum) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            return new ExcelImportService().importExcelByIs(in, pojoClass, params, true,importRowNum);
        } catch (ExcelImportException e) {
            throw new ExcelImportException(e.getType(), e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new ExcelImportException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * Excel 通过SAX解析方法,适合大数据导入,不支持图片
     * 导入 数据源本地文件,不返回校验结果 导入 字 段类型 Integer,Long,Double,Date,String,Boolean
     * 
     * @param inputstream
     * @param pojoClass
     * @param params
     * @param handler
     */
    public static void importExcelBySax(InputStream inputstream, Class<?> pojoClass,
                                        ImportParams params, IReadHandler handler) {
        new SaxReadExcel().readExcel(inputstream, pojoClass, params, handler);
    }

    /**
     * Excel 导入 数据源本地文件,不返回校验结果 导入 字 段类型 Integer,Long,Double,Date,String,Boolean
     *
     * @param file
     * @param pojoClass
     * @param params
     * @return
     */
    public static <T> List<T> importExcel(MultipartFile file, Class<?> pojoClass, ImportParams params) {
        InputStream in = null;
        try {
            in = file.getInputStream();
            return new ExcelImportService().importExcelByIs(in, pojoClass, params, false).getList();
        } catch (ExcelImportException e) {
            throw new ExcelImportException(e.getType(), e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new ExcelImportException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * 根据列获取下标
     * @param str
     * @return
     */
    public static int getIndex(String str){
        str.toUpperCase();
        int len = str.length();
        int result = 0;
        for (int i = 0; i < len; i++) {
            char ch = str.charAt(len - i - 1);
            int num = ch - (int) 'A';
            num *= Math.pow(26, i);
            result += num;
        }
        return result;
    }

    public static String convert (Cell cell){
        String value = null;
        if(cell==null){
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                value = formatStringValue(cell);
                break;
            case FORMULA:// 公式
                value = formatFormulaValue(cell);
                break;
            case NUMERIC:
                value = formatDoubleValue(cell);
                break;
            case BOOLEAN:
                value = formatBooleanValue(cell);
                break;
            case BLANK:
                value = null;
                break;
            default:
                break;
        }
        return value;
    }


    private static String formatStringValue(Cell cell) {
        return cell == null ? null : cell.getStringCellValue();
    }

    private static String formatDoubleValue(Cell cell) {
        if(DateUtil.isCellDateFormatted(cell)){
            return new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
        }else {
            Double d = cell.getNumericCellValue();
            String s = d.toString();
            if (s.contains("E")) {
                cell.setCellType(CellType.STRING);
                return cell.getStringCellValue();
            } else {
                return s;
            }
        }
    }

    private static String formatBooleanValue(Cell cell) {
        Boolean b = cell.getBooleanCellValue();
        return String.valueOf(b);
    }

    private static String formatFormulaValue(Cell cell) {
        return cell.getCellFormula();
    }


}
