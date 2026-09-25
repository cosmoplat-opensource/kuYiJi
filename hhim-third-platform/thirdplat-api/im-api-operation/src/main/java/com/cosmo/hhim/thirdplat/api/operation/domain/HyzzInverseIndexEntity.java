/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 倒排索引对象 hyzz_inverse_index
 *
 * @author cosmo-hhim-open Team
 * @date 2023-06-05
 */
@Data
public class HyzzInverseIndexEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 索引值
     */
    private String index;
    private String pinyin;

    /**
     * 对象ID
     */
    private Long objectId;
    /**
     * 权重
     */
    private Long weight;
    /**
     * 对象内容
     */
    private String objectContent;

    /**
     * 索引来源(问答/....)
     */
    private String indexSource;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;
}
