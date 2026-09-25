/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.mapper;

import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzCustomerUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户用户信息Mapper接口
 *
 * @author cosmo-hhim-open Team
 */
public interface HyzzCustomerUserMapper {

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
     * 用于uuc激活修改用户信息
     * @param hyzzCustomerUser
     * @return
     */
    int updateForUucActive(HyzzCustomerUser hyzzCustomerUser);

    /**
     * 批量保存用户信息
     *
     * @param hyzzCustomerUsers
     * @return
     */
    int insertHyzzCustomerUserBatch(@Param("hyzzCustomerUsers") List<HyzzCustomerUser> hyzzCustomerUsers);


    HyzzCustomerUser findByUser(String username);

    HyzzCustomerUser findByPhone(String phone);

    void deleteByUserId(Long userId);
}
