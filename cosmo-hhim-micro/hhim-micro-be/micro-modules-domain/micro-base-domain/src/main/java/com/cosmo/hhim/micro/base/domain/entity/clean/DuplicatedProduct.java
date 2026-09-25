/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.clean;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @date 2023/1/13 16:05
 */
@Data
public class DuplicatedProduct {

    private String productName;

    private String duplicatedProductSeq;

    private String duplicatedProductCode;

    private List<String> duplicatedProductSeqList;

    private List<String> duplicatedProductCodeList;

    public List<String> getDuplicatedProductSeqList() {
        return Arrays.asList(duplicatedProductSeq.split(","));
    }

    public List<String> getDuplicatedProductCodeList() {
        return Arrays.asList(duplicatedProductCode.split(","));
    }
}
