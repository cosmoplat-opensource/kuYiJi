/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.operation.impl;

import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzCustomerUser;
import com.cosmo.hhim.thirdplat.web.mapper.HyzzCustomerUserMapper;
import com.cosmo.hhim.thirdplat.web.service.operation.IHyzzCustomerUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 客户用户信息Service业务层处理
 *
 * @author cosmo-hhim-open Team
 */
@Service
public class HyzzCustomerUserServiceImpl implements IHyzzCustomerUserService {
    @Autowired
    private HyzzCustomerUserMapper hyzzCustomerUserMapper;

    /**
     * 新增客户用户信息
     *
     * @param hyzzCustomerUser 客户用户信息
     * @return 结果
     */
    @Override
    public int insertHyzzCustomerUser(HyzzCustomerUser hyzzCustomerUser) {
        hyzzCustomerUser.setCreateTime(DateUtils.getNowDate());
        return hyzzCustomerUserMapper.insertHyzzCustomerUser(hyzzCustomerUser);
    }

    /**
     * 修改客户用户信息
     *
     * @param hyzzCustomerUser 客户用户信息
     * @return 结果
     */
    @Override
    public int updateHyzzCustomerUser(HyzzCustomerUser hyzzCustomerUser) {
        hyzzCustomerUser.setUpdateTime(DateUtils.getNowDate());
        return hyzzCustomerUserMapper.updateHyzzCustomerUser(hyzzCustomerUser);
    }

    /**
     * 批量保存用户信息
     *
     * @param hyzzCustomerUsers
     * @return
     */
    @Override
    public int insertHyzzCustomerUserBatch(List<HyzzCustomerUser> hyzzCustomerUsers) {
        return hyzzCustomerUserMapper.insertHyzzCustomerUserBatch(hyzzCustomerUsers);
    }

    @Override
    public HyzzCustomerUser findByUserName(String username) {
        return hyzzCustomerUserMapper.findByUser(username);
    }

    @Override
    public HyzzCustomerUser findByPhone(String phoneNumber) {
        return hyzzCustomerUserMapper.findByPhone(phoneNumber);
    }

}
