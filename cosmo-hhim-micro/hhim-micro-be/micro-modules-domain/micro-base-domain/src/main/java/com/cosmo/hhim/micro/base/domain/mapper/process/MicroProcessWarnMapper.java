/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.process;

import com.cosmo.hhim.micro.base.domain.entity.warn.ProductWarn;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 工序预警mapper
 * @date 2022/12/9 14:48
 */
public interface MicroProcessWarnMapper {

    /**
     * 产品对应的尾序
     *
     * @return
     */
    List<ProductWarn> selectMultiLastProcessForProduct();

    /**
     * 没有尾序的产品
     *
     * @return
     */
    List<ProductWarn> selectNotHaveLastProcessForProduct();

    /**
     * 没有首序的产品
     *
     * @return
     */
    List<ProductWarn> selectNotHaveFirstProcessForProduct();
}
