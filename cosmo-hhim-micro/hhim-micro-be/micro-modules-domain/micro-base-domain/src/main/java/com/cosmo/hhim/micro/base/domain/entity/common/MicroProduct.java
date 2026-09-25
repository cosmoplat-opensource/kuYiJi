/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import com.cosmo.hhim.common.core.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSortEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 产品对象 micro_product
 *
 * @date 2022-10-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroProduct extends MicroSortEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 产品唯一码
     */
    private String productSeq;

    /**
     * 产品名称
     */
    @NotNull(message = "产品名称不能为空")
    private String productName;

    /**
     * 产品描述
     */
    private String productDesc;

    /**
     * 产品类型(成品、半成品)
     */
    @NotNull(message = "产品类型不能为空")
    private String productType;
    /**
     * 生产方式(10自制20外购30委外)
     */
    private String productionMode;

    /**
     * 产品图片
     */
    private String picture;

    /**
     * 单位
     */
    private String unit;

    /**
     * 规格
     */
    private String standards;

    private String remark;
    /**
     * 安全库存上限
     */
    private BigDecimal stockUpperLimit;
    /**
     * 安全库存下限
     */
    private BigDecimal stockLowerLimit;

    /**
     * 创建类型 手动/自动
     */
    private String createdType;
    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdDate;

    /**
     * 最后修改人
     */
    private String lastUpdBy;
    /**
     * 搜索key
     */
    private String key;

    /**
     * 最后修改时间
     */
    private Date lastUpdDate;

    public void setCreateInfo() {
        String userId = String.valueOf(SecurityUtils.getUserId());
        if (StringUtils.hasText(userId)) {
            if (!StringUtils.hasText(this.createdBy)) {
                this.createdBy = userId;
            }
        }
        this.setCreatedDate(DateUtils.getNowDate());
    }

}
