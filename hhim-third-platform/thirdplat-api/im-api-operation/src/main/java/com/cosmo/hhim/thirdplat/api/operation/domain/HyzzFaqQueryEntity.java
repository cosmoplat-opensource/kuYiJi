/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

/**
 * 智能客服问答对对象 hyzz_faq_pair_record
 *
 * @author cosmo-hhim-open Team
 * @date 2023-06-05
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HyzzFaqQueryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<String> roleCodeList;

    private List<String> indexList;

    private String indexSource;

    private String questionContent;

    private String userName;

    private Long userId;

}
