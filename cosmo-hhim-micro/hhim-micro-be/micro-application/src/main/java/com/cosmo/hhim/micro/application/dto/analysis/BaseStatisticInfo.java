/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.analysis;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description: 基础数据信息
 * @date 2023/5/5 16:52
 */
@Data
public class BaseStatisticInfo {

    /**
     * 员工数量
     */
    private int staffNum = 0;

    /**
     * 产品数量
     */
    private int productNum = 0;

    /**
     * 工序数量
     */
    private int processNum = 0;

    /**
     * 标准bom数量
     */
    private int standardBomNum = 0;

    /**
     * 草稿bom数量
     */
    private int draftBomNum = 0;

    /**
     * 车间数量
     */
    private int workShopNum = 0;

    /**
     * 产线数量
     */
    private int manufactureLineNum = 0;

    /**
     * 客户数量
     */
    private int customerNum = 0;
}
