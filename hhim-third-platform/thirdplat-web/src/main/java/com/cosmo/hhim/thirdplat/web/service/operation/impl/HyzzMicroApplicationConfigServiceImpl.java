/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.operation.impl;

import com.cosmo.hhim.thirdplat.api.operation.constants.BusinessConstants;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzCustomerApplication;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzMicroApplicationConfig;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.core.cache.ThreadLocalCache;
import com.cosmo.hhim.thirdplat.web.mapper.HyzzCustomerApplicationMapper;
import com.cosmo.hhim.thirdplat.web.mapper.HyzzMicroApplicationConfigMapper;
import com.cosmo.hhim.thirdplat.web.service.operation.IHyzzMicroApplicationConfigService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 微应用-应用配置信息Service业务层处理
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-08
 */
@Service
public class HyzzMicroApplicationConfigServiceImpl implements IHyzzMicroApplicationConfigService {
    @Autowired
    private HyzzMicroApplicationConfigMapper hyzzMicroApplicationConfigMapper;
    @Autowired
    private HyzzCustomerApplicationMapper hyzzCustomerApplicationMapper;

    /**
     * 根据应用标识查询应用配置信息
     *
     * @param appSign
     * @return 应用配置信息
     */
    @Override
    public APIResponse<HyzzMicroApplicationConfig> selectMicroApplicationConfigByAppSign(String appSign) {
        return APIResponse.success(hyzzMicroApplicationConfigMapper.selectMicroApplicationConfigByAppSign(appSign));
    }

    /**
     * 根据应用编码查询应用配置信息
     *
     * @param appCode
     * @return 应用配置信息
     */
    @Override
    public APIResponse<HyzzMicroApplicationConfig> selectMicroApplicationConfigByAppCode(String appCode) {
        return APIResponse.success(hyzzMicroApplicationConfigMapper.selectMicroApplicationConfigByAppCode(appCode));
    }

    /**
     * 获取所有微应用配置信息
     *
     * @return
     */
    @Override
    public APIResponse<List<HyzzMicroApplicationConfig>> selectMicroApplicationAllMapping() {
        return APIResponse.success(hyzzMicroApplicationConfigMapper.selectMicroApplicationAllMapping());
    }

    /**
     * 获取指定租户所购买的所有应用信息
     * @return
     */
    @Override
    public APIResponse<List<HyzzMicroApplicationConfig>> selectMicroApplicationDetailInfo(String tenantCode, Integer productType) {
        List<HyzzMicroApplicationConfig> resultList = Lists.newArrayList();
        List<HyzzCustomerApplication> applications = hyzzCustomerApplicationMapper.selectHyzzCustomerApplications(tenantCode, productType);
        for (HyzzCustomerApplication application : applications) {
            HyzzMicroApplicationConfig applicationConfig = hyzzMicroApplicationConfigMapper.selectMicroApplicationConfigByAppCode(application.getAppId());
            resultList.add(applicationConfig);
        }

        return APIResponse.success(resultList);
    }
}
