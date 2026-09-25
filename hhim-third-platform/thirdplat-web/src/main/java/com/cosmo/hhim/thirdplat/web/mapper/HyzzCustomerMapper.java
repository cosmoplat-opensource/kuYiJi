/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.mapper;


import com.cosmo.hhim.thirdplat.api.operation.domain.CustomerUser;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzCustomer;
import com.cosmo.hhim.thirdplat.api.operation.domain.QueryIdentity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 客户（企业）Mapper接口
 *
 * @author cosmo-hhim-open Team
 */
public interface HyzzCustomerMapper {

    HyzzCustomer selectHyzzCustomerByApi(@Param("userName") String userName, @Param("mobile") String mobile,
                                         @Param("productType") Integer productType, @Param("productId") String productId);


    List<HyzzCustomer> selectHyzzCustomerByNoParam();

    HyzzCustomer selectDataByCustomerCode(String customerCode);

    HyzzCustomer selectDataByCubaId(String cubaId);

    List<HyzzCustomer> selectDataByCustomerName(String customerName);

    List<CustomerUser> findCustomUserByName(Set<String> username);

    HyzzCustomer queryIdentity(QueryIdentity username);

    List<CustomerUser> findUserByCustomer(String userName, String customerCode);

    HyzzCustomer selectCustomerAndUserInfo(@Param("username") String username, @Param("phoneNumber") String phoneNumber,
                                                      @Param("productType") Integer productType, @Param("productId") String productId);
    HyzzCustomer getCustomByUucUserId(String uucUserId,String type);

    HyzzCustomer findPcbaCustomer();

    HyzzCustomer findByInviteCode(@Param("inviteCode") String inviteCode);
}
