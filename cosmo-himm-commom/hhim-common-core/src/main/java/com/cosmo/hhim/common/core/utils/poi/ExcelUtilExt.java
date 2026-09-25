/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
//package com.cosmo.hhim.common.core.utils.poi;
//
//import com.cosmo.hhim.common.core.annotation.Excel;
//import com.cosmo.hhim.common.core.annotation.Excel.ColumnType;
//import com.cosmo.hhim.common.core.annotation.Excel.Type;
//import com.cosmo.hhim.common.core.annotation.Excels;
//import com.cosmo.hhim.common.core.text.Convert;
//import com.cosmo.hhim.common.core.utils.WmsCheckObjectUtils;
//import com.cosmo.hhim.common.core.utils.DateUtils;
//import com.cosmo.hhim.common.core.utils.StringUtils;
//import com.cosmo.hhim.common.core.utils.file.FileTypeUtils;
//import com.cosmo.hhim.common.core.utils.file.ImageUtils;
//import org.apache.poi.hssf.usermodel.DVConstraint;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.ss.util.CellRangeAddressList;
//import org.apache.poi.xssf.streaming.SXSSFWorkbook;
//import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
//import org.apache.poi.xssf.usermodel.XSSFDataValidation;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.ByteArrayOutputStream;
//import java.lang.reflect.Field;
//import java.math.BigDecimal;
//import java.text.DecimalFormat;
//import java.util.*;
//import java.util.Map.Entry;
//import java.util.stream.Collectors;
//
//import static com.cosmo.hhim.common.core.utils.poi.ExcelUtil.addValidationToSheet;
//
///**
// * Excel相关处理-这个类仅作为excelUtil的辅助类，不能单独使用.
// * Excel相关处理-这个类仅作为excelUtil的辅助类，不能单独使用.
// * Excel相关处理-这个类仅作为excelUtil的辅助类，不能单独使用.
// * Excel相关处理-这个类仅作为excelUtil的辅助类，不能单独使用.
// * Excel相关处理-这个类仅作为excelUtil的辅助类，不能单独使用.
// * Excel相关处理-这个类仅作为excelUtil的辅助类，不能单独使用.
// *
// * @author cosmo-hhim-open Team
// */
//public class ExcelUtilExt<T> {
//
//  private static final Logger log = LoggerFactory.getLogger(ExcelUtilExt.class);
//
//  /**
//   * Excel sheet最大行数，默认65536
//   */
//  public static final int SHEETSIZE = 65536;
//
//  /**
//   * 默认字体
//   */
//  public static final String DEFULTFONTNAME = "Arial";
//
//  /**
//   * 工作表名称
//   */
//  private String sheetName;
//
//  /**
//   * 导出类型（EXPORT:导出数据；IMPORT：导入模板）
//   */
//  private Type type;
//
//  /**
//   * 工作薄对象
//   */
//  private Workbook wb;
//
//  /**
//   * 工作表对象
//   */
//  private Sheet sheet;
//
//  /**
//   * 样式列表
//   */
//  private Map<String, CellStyle> styles;
//
//  /**
//   * 导入导出数据列表
//   */
//  private List<T> list;
//
//  /**
//   * 注解列表
//   */
//  private List<Object[]> fields;
//
//  /**
//   * 最大高度
//   */
//  private short maxHeight;
//
//  /**
//   * 统计列表
//   */
//  private Map<Integer, Double> statistics = new HashMap<Integer, Double>();
//
//  /**
//   * 数字格式
//   */
//  private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("######0.00");
//
//  /**
//   * 实体对象
//   */
//  public Class<T> clazz;
//
//  //下拉菜单等的起始行
//  private Integer firstRow;
//  //下拉菜单等的终止行
//  private Integer endRow;
//
//
//  public ExcelUtilExt(Class<T> clazz) {
//    this.clazz = clazz;
//  }
//
//  /**
//   * 下拉sheet页Map形式. <sheetName,List<Object>>
//   */
//  public Map<String, List<T>> comboSheetMap;
//
//  public void init(List<T> list, String sheetName, Type type, Workbook wb,Integer firstRow, Integer endRow) {
//    if (list == null) {
//      list = new ArrayList<T>();
//    }
//    if (comboSheetMap == null) {
//      comboSheetMap = new HashMap<>();
//    }
//
//    this.list = list;
//    this.sheetName = sheetName;
//    this.type = type;
//    this.firstRow = firstRow;
//    this.endRow = endRow;
//    if (WmsCheckObjectUtils.isEmpty(firstRow)){
//      this.firstRow = endRow;
//    }
//    if (WmsCheckObjectUtils.isEmpty(endRow)){
//      this.endRow = 200;
//    }
//    createExcelField();
//    this.wb = wb;
//    if (WmsCheckObjectUtils.isEmpty(wb)) {
//      createWorkbook();
//    }
//  }
//
//
//  /**
//   * 对list数据源将其里面的数据导入到excel表单
//   *
//   * @return 结果hhim-wms-modules/hhim-wms-system/pom.xml
//   */
//  public ByteArrayOutputStream exportExcelWorkbook(ByteArrayOutputStream byteArrayOutputStream,int sheetNum) {
//    try {
//      // 取出一共有多少个sheet.
//      double sheetNo = Math.ceil(list.size() / SHEETSIZE) ;
//      for (int index = 0; index <= sheetNo; index++) {
//        createSheet(index+sheetNum);
//
//        // 产生一行
//        Row row = sheet.createRow(0);
//        int column = 0;
//        // 写入各个字段的列头名称
//        for (Object[] os : fields) {
//          Excel excel = (Excel) os[1];
//          this.createCell(excel, row, column++, 1);
//        }
//        if (Type.EXPORT.equals(type)) {
//          fillExcelData(index, row);
//          addStatisticsRow();
//        }
//      }
//    } catch (Exception e) {
//      log.error("导出Excel异常{}", e);
//    }
//    return byteArrayOutputStream;
//  }
//
//
//  public Workbook exportExcelWorkbook(ByteArrayOutputStream byteArrayOutputStream, List<T> list,
//      String sheetName, Workbook wb,int sheetNum,Integer firstRow, Integer  endRow) {
//    this.init(list, sheetName, Type.EXPORT, wb,firstRow, endRow);
//    this.exportExcelWorkbook(byteArrayOutputStream,sheetNum);
//    return wb;
//  }
//
//  /**
//   * 对list数据源将其里面的数据导入到excel表单
//   *
//   * @return 结果hhim-wms-modules/hhim-wms-system/pom.xml
//   */
//  public void exportDictionarySheet(ByteArrayOutputStream byteArrayOutputStream) {
//    try {
//      // 取出一共有多少个sheet.
//      int index = 0;
//      for (Entry<String, List<T>> entry : comboSheetMap.entrySet()) {
//        this.createDictionarySheet(entry.getKey(), index);
//        index++;
//        // 产生一行
//        Row row = sheet.createRow(0);
//        int column = 0;
//        // 写入各个字段的列头名称
//        for (Object[] os : fields) {
//          Excel excel = (Excel) os[1];
//          this.createCell(excel, row, column++, entry.getValue().size());
//        }
//        fillExcelData(index, row, entry.getValue());
//      }
//      wb.write(byteArrayOutputStream);
//    } catch (Exception e) {
//      log.error("导出Excel异常{}", e.getMessage());
//    }
//
//  }
//
//  /**
//   * 填充excel数据
//   *
//   * @param index 序号
//   * @param row   单元格行
//   */
//  public void fillExcelData(int index, Row row) {
//    int startNo = index * SHEETSIZE;
//    int endNo = Math.min(startNo + SHEETSIZE, list.size());
//    for (int i = startNo; i < endNo; i++) {
//      row = sheet.createRow(i + 1 - startNo);
//      // 得到导出对象.
//      T vo = (T) list.get(i);
//      int column = 0;
//      for (Object[] os : fields) {
//        Field field = (Field) os[0];
//        Excel excel = (Excel) os[1];
//        // 设置实体类私有属性可访问
//        field.setAccessible(true);
//        this.addCell(excel, row, vo, field, column++);
//      }
//    }
//  }
//
//  /**
//   * 填充excel数据-字典数据
//   *
//   * @param index 序号
//   * @param row   单元格行
//   */
//  public void fillExcelData(int index, Row row, List<T> list) {
//    int startNo = index * SHEETSIZE;
//    int endNo = Math.min(startNo + SHEETSIZE, list.size());
//    for (int i = startNo; i < endNo; i++) {
//      row = sheet.createRow(i + 1 - startNo);
//      // 得到导出对象.
//      T vo = (T) list.get(i);
//      int column = 0;
//      for (Object[] os : fields) {
//        Field field = (Field) os[0];
//        Excel excel = (Excel) os[1];
//        // 设置实体类私有属性可访问
//        field.setAccessible(true);
//        this.addCell(excel, row, vo, field, column++);
//      }
//    }
//  }
//
//  /**
//   * 创建表格样式
//   *
//   * @param wb 工作薄对象
//   * @return 样式列表
//   */
//  private Map<String, CellStyle> createStyles(Workbook wb) {
//    // 写入各条记录,每条记录对应excel表中的一行
//    Map<String, CellStyle> styles = new HashMap<>();
//    CellStyle style = wb.createCellStyle();
//    style.setAlignment(HorizontalAlignment.CENTER);
//    style.setVerticalAlignment(VerticalAlignment.CENTER);
//    style.setBorderRight(BorderStyle.THIN);
//    style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
//    style.setBorderLeft(BorderStyle.THIN);
//    style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
//    style.setBorderTop(BorderStyle.THIN);
//    style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
//    style.setBorderBottom(BorderStyle.THIN);
//    style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
//    Font dataFont = wb.createFont();
//    dataFont.setFontName(DEFULTFONTNAME);
//    dataFont.setFontHeightInPoints((short) 10);
//    style.setFont(dataFont);
//    styles.put("data", style);
//
//    style = wb.createCellStyle();
//    style.cloneStyleFrom(styles.get("data"));
//    style.setAlignment(HorizontalAlignment.CENTER);
//    style.setVerticalAlignment(VerticalAlignment.CENTER);
//    style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
//    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//    Font headerFont = wb.createFont();
//    headerFont.setFontName(DEFULTFONTNAME);
//    headerFont.setFontHeightInPoints((short) 10);
//    headerFont.setBold(true);
//    headerFont.setColor(IndexedColors.WHITE.getIndex());
//    style.setFont(headerFont);
//    styles.put("header", style);
//
//    style = wb.createCellStyle();
//    style.setAlignment(HorizontalAlignment.CENTER);
//    style.setVerticalAlignment(VerticalAlignment.CENTER);
//    Font totalFont = wb.createFont();
//    totalFont.setFontName(DEFULTFONTNAME);
//    totalFont.setFontHeightInPoints((short) 10);
//    style.setFont(totalFont);
//    styles.put("total", style);
//
//
//
//    style = wb.createCellStyle();
//    style.cloneStyleFrom(styles.get("data"));
//    style.setAlignment(HorizontalAlignment.LEFT);
//    styles.put("data1", style);
//
//    style = wb.createCellStyle();
//    style.cloneStyleFrom(styles.get("data"));
//    style.setAlignment(HorizontalAlignment.CENTER);
//    styles.put("data2", style);
//
//    style = wb.createCellStyle();
//    style.cloneStyleFrom(styles.get("data"));
//    style.setAlignment(HorizontalAlignment.RIGHT);
//    styles.put("data3", style);
//
//    return styles;
//  }
//
//  /**
//   * 创建单元格
//   */
//  /**
//   * 创建单元格
//   */
//  public Cell createCell(Excel attr, Row row, int column, int dictionaryLastRow)
//  {
//    // 创建列
//    Cell cell = row.createCell(column);
//    // 写入列信息
//    cell.setCellValue(attr.name());
//    setDataValidation(attr, row, column);
//    //如果是表头是必填项设置颜色是红色
//
//
//    if (WmsCheckObjectUtils.isNotEmpty(attr.required())) {
//      CellStyle style = wb.createCellStyle();
//      style.setAlignment(HorizontalAlignment.CENTER);
//      style.setVerticalAlignment(VerticalAlignment.CENTER);
//      style.setBorderRight(BorderStyle.THIN);
//      style.setRightBorderColor(IndexedColors.RED.getIndex());
//      style.setBorderLeft(BorderStyle.THIN);
//      style.setLeftBorderColor(IndexedColors.RED.getIndex());
//      style.setBorderTop(BorderStyle.THIN);
//      style.setTopBorderColor(IndexedColors.RED.getIndex());
//      style.setBorderBottom(BorderStyle.THIN);
//      style.setBottomBorderColor(IndexedColors.RED.getIndex());
//      style.setAlignment(HorizontalAlignment.CENTER);
//      style.setVerticalAlignment(VerticalAlignment.CENTER);
//      style.setFillForegroundColor(IndexedColors.RED.getIndex());
//      style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//      Font headerFont = wb.createFont();
//      headerFont.setFontName(DEFULTFONTNAME);
//      headerFont.setFontHeightInPoints((short) 10);
//      headerFont.setBold(true);
//      headerFont.setColor(IndexedColors.WHITE.getIndex());
//      style.setFont(headerFont);
//      cell.setCellStyle(style);
//    } else {
//      CellStyle cellStyle = styles.get("header");
//      cell.setCellStyle(cellStyle);
//    }
//
//    return cell;
//  }
//
//  /**
//   * 设置单元格信息
//   *
//   * @param value 单元格值
//   * @param attr  注解相关
//   * @param cell  单元格信息
//   */
//  public void setCellVo(Object value, Excel attr, Cell cell) {
//    if (ColumnType.STRING == attr.cellType()) {
//      cell.setCellValue(StringUtils.isNull(value) ? attr.defaultValue() : value + attr.suffix());
//    } else if (ColumnType.NUMERIC == attr.cellType()) {
//      cell.setCellValue(StringUtils.contains(Convert.toStr(value), ".") ? Convert.toDouble(value)
//          : Convert.toInt(value));
//    } else if (ColumnType.IMAGE == attr.cellType()) {
//      ClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, (short) cell.getColumnIndex(),
//          cell.getRow().getRowNum(), (short) (cell.getColumnIndex() + 1),
//          cell.getRow().getRowNum() + 1);
//      String imagePath = Convert.toStr(value);
//      if (StringUtils.isNotEmpty(imagePath)) {
//        byte[] data = ImageUtils.getImage(imagePath);
//        getDrawingPatriarch(cell.getSheet()).createPicture(anchor,
//            cell.getSheet().getWorkbook().addPicture(data, getImageType(data)));
//      }
//    }
//  }
//
//  /**
//   * 获取画布
//   */
//  public static Drawing<?> getDrawingPatriarch(Sheet sheet) {
//    if (sheet.getDrawingPatriarch() == null) {
//      sheet.createDrawingPatriarch();
//    }
//    return sheet.getDrawingPatriarch();
//  }
//
//  /**
//   * 获取图片类型,设置图片插入类型
//   */
//  public int getImageType(byte[] value) {
//    String type = FileTypeUtils.getFileExtendName(value);
//    if ("JPG".equalsIgnoreCase(type)) {
//      return Workbook.PICTURE_TYPE_JPEG;
//    } else if ("PNG".equalsIgnoreCase(type)) {
//      return Workbook.PICTURE_TYPE_PNG;
//    }
//    return Workbook.PICTURE_TYPE_JPEG;
//  }
//
//  /**
//   * 创建表格样式
//   */
//  public void setDataValidation(Excel attr, Row row, int column)
//  {
//    if (column == 0){//表头行高
//      row.setHeight((short) 480);
//    }
//
//    if (attr.name().indexOf("注：") >= 0)
//    {
//      sheet.setColumnWidth(column, 6000);
//    }
//    else
//    {
//      // 设置列宽
//      sheet.setColumnWidth(column, (int) ((attr.width() + 0.72) * 256));
//    }
//    // 如果设置了提示信息则鼠标放上去提示.
//    if (StringUtils.isNotEmpty(attr.prompt()))
//    {
//      // 这里默认设了2-101列提示.
//      setXSSFPrompt(sheet, "", attr.prompt(), 1, 100, column, column);
//    }
//    if (StringUtils.isNotEmpty(attr.maxStringInfo()) && attr.maxStringInfo().length == 2){
//      this.setXSSFLengthError(
//          sheet,
//          "文本超长",
//          attr.name() + attr.maxStringInfo()[1],
//          firstRow,
//          endRow,
//          column,
//          column,
//          attr.maxStringInfo()[0]);
//    }
//
//    // 如果设置了combo属性则本列只能选择不能输入
//    if (attr.combo().length > 0)
//    {
//      // 这里默认设了2-101列只能选择不能输入.
//      setXSSFValidation(sheet, attr.combo(), 1, 100, column, column);
//    }
//    /*// 如果设置了comboName属性则本列只能选择不能输入
//    if (CheckObjectUtils.isNotEmpty(attr.comboName()))
//    {
//      String[] textlist = new String[]{};
//
//      // 这里默认设了2-101列只能选择不能输入.
//      setXSSFValidation(sheet, textlist, 1, SHEETSIZE, column, column);
//    }*/
//    // 如果设置了comboSheetName属性则本列只能选择，从sheet页中选取
//    if (WmsCheckObjectUtils.isNotEmpty(attr.dictionarySheetInfo()) &&  attr.dictionarySheetInfo().length == 2)
//    {
//      // 这里默认设了2-101列只能选择不能输入.
//      setXSSFValidation(
//          sheet,
//          attr.dictionarySheetInfo()[0],
//          attr.dictionarySheetInfo()[1],
//          firstRow,
//          endRow,
//          column,
//          column,
//          wb.getSheet(attr.dictionarySheetInfo()[0]).getPhysicalNumberOfRows());
//    }
//    if(WmsCheckObjectUtils.isNotEmpty(attr.dictionaryCascadeSheetInfo()) && attr.dictionaryCascadeSheetInfo().length > 1){
//      //DownloadTemplate 级联下拉列表
//      String[] cascadeSheetInfo = attr.dictionaryCascadeSheetInfo();
//      addValidationToSheet(wb, sheet,cascadeSheetInfo,  column,column, firstRow, endRow);
//    }
//  }
//
//  /**
//   * 设置 POI 长度警告
//   *
//   * @param sheet 表单
//   * @param promptTitle 提示标题
//   * @param promptContent 提示内容
//   * @param firstRow 开始行
//   * @param endRow 结束行
//   * @param firstCol 开始列
//   * @param endCol 结束列
//   * @param maxLength 最大长度
//   */
//  public void setXSSFLengthError(
//      Sheet sheet,
//      String promptTitle,
//      String promptContent,
//      int firstRow,
//      int endRow,
//      int firstCol,
//      int endCol,
//      String maxLength) {
//    DataValidationHelper helper = sheet.getDataValidationHelper();
//    DataValidationConstraint constraint = helper.createNumericConstraint(
//        DVConstraint.ValidationType.TEXT_LENGTH,
//        DVConstraint.OperatorType.BETWEEN, "1", maxLength);
//    // 设定在哪个单元格生效
//    CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
//    DataValidation dataValidation = helper.createValidation(constraint, regions);
//    dataValidation.createErrorBox(promptTitle, promptContent);
//    dataValidation.setShowErrorBox(true);
//    sheet.addValidationData(dataValidation);
//  }
//
//  /**
//   * 添加单元格
//   */
//  public Cell addCell(Excel attr, Row row, T vo, Field field, int column) {
//    Cell cell = null;
//    try {
//      // 设置行高
//      row.setHeight(maxHeight);
//      // 根据Excel中设置情况决定是否导出,有些情况需要保持为空,希望用户填写这一列.
//      if (attr.isExport()) {
//        // 创建cell
//        cell = row.createCell(column);
//        int align = attr.align().value();
//        cell.setCellStyle(styles.get("data" + (align >= 1 && align <= 3 ? align : "")));
//
//        // 用于读取对象中的属性
//        Object value = getTargetValue(vo, field, attr);
//        String dateFormat = attr.dateFormat();
//        String readConverterExp = attr.readConverterExp();
//        String separator = attr.separator();
//        if (StringUtils.isNotEmpty(dateFormat) && StringUtils.isNotNull(value)) {
//          cell.setCellValue(DateUtils.parseDateToStr(dateFormat, (Date) value));
//        } else if (StringUtils.isNotEmpty(readConverterExp) && StringUtils.isNotNull(value)) {
//          cell.setCellValue(convertByExp(Convert.toStr(value), readConverterExp, separator));
//        } else if (value instanceof BigDecimal && -1 != attr.scale()) {
//          cell.setCellValue(
//              (((BigDecimal) value).setScale(attr.scale(), attr.roundingMode())).toString());
//        } else {
//          // 设置列类型
//          setCellVo(value, attr, cell);
//        }
//        addStatisticsData(column, Convert.toStr(value), attr);
//      }
//    } catch (Exception e) {
//      log.error("导出Excel失败{}", e);
//    }
//    return cell;
//  }
//
//  /**
//   * 设置 POI XSSFSheet 单元格提示
//   *
//   * @param sheet         表单
//   * @param promptTitle   提示标题
//   * @param promptContent 提示内容
//   * @param firstRow      开始行
//   * @param endRow        结束行
//   * @param firstCol      开始列
//   * @param endCol        结束列
//   */
//  public void setXSSFPrompt(Sheet sheet, String promptTitle, String promptContent, int firstRow,
//      int endRow,
//      int firstCol, int endCol) {
//    DataValidationHelper helper = sheet.getDataValidationHelper();
//    DataValidationConstraint constraint = helper.createCustomConstraint("DD1");
//    CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
//    DataValidation dataValidation = helper.createValidation(constraint, regions);
//    dataValidation.createPromptBox(promptTitle, promptContent);
//    dataValidation.setShowPromptBox(true);
//    sheet.addValidationData(dataValidation);
//  }
//
//  /**
//   * 设置某些列的值只能输入预制的数据,显示下拉框.
//   *
//   * @param sheet    要设置的sheet.
//   * @param textlist 下拉框显示的内容
//   * @param firstRow 开始行
//   * @param endRow   结束行
//   * @param firstCol 开始列
//   * @param endCol   结束列
//   * @return 设置好的sheet.
//   */
//  public void setXSSFValidation(Sheet sheet, String[] textlist, int firstRow, int endRow,
//      int firstCol, int endCol) {
//    DataValidationHelper helper = sheet.getDataValidationHelper();
//    // 加载下拉列表内容
//    DataValidationConstraint constraint = helper.createExplicitListConstraint(textlist);
//    // 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
//    CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
//    // 数据有效性对象
//    DataValidation dataValidation = helper.createValidation(constraint, regions);
//    // 处理Excel兼容性问题
//    if (dataValidation instanceof XSSFDataValidation) {
//      dataValidation.setSuppressDropDownArrow(true);
//      dataValidation.setShowErrorBox(true);
//    } else {
//      dataValidation.setSuppressDropDownArrow(false);
//    }
//
//    sheet.addValidationData(dataValidation);
//  }
//
//  /**
//   * 设置某些列的值只能sheet中某列输入预制的数据,显示下拉框.
//   *
//   * @param sheet                模板sheet页（需要设置下拉框的sheet）
//   * @param dictionarySheetName  隐藏的sheet页，用于存放下拉框的值 （下拉框值对应一列）
//   * @param dictionaryLastRow    存放下拉框值的最后一行
//   * @param dictionaryColumnName 存放下拉框值的列名 "A"
//   * @param firstRow             添加下拉框对应开始行
//   * @param endRow               添加下拉框对应结束行
//   * @param firstCol             添加下拉框对应开始列
//   * @param endCol               添加下拉框对应结束列
//   */
//  public void setXSSFValidation(Sheet sheet, String dictionarySheetName,
//      String dictionaryColumnName, int firstRow, int endRow, int firstCol, int endCol,
//      int dictionaryLastRow) {
//    //设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
//    // 创建名称，可被其他单元格引用
//
//    // 加载下拉列表内容
//    DataValidationHelper helper = sheet.getDataValidationHelper();
//    String cell1 =
//        "" + dictionarySheetName + "!$" + dictionaryColumnName + "$2:$" + dictionaryColumnName
//            + "$" + dictionaryLastRow + "";
//    DataValidationConstraint constraint = helper.createFormulaListConstraint(cell1);
//    // 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
//    CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
//    // 数据有效性对象
//    DataValidation dataValidation = helper.createValidation(constraint, regions);
//    // 处理Excel兼容性问题
//    if (dataValidation instanceof XSSFDataValidation) {
//      dataValidation.setSuppressDropDownArrow(true);
//      dataValidation.setShowErrorBox(true);
//    } else {
//      dataValidation.setSuppressDropDownArrow(false);
//    }
//    sheet.addValidationData(dataValidation);
//  }
//
//  /**
//   * 解析导出值 0=男,1=女,2=未知
//   *
//   * @param propertyValue 参数值
//   * @param converterExp  翻译注解
//   * @param separator     分隔符
//   * @return 解析后值
//   */
//  public static String convertByExp(String propertyValue, String converterExp, String separator) {
//    StringBuilder propertyString = new StringBuilder();
//    String[] convertSource = converterExp.split(",");
//    for (String item : convertSource) {
//      String[] itemArray = item.split("=");
//      if (StringUtils.containsAny(separator, propertyValue)) {
//        for (String value : propertyValue.split(separator)) {
//          if (itemArray[0].equals(value)) {
//            propertyString.append(itemArray[1] + separator);
//            break;
//          }
//        }
//      } else {
//        if (itemArray[0].equals(propertyValue)) {
//          return itemArray[1];
//        }
//      }
//    }
//    return StringUtils.stripEnd(propertyString.toString(), separator);
//  }
//
//  /**
//   * 反向解析值 男=0,女=1,未知=2
//   *
//   * @param propertyValue 参数值
//   * @param converterExp  翻译注解
//   * @param separator     分隔符
//   * @return 解析后值
//   */
//  public static String reverseByExp(String propertyValue, String converterExp, String separator) {
//    StringBuilder propertyString = new StringBuilder();
//    String[] convertSource = converterExp.split(",");
//    for (String item : convertSource) {
//      String[] itemArray = item.split("=");
//      if (StringUtils.containsAny(separator, propertyValue)) {
//        for (String value : propertyValue.split(separator)) {
//          if (itemArray[1].equals(value)) {
//            propertyString.append(itemArray[0] + separator);
//            break;
//          }
//        }
//      } else {
//        if (itemArray[1].equals(propertyValue)) {
//          return itemArray[0];
//        }
//      }
//    }
//    return StringUtils.stripEnd(propertyString.toString(), separator);
//  }
//
//  /**
//   * 合计统计信息
//   */
//  private void addStatisticsData(Integer index, String text, Excel entity) {
//    if (entity != null && entity.isStatistics()) {
//      Double temp = 0D;
//      if (!statistics.containsKey(index)) {
//        statistics.put(index, temp);
//      }
//      try {
//        temp = Double.valueOf(text);
//      } catch (NumberFormatException e) {
//      }
//      statistics.put(index, statistics.get(index) + temp);
//    }
//  }
//
//  /**
//   * 创建统计行
//   */
//  public void addStatisticsRow() {
//    if (statistics.size() > 0) {
//      Cell cell = null;
//      Row row = sheet.createRow(sheet.getLastRowNum() + 1);
//      Set<Integer> keys = statistics.keySet();
//      cell = row.createCell(0);
//      cell.setCellStyle(styles.get("total"));
//      cell.setCellValue("合计");
//
//      for (Integer key : keys) {
//        cell = row.createCell(key);
//        cell.setCellStyle(styles.get("total"));
//        cell.setCellValue(DOUBLE_FORMAT.format(statistics.get(key)));
//      }
//      statistics.clear();
//    }
//  }
//
//  /**
//   * 获取bean中的属性值
//   *
//   * @param vo    实体对象
//   * @param field 字段
//   * @param excel 注解
//   * @return 最终的属性值
//   * @throws Exception
//   */
//  private Object getTargetValue(T vo, Field field, Excel excel) throws Exception {
//    Object o = field.get(vo);
//    if (StringUtils.isNotEmpty(excel.targetAttr())) {
//      String target = excel.targetAttr();
//      if (target.indexOf(".") > -1) {
//        String[] targets = target.split("[.]");
//        for (String name : targets) {
//          o = getValue(o, name);
//        }
//      } else {
//        o = getValue(o, target);
//      }
//    }
//    return o;
//  }
//
//  /**
//   * 以类的属性的get方法方法形式获取值
//   *
//   * @param o
//   * @param name
//   * @return value
//   * @throws Exception
//   */
//  private Object getValue(Object o, String name) throws Exception {
//    if (StringUtils.isNotNull(o) && StringUtils.isNotEmpty(name)) {
//      Class<?> clazz = o.getClass();
//      Field field = clazz.getDeclaredField(name);
//      field.setAccessible(true);
//      o = field.get(o);
//    }
//    return o;
//  }
//
//  /**
//   * 得到所有定义字段
//   */
//  private void createExcelField() {
//    this.fields = new ArrayList<Object[]>();
//    List<Field> tempFields = new ArrayList<>();
//    tempFields.addAll(Arrays.asList(clazz.getSuperclass().getDeclaredFields()));
//    tempFields.addAll(Arrays.asList(clazz.getDeclaredFields()));
//    for (Field field : tempFields) {
//      // 单注解
//      if (field.isAnnotationPresent(Excel.class)) {
//        putToField(field, field.getAnnotation(Excel.class));
//      }
//
//      // 多注解
//      if (field.isAnnotationPresent(Excels.class)) {
//        Excels attrs = field.getAnnotation(Excels.class);
//        Excel[] excels = attrs.value();
//        for (Excel excel : excels) {
//          putToField(field, excel);
//        }
//      }
//    }
//    this.fields = this.fields.stream()
//        .sorted(Comparator.comparing(objects -> ((Excel) objects[1]).sort()))
//        .collect(Collectors.toList());
//    this.maxHeight = getRowHeight();
//  }
//
//  /**
//   * 根据注解获取最大行高
//   */
//  public short getRowHeight() {
//    double maxHeight = 0;
//    for (Object[] os : this.fields) {
//      Excel excel = (Excel) os[1];
//      maxHeight = maxHeight > excel.height() ? maxHeight : excel.height();
//    }
//    return (short) (maxHeight * 20);
//  }
//
//  /**
//   * 放到字段集合中
//   */
//  private void putToField(Field field, Excel attr) {
//    if (attr != null && (attr.type() == Type.ALL || attr.type() == type)) {
//      this.fields.add(new Object[]{field, attr});
//    }
//  }
//
//  /**
//   * 创建一个工作簿
//   */
//  public void createWorkbook() {
//    this.wb = new SXSSFWorkbook(500);
//  }
//
//  /**
//   * 创建工作表
//   *
//   * @param index   序号
//   */
//  public void createSheet(int index) {
//    this.sheet = wb.createSheet();
//    this.styles = createStyles(wb);
//    // 设置工作表的名称.
//    wb.setSheetName(index, sheetName);
//
//  }
//
//  /**
//   * 创建工作表
//   *
//   * @param sheetName sheet名称
//   * @param index     序号
//   */
//  public void createDictionarySheet(String sheetName, int index) {
//    this.sheet = wb.createSheet();
//    this.styles = createStyles(wb);
//    // 设置工作表的名称.
//    wb.setSheetName(index, sheetName);
//  }
//
//  /**
//   * 获取单元格值
//   *
//   * @param row    获取的行
//   * @param column 获取单元格列号
//   * @return 单元格值
//   */
//  public Object getCellValue(Row row, int column) {
//    if (row == null) {
//      return row;
//    }
//    Object val = "";
//    try {
//      Cell cell = row.getCell(column);
//      if (StringUtils.isNotNull(cell)) {
//        if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
//          val = cell.getNumericCellValue();
//          if (DateUtil.isCellDateFormatted(cell)) {
//            val = DateUtil.getJavaDate((Double) val); // POI Excel 日期格式转换
//          } else {
//            if ((Double) val % 1 != 0) {
//              val = new BigDecimal(val.toString());
//            } else {
//              val = new DecimalFormat("0").format(val);
//            }
//          }
//        } else if (cell.getCellType() == CellType.STRING) {
//          val = cell.getStringCellValue();
//        } else if (cell.getCellType() == CellType.BOOLEAN) {
//          val = cell.getBooleanCellValue();
//        } else if (cell.getCellType() == CellType.ERROR) {
//          val = cell.getErrorCellValue();
//        }
//
//      }
//    } catch (Exception e) {
//      return val;
//    }
//    return val;
//  }
//}
