/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.operation;



import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzCustomerUser;

import java.util.List;

/**
 * 客户用户信息Service接口
 *
 * @author cosmo-hhim-open Team
 */
public interface IHyzzCustomerUserService {

    /**
     * 新增客户用户信息
     *
     * @param hyzzCustomerUser 客户用户信息
     * @return 结果
     */
    int insertHyzzCustomerUser(HyzzCustomerUser hyzzCustomerUser);

    /**
     * 修改客户用户信息
     *
     * @param hyzzCustomerUser 客户用户信息
     * @return 结果
     */
    int updateHyzzCustomerUser(HyzzCustomerUser hyzzCustomerUser);

    /**
     * 批量保存用户信息
     *
     * @param hyzzCustomerUsers
     * @return
     */
    int insertHyzzCustomerUserBatch(List<HyzzCustomerUser> hyzzCustomerUsers);


    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    HyzzCustomerUser findByUserName(String username);


    /**
     * 根据手机号查询用户
     * @param phoneNumber
     * @return
     */
    HyzzCustomerUser findByPhone(String phoneNumber);
}
