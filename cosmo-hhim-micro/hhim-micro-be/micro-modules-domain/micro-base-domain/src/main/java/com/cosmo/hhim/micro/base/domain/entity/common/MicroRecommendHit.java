/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 推荐数据命中信息表对象 micro_recommend_hit
 *
 * @author cosmo-hhim-open Team
 * @date 2022-10-26
 */
@Data
public class MicroRecommendHit implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 用户名
     */
    private String username;

    /**
     * 源数据
     */
    private String originData;

    /**
     * 使用数据
     */
    private String useData;

    /**
     * 数据类型
     */
    private String dataType;
    /**
     * 数据ID
     */
    private String dataId;

    /**
     * 命中精准度
     */
    private BigDecimal hitPrecision;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdDate;

}
