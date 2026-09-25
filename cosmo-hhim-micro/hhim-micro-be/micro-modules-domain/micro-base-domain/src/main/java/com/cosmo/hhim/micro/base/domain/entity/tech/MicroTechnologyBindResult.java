/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.tech;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 工艺链绑定结果集
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroTechnologyBindResult implements Serializable {
    /**
     * 推荐已存在标准工艺的且与当前工艺链相同的产品集合
     */
    private List<MicroTechBindProductDomain> coverProductList;
    /**
     * 推荐的无标准工艺的且与当前工艺链相同的产品集合
     */
    private List<MicroTechBindProductDomain> similarProductList;
}
