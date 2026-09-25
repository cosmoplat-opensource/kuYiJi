/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.constant;

/**
 * @author cosmo-hhim-open Team
 * @description: 邮件服务常量类
 * @date 2023/1/6 09:39
 */
public class MailConstants {

    public static final String MAIL_REPORT_NAME = "reportName";

    public static final String MAIL_APP_NAME = "appName";

    /**
     * 通用邮件模版ID，使用占位符进行区分
     */
    public static final String EXCEL_EXPORT_COMMON_MAIL_TEMPLATE_ID = "AD073C17-3BD9-4A6E";

    /**
     * 邮件服务 - 员工记工导出邮件主题 (sheet、fileName)
     */
    public static final String EMPLOYEE_SUBMIT_MAIL_SUBJECT = "员工计件统计";
    public static final String EMPLOYEE_SUBMIT_EXCEL_NAME = "员工计件统计";

    /**
     * 邮件服务 - 完工产品导出邮件主题
     */
    public static final String FINISHED_PRODUCT_MAIL_SUBJECT = "完工产品";
    public static final String FINISHED_PRODUCT_EXCEL_NAME = "完工产品";

    /**
     * 邮件服务 - 车间库存邮件主题
     */
    public static final String PROCESS_STORAGE_MAIL_SUBJECT = "车间库存";
    public static final String PROCESS_STORAGE_EXCEL_NAME = "车间库存";

    /**
     * 邮件服务 - 质量趋势邮件主题
     */
    public static final String QUALITY_TRENDS_MAIL_SUBJECT = "质量趋势";
    public static final String QUALITY_TRENDS_EXCEL_NAME = "质量趋势";

    /**
     * 邮件服务 - 库存变动记录邮件主题
     */
    public static final String STOCK_CHANGE_MAIL_SUBJECT = "库存变动记录";
    public static final String STOCK_CHANGE_EXCEL_NAME = "库存变动记录";

    // 期初工序库存导入模版 
    public static final String PROCESS_STORAGE_IMPORT_TEMPLATE_MAIL_SUBJECT = "期初工序库存导入模版";
    public static final String PROCESS_STORAGE_IMPORT_TEMPLATE_EXCEL_NAME = "期初工序库存导入模版";


    /**
     * 完工报告
     */
    public static final String COMPLETE_REPORT_MAIL_SUBJECT = "完工报告";
    public static final String COMPLETE_REPORT_MAIL_WAIT_SUBJECT = "完工报告（待确认）";
    public static final String COMPLETE_REPORT_MAIL_FINISH_SUBJECT = "完工报告（已完成）";
    public static final String COMPLETE_REPORT_EXCEL_NAME = "完工报告";
    public static final String WAIT_COMPLETE_REPORT_EXCEL_NAME = "待确认-完工报告";
    public static final String FINISH_COMPLETE_REPORT_EXCEL_NAME = "已完成-完工报告";

    /**
     * 成品库存
     */
    public static final String FINISH_PRODUCT_STORAGE_MAIL_SUBJECT = "成品库存";
    public static final String FINISH_PRODUCT_STORAGE_EXCEL_NAME = "成品库存";

    /**
     * 成品库存历史变动
     */
    public static final String FINISH_PRODUCT_STORAGE_CHANGE_MAIL_SUBJECT = "成品库存变动记录";
    public static final String FINISH_PRODUCT_STORAGE_CHANGE_EXCEL_NAME = "成品库存变动记录";

    /**
     * 计件结算报告
     */
    public static final String SETTLED_REPORT_MAIL_SUBJECT = "计件已结算报告";
    public static final String SETTLED_REPORT_EXCEL_NAME = "计件已结算报告";

    /**
     * 计件结算报告
     */
    public static final String OPEN_SETTLEMENT_REPORT_MAIL_SUBJECT = "计件待结算报告";
    public static final String OPEN_SETTLEMENT_REPORT_EXCEL_NAME = "计件待结算报告";

    /**
     * 不良品清单
     */
    public static final String NG_PRODUCT_MANAGE_MAIL_SUBJECT = "不良品清单";
    public static final String NG_PRODUCT_MANAGE_EXCEL_NAME = "不良品清单";
}
