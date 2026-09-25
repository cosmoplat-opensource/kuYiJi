/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.manager;

import com.cosmo.hhim.thirdplat.api.unipush.domain.in.DeviceUserBindInfo;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.DeviceUserBind;

import java.util.List;

/**
 * 设备与用户绑定关系Service接口
 *
 * @author cosmo-hhim-open Team
 */
public interface IDeviceUserBindService {
    /**
     * 根据设备ID查询设备与用户绑定信息
     */
    DeviceUserBind selectDeviceUserBindByDeviceId(String deviceId);

    /**
     * 根据用户账户集合获取对应的设备ID集合
     * @param usernames
     * @return
     */
    List<String> selectDeviceIdsByUsernames(List<String> usernames);

    /**
     * 根据租户编码获取对应的设备ID集合
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
     */
    int deleteByDeviceIds(String[] deviceIds);

    /**
     * 重置设备绑定的用户账号
     * @param deviceUserBindInfo
     */
    boolean resetBind(DeviceUserBindInfo deviceUserBindInfo);

    /**
     * 根据用户名查询绑定信息
     */
    List<DeviceUserBind> findByUserName(List<String> userName);

}
