/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.mapper;

import com.cosmo.hhim.common.core.utils.ServletUtils;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.DeviceUserBind;

import java.util.List;

/**
 * 设备与用户绑定关系Mapper接口
 *
 * @author cosmo-hhim-open Team
 */
public interface DeviceUserBindMapper {
    /**
     * 根据设备ID查询设备与用户绑定关系信息
     */
    DeviceUserBind selectDeviceUserBindByDeviceId(String deviceId);

    /**
     * 根据用户账户查询对应的设备ID列表
     * @param usernames
     * @return
     */
    List<String> selectDeviceIdsByUsernames(List<String> usernames);

    /**
     * 根据租户编码查询租户下所有用户对应的设备ID列表
     * @param tenantCode
     * @return
     */
    List<String> selectDeviceIdsByTenant(String tenantCode);

    /**
     * 新增设备与用户绑定关系
     *
     * @param deviceUserBind 设备与用户绑定关系
     * @return 结果
     */
    int insertHyzzDeviceUserBind(DeviceUserBind deviceUserBind);

    /**
     * 修改设备与用户绑定关系
     *
     * @param deviceUserBind 设备与用户绑定关系
     * @return 结果
     */
    int updateHyzzDeviceUserBind(DeviceUserBind deviceUserBind);

    /**
     * 批量删除设备与用户绑定关系
     *
     * @param deviceIds
     * @return
     */
    int deleteByDeviceIds(String[] deviceIds);

    /**
     * 根据用户名批量删除绑定的所有设备
     * @param username
     * @return
     */
    int deleteByUserName(String username);

    /**
     * 根据设备Id批量删除所绑定的用户名
     * @param deviceId
     * @return
     */
    int deleteByDeviceId(String deviceId);

    /**
     * 根据用户名查询绑定信息
     */
    List<DeviceUserBind> findByUserName(List<String> usernames);


}
