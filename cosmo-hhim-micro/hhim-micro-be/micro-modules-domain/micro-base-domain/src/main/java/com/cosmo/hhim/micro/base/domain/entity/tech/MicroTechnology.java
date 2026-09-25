/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.tech;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 工艺链定义对象 micro_technology
 *
 * @author cosmo-hhim-open Team
 * @date 2023-02-20
 */
@Data
public class MicroTechnology implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * 工艺编码
     */
    private String techCode;

    /**
     * 工艺名称
     */
    private String techName;

    /**
     * 工艺组
     */
    private String techGroup;

    /**
     * 工艺描述
     */
    private String techDesc;

    /**
     * 工艺版本
     */
    private String techVersion;

    /**
     * 产品seq
     */
    private String productSeq;
    private String productName;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 关联类型(产品/产品组)
     */
    private String relationType;
    /**
     * 工艺类型 草稿/标准
     */
    private String techType;
    /**
     * 工艺形式
     */
    private Integer techPattern;

    /**
     * 产品组编码
     */
    private String classCode;

    /**
     * 产品组名称
     */
    private String className;

    /**
     * 租户
     */
    private String tenantCode;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdDate;

    /**
     * 最后修改人
     */
    private String lastUpdBy;

    /**
     * 最后修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastUpdDate;

    /**
     * 激活标记1是0否
     */
    private String activeFlag;

}
