/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.base;

import com.cosmo.hhim.micro.application.dto.base.MicroUserDTO;
import com.cosmo.hhim.micro.application.dto.base.MicroUserRecommendButtonDTO;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserEditEntity;

import java.util.List;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 */
public interface IMicroUserFacadeService {
    /**
     * 请求三方平台初始化用户数据
     *
     * @param signAndCodeMap app_sign(k)和app_code(v)对应关系
     */
    void cacheAllCustomerUserInfo(Map<String, String> signAndCodeMap, String customerCode); 

    int editUser(MicroUserEditEntity microUser); 

    MicroUserDTO getUserCompleteInfo(String username); 

    /**
     * 开启选项
     *
     * @param option
     * @return
     */
    int enablePersonalized(Integer option);

    /**
     * 根据pageType找到根据用户使用情况,推荐出的页面链接
     *
     * @param pageType
     * @return
     */
    String findUserPersonalizedRecommendPage(String pageType);

    /**
     * 根据用户输入的报工数据找到根据用户使用情况,推荐出的按钮(记工/送检)
     *
     * @param dataList
     * @return
     */
    List<String> findUserPersonalizedRecommendButton(List<MicroUserRecommendButtonDTO> dataList);
}
