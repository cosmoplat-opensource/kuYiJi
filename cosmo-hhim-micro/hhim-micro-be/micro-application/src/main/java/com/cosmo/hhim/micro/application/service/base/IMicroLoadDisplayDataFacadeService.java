/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base;

/**
 * @author cosmo-hhim-open Team
 * @description: 导入演示数据
 * @date 2023/3/29 11:05
 */
public interface IMicroLoadDisplayDataFacadeService {

    /**
     * 根据接口url获取演示数据
     *
     * @param apiUrl
     * @return
     */
    Object loadDisplayData(String apiUrl);
}
