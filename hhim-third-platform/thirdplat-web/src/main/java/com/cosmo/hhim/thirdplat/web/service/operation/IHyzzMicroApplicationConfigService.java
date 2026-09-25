/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.operation;

import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzMicroApplicationConfig;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;

import java.util.List;

/**
 * 微应用-应用配置信息Service接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-08
 */
public interface IHyzzMicroApplicationConfigService {
    /**
     * 根据应用标识查询应用配置信息
     *
     * @param appSign
     * @return 应用配置信息
     */
    APIResponse<HyzzMicroApplicationConfig> selectMicroApplicationConfigByAppSign(String appSign);

    /**
     * 根据应用编码查询应用配置信息
     *
     * @param appCode
     * @return 应用配置信息
     */
    APIResponse<HyzzMicroApplicationConfig> selectMicroApplicationConfigByAppCode(String appCode);

    /**
     * 获取所有微应用配置信息
     *
     * @return
     */
    APIResponse<List<HyzzMicroApplicationConfig>> selectMicroApplicationAllMapping();


    /**
     * 获取指定租户所购买的所有应用信息
     * @return
     */
    APIResponse<List<HyzzMicroApplicationConfig>> selectMicroApplicationDetailInfo(String tenantCode, Integer productType);
}
