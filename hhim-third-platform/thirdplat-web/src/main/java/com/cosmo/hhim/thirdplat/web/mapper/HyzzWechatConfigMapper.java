/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.mapper;

import com.cosmo.hhim.thirdplat.api.wechatmp.domain.HyzzWechatConfig;

import java.util.List;

/**
 * 微信公众号配置信息Mapper接口
 *
 * @author cosmo-hhim-open Team
 */
public interface HyzzWechatConfigMapper {
    /**
     * 根据企业编码查询微信公众号配置信息
     */
    HyzzWechatConfig selectHyzzWechatConfigByCustomerCode(String customerCode);

    /**
     * 查询运营平台配置的所有微信公众号配置信息
     * @return
     */
    List<HyzzWechatConfig> selectHyzzWechatConfigList();

    /**
     * 根据公众号AppId查询微信公众号配置信息
     * @param appId
     * @return
     */
    HyzzWechatConfig selectHyzzWechatConfigByAppId(String appId);


}
