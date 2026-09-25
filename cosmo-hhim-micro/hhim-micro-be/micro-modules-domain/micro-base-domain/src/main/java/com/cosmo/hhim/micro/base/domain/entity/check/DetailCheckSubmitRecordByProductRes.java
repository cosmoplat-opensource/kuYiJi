/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.check;

import lombok.Data;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 产品维度中某个产品的详细返回值, 主要是为了有工艺封装的返回对象
 * @date 2023/3/15 08:38
 */
@Data
public class DetailCheckSubmitRecordByProductRes {

    /**
     * 要审核的报工记录数
     */
    int needCheckRecordNum;

    /**
     * 存在异常的报工记录数
     */
    int exceptionRecordNum;

    /**
     * 异常的报工记录跳转的id汇总, 针对有标准工艺情况下，不符合标准工艺的报工记录
     */
    List<Long> unStandardTechSubmitRecordIds;

    /**
     * 详细的报工记录
     */
    List<DetailCheckSubmitRecordByProduct> detailCheckSubmitRecordByProductList;

    /**
     * 标准工艺
     *
     */
    private Boolean standard;
}
