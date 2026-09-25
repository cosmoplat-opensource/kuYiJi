/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.mapper;

import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.HyzzMicroMiniappConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 小程序配置Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2022-10-20
 */
public interface HyzzMicroMiniappConfigMapper {

    /**
     * 查询所有的小程序配置信息
     * @return
     */
    
    List<HyzzMicroMiniappConfig> selectAllMicroMiniAppConfig();

    /**
     * 根据唯一标识查询小程序配置信息
     * @param applicationSign
     * @param platformType
     * @return
     */
    HyzzMicroMiniappConfig selectMicroMiniAppConfigByCondition(@Param("applicationSign") String applicationSign, @Param("platformType") String platformType);

    /**
     * 查询小程序配置
     *
     * @param id 小程序配置ID
     * @return 小程序配置
     */
    public HyzzMicroMiniappConfig selectHyzzMicroMiniappConfigById(Long id);

    /**
     * 查询小程序配置列表
     *
     * @param hyzzMicroMiniappConfig 小程序配置
     * @return 小程序配置集合
     */
    public List<HyzzMicroMiniappConfig> selectHyzzMicroMiniappConfigList(HyzzMicroMiniappConfig hyzzMicroMiniappConfig);

    /**
     * 新增小程序配置
     *
     * @param hyzzMicroMiniappConfig 小程序配置
     * @return 结果
     */
    public int insertHyzzMicroMiniappConfig(HyzzMicroMiniappConfig hyzzMicroMiniappConfig);

    /**
     * 修改小程序配置
     *
     * @param hyzzMicroMiniappConfig 小程序配置
     * @return 结果
     */
    public int updateHyzzMicroMiniappConfig(HyzzMicroMiniappConfig hyzzMicroMiniappConfig);

    /**
     * 删除小程序配置
     *
     * @param id 小程序配置ID
     * @return 结果
     */
    public int deleteHyzzMicroMiniappConfigById(Long id);

    /**
     * 批量删除小程序配置
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteHyzzMicroMiniappConfigByIds(Long[] ids);
}
