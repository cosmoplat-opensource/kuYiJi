/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.mapper;

import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzMicroApplicationConfig;

import java.util.List;

/**
 * 微应用-应用配置信息Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-08
 */
public interface HyzzMicroApplicationConfigMapper {
    /**
     * 根据应用标识查询应用配置信息
     *
     * @param appSign
     * @return 应用配置信息
     */
    HyzzMicroApplicationConfig selectMicroApplicationConfigByAppSign(String appSign);

    /**
     * 根据应用编码查询应用配置信息
     *
     * @param appCode
     * @return 应用配置信息
     */
    HyzzMicroApplicationConfig selectMicroApplicationConfigByAppCode(String appCode);

    /**
     * 获取所有微应用配置信息
     *
     * @return
     */
    List<HyzzMicroApplicationConfig> selectMicroApplicationAllMapping();
}
