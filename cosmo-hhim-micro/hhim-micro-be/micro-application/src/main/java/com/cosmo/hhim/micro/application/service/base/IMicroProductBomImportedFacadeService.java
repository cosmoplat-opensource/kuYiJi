/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/11
 */
public interface IMicroProductBomImportedFacadeService {

    /**
     * 确认导入
     * flag(0:错误数据修复，1:确认导入)
     */
    void importConfirm(String flag); 

    /**
     * 取消导入
     */
    void importCancel();

}
