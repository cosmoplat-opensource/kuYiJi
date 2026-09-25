/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessBuyInfo;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-11-04
 */
public interface IMicroDataInitService {

    /**
     * 初始化DB数据
     */
    void initDBData(final String ssoInitDataSqlUrl, final String originalCustomerCode); 

    /**
     * 初始化业务配置
     */
    void initBusinessConfig(final MicroProcessBuyInfo microProcessBuyInfo, final String appSign); 

}
