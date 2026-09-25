
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

import com.cosmo.hhim.common.core.text.UUID;
import com.cosmo.hhim.common.core.utils.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;

import java.math.BigDecimal;
import java.util.List;


/**
 * Excel的公式解析工具类
 *
 * @author cosmo-hhim-open Team
 * @date 2022-10-19
 */
public class ExcelFunctionUtil {

/*

    public static void main(String[] args) throws IOException {
        String[] strings = {"CONCATENATE(\"ABC\",%s+%s)", "CONCATENATE(\"AB000C-\",%s-%s)"};
        Object[] data = {1, 2};
        Object[] data2 = {5, 6};
        List<ExcelFunction> functions = new ArrayList<>();
        functions.add(new ExcelFunction("CONCATENATE(\"ABC\",1+1)", null));
        functions.add(new ExcelFunction("CONCATENATE(\"AB000C-\",%s-%s)", data2));
        List<ExcelFunction> excelFunctions = excelEvaluateFunctions(functions);
        System.out.println(StringUtils.getObjectString(excelFunctions));
    }
*/

    /**
     * 获取Excel公式的计算结果
     *
     * @param functions 方法公式集合，公式中的字段要和后面的数据顺序保持一致,注意占位符是%s
     * <p>
     * 例如：
     * Object[] data2 = {5, 6};
     * List<ExcelFunction> functions = new ArrayList<>();
     * functions.add(new ExcelFunction("CONCATENATE(\"ABC\",1+1)", null));
     * functions.add(new ExcelFunction("CONCATENATE(\"AB000C-\",%s-%s)", data2));
     */
    public static List<ExcelFunction> excelEvaluateFunctions(List<ExcelFunction> functions) {
        if (StringUtils.isEmpty(functions)) {
            throw new RuntimeException("数据不能为空");
        }
        functions.forEach(functionParam -> {
            String[] functionList = functionParam.getFunction().split("%s");
            if (StringUtils.isEmpty(functionParam.getData())) {
                functionParam.setData(new Object[]{});
            }
            if (functionList.length != functionParam.getData().length + 1) {
                throw new RuntimeException("公式中的占位符和输入的数据个数不匹配");
            }
        });
        Workbook hw = new HSSFWorkbook();
        Sheet hsheet = hw.createSheet("excel");
        Row hrow;
        Cell cell;
        String[] dataColumn = new String[functions.size()];
        for (int count = 0; count < functions.size(); count++) {
            String column = CellReference.convertNumToColString(count);//获取列名字
            ExcelFunction functionParam = functions.get(count);
            if (StringUtils.isEmpty(functionParam.getKey())) {
                functionParam.setKey(UUID.fastUUID().toString());
            }
            Object[] data = functionParam.getData();
            for (int i = 0; i < data.length; i++) {
                hrow = hsheet.createRow(i);
                cell = hrow.createCell(0);
                cell.setCellValue(data[i].toString());
                dataColumn[i] = column + data[i];
            }
            String function = String.format(functionParam.getFunction(), dataColumn);
            //最后一行是放function的
            hrow = hsheet.createRow(data.length + 1);
            cell = hrow.createCell(count);
            cell.setCellFormula(function);
            hsheet.setForceFormulaRecalculation(true);
            functionParam.setValue(getCellValue(cell, hw));
        }

        return functions;
    }

    //获取值
    public static Object getCellValue(Cell cellValue, Workbook hw) {
        Object value = null;
        switch (cellValue.getCellType()) {
            case BOOLEAN:
                value = cellValue.getBooleanCellValue();
                break;
            case NUMERIC:
                value = cellValue.getNumericCellValue();
                break;
            case STRING:
                value = cellValue.getStringCellValue();
                break;
            case BLANK:
                break;
            case ERROR:
                break;
            case FORMULA:
                FormulaEvaluator eva = hw.getCreationHelper().createFormulaEvaluator();
                CellValue evaluate = eva.evaluate(cellValue);
                value = getCellValue(evaluate, hw);
                break;
        }
        return value;
    }

    public static Object getCellValue(CellValue cellValue, Workbook hw) {
        Object value = null;
        switch (cellValue.getCellType()) {
            case BOOLEAN:
                value = cellValue.getBooleanValue();
                break;
            case NUMERIC:
                value = new BigDecimal(cellValue.getNumberValue()).stripTrailingZeros().toPlainString();
                break;
            case STRING:
                value = cellValue.getStringValue();
                break;
            case BLANK:
                break;
            case ERROR:
                break;
            case FORMULA:
                break;
        }
        return value;
    }
}


