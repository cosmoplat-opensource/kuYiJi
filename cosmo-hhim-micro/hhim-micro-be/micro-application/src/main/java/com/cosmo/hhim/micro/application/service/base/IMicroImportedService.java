/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base;

import javax.servlet.http.HttpServletResponse;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/6
 */
public interface IMicroImportedService {

    /**
     * 生成导入临时链接
     * @return
     */
    String genImportLinkTemp(String taskType); 

    /**
     * 下载导入模版
     * @param response
     * @param certificate
     */
    void getImportTemplate(HttpServletResponse response, String certificate);

    /**
     * 确认导入
     * flag(0:错误数据修复，1:确认导入)
     * @param flag
     */
    void importConfirm(String flag);

    /**
     * 取消导入
     */
    void importCancel();

}
