/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.manager.impl;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.redis.distributedlock.utils.RedisLockHelper;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.DeviceUserBindInfo;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.DeviceUserBind;
import com.cosmo.hhim.thirdplat.web.constants.RedisKeys;
import com.cosmo.hhim.thirdplat.web.mapper.DeviceUserBindMapper;
import com.cosmo.hhim.thirdplat.web.service.manager.IDeviceUserBindService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 设备与用户绑定关系Service业务层处理
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class DeviceUserBindServiceImpl implements IDeviceUserBindService {

    @Autowired
    private DeviceUserBindMapper deviceUserBindMapper;
    @Autowired
    private RedisLockHelper redisLockHelper;

    /**
     * 根据设备ID查询设备与用户绑定信息
     */
    @Override
    public DeviceUserBind selectDeviceUserBindByDeviceId(String deviceId) {
        return deviceUserBindMapper.selectDeviceUserBindByDeviceId(deviceId);
    }

    @Override
    public List<String> selectDeviceIdsByUsernames(List<String> usernames) {
        List<String> deviceIds = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(usernames)) {
            deviceIds = deviceUserBindMapper.selectDeviceIdsByUsernames(usernames);
        }
        return deviceIds;
    }

    @Override
    public List<String> selectDeviceIdsByTenant(String tenantCode) {
        return deviceUserBindMapper.selectDeviceIdsByTenant(tenantCode);
    }


    /**
     * 新增设备与用户绑定关系
     *
     * @param deviceUserBind 设备与用户绑定关系
     * @return 结果
     */
    @Override
    public int insertHyzzDeviceUserBind(DeviceUserBind deviceUserBind) {
        return deviceUserBindMapper.insertHyzzDeviceUserBind(deviceUserBind);
    }

    /**
     * 修改设备与用户绑定关系
     *
     * @param deviceUserBind 设备与用户绑定关系
     * @return 结果
     */
    @Override
    public int updateHyzzDeviceUserBind(DeviceUserBind deviceUserBind) {

        return deviceUserBindMapper.updateHyzzDeviceUserBind(deviceUserBind);
    }

    /**
     * 批量删除设备与用户绑定关系
     */
    @Override
    public int deleteByDeviceIds(String[] deviceIds) {
        return deviceUserBindMapper.deleteByDeviceIds(deviceIds);
    }

    /**
     * 重置设备绑定的用户账号
     *
     * @param deviceUserBindInfo
     * @return
     */
    @Transactional
    @Override
    public boolean resetBind(DeviceUserBindInfo deviceUserBindInfo) {
        String lockKey = RedisKeys.Unipush.MANAGER.value(RedisKeys.DEVICE_BIND_USER_KEY + deviceUserBindInfo.getDeviceId());
        String lockValue = UUID.randomUUID().toString();
        try {
            boolean haveLock = redisLockHelper.lock(lockKey, lockValue, 5L, TimeUnit.SECONDS);
            log.info("重置设备绑定的用户账号，分布式锁加锁结果：{}，lockKey:lock:{}, requestParam:{}", haveLock, lockKey, JSON.toJSONString(deviceUserBindInfo));
            if (!haveLock) {
                log.warn("重置设备绑定的用户账号失败！reason:获取分布式锁失败！绑定请求信息：{}", JSON.toJSONString(deviceUserBindInfo));
                return false;
            }
            DeviceUserBind deviceUserBind = new DeviceUserBind();
//            DeviceUserBind bindInfo = deviceUserBindMapper.selectDeviceUserBindByDeviceId(deviceUserBindInfo.getDeviceId());
//            if (null == bindInfo) { //不存在设备ID，则创建
//                deviceUserBind.setUserName(deviceUserBindInfo.getUserName());
//                deviceUserBind.setDeviceId(deviceUserBindInfo.getDeviceId());
//                deviceUserBindMapper.insertHyzzDeviceUserBind(deviceUserBind);
//            } else { //存在设备ID，更新设备ID所绑定的用户账号
//                deviceUserBind.setId(bindInfo.getId());
//                deviceUserBind.setUserName(deviceUserBindInfo.getUserName());
//                deviceUserBindMapper.updateHyzzDeviceUserBind(deviceUserBind);
//            }

            // 删除该账号所绑定的所有设备ID
            deviceUserBindMapper.deleteByUserName(deviceUserBindInfo.getUserName());
            // 删除该设备所绑定的所有账号
            deviceUserBindMapper.deleteByDeviceId(deviceUserBindInfo.getDeviceId());

            // 新增账号设备绑定信息
            deviceUserBind.setUserName(deviceUserBindInfo.getUserName());
            deviceUserBind.setDeviceId(deviceUserBindInfo.getDeviceId());
            deviceUserBindMapper.insertHyzzDeviceUserBind(deviceUserBind);

            return true;
        } finally {
            redisLockHelper.unlock(lockKey, lockValue);
            log.info("重置设备绑定的用户账号，分布式锁已释放！lockKey:lock:{}", lockKey);
        }
    }

    @Override
    public List<DeviceUserBind> findByUserName(List<String> userName) {
        return deviceUserBindMapper.findByUserName(userName);
    }
}
