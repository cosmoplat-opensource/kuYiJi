/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.constant;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-11-07
 */
public class MicroBusinessConstants {

    // 用户角色编码 
    public static final String MANAGER_ROLE = "10"; // 管理员 
    public static final String AUDITOR_ROLE = "20"; // 审产员 
    public static final String WORKER_ROLE = "30"; // 工人 
    public static final String TRIAL_ROLE = "40"; // 试用角色 
    public static final String QUALITY_INSPECTOR = "25"; // 质检员 

    // Easypoi按模版导出所需的ioss的模版excel路径 
    public static final String BOM_IMPORT_TEMPLATE_URL = "/hhim-micro/importTemplate/BOM导入错误信息模版.xlsx";
    public static final String TECHNOLOGY_IMPORT_TEMPLATE_URL = "/hhim-micro/importTemplate/工艺导入错误信息模版.xlsx";
    public static final String FINISH_STORAGE_IMPORT_TEMPLATE_URL = "/hhim-micro/importTemplate/产成品库存期初导入错误信息模版.xlsx";

}
