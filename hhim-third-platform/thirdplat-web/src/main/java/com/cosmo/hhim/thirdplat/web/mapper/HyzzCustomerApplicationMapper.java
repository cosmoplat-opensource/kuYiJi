/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.mapper;


import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzCustomerApplication;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 企业订购应用Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2022-10-21
 */
public interface HyzzCustomerApplicationMapper {
    /**
     * 查询企业订购应用
     *
     * @param customerCode
     * @param appId
     * @return 企业订购应用
     */
    HyzzCustomerApplication selectHyzzCustomerApplication(@Param("customerCode") String customerCode, @Param("appId") String appId);

    /**
     * 根据租户编码+产品类型查询企业订购的应用集合
     * @return
     */
    List<HyzzCustomerApplication> selectHyzzCustomerApplications(@Param("customerCode") String customerCode, @Param("productType") Integer productType);

    /**
     * 根据租户+应用ID更新应用使用状态
     * @param customerCode
     * @param appId
     * @param useStatus
     * @return
     */
    int updateUseStatusByAppIdAndCustomer(@Param("customerCode") String customerCode, @Param("appId") String appId, @Param("useStatus") String useStatus);
}
