/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.tech;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @author cosmo-hhim-open Team
 */
@Getter
@Setter
public class MicroTechSimilarDomain {
    private Long productId;
    private String productCode;
    private String productSeq;
    private String productName;
    private String processSeq;
    private String parentProcessSeq;

    /**
     * 重写了equals
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        MicroTechSimilarDomain domain = (MicroTechSimilarDomain) o;
        return Objects.equals(productId, domain.productId) && Objects.equals(productCode, domain.productCode) && Objects.equals(productSeq, domain.productSeq) && Objects.equals(productName, domain.productName);
    }

    /**
     * 重写了hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(productId, productCode, productSeq, productName);
    }
}
