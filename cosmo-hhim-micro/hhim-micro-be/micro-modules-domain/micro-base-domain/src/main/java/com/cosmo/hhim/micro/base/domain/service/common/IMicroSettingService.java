/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroAppInfo;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-11-02
 */
public interface IMicroSettingService {

    /**
     * 开启正式使用
     *
     * @return
     */
    Boolean officialUse(boolean clearData); 

    /**
     * 获取正式使用标识
     *
     * @return
     */
    Boolean getOfficialUseFlag();


    /**
     * 获取当前租户所购买的所有应用信息
     * @return
     */
    List<MicroAppInfo> getAllAppInfosForCurrentTenant();

}
