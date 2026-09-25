/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
//package com.cosmo.hhim.common.core.utils.poi;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.cosmo.hhim.common.core.annotation.Excel;
//import com.cosmo.hhim.common.core.annotation.Excel.ColumnType;
//import com.cosmo.hhim.common.core.annotation.Excel.Type;
//import com.cosmo.hhim.common.core.annotation.Excels;
//import com.cosmo.hhim.common.core.config.CosmoConfig;
//import com.cosmo.hhim.common.core.constant.CacheConstants;
//import com.cosmo.hhim.common.core.constant.Constants;
//import com.cosmo.hhim.common.core.constant.HttpStatus;
//import com.cosmo.hhim.common.core.domain.DictData;
//import com.cosmo.hhim.common.core.exception.CustomException;
//import com.cosmo.hhim.common.core.exception.WmsServiceException;
//import com.cosmo.hhim.common.core.text.Convert;
//import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
//import com.cosmo.hhim.common.core.utils.*;
//import com.cosmo.hhim.common.core.utils.file.FileTypeUtils;
//import com.cosmo.hhim.common.core.utils.file.ImageUtils;
//import com.cosmo.hhim.common.core.utils.reflect.ReflectUtils;
//import com.cosmo.hhim.common.core.web.domain.AjaxResult;
//import org.apache.poi.hssf.usermodel.DVConstraint;
//import org.apache.poi.ss.usermodel.BorderStyle;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.CellStyle;
//import org.apache.poi.ss.usermodel.CellType;
//import org.apache.poi.ss.usermodel.ClientAnchor;
//import org.apache.poi.ss.usermodel.DataValidation;
//import org.apache.poi.ss.usermodel.DataValidationConstraint;
//import org.apache.poi.ss.usermodel.DataValidationHelper;
//import org.apache.poi.ss.usermodel.DateUtil;
//import org.apache.poi.ss.usermodel.Drawing;
//import org.apache.poi.ss.usermodel.FillPatternType;
//import org.apache.poi.ss.usermodel.Font;
//import org.apache.poi.ss.usermodel.HorizontalAlignment;
//import org.apache.poi.ss.usermodel.IndexedColors;
//import org.apache.poi.ss.usermodel.Name;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.VerticalAlignment;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.ss.usermodel.WorkbookFactory;
//import org.apache.poi.ss.util.CellRangeAddressList;
//import org.apache.poi.xssf.streaming.SXSSFWorkbook;
//import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
//import org.apache.poi.xssf.usermodel.XSSFDataValidation;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.util.CollectionUtils;
//
//import javax.servlet.http.HttpServletResponse;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.lang.reflect.Field;
//import java.math.BigDecimal;
//import java.text.DecimalFormat;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Comparator;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.Set;
//import java.util.UUID;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.Collectors;
//
///**
// * Excel相关处理
// *
// * @author cosmo-hhim-open Team
// */
//public class ExcelUtil<T> {
//
//    private static final Logger log = LoggerFactory.getLogger(ExcelUtil.class);
//
//    /**
//     * Excel sheet最大行数，默认65536
//     */
//    public static final int SHEETSIZE = 65536;
//
//    //当前sheet号
//    public static int currentIndex = 0;
//
//    /**
//     * 默认字体
//     */
//    public static final String DEFULTFONTNAME = "Arial";
//
//    /**
//     * 工作表名称
//     */
//    private String sheetName;
//
//    /**
//     * 导出类型（EXPORT:导出数据；IMPORT：导入模板）
//     */
//    private Type type;
//
//    /**
//     * 工作薄对象
//     */
//    private Workbook wb;
//
//    /**
//     * 工作表对象
//     */
//    private Sheet sheet;
//
//    /**
//     * 样式列表
//     */
//    private Map<String, CellStyle> styles;
//
//    /**
//     * 导入导出数据列表
//     */
//    private List<T> list;
//
//    /**
//     * 注解列表
//     */
//    private List<Object[]> fields;
//
//    /**
//     * 最大高度
//     */
//    private short maxHeight;
//
//    /**
//     * 统计列表
//     */
//    private Map<Integer, Double> statistics = new HashMap<>();
//
//    /**
//     * 数字格式
//     */
//    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("######0.00");
//
//    /**
//     * 实体对象
//     */
//    public Class<T> clazz;
//
//    //下拉菜单等的起始行
//    private Integer firstRow;
//    //下拉菜单等的终止行
//    private Integer endRow;
//
//    /**
//     * 下拉map.
//     */
//    public Map<String, String[]> comboMap;
//
//    /**
//     * 下拉sheet页Map形式.
//     * <sheetName,List<Object>>
//     */
//    public Map<String, List<T>> comboSheetMap;
//
//    public ExcelUtil(Class<T> clazz) {
//        this.clazz = clazz;
//    }
//
//    public void init(List<T> list, String sheetName, Type type, Map<String, String[]> comboMap) {
//        if (list == null) {
//            list = new ArrayList<>();
//        }
//        if (comboMap == null) {
//            comboMap = new HashMap<>();
//        }
//        if (comboSheetMap == null) {
//            comboSheetMap = new HashMap<>();
//        }
//        this.list = list;
//        this.comboMap = comboMap;
//        this.sheetName = sheetName;
//        this.type = type;
//        createExcelField();
//        createWorkbook();
//    }
//
//    public void init(List<T> list, String sheetName, Type type, Map<String, String[]> comboMap, Map<String, List<T>> comboSheetMap, Integer firstRow, Integer endRow) {
//        if (list == null) {
//            list = new ArrayList<>();
//        }
//        if (comboSheetMap == null) {
//            comboSheetMap = new HashMap<>();
//        }
//        if (comboMap == null) {
//            comboMap = new HashMap<>();
//        }
//        this.list = list;
//        this.comboMap = comboMap;
//        this.comboSheetMap = comboSheetMap;
//        this.sheetName = sheetName;
//        this.type = type;
//        this.firstRow = firstRow;
//        this.endRow = endRow;
//        if (WmsCheckObjectUtils.isEmpty(firstRow)) {
//            this.firstRow = 1;
//        }
//        if (WmsCheckObjectUtils.isEmpty(endRow)) {
//            this.endRow = 200;
//        }
//        createExcelField();
//        createWorkbook();
//    }
//
//    /**
//     * 对excel表单默认第一个索引名转换成list
//     *
//     * @param is 输入流
//     * @return 转换后集合
//     */
//    public List<T> importExcel(InputStream is) throws Exception {
//        return importExcel(StringUtils.EMPTY, is, 1, null);
//    }
//
//    /**
//     * 指定sheetName转换成list
//     *
//     * @param sheetName
//     * @param is
//     * @return
//     * @throws Exception
//     */
//    public List<T> importExcel(String sheetName, InputStream is) throws Exception {
//        return importExcel(sheetName, is, 1, null);
//    }
//
//    /**
//     * 对excel表单默认第一个索引名转换成list
//     *
//     * @param is       输入流
//     * @param beginRow 开始行
//     * @param endRow   结束行
//     * @return 转换后集合
//     */
//    public List<T> importExcel(InputStream is, int beginRow, int endRow) throws Exception {
//        return importExcel(StringUtils.EMPTY, is, beginRow, endRow);
//    }
//
//    /**
//     * 对excel表单默认第一个索引名转换成list
//     *
//     * @param is       输入流
//     * @param beginRow 开始行
//     * @return 转换后集合
//     */
//    public List<T> importExcel(InputStream is, int beginRow) throws Exception {
//        return importExcel(StringUtils.EMPTY, is, beginRow, null);
//    }
//
//    /**
//     * 对excel表单指定表格索引名转换成list
//     *
//     * @param sheetName 表格索引名
//     * @param is        输入流
//     * @return 转换后集合
//     */
//    public List<T> importExcel(String sheetName, InputStream is, Integer beginRow, Integer endRow) throws Exception {
//        this.type = Type.IMPORT;
//        this.wb = WorkbookFactory.create(is);
//        List<T> list = new ArrayList<>();
//        Sheet sheet = null;
//        if (StringUtils.isNotEmpty(sheetName)) {
//            // 如果指定sheet名,则取指定sheet中的内容.
//            sheet = wb.getSheet(sheetName);
//        } else {
//            // 如果传入的sheet名不存在则默认指向第1个sheet.
//            sheet = wb.getSheetAt(0);
//        }
//
//        if (sheet == null) {
//            throw new IOException("文件sheet不存在");
//        }
//
//        int rows = sheet.getPhysicalNumberOfRows();
//        if (WmsCheckObjectUtils.isNotEmpty(endRow) && endRow < rows) {//指定的行数要小于总行数的时候才进行赋值
//            rows = endRow;
//        }
//
//        if (rows > 0) {
//            // 定义一个map用于存放excel列的序号和field.
//            Map<String, Integer> cellMap = new HashMap<>();
//            // 获取表头
//            Row heard = sheet.getRow(beginRow - 1);
//            for (int i = 0; i < heard.getPhysicalNumberOfCells(); i++) {
//                Cell cell = heard.getCell(i);
//                if (StringUtils.isNotNull(cell)) {
//                    String value = this.getCellValue(heard, i).toString();
//                    cellMap.put(value, i);
//                } else {
//                    cellMap.put(null, i);
//                }
//            }
//            // 有数据时才处理 得到类的所有field.
//            Field[] allFields = clazz.getDeclaredFields();
//            // 定义一个map用于存放列的序号和field.
//            Map<Integer, Field> fieldsMap = new HashMap<>();
//            for (int col = 0; col < allFields.length; col++) {
//                Field field = allFields[col];
//                Excel attr = field.getAnnotation(Excel.class);
//                if (attr != null && (attr.type() == Type.ALL || attr.type() == type)) {
//                    // 设置类的私有字段属性可访问.
//                    field.setAccessible(true);
//                    Integer column = cellMap.get(attr.name());
//                    if (column != null) {
//                        fieldsMap.put(column, field);
//                    }
//                }
//            }
//            for (int i = beginRow; i < rows; i++) {
//                // 从第2行开始取数据,默认第一行是表头.
//                Row row = sheet.getRow(i);
//                T entity = null;
//                Map<String, StringBuilder> requiredErrorMsg = new HashMap<>();//必填项的错误信息集合，<列名，错误信息>
//                for (Map.Entry<Integer, Field> entry : fieldsMap.entrySet()) {
//                    Object val = this.getCellValue(row, entry.getKey());
//
//                    // 如果不存在实例则新建.
//                    entity = (entity == null ? clazz.newInstance() : entity);
//                    // 从map中得到对应列的field.
//                    Field field = fieldsMap.get(entry.getKey());
//                    // 取得类型,并根据对象类型设置值.
//                    Class<?> fieldType = field.getType();
//                    if (String.class == fieldType) {
//                        String s = Convert.toStr(val);
//                        if (WmsCheckObjectUtils.isNotEmpty(s)) {
//                            s = s.trim();
//                        }
//                        if (StringUtils.endsWith(s, ".0")) {
//                            val = StringUtils.substringBefore(s, ".0");
//                        } else {
//                            String dateFormat = field.getAnnotation(Excel.class).dateFormat();
//                            if (StringUtils.isNotEmpty(dateFormat)) {
//                                val = DateUtils.parseDateToStr(dateFormat, (Date) val);
//                            } else {
//                                val = Convert.toStr(val);
//                            }
//                        }
//                    } else if ((Integer.TYPE == fieldType || Integer.class == fieldType) && StringUtils.isNumeric(Convert.toStr(val))) {
//                        val = Convert.toInt(val);
//                    } else if (Long.TYPE == fieldType || Long.class == fieldType) {
//                        val = Convert.toLong(val);
//                    } else if (Double.TYPE == fieldType || Double.class == fieldType) {
//                        val = Convert.toDouble(val);
//                    } else if (Float.TYPE == fieldType || Float.class == fieldType) {
//                        val = Convert.toFloat(val);
//                    } else if (BigDecimal.class == fieldType) {
//                        val = Convert.toBigDecimal(val);
//                    } else if (Date.class == fieldType) {
//                        if (val instanceof String) {
//                            val = DateUtils.parseDate(val);
//                        } else if (val instanceof Double) {
//                            val = DateUtil.getJavaDate((Double) val);
//                        }
//                    } else if (Boolean.TYPE == fieldType || Boolean.class == fieldType) {
//                        val = Convert.toBool(val, false);
//                    }
//                    if (StringUtils.isNotNull(fieldType)) {
//                        Excel attr = field.getAnnotation(Excel.class);
//                        String propertyName = field.getName();
//                        if (StringUtils.isNotEmpty(attr.targetAttr())) {
//                            propertyName = field.getName() + "." + attr.targetAttr();
//                        } else if (StringUtils.isNotEmpty(attr.readConverterExp())) {
//                            val = reverseByExp(Convert.toStr(val), attr.readConverterExp(), attr.separator());
//                        } else if (StringUtils.isNotEmpty(attr.dictType())) {
//                            val = reverseDictByExp(Convert.toStr(val), attr.dictType(), attr.separator());
//                        }
//                        ReflectUtils.invokeSetter(entity, propertyName, val);
//                        if (WmsCheckObjectUtils.isNotEmpty(attr.required()) && WmsCheckObjectUtils.isEmpty(val)) {//指定必填列
//                            StringBuilder errorMsg = new StringBuilder("");
//                            if (WmsCheckObjectUtils.isNotEmpty(requiredErrorMsg.get(attr.required()))) {
//                                errorMsg = requiredErrorMsg.get(attr.required());
//                            }
//                            errorMsg.append(attr.name());
//                            errorMsg.append("不能为空;");
//                            requiredErrorMsg.put(attr.required(), errorMsg);
//                        }
//                    }
//                    if ("errorMsg".equals(field.getName())) {
//                        ReflectUtils.invokeSetter(entity, field.getName(), "");
//                    }
//                }
//                if (WmsCheckObjectUtils.isNotEmpty(requiredErrorMsg)) {
//                    for (Map.Entry<String, StringBuilder> entry : requiredErrorMsg.entrySet()) {
//                        ReflectUtils.invokeSetter(entity, entry.getKey(), entry.getValue());
//                    }
//                }
//                list.add(entity);
//            }
//        }
//        return list;
//    }
//
//    /**
//     * 解析值字典值
//     *
//     * @param dictValue 字典标签
//     * @param dictType  字典类型
//     * @param separator 分隔符
//     * @return 字典值
//     */
//    public static String convertDictByExp(String dictValue, String dictType, String separator) {
//        StringBuilder propertyString = new StringBuilder();
//        List<DictData> datas = getDictCache(dictType);
//        if (datas == null || StringUtils.isBlank(dictValue)) {
//            return dictValue;
//        } else if (StringUtils.containsAny(separator, dictValue)) {
//            for (DictData dict : datas) {
//                for (String value : dictValue.split(separator)) {
//                    if (value.equals(dict.getDictValue())) {
//                        propertyString.append(dict.getDictLabel() + separator);
//                        break;
//                    }
//                }
//            }
//            return StringUtils.stripEnd(propertyString.toString(), separator);
//        } else {
//            for (DictData dict : datas) {
//                if (dictValue.equals(dict.getDictValue())) {
//                    return dict.getDictLabel();
//                }
//            }
//        }
//        return dictValue;
//    }
//
//    /**
//     * 反向解析值字典值
//     *
//     * @param dictLabel 字典标签
//     * @param dictType  字典类型
//     * @param separator 分隔符
//     * @return 字典值
//     */
//    public static String reverseDictByExp(String dictLabel, String dictType, String separator) {
//        StringBuilder propertyString = new StringBuilder();
//        List<DictData> datas = getDictCache(dictType);
////        System.out.println("dictType:" + dictType);
////        System.out.println("datas:" + (datas == null ? null : datas.stream().map(DictData::getDictLabel).collect(Collectors.joining())));
//        if (datas == null || StringUtils.isBlank(dictLabel)) {
//            return dictLabel;
//        } else if (StringUtils.containsAny(separator, dictLabel)) {
//            for (DictData dict : datas) {
//                for (String label : dictLabel.split(separator)) {
//                    if (label.equals(dict.getDictLabel())) {
//                        propertyString.append(dict.getDictValue() + separator);
//                        break;
//                    }
//                }
//            }
//            return StringUtils.stripEnd(propertyString.toString(), separator);
//        } else {
//            for (DictData dict : datas) {
//                if (dictLabel.equals(dict.getDictLabel())) {
//                    return dict.getDictValue();
//                }
//            }
//        }
//        return dictLabel;
//    }
//
//    // 获取字典缓存
//    public static List<DictData> getDictCache(String dictType) {
//        List<DictData> list = new ArrayList<>();
//        String[] split = dictType.split(",");
//        Arrays.stream(split).forEach(k -> {
//            String cacheString = SpringUtils.getBean(StringRedisTemplate.class).opsForValue().get(Constants.SYS_DICT_KEY + ThreadContext.get(Constants.TARGET_CUSTOMER) + k);
//            JSONArray cacheObj = JSONArray.parseArray(cacheString);
//            if (StringUtils.isNotNull(cacheObj)) {
//                List<DictData> dicts = cacheObj.stream().map(t -> {
//                    JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(t));
//                    jsonObject.remove("@type");
//                    return jsonObject.toJavaObject(DictData.class);
//                }).collect(Collectors.toList());
//                list.addAll(dicts);
//            }
//        });
//        return CheckObjectUtils.isNotEmpty(list) ? list : null;
//    }
//
//    /**
//     * 对list数据源将其里面的数据导入到excel表单-返回Workbook对象
//     *
//     * @param list      导出数据集合
//     * @param sheetName 工作表的名称
//     * @return 结果
//     * @throws IOException
//     */
//    public Workbook exportExcelWorkbook(ByteArrayOutputStream byteArrayOutputStream, List<T> list, String sheetName) throws IOException {
//        this.init(list, sheetName, Type.EXPORT, null);
//        this.exportExcelWorkbook(byteArrayOutputStream);
//        return wb;
//    }
//
//    /**
//     * 对list数据源将其里面的数据导入到excel表单-返回Workbook对象
//     *
//     * @param list      导出数据集合
//     * @param sheetName 工作表的名称
//     * @return 结果
//     * @throws IOException
//     */
//    public Workbook exportExcelWorkbook(ByteArrayOutputStream byteArrayOutputStream, List<T> list, String sheetName, Map<String, String[]> comboMap) throws IOException {
//        this.init(list, sheetName, Type.EXPORT, comboMap);
//        this.exportExcelWorkbook(byteArrayOutputStream);
//        return wb;
//    }
//
//
//    /**
//     * 对list数据源将其里面的数据导入到excel表单-返回Workbook对象
//     *
//     * @param headData  导出数据集合
//     * @param sheetName 工作表的名称
//     * @return 结果
//     * @throws IOException
//     */
//    public Workbook exportExcelWorkbook(
//            LinkedHashMap<Class<T>, List<T>> headData,
//            ByteArrayOutputStream byteArrayOutputStream,
//            String sheetName,
//            Map<String, String[]> comboMap,
//            Map<String, Map<Class<T>, List<T>>> comboSheetMap) throws IOException {
//        Map<String, List<T>> comboSheetMap1 = new HashMap<>();
//        for (String key : comboSheetMap.keySet()) {
//            for (Class<T> clazz : comboSheetMap.get(key).keySet()) {
//                comboSheetMap1.put(key, comboSheetMap.get(key).get(clazz));
//            }
//        }
//        this.init(null, sheetName, Type.EXPORT, null, comboSheetMap1, null, null);
//        int sheetNum = 0;
//        for (String key : comboSheetMap.keySet()) {
//            for (Class<T> clazz : comboSheetMap.get(key).keySet()) {
//                ExcelUtilExt<T> excelUtilExt = new ExcelUtilExt(clazz);
//                //创建字典sheet
//                Workbook workbook = excelUtilExt
//                        .exportExcelWorkbook(byteArrayOutputStream, comboSheetMap.get(key).get(clazz),
//                                key, this.wb, sheetNum, null, null);
//                currentIndex = workbook.getNumberOfSheets();
//            }
//            wb.setSheetHidden(sheetNum, true);
//            sheetNum++;
//        }
//        List<Class<T>> mapkey = new ArrayList<>(headData.keySet());
//        if (mapkey.size() > 1) {
//            for (int i = 0; i < mapkey.size() - 1; i++) {
//                ExcelUtilExt<T> excelUtilExt = new ExcelUtilExt(mapkey.get(i));
//                Workbook workbook = excelUtilExt.exportExcelWorkbook(byteArrayOutputStream, headData.get(mapkey.get(i)), sheetName, this.wb, sheetNum, 1, 1);
//                sheetNum++;
//                currentIndex = workbook.getNumberOfSheets();
//            }
//            this.list = headData.get(mapkey.get(mapkey.size() - 1));
//            if (this.list == null) {
//                this.list = new ArrayList<>();
//            }
//            this.firstRow = (mapkey.size() - 1) * 2 + 1;
//            this.endRow = SHEETSIZE;
//            this.exportExcelWorkbookEnd(byteArrayOutputStream, (mapkey.size() - 1) * 2);
//
//        } else {
//            this.exportExcelWorkbook(byteArrayOutputStream);
//        }
//        return wb;
//    }
//
//    /**
//     * 对list数据源将其里面的数据导入到excel表单-返回Workbook对象
//     *
//     * @param list      导出数据集合
//     * @param sheetName 工作表的名称
//     * @return 结果
//     * @throws IOException
//     */
//    public Workbook exportExcelWorkbook(ByteArrayOutputStream byteArrayOutputStream, List<T> list, String sheetName, Map<String, String[]> comboMap, Map<String, Map<Class<T>, List<T>>> comboSheetMap) throws IOException {
//        Map<String, List<T>> comboSheetMap1 = new HashMap<>();
//        for (String key : comboSheetMap.keySet()) {
//            for (Class<T> clazz : comboSheetMap.get(key).keySet()) {
//                comboSheetMap1.put(key, comboSheetMap.get(key).get(clazz));
//            }
//        }
//        this.init(list, sheetName, Type.EXPORT, null, comboSheetMap1, null, null);
//        int sheetNum = 0;
//        for (String key : comboSheetMap.keySet()) {
//            for (Class<T> clazz : comboSheetMap.get(key).keySet()) {
//                ExcelUtilExt<T> excelUtilExt = new ExcelUtilExt(clazz);
//                //创建字典sheet
//                Workbook workbook = excelUtilExt
//                        .exportExcelWorkbook(byteArrayOutputStream, comboSheetMap.get(key).get(clazz),
//                                key, this.wb, sheetNum, null, null);
//                currentIndex = workbook.getNumberOfSheets();
//            }
//            wb.setSheetHidden(sheetNum, true);
//            sheetNum++;
//        }
//        this.exportExcelWorkbook(byteArrayOutputStream);
//        return wb;
//    }
//
//    /**
//     * 对list数据源将其里面的数据导入到excel表单
//     *
//     * @param response  返回数据
//     * @param list      导出数据集合
//     * @param sheetName 工作表的名称
//     * @return 结果
//     * @throws IOException
//     */
//    public void exportExcel(HttpServletResponse response, List<T> list, String sheetName) throws IOException {
//        response.setContentType("application/vnd.ms-excel");
//        response.setCharacterEncoding("utf-8");
//        this.init(list, sheetName, Type.EXPORT, null);
//        exportExcel(response.getOutputStream());
//    }
//
//    /**
//     * 对list数据源将其里面的数据导入到excel表单
//     *
//     * @param response  返回数据
//     * @param list      导出数据集合
//     * @param sheetName 工作表的名称
//     * @return 结果
//     * @throws IOException
//     */
//    public void exportExcel(HttpServletResponse response, List<T> list, String sheetName, Map<String, String[]> comboMap) throws IOException {
//        response.setContentType("application/vnd.ms-excel");
//        response.setCharacterEncoding("utf-8");
//        this.init(list, sheetName, Type.EXPORT, comboMap);
//        exportExcel(response.getOutputStream());
//    }
//
//    /**
//     * 对list数据源将其里面的数据导入到excel表单
//     *
//     * @param sheetName 工作表的名称
//     * @return 结果
//     */
//    public void importTemplateExcel(HttpServletResponse response, String sheetName) throws IOException {
//        response.setContentType("application/vnd.ms-excel");
//        response.setCharacterEncoding("utf-8");
//        this.init(null, sheetName, Type.IMPORT, null);
//        exportExcel(response.getOutputStream());
//    }
//
//    /**
//     * 对list数据源将其里面的数据导入到excel表单
//     *
//     * @return 结果hhim-wms-modules/hhim-wms-system/pom.xml
//     */
//    public void exportExcelWorkbook(ByteArrayOutputStream byteArrayOutputStream) {
//        try {
//            // 取出一共有多少个sheet.
//            double sheetNo = Math.ceil(list.size() / SHEETSIZE);
//            for (int index = 0; index <= sheetNo; index++) {
//                createSheet(sheetNo, currentIndex + index);
//
//                // 产生一行
//                Row row = sheet.createRow(0);
//                int column = 0;
//                // 写入各个字段的列头名称
//                for (Object[] os : fields) {
//                    Excel excel = (Excel) os[1];
//                    this.createCell(excel, row, column++);
//                }
//                if (Type.EXPORT.equals(type)) {
//                    fillExcelData(index, row);
//                    addStatisticsRow();
//                }
//            }
//            //排序
//            for (int i = currentIndex; i < wb.getNumberOfSheets(); i++) {
//                Sheet sheetCur = wb.getSheetAt(i);
//                wb.setSheetOrder(sheetCur.getSheetName(), i - currentIndex);
//            }
//            wb.write(byteArrayOutputStream);
//        } catch (Exception e) {
//
//            log.error("导出Excel异常{}", e.getMessage());
//        } finally {
//            if (wb != null) {
//                try {
//                    wb.close();
//                } catch (IOException e1) {
//                }
//            }
//            if (byteArrayOutputStream != null) {
//                try {
//                    byteArrayOutputStream.close();
//                } catch (IOException e1) {
//
//                }
//            }
//        }
//    }
//
//    /**
//     * 对list数据源将其里面的数据导入到excel表单
//     *
//     * @return 结果hhim-wms-modules/hhim-wms-system/pom.xml
//     */
//    public void exportExcelWorkbookEnd(ByteArrayOutputStream byteArrayOutputStream, int rownum) {
//        try {
//            // 取出一共有多少个sheet.
//            double sheetNo = Math.ceil(list.size() / SHEETSIZE);
//            for (int index = 0; index <= sheetNo; index++) {
//                //createSheet(sheetNo, currentIndex);
//                this.styles = createStyles(wb);
//                // 设置工作表的名称.
//                if (currentIndex == 0) {
//                    this.sheet = wb.createSheet();
//                    wb.setSheetName(index, sheetName);
//                } else if (sheetNo == 0 && currentIndex != 0) {
//                    this.sheet = wb.getSheetAt(currentIndex - 1);
//                    //wb.setSheetName(index, sheetName);
//                } else {
//                    this.sheet = wb.createSheet();
//                    wb.setSheetName(index, sheetName + currentIndex);
//                }
//                // 产生一行
//                Row row = sheet.createRow(rownum);
//                int column = 0;
//                // 写入各个字段的列头名称
//                for (Object[] os : fields) {
//                    Excel excel = (Excel) os[1];
//                    this.createCell(excel, row, column++);
//                }
//
//                if (Type.EXPORT.equals(type)) {
//                    fillExcelDataEnd(rownum + 1, row);
//                    addStatisticsRow();
//                }
//            }
//            //排序
//            Sheet sheetCurE = wb.getSheetAt(currentIndex - 1);
//            Sheet sheetCurF = wb.getSheetAt(0);
//            wb.setSheetOrder(sheetCurE.getSheetName(), 0);
//            wb.setSheetOrder(sheetCurF.getSheetName(), currentIndex - 1);
//            wb.write(byteArrayOutputStream);
//        } catch (Exception e) {
//
//            log.error("导出Excel异常{}", e.getMessage());
//        } finally {
//            if (wb != null) {
//                try {
//                    wb.close();
//                } catch (IOException e1) {
//                }
//            }
//            if (byteArrayOutputStream != null) {
//                try {
//                    byteArrayOutputStream.close();
//                } catch (IOException e1) {
//
//                }
//            }
//        }
//    }
//
//    /**
//     * 对list数据源将其里面的数据导入到excel表单
//     *
//     * @return 结果
//     */
//    public void exportExcel(OutputStream outputStream) {
//        try {
//            // 取出一共有多少个sheet.
//            double sheetNo = Math.ceil(list.size() / SHEETSIZE);
//            for (int index = 0; index <= sheetNo; index++) {
//                createSheet(sheetNo, index);
//
//                // 产生一行
//                Row row = sheet.createRow(0);
//                int column = 0;
//                // 写入各个字段的列头名称
//                for (Object[] os : fields) {
//                    Excel excel = (Excel) os[1];
//                    this.createCell(excel, row, column++);
//                }
//                if (Type.EXPORT.equals(type)) {
//                    fillExcelData(index, row);
//                    addStatisticsRow();
//                }
//            }
//            wb.write(outputStream);
//        } catch (Exception e) {
//            log.error("导出Excel异常{}", e.getMessage());
//        } finally {
//            if (wb != null) {
//                try {
//                    wb.close();
//                } catch (IOException e1) {
//                }
//            }
//            if (outputStream != null) {
//                try {
//                    outputStream.close();
//                } catch (IOException e1) {
//
//                }
//            }
//        }
//    }
//
//    /**
//     * 填充excel数据
//     *
//     * @param index 序号
//     * @param row   单元格行
//     */
//    public void fillExcelData(int index, Row row) {
//        int startNo = index * SHEETSIZE;
//        int endNo = Math.min(startNo + SHEETSIZE, list.size());
//        for (int i = startNo; i < endNo; i++) {
//            row = sheet.createRow(i + 1 - startNo);
//            // 得到导出对象.
//            T vo = (T) list.get(i);
//            int column = 0;
//            for (Object[] os : fields) {
//                Field field = (Field) os[0];
//                Excel excel = (Excel) os[1];
//                // 设置实体类私有属性可访问
//                field.setAccessible(true);
//                this.addCell(excel, row, vo, field, column++);
//            }
//        }
//    }
//
//    /**
//     * 填充excel数据
//     *
//     * @param index 序号
//     * @param row   单元格行
//     */
//    public void fillExcelDataEnd(int index, Row row) {
//        int endNo = Math.min(SHEETSIZE, list.size());
//        for (int i = index; i < endNo + index; i++) {
//            row = sheet.createRow(i);
//            // 得到导出对象.
//            T vo = (T) list.get(i - index);
//            int column = 0;
//            for (Object[] os : fields) {
//                Field field = (Field) os[0];
//                Excel excel = (Excel) os[1];
//                // 设置实体类私有属性可访问
//                field.setAccessible(true);
//                this.addCell(excel, row, vo, field, column++);
//            }
//        }
//    }
//
//    /**
//     * 填充excel数据-字典数据
//     *
//     * @param index 序号
//     * @param row   单元格行
//     */
//    public void fillExcelData(int index, Row row, List<T> list) {
//        int startNo = index * SHEETSIZE;
//        int endNo = Math.min(startNo + SHEETSIZE, list.size());
//        for (int i = startNo; i < endNo; i++) {
//            row = sheet.createRow(i + 1 - startNo);
//            // 得到导出对象.
//            T vo = (T) list.get(i);
//            int column = 0;
//            for (Object[] os : fields) {
//                Field field = (Field) os[0];
//                Excel excel = (Excel) os[1];
//                // 设置实体类私有属性可访问
//                field.setAccessible(true);
//                this.addCell(excel, row, vo, field, column++);
//            }
//        }
//    }
//
//    /**
//     * 创建表格样式
//     *
//     * @param wb 工作薄对象
//     * @return 样式列表
//     */
//    private Map<String, CellStyle> createStyles(Workbook wb) {
//        // 写入各条记录,每条记录对应excel表中的一行
//        Map<String, CellStyle> styles = new HashMap<>();
//        CellStyle style = wb.createCellStyle();
//        style.setAlignment(HorizontalAlignment.CENTER);
//        style.setVerticalAlignment(VerticalAlignment.CENTER);
//        style.setBorderRight(BorderStyle.THIN);
//        style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
//        style.setBorderLeft(BorderStyle.THIN);
//        style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
//        style.setBorderTop(BorderStyle.THIN);
//        style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
//        style.setBorderBottom(BorderStyle.THIN);
//        style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
//        Font dataFont = wb.createFont();
//        dataFont.setFontName(DEFULTFONTNAME);
//        dataFont.setFontHeightInPoints((short) 10);
//        style.setFont(dataFont);
//        styles.put("data", style);
//
//        style = wb.createCellStyle();
//        style.cloneStyleFrom(styles.get("data"));
//        style.setAlignment(HorizontalAlignment.CENTER);
//        style.setVerticalAlignment(VerticalAlignment.CENTER);
//        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
//        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//        Font headerFont = wb.createFont();
//        headerFont.setFontName(DEFULTFONTNAME);
//        headerFont.setFontHeightInPoints((short) 10);
//        headerFont.setBold(true);
//        headerFont.setColor(IndexedColors.WHITE.getIndex());
//        style.setFont(headerFont);
//        styles.put("header", style);
//
//        style = wb.createCellStyle();
//        style.setAlignment(HorizontalAlignment.CENTER);
//        style.setVerticalAlignment(VerticalAlignment.CENTER);
//        Font totalFont = wb.createFont();
//        totalFont.setFontName(DEFULTFONTNAME);
//        totalFont.setFontHeightInPoints((short) 10);
//        style.setFont(totalFont);
//        styles.put("total", style);
//
//        style = wb.createCellStyle();
//        style.cloneStyleFrom(styles.get("data"));
//        style.setAlignment(HorizontalAlignment.LEFT);
//        styles.put("data1", style);
//
//        style = wb.createCellStyle();
//        style.cloneStyleFrom(styles.get("data"));
//        style.setAlignment(HorizontalAlignment.CENTER);
//        styles.put("data2", style);
//
//        style = wb.createCellStyle();
//        style.cloneStyleFrom(styles.get("data"));
//        style.setAlignment(HorizontalAlignment.RIGHT);
//        styles.put("data3", style);
//
//        return styles;
//    }
//
//    public Cell createCell(Excel attr, Row row, int column) {
//        return createCell(attr, row, column, false);
//    }
//
//    /**
//     * 创建单元格
//     *
//     * @param attr
//     * @param row
//     * @param column
//     * @param isTemplate 是否是模板导出
//     * @return
//     */
//    public Cell createCell(Excel attr, Row row, int column, boolean isTemplate) {
//        // 创建列
//        Cell cell = row.createCell(column);
//        // 写入列信息
//        cell.setCellValue(attr.name());
//        setDataValidation(attr, row, column, isTemplate);
//        //如果是表头是必填项设置颜色是红色
//
//        if (WmsCheckObjectUtils.isNotEmpty(attr.required())) {
//            CellStyle style = wb.createCellStyle();
//            style.setAlignment(HorizontalAlignment.CENTER);
//            style.setVerticalAlignment(VerticalAlignment.CENTER);
//            style.setBorderRight(BorderStyle.THIN);
//            style.setRightBorderColor(IndexedColors.RED.getIndex());
//            style.setBorderLeft(BorderStyle.THIN);
//            style.setLeftBorderColor(IndexedColors.RED.getIndex());
//            style.setBorderTop(BorderStyle.THIN);
//            style.setTopBorderColor(IndexedColors.RED.getIndex());
//            style.setBorderBottom(BorderStyle.THIN);
//            style.setBottomBorderColor(IndexedColors.RED.getIndex());
//            style.setAlignment(HorizontalAlignment.CENTER);
//            style.setVerticalAlignment(VerticalAlignment.CENTER);
//            style.setFillForegroundColor(IndexedColors.RED.getIndex());
//            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//            Font headerFont = wb.createFont();
//            headerFont.setFontName(DEFULTFONTNAME);
//            headerFont.setFontHeightInPoints((short) 10);
//            headerFont.setBold(true);
//            headerFont.setColor(IndexedColors.WHITE.getIndex());
//            style.setFont(headerFont);
//            cell.setCellStyle(style);
//        } else {
//            CellStyle cellStyle = styles.get("header");
//            cell.setCellStyle(cellStyle);
//        }
//
//        return cell;
//    }
//
//    /**
//     * 设置单元格信息
//     *
//     * @param value 单元格值
//     * @param attr  注解相关
//     * @param cell  单元格信息
//     */
//    public void setCellVo(Object value, Excel attr, Cell cell) {
//        if (ColumnType.STRING == attr.cellType()) {
//            cell.setCellValue(StringUtils.isNull(value) ? attr.defaultValue() : value + attr.suffix());
//        } else if (ColumnType.NUMERIC == attr.cellType()) {
//            cell.setCellValue(StringUtils.contains(Convert.toStr(value), ".") ? Convert.toDouble(value) : Convert.toInt(value));
//        } else if (ColumnType.IMAGE == attr.cellType()) {
//            ClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, (short) cell.getColumnIndex(), cell.getRow().getRowNum(), (short) (cell.getColumnIndex() + 1),
//                    cell.getRow().getRowNum() + 1);
//            String imagePath = Convert.toStr(value);
//            if (StringUtils.isNotEmpty(imagePath)) {
//                byte[] data = ImageUtils.getImage(imagePath);
//                getDrawingPatriarch(cell.getSheet()).createPicture(anchor,
//                        cell.getSheet().getWorkbook().addPicture(data, getImageType(data)));
//            }
//        }
//
//        // 数据脱敏 add by zyh
//        cellValueMask(attr, cell);
//
//    }
//
//    /**
//     * 获取画布
//     */
//    public static Drawing<?> getDrawingPatriarch(Sheet sheet) {
//        if (sheet.getDrawingPatriarch() == null) {
//            sheet.createDrawingPatriarch();
//        }
//        return sheet.getDrawingPatriarch();
//    }
//
//    /**
//     * 获取图片类型,设置图片插入类型
//     */
//    public int getImageType(byte[] value) {
//        String type = FileTypeUtils.getFileExtendName(value);
//        if ("JPG".equalsIgnoreCase(type)) {
//            return Workbook.PICTURE_TYPE_JPEG;
//        } else if ("PNG".equalsIgnoreCase(type)) {
//            return Workbook.PICTURE_TYPE_PNG;
//        }
//        return Workbook.PICTURE_TYPE_JPEG;
//    }
//
//    /**
//     * 创建表格样式
//     */
//    public void setDataValidation(Excel attr, Row row, int column, boolean isTemplate) {
//        if (column == 0) {//表头行高
//            row.setHeight((short) 480);
//        }
//
//        if (attr.name().indexOf("注：") >= 0) {
//            sheet.setColumnWidth(column, 6000);
//        } else {
//            // 设置列宽
//            sheet.setColumnWidth(column, (int) ((attr.width() + 0.72) * 256));
//        }
//        // 如果设置了提示信息则鼠标放上去提示.
//        if (StringUtils.isNotEmpty(attr.prompt())) {
//            // 这里默认设了2-101列提示.
//            setXSSFPrompt(sheet, "", attr.prompt(), 1, 100, column, column);
//        }
//        if (StringUtils.isNotEmpty(attr.maxStringInfo()) && attr.maxStringInfo().length == 2) {
//            this.setXSSFLengthError(
//                    sheet,
//                    "文本超长",
//                    attr.name() + attr.maxStringInfo()[1],
//                    firstRow,
//                    endRow,
//                    column,
//                    column,
//                    attr.maxStringInfo()[0]);
//        }
//
//        // 如果设置了combo属性则本列只能选择不能输入
//        if (attr.combo().length > 0) {
//            // 这里默认设了2-101列只能选择不能输入.
//            setXSSFValidation(sheet, attr.combo(), 1, 100, column, column);
//        }
//        // 如果设置了comboName属性则本列只能选择不能输入
//        if (WmsCheckObjectUtils.isNotEmpty(attr.comboName())) {
//            String[] textlist = new String[]{};
//            if (WmsCheckObjectUtils.isNotEmpty(comboMap.get(attr.comboName()))) {
//                textlist = comboMap.get(attr.comboName());
//                if (textlist.length > 232) {
//                    throw new WmsServiceException("下拉选择项不能超过232个，请使用Sheet页引入的方式");
//                }
//            }
//            // 这里默认设了2-101列只能选择不能输入.
//            setXSSFValidation(sheet, textlist, 1, SHEETSIZE, column, column);
//        }
//        // 如果设置了字典,则本列只能选择字典不能输入
//        if (isTemplate && StringUtils.isNotEmpty(attr.dictType())) {
//            List<DictData> dicts = getDictCache(attr.dictType());
//            if (dicts != null && dicts.size() > 0) {
//                String[] combo = dicts.stream().map(DictData::getDictLabel).toArray(String[]::new);
//                log.info(Arrays.toString(combo));
//                // 这里默认设了2-101列只能选择不能输入.
//                setXSSFValidation(sheet, combo, 1, 100, column, column);
//            }
//        }
//        // 如果设置了comboSheetName属性则本列只能选择，从sheet页中选取
//        if (WmsCheckObjectUtils.isNotEmpty(attr.dictionarySheetInfo()) && attr.dictionarySheetInfo().length == 2) {
//            // 这里默认设了2-101列只能选择不能输入.
//            setXSSFValidation(
//                    sheet,
//                    attr.dictionarySheetInfo()[0],
//                    attr.dictionarySheetInfo()[1],
//                    firstRow,
//                    endRow,
//                    column,
//                    column,
//                    wb.getSheet(attr.dictionarySheetInfo()[0]).getPhysicalNumberOfRows());
//        }
//        if (WmsCheckObjectUtils.isNotEmpty(attr.dictionaryCascadeSheetInfo()) && attr.dictionaryCascadeSheetInfo().length > 1) {
//            //DownloadTemplate 级联下拉列表
//            String[] cascadeSheetInfo = attr.dictionaryCascadeSheetInfo();
//            addValidationToSheet(wb, sheet, cascadeSheetInfo, column, column, firstRow, endRow);
//        }
//
//
//    }
//
//    /**
//     * 给sheet页  添加级联下拉列表
//     *
//     * @param workbook    excel
//     * @param targetSheet sheet页
//     * @param options     要添加的下拉列表内容  ， keys 是下拉列表1中的内容，每个Map.Entry.Value 是对应的级联下拉列表内容
//     * @param firstCol    下拉列表1位置
//     * @param endCol      级联下拉列表位置
//     * @param fromRow     级联限制开始行
//     * @param endRow      级联限制结束行
//     */
//    public static void addValidationToSheet(
//            Workbook workbook,
//            Sheet targetSheet,
//            //Map<String, List<String>> options,
//            String[] options,
//            int firstCol,
//            int endCol,
//            int fromRow,
//            int endRow) {
//        //String hiddenSheetName = "sheet" + workbook.getNumberOfSheets();
//        //Sheet hiddenSheet = workbook.createSheet(hiddenSheetName);
//        int rowIndex = 2;
//        //for (Map.Entry<String, List<String>> entry : options.entrySet()) {
//        for (int i = 0; i < options.length; i = i + 3) {
//            String parent = options[i];
//            // 加载下拉列表内容
//            DataValidationHelper helper = targetSheet.getDataValidationHelper();
//            String cell1 = "INDIRECT(" + "B" + "2)";
//            DataValidationConstraint constraint = helper.createFormulaListConstraint(cell1);
//            // 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
//            CellRangeAddressList regions = new CellRangeAddressList(fromRow, endRow, firstCol, endCol);
//            // 数据有效性对象
//            int lastChildrenColumn = workbook.getSheet(parent).getPhysicalNumberOfRows();
//            createName(workbook, options[i + 2], String.format(parent + "!$%s$%s:$%s$%s", options[i + 1], rowIndex, options[i + 1], lastChildrenColumn));
//            DataValidation dataValidation = helper.createValidation(constraint, regions);
//            // 处理Excel兼容性问题
//            if (dataValidation instanceof XSSFDataValidation) {
//                dataValidation.setSuppressDropDownArrow(true);
//                dataValidation.setShowErrorBox(true);
//            } else {
//                dataValidation.setSuppressDropDownArrow(false);
//            }
//            targetSheet.addValidationData(dataValidation);
//        }
//    }
//
//    private static Name createName(Workbook workbook, String nameName, String formula) {
//        Name name = workbook.createName();
//        name.setNameName(nameName);
//        name.setRefersToFormula(formula);
//        return name;
//    }
//
//    /**
//     * 添加单元格
//     */
//    public Cell addCell(Excel attr, Row row, T vo, Field field, int column) {
//        Cell cell = null;
//        try {
//            // 设置行高
//            row.setHeight(maxHeight);
//            // 根据Excel中设置情况决定是否导出,有些情况需要保持为空,希望用户填写这一列.
//            if (attr.isExport()) {
//                // 创建cell
//                cell = row.createCell(column);
//                int align = attr.align().value();
//                cell.setCellStyle(styles.get("data" + (align >= 1 && align <= 3 ? align : "")));
//
//                // 用于读取对象中的属性
//                Object value = getTargetValue(vo, field, attr);
//                String dateFormat = attr.dateFormat();
//                String readConverterExp = attr.readConverterExp();
//                String separator = attr.separator();
//                if (StringUtils.isNotEmpty(dateFormat) && StringUtils.isNotNull(value)) {
//                    cell.setCellValue(DateUtils.parseDateToStr(dateFormat, (Date) value));
//                } else if (StringUtils.isNotEmpty(readConverterExp) && StringUtils.isNotNull(value)) {
//                    cell.setCellValue(convertByExp(Convert.toStr(value), readConverterExp, separator));
//                } else if (value instanceof BigDecimal && -1 != attr.scale()) {
//                    cell.setCellValue((((BigDecimal) value).setScale(attr.scale(), attr.roundingMode())).toString());
//                } else if (StringUtils.isNotEmpty(attr.dictType())) {
//                    cell.setCellValue(convertDictByExp(Convert.toStr(value), attr.dictType(), separator));
//                } else {
//                    // 设置列类型
//                    setCellVo(value, attr, cell);
//                }
//                addStatisticsData(column, Convert.toStr(value), attr);
//
//                // 数据脱敏 add by zyh
//                cellValueMask(attr, cell);
//            }
//        } catch (Exception e) {
//            log.error("导出Excel失败{}", e);
//        }
//        return cell;
//    }
//
//
//    /**
//     * 设置 POI XSSFSheet 单元格提示
//     *
//     * @param sheet         表单
//     * @param promptTitle   提示标题
//     * @param promptContent 提示内容
//     * @param firstRow      开始行
//     * @param endRow        结束行
//     * @param firstCol      开始列
//     * @param endCol        结束列
//     */
//    public void setXSSFPrompt(Sheet sheet, String promptTitle, String promptContent, int firstRow, int endRow,
//                              int firstCol, int endCol) {
//        DataValidationHelper helper = sheet.getDataValidationHelper();
//        DataValidationConstraint constraint = helper.createCustomConstraint("DD1");
//        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
//        DataValidation dataValidation = helper.createValidation(constraint, regions);
//        dataValidation.createPromptBox(promptTitle, promptContent);
//        dataValidation.setShowPromptBox(true);
//        sheet.addValidationData(dataValidation);
//    }
//
//    /**
//     * 设置 POI 长度警告
//     *
//     * @param sheet         表单
//     * @param promptTitle   提示标题
//     * @param promptContent 提示内容
//     * @param firstRow      开始行
//     * @param endRow        结束行
//     * @param firstCol      开始列
//     * @param endCol        结束列
//     * @param maxLength     最大长度
//     */
//    public void setXSSFLengthError(
//            Sheet sheet,
//            String promptTitle,
//            String promptContent,
//            int firstRow,
//            int endRow,
//            int firstCol,
//            int endCol,
//            String maxLength) {
//        DataValidationHelper helper = sheet.getDataValidationHelper();
//        DataValidationConstraint constraint = helper.createNumericConstraint(
//                DVConstraint.ValidationType.TEXT_LENGTH,
//                DVConstraint.OperatorType.BETWEEN, "1", maxLength);
//        // 设定在哪个单元格生效
//        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
//        DataValidation dataValidation = helper.createValidation(constraint, regions);
//        dataValidation.createErrorBox(promptTitle, promptContent);
//        dataValidation.setShowErrorBox(true);
//        sheet.addValidationData(dataValidation);
//    }
//
//    /**
//     * 设置某些列的值只能输入预制的数据,显示下拉框.
//     *
//     * @param sheet    要设置的sheet.
//     * @param textlist 下拉框显示的内容
//     * @param firstRow 开始行
//     * @param endRow   结束行
//     * @param firstCol 开始列
//     * @param endCol   结束列
//     * @return 设置好的sheet.
//     */
//    public void setXSSFValidation(Sheet sheet, String[] textlist, int firstRow, int endRow, int firstCol, int endCol) {
//        if (WmsCheckObjectUtils.isEmpty(textlist)) {
//            return;
//        }
//        DataValidationHelper helper = sheet.getDataValidationHelper();
//        // 加载下拉列表内容
//        DataValidationConstraint constraint = helper.createExplicitListConstraint(textlist);
//        // 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
//        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
//        // 数据有效性对象
//        DataValidation dataValidation = helper.createValidation(constraint, regions);
//        // 处理Excel兼容性问题
//        if (dataValidation instanceof XSSFDataValidation) {
//            dataValidation.setSuppressDropDownArrow(true);
//            dataValidation.setShowErrorBox(true);
//        } else {
//            dataValidation.setSuppressDropDownArrow(false);
//        }
//
//        sheet.addValidationData(dataValidation);
//    }
//
//    /**
//     * 设置某些列的值只能sheet中某列输入预制的数据,显示下拉框.
//     *
//     * @param sheet                模板sheet页（需要设置下拉框的sheet）
//     * @param dictionarySheetName  隐藏的sheet页，用于存放下拉框的值 （下拉框值对应一列）
//     * @param dictionaryLastRow    存放下拉框值的最后一行
//     * @param dictionaryColumnName 存放下拉框值的列名 "A"
//     * @param firstRow             添加下拉框对应开始行
//     * @param endRow               添加下拉框对应结束行
//     * @param firstCol             添加下拉框对应开始列
//     * @param endCol               添加下拉框对应结束列
//     */
//    public void setXSSFValidation(Sheet sheet, String dictionarySheetName,
//                                  String dictionaryColumnName, int firstRow, int endRow, int firstCol, int endCol,
//                                  int dictionaryLastRow) {
//        //设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
//        // 创建名称，可被其他单元格引用
//
//        // 加载下拉列表内容
//        DataValidationHelper helper = sheet.getDataValidationHelper();
//        String cell1 =
//                "" + dictionarySheetName + "!$" + dictionaryColumnName + "$2:$" + dictionaryColumnName
//                        + "$" + dictionaryLastRow + "";
//        DataValidationConstraint constraint = helper.createFormulaListConstraint(cell1);
//        // 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
//        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
//        // 数据有效性对象
//        DataValidation dataValidation = helper.createValidation(constraint, regions);
//        // 处理Excel兼容性问题
//        if (dataValidation instanceof XSSFDataValidation) {
//            dataValidation.setSuppressDropDownArrow(true);
//            dataValidation.setShowErrorBox(true);
//        } else {
//            dataValidation.setSuppressDropDownArrow(false);
//        }
//        sheet.addValidationData(dataValidation);
//    }
//
//    /**
//     * 解析导出值 0=男,1=女,2=未知
//     *
//     * @param propertyValue 参数值
//     * @param converterExp  翻译注解
//     * @param separator     分隔符
//     * @return 解析后值
//     */
//    public static String convertByExp(String propertyValue, String converterExp, String separator) {
//        StringBuilder propertyString = new StringBuilder();
//        String[] convertSource = converterExp.split(",");
//        for (String item : convertSource) {
//            String[] itemArray = item.split("=");
//            if (StringUtils.containsAny(separator, propertyValue)) {
//                for (String value : propertyValue.split(separator)) {
//                    if (itemArray[0].equals(value)) {
//                        propertyString.append(itemArray[1] + separator);
//                        break;
//                    }
//                }
//            } else {
//                if (itemArray[0].equals(propertyValue)) {
//                    return itemArray[1];
//                }
//            }
//        }
//        return StringUtils.stripEnd(propertyString.toString(), separator);
//    }
//
//    /**
//     * 反向解析值 男=0,女=1,未知=2
//     *
//     * @param propertyValue 参数值
//     * @param converterExp  翻译注解
//     * @param separator     分隔符
//     * @return 解析后值
//     */
//    public static String reverseByExp(String propertyValue, String converterExp, String separator) {
//        StringBuilder propertyString = new StringBuilder();
//        String[] convertSource = converterExp.split(",");
//        for (String item : convertSource) {
//            String[] itemArray = item.split("=");
//            if (StringUtils.containsAny(separator, propertyValue)) {
//                for (String value : propertyValue.split(separator)) {
//                    if (itemArray[1].equals(value)) {
//                        propertyString.append(itemArray[0] + separator);
//                        break;
//                    }
//                }
//            } else {
//                if (itemArray[1].equals(propertyValue)) {
//                    return itemArray[0];
//                }
//            }
//        }
//        return StringUtils.stripEnd(propertyString.toString(), separator);
//    }
//
//    /**
//     * 合计统计信息
//     */
//    private void addStatisticsData(Integer index, String text, Excel entity) {
//        if (entity != null && entity.isStatistics()) {
//            Double temp = 0D;
//            if (!statistics.containsKey(index)) {
//                statistics.put(index, temp);
//            }
//            try {
//                temp = Double.valueOf(text);
//            } catch (NumberFormatException e) {
//            }
//            statistics.put(index, statistics.get(index) + temp);
//        }
//    }
//
//    /**
//     * 创建统计行
//     */
//    public void addStatisticsRow() {
//        if (statistics.size() > 0) {
//            Cell cell = null;
//            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
//            Set<Integer> keys = statistics.keySet();
//            cell = row.createCell(0);
//            cell.setCellStyle(styles.get("total"));
//            cell.setCellValue("合计");
//
//            for (Integer key : keys) {
//                cell = row.createCell(key);
//                cell.setCellStyle(styles.get("total"));
//                cell.setCellValue(DOUBLE_FORMAT.format(statistics.get(key)));
//            }
//            statistics.clear();
//        }
//    }
//
//    /**
//     * 获取bean中的属性值
//     *
//     * @param vo    实体对象
//     * @param field 字段
//     * @param excel 注解
//     * @return 最终的属性值
//     * @throws Exception
//     */
//    private Object getTargetValue(T vo, Field field, Excel excel) throws Exception {
//        Object o = field.get(vo);
//        if (StringUtils.isNotEmpty(excel.targetAttr())) {
//            String target = excel.targetAttr();
//            if (target.indexOf(".") > -1) {
//                String[] targets = target.split("[.]");
//                for (String name : targets) {
//                    o = getValue(o, name);
//                }
//            } else {
//                o = getValue(o, target);
//            }
//        }
//        return o;
//    }
//
//    /**
//     * 以类的属性的get方法方法形式获取值
//     *
//     * @param o
//     * @param name
//     * @return value
//     * @throws Exception
//     */
//    private Object getValue(Object o, String name) throws Exception {
//        if (StringUtils.isNotNull(o) && StringUtils.isNotEmpty(name)) {
//            Class<?> clazz = o.getClass();
//            Field field = clazz.getDeclaredField(name);
//            field.setAccessible(true);
//            o = field.get(o);
//        }
//        return o;
//    }
//
//    /**
//     * 得到所有定义字段
//     */
//    private void createExcelField() {
//        this.fields = new ArrayList<>();
//        List<Field> tempFields = new ArrayList<>();
//        tempFields.addAll(Arrays.asList(clazz.getSuperclass().getDeclaredFields()));
//        tempFields.addAll(Arrays.asList(clazz.getDeclaredFields()));
//        for (Field field : tempFields) {
//            // 单注解
//            if (field.isAnnotationPresent(Excel.class)) {
//                putToField(field, field.getAnnotation(Excel.class));
//            }
//
//            // 多注解
//            if (field.isAnnotationPresent(Excels.class)) {
//                Excels attrs = field.getAnnotation(Excels.class);
//                Excel[] excels = attrs.value();
//                for (Excel excel : excels) {
//                    putToField(field, excel);
//                }
//            }
//        }
//        this.fields = this.fields.stream().sorted(Comparator.comparing(objects -> ((Excel) objects[1]).sort())).collect(Collectors.toList());
//        this.maxHeight = getRowHeight();
//    }
//
//    /**
//     * 根据注解获取最大行高
//     */
//    public short getRowHeight() {
//        double maxHeight = 0;
//        for (Object[] os : this.fields) {
//            Excel excel = (Excel) os[1];
//            maxHeight = maxHeight > excel.height() ? maxHeight : excel.height();
//        }
//        return (short) (maxHeight * 20);
//    }
//
//    /**
//     * 放到字段集合中
//     */
//    private void putToField(Field field, Excel attr) {
//        if (attr != null && (attr.type() == Type.ALL || attr.type() == type)) {
//            this.fields.add(new Object[]{field, attr});
//        }
//    }
//
//    /**
//     * 创建一个工作簿
//     */
//    public void createWorkbook() {
//        this.wb = new SXSSFWorkbook(500);
//    }
//
//    /**
//     * 创建工作表
//     *
//     * @param sheetNo sheet数量
//     * @param index   序号
//     */
//    public void createSheet(double sheetNo, int index) {
//        this.sheet = wb.createSheet();
//        this.styles = createStyles(wb);
//        // 设置工作表的名称.
//        if (sheetNo == 0) {
//            wb.setSheetName(index, sheetName);
//        } else {
//            wb.setSheetName(index, sheetName + index);
//        }
//    }
//
//    /**
//     * 创建工作表
//     *
//     * @param sheetName sheet名称
//     * @param index     序号
//     */
//    public void createDictionarySheet(String sheetName, int index) {
//        this.sheet = wb.createSheet();
//        this.styles = createStyles(wb);
//        // 设置工作表的名称.
//        wb.setSheetName(index, sheetName);
//    }
//
//    /**
//     * 获取单元格值
//     *
//     * @param row    获取的行
//     * @param column 获取单元格列号
//     * @return 单元格值
//     */
//    public Object getCellValue(Row row, int column) {
//        if (row == null) {
//            return row;
//        }
//        Object val = "";
//        try {
//            Cell cell = row.getCell(column);
//            if (StringUtils.isNotNull(cell)) {
//                if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
//                    val = cell.getNumericCellValue();
//                    if (DateUtil.isCellDateFormatted(cell)) {
//                        val = DateUtil.getJavaDate((Double) val); // POI Excel 日期格式转换
//                    } else {
//                        if ((Double) val % 1 != 0) {
//                            val = new BigDecimal(val.toString());
//                        } else {
//                            val = new DecimalFormat("0").format(val);
//                        }
//                    }
//                } else if (cell.getCellType() == CellType.STRING) {
//                    val = cell.getStringCellValue();
//                } else if (cell.getCellType() == CellType.BOOLEAN) {
//                    val = cell.getBooleanCellValue();
//                } else if (cell.getCellType() == CellType.ERROR) {
//                    val = cell.getErrorCellValue();
//                }
//
//            }
//        } catch (Exception e) {
//            return val;
//        }
//        return val;
//    }
//
//    /**
//     * 多sheet页导出
//     *
//     * @param outputStream
//     * @param dataClass    key: sheetName value: 对应数据类型  ps:sheet页循环对象※※※※※
//     * @param data         key: sheetName value: 对应数据
//     */
//    public void exportExcelCustomize(OutputStream outputStream, LinkedHashMap<String, Class> dataClass, LinkedHashMap<String, List<T>> data) {
//        // init
//        this.list = new ArrayList<>();
//        this.comboMap = new HashMap<>();
//        this.type = Type.EXPORT;
//        createWorkbook();
//        try {
//            // export
//            AtomicInteger index = new AtomicInteger();
//            dataClass.forEach((k, v) -> {
//                this.clazz = v;
//                this.list = data.get(k);
//                this.sheetName = k;
//                createExcelField();
//                createDictionarySheet(k, index.intValue());
//                // 产生一行
//                Row row = sheet.createRow(0);
//                int column = 0;
//                // 写入各个字段的列头名称
//                for (Object[] os : fields) {
//                    Excel excel = (Excel) os[1];
//                    this.createCell(excel, row, column++, true);
//                }
//                if (Type.EXPORT.equals(type)) {
//                    // 每个sheet页从第一行写数据
//                    fillExcelData(0, row);
//                    addStatisticsRow();
//                }
//                index.getAndIncrement();
//            });
//            wb.write(outputStream);
//        } catch (Exception e) {
//            log.error("导出Excel异常{}", e.getMessage());
//        } finally {
//            if (wb != null) {
//                try {
//                    wb.close();
//                } catch (IOException e1) {
//
//                }
//            }
//            if (outputStream != null) {
//                try {
//                    outputStream.close();
//                } catch (IOException e1) {
//
//                }
//            }
//        }
//    }
/////////////////////////////////// srm-excel start///////////////////////////////
//
//    /**
//     * 对list数据源将其里面的数据导入到excel表单
//     *
//     * @param list      导出数据集合
//     * @param sheetName 工作表的名称
//     * @return 结果
//     */
//    public AjaxResult exportExcel(List<T> list, String sheetName) {
//        this.init(list, sheetName, Type.EXPORT);
//        return exportExcel();
//    }
//
//
//    public void init(List<T> list, String sheetName, Type type) {
//        if (list == null) {
//            list = new ArrayList<T>();
//        }
//        this.list = list;
//        this.sheetName = sheetName;
//        this.type = type;
//        createExcelField();
//        createWorkbook();
//    }
//
//    /**
//     * 对list数据源将其里面的数据导入到excel表单
//     *
//     * @return 结果
//     */
//    public AjaxResult exportExcel() {
//        return exportExcel(false);
//    }
//
//    /**
//     * 对list数据源将其里面的数据导入到excel表单
//     *
//     * @return 结果
//     */
//    public AjaxResult exportExcel(boolean isTemplate) {
//        OutputStream out = null;
//        try {
//            // 取出一共有多少个sheet.
//            double sheetNo = Math.ceil(list.size() / SHEETSIZE);
//            for (int index = 0; index <= sheetNo; index++) {
//                createSheet(sheetNo, index);
//
//                // 产生一行
//                Row row = sheet.createRow(0);
//                int column = 0;
//                // 写入各个字段的列头名称
//                for (Object[] os : fields) {
//                    Excel excel = (Excel) os[1];
//                    this.createCell(excel, row, column++, isTemplate);
//                }
//                if (Type.EXPORT.equals(type)) {
//                    fillExcelData(index, row);
//                    addStatisticsRow();
//                }
//            }
//            String filename = encodingFilename(sheetName);
//            out = new FileOutputStream(getAbsoluteFile(filename));
//            wb.write(out);
//            return AjaxResult.success(filename);
//        } catch (Exception e) {
//            log.error("导出Excel异常{}", e.getMessage());
//            throw new CustomException("导出Excel失败，请联系网站管理员！");
//        } finally {
//            if (wb != null) {
//                try {
//                    wb.close();
//                } catch (IOException e1) {
//
//                }
//            }
//            if (out != null) {
//                try {
//                    out.close();
//                } catch (IOException e1) {
//
//                }
//            }
//        }
//    }
//
//    /**
//     * 编码文件名
//     */
//    public String encodingFilename(String filename) {
//        filename = UUID.randomUUID().toString() + "_" + filename + ".xlsx";
//        return filename;
//    }
//
//    /**
//     * 获取下载路径
//     *
//     * @param filename 文件名称
//     */
//    public String getAbsoluteFile(String filename) {
//        String downloadPath = CosmoConfig.getDownloadPath() + filename;
//        File desc = new File(downloadPath);
//        if (!desc.getParentFile().exists()) {
//            desc.getParentFile().mkdirs();
//        }
//        return downloadPath;
//    }
//
//    /**
//     * 导出模板
//     *
//     * @param sheetName 工作表的名称
//     * @return 结果
//     */
//    public AjaxResult exportTemplateExcel(String sheetName) {
//        this.init(null, sheetName, Type.IMPORT);
//        return exportExcel(true);
//    }
/////////////////////////////////// srm-excel end///////////////////////////////
//
//    /**
//     * 获取当前登录用户拥有的价格权限
//     *
//     * @return
//     */
//    private List<String> getLoginUserPricePermission() {
//        List<String> pricePermissionList = null;
//        try {
//            // 获取header中的username
//            RedisTemplate redisTemplate = SpringUtils.getBean("redisTemplate");
//            String name = ServletUtils.getHeader("username");
//
//            // 获取header中的Authorization令牌
//            String auth = ServletUtils.getRequest().getHeader(CacheConstants.HEADER);
//            if (StringUtils.isNotEmpty(auth) && auth.startsWith(CacheConstants.TOKEN_PREFIX)) {
//                auth = auth.replace(CacheConstants.TOKEN_PREFIX, "");
//            }
//
//            // 组装redisKey && 获取redis中的当前登录用户的价格权限信息
//            String redisKey = Constants.LOGIN_TOKEN_KEY + name + ":" + auth;
//            if (StringUtils.isNotEmpty(redisKey)) {
//                Object redisValue = redisTemplate.opsForValue().get(redisKey);
//                if (null != redisValue) {
//                    JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(redisValue));
//                    JSONArray pricePermissions = jsonObject.getJSONArray("pricePermissions");
//                    if (null != pricePermissions) {
//                        pricePermissionList = JSONObject.parseArray(pricePermissions.toJSONString(), String.class);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.warn("获取用户价格权限信息异常:{}", e.getMessage());
//            throw new CustomException("获取用户价格权限信息异常", HttpStatus.UNAUTHORIZED);
//        }
//        return pricePermissionList;
//    }
//
//    /**
//     * 单元格数据脱敏处理
//     *
//     * @param excel
//     * @param cell
//     */
//    private void cellValueMask(Excel excel, Cell cell) {
//        if (excel.dataMask()) {
//            List<String> loginUserPricePermission = getLoginUserPricePermission();
//            if (CollectionUtils.isEmpty(loginUserPricePermission) || !loginUserPricePermission.contains(excel.dataMaskType().getCode())) {
//                cell.setCellValue(org.springframework.util.StringUtils.hasText(excel.dataMaskChar()) ? excel.dataMaskChar() : "**");
//            }
//        }
//    }
//
//}
