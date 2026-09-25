/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.tech;

import com.cosmo.hhim.micro.base.domain.entity.tech.MicroTechBindProductDomain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 工艺链绑定值对象
 *
 * @author cosmo-hhim-open Team
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroTechnologyBindDTO implements Serializable {
    /**
     * 是否是标准工艺
     */
    private Boolean standard;
    /**
     * 工艺形式
     */
    private Integer techPattern;
    /**
     * 是否为拷贝工艺
     */
    private Boolean clone;
    private Long techId;
    private Long productId;
    private String productSeq;
    private String productName;
    /**
     * 工艺链节点集合
     */
    private List<MicroProcessChainBindDTO> chainList;
    /**
     * 要覆盖的产品列表
     */
    private List<MicroTechBindProductDomain> coverProductList;
    /**
     * 相似工艺的产品列表
     */
    private List<MicroTechBindProductDomain> similarProductList;
}